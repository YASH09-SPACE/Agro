//package com.example.agro.Fragment
//
//import android.content.Intent
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.GridLayoutManager
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import androidx.viewpager2.widget.ViewPager2
//import com.example.agro.*
//import com.example.agro.Model.CartItem
//import com.example.agro.R
//import com.example.agro.data.BannerItem
//import com.example.agro.data.Category
//import com.example.agro.data.Product
//import com.example.agro.databinding.FragmentHomeBinding
//import com.google.firebase.firestore.FirebaseFirestore
//
//class HomeFragment : Fragment() {
//
//    private var _binding: FragmentHomeBinding? = null
//    private val binding get() = _binding!!
//
//    private lateinit var bannerSlider: ViewPager2
//    private lateinit var recyclerView: RecyclerView
//    private val handler = Handler(Looper.getMainLooper())
//    private val scrollDelay = 3000L // 3 seconds
//
//    // Firestore
//    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
//
//    // Products (grid)
//    private val productList = mutableListOf<Product>()
//    private lateinit var productsAdapter: mainAdapter
//
//    // Categories (horizontal) derived from products
//    private val categoryList = mutableListOf<Category>()
//    private lateinit var categoryAdapter: CategoryAdapter
//
//    private val autoScrollRunnable = object : Runnable {
//        override fun run() {
//            if (_binding != null) {
//                val bannerSlider = binding.bannerSlider
//                val itemCount = bannerSlider.adapter?.itemCount ?: 0
//                if (itemCount > 0) {
//                    val nextItem = (bannerSlider.currentItem + 1) % itemCount
//                    bannerSlider.currentItem = nextItem
//                }
//                handler.postDelayed(this, scrollDelay)
//            }
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = FragmentHomeBinding.inflate(inflater, container, false)
//        val view = binding.root
//
//        // --- Search EditText ---
//        binding.etSearch.setOnClickListener {
//            val i = Intent(requireContext(), search_products::class.java)
//            startActivity(i)
//        }
//
//        // --- Initialize Views ---
//        bannerSlider = view.findViewById(R.id.bannerSlider)
//        recyclerView = view.findViewById(R.id.rvProducts)
//
//        // ===========================
//        // 1. BANNER SLIDER
//        // ===========================
//        val listOfBanners = listOf(
//            BannerItem("Happy Weekend", "20% OFF", R.color.banner_blue),
//            BannerItem("New Arrivals", "Free Shipping", R.color.banner_green),
//            BannerItem("Summer Sale", "Up to 50% Off", R.color.banner_red)
//        )
//        binding.bannerSlider.adapter = BannerAdapter(listOfBanners)
//
//        binding.bannerSlider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageScrollStateChanged(state: Int) {
//                when (state) {
//                    ViewPager2.SCROLL_STATE_DRAGGING -> handler.removeCallbacks(autoScrollRunnable)
//                    ViewPager2.SCROLL_STATE_IDLE -> handler.postDelayed(autoScrollRunnable, scrollDelay)
//                }
//            }
//        })
//
//        // ===========================
//        // 2. CATEGORIES (HORIZONTAL, FROM PRODUCTS)
//        // ===========================
//        categoryAdapter = CategoryAdapter(categoryList) { category ->
//            // handle click on category (optional)
//            // e.g. later: filter products by category.title
//        }
//
//        binding.rvCategories.layoutManager =
//            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//
//        binding.rvCategories.adapter = categoryAdapter
//
//        // ===========================
//        // 3. PRODUCTS (GRID) FROM FIRESTORE
//        // ===========================
//        productsAdapter = mainAdapter(productList)
//        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
//        binding.rvProducts.adapter = productsAdapter
//
//        loadProductsFromFirestore()
//
//        // ===========================
//        // 4. ADD TO CART BUTTON (PER ITEM)
//        // ===========================
//        binding.rvProducts.addOnChildAttachStateChangeListener(object :
//            RecyclerView.OnChildAttachStateChangeListener {
//            override fun onChildViewAttachedToWindow(view: View) {
//                val addToCartButton = view.findViewById<View>(R.id.btnAddToCart)
//                addToCartButton?.setOnClickListener {
//                    val pos = binding.rvProducts.getChildAdapterPosition(view)
//                    if (pos == RecyclerView.NO_POSITION || pos >= productList.size) return@setOnClickListener
//
//                    val product = productList[pos]
//
//                    // Convert "₹1000" → 1000.0
//                    val priceValue = product.price.replace("₹", "").trim().toDoubleOrNull() ?: 0.0
//
//                    CartManager.addToCart(
//                        CartItem(
//                            imageResId = product.image,
//                            name = product.name,
//                            category = "", // you can later pass real category if you add it to Product
//                            price = priceValue
//                        )
//                    )
//
//                    Toast.makeText(requireContext(), "${product.name} added to cart", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onChildViewDetachedFromWindow(view: View) {
//                view.findViewById<View>(R.id.btnAddToCart)?.setOnClickListener(null)
//            }
//        })
//
//        return view
//    }
//
//    /**
//     * Fetch products from Firestore "product" collection.
//     * Also builds category list by grouping categories and summing stockQuantity.
//     *
//     * Document fields expected:
//     *  - name: String
//     *  - price: Number (Double/Int)
//     *  - category: String
//     *  - stockQuantity: Number (Int/Long)
//     */
//    private fun loadProductsFromFirestore() {
//        db.collection("product")
//            .get()
//            .addOnSuccessListener { querySnapshot ->
//                productList.clear()
//
//                // category -> total stock
//                val categoryStockMap = mutableMapOf<String, Int>()
//
//                for (doc in querySnapshot) {
//                    val name = doc.getString("name") ?: continue
//                    val priceDouble = doc.getDouble("price") ?: 0.0
//                    val category = doc.getString("category") ?: "General"
//                    val stockQty = (doc.getLong("stockQuantity") ?: 0L).toInt()
//
//                    // Convert Double to a display string like "₹1000"
//                    val priceString = "₹${priceDouble.toInt()}"
//
//                    // Product list for grid
//                    val product = Product(
//                        name = name,
//                        image = R.drawable.ic_product, // placeholder image
//                        price = priceString,
//                        buttonText = "Add to Cart"
//                    )
//                    productList.add(product)
//
//                    // Accumulate stock per category
//                    val currentTotal = categoryStockMap[category] ?: 0
//                    categoryStockMap[category] = currentTotal + stockQty
//                }
//
//                // Update products adapter
//                productsAdapter.notifyDataSetChanged()
//
//                // Build category list from map
//                categoryList.clear()
//                for ((catName, totalStock) in categoryStockMap) {
//                    val categoryItem = Category(
//                        title = catName,
//                        itemCount = totalStock,
//                        iconResId = R.drawable.ic_crop_tonics // placeholder icon
//                    )
//                    categoryList.add(categoryItem)
//                }
//
//                // Update categories adapter
//                categoryAdapter.notifyDataSetChanged()
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(
//                    requireContext(),
//                    "Failed to load products: ${e.message}",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        handler.postDelayed(autoScrollRunnable, scrollDelay)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        handler.removeCallbacks(autoScrollRunnable)
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    companion object {
//        fun newInstance(): HomeFragment = HomeFragment()
//    }
//}
package com.example.agro.Fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.agro.*
import com.example.agro.Model.CartItem
import com.example.agro.R
import com.example.agro.data.BannerItem
import com.example.agro.data.Category
import com.example.agro.data.Product
import com.example.agro.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var bannerSlider: ViewPager2
    private lateinit var recyclerView: RecyclerView
    private val handler = Handler(Looper.getMainLooper())
    private val scrollDelay = 3000L // 3 seconds

    // Firebase
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // Products (grid)
    private val productList = mutableListOf<Product>()
    private lateinit var productsAdapter: mainAdapter

    // Categories (horizontal) derived from products
    private val categoryList = mutableListOf<Category>()
    private lateinit var categoryAdapter: CategoryAdapter

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            if (_binding != null) {
                val bannerSlider = binding.bannerSlider
                val itemCount = bannerSlider.adapter?.itemCount ?: 0
                if (itemCount > 0) {
                    val nextItem = (bannerSlider.currentItem + 1) % itemCount
                    bannerSlider.currentItem = nextItem
                }
                handler.postDelayed(this, scrollDelay)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // --- Search EditText ---
        binding.etSearch.setOnClickListener {
            val i = Intent(requireContext(), search_products::class.java)
            startActivity(i)
        }

        // ===========================
        // 0. LOAD USER NAME
        // ===========================
        loadUserNameFromFirestore()

        // --- Initialize Views ---
        bannerSlider = view.findViewById(R.id.bannerSlider)
        recyclerView = view.findViewById(R.id.rvProducts)

        // ===========================
        // 1. BANNER SLIDER
        // ===========================
        val listOfBanners = listOf(
            BannerItem("Happy Weekend", "20% OFF", R.color.banner_blue),
            BannerItem("New Arrivals", "Free Shipping", R.color.banner_green),
            BannerItem("Summer Sale", "Up to 50% Off", R.color.banner_red)
        )
        binding.bannerSlider.adapter = BannerAdapter(listOfBanners)

        binding.bannerSlider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    ViewPager2.SCROLL_STATE_DRAGGING -> handler.removeCallbacks(autoScrollRunnable)
                    ViewPager2.SCROLL_STATE_IDLE -> handler.postDelayed(autoScrollRunnable, scrollDelay)
                }
            }
        })

        // ===========================
        // 2. CATEGORIES (HORIZONTAL, FROM PRODUCTS)
        // ===========================
        categoryAdapter = CategoryAdapter(categoryList) { category ->
            // later: filter products by category.title
        }

        binding.rvCategories.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.rvCategories.adapter = categoryAdapter

        // ===========================
        // 3. PRODUCTS (GRID) FROM FIRESTORE
        // ===========================
        productsAdapter = mainAdapter(productList)
        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProducts.adapter = productsAdapter

        loadProductsFromFirestore()

        // ===========================
        // 4. ADD TO CART BUTTON (PER ITEM)
        // ===========================
        binding.rvProducts.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                val addToCartButton = view.findViewById<View>(R.id.btnAddToCart)
                addToCartButton?.setOnClickListener {
                    val pos = binding.rvProducts.getChildAdapterPosition(view)
                    if (pos == RecyclerView.NO_POSITION || pos >= productList.size) return@setOnClickListener

                    val product = productList[pos]

                    // Convert "₹1000" → 1000.0
                    val priceValue = product.price.replace("₹", "").trim().toDoubleOrNull() ?: 0.0

                    CartManager.addToCart(
                        CartItem(
                            imageResId = product.image,
                            name = product.name,
                            category = "", // you can later pass real category if you add it to Product
                            price = priceValue
                        )
                    )

                    Toast.makeText(requireContext(), "${product.name} added to cart", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                view.findViewById<View>(R.id.btnAddToCart)?.setOnClickListener(null)
            }
        })

        return view
    }

    /**
     * Fetch products from Firestore "product" collection.
     * Also builds category list by grouping categories and summing stockQuantity.
     */
    private fun loadProductsFromFirestore() {
        db.collection("product")
            .get()
            .addOnSuccessListener { querySnapshot ->
                productList.clear()

                // category -> total stock
                val categoryStockMap = mutableMapOf<String, Int>()

                for (doc in querySnapshot) {
                    val name = doc.getString("name") ?: continue
                    val priceDouble = doc.getDouble("price") ?: 0.0
                    val category = doc.getString("category") ?: "General"
                    val stockQty = (doc.getLong("stockQuantity") ?: 0L).toInt()

                    // Convert Double to "₹1000"
                    val priceString = "₹${priceDouble.toInt()}"

                    // Product list for grid
                    val product = Product(
                        name = name,
                        image = R.drawable.ic_product, // placeholder image
                        price = priceString,
                        buttonText = "Add to Cart"
                    )
                    productList.add(product)

                    // Accumulate stock per category
                    val currentTotal = categoryStockMap[category] ?: 0
                    categoryStockMap[category] = currentTotal + stockQty
                }

                // Update products adapter
                productsAdapter.notifyDataSetChanged()

                // Build category list from map
                categoryList.clear()
                for ((catName, totalStock) in categoryStockMap) {
                    val categoryItem = Category(
                        title = catName,
                        itemCount = totalStock,
                        iconResId = R.drawable.ic_crop_tonics // placeholder icon
                    )
                    categoryList.add(categoryItem)
                }

                // Update categories adapter
                categoryAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load products: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /**
     * Load logged-in user's name from Firestore "Users" collection
     * and show it in tvUserName.
     */
    private fun loadUserNameFromFirestore() {
        val currentUser = auth.currentUser ?: return

        db.collection("Users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Try fullName first, fallback to name, then email
                    val fullName = snapshot.getString("fullName")
                        ?: snapshot.getString("name")
                        ?: snapshot.getString("email")
                        ?: "User"

                    binding.tvUserName.text = "$fullName 👋"
                }
            }
            .addOnFailureListener {
                // optional: keep default text
            }
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(autoScrollRunnable, scrollDelay)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(autoScrollRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
    }
}
