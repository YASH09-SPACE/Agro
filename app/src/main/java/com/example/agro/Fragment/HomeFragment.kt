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
                val slider = binding.bannerSlider
                val itemCount = slider.adapter?.itemCount ?: 0
                if (itemCount > 0) {
                    val nextItem = (slider.currentItem + 1) % itemCount
                    slider.currentItem = nextItem
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

        // 0. Load user name
        loadUserNameFromFirestore()

        // --- Initialize Views ---
        bannerSlider = view.findViewById(R.id.bannerSlider)
        recyclerView = view.findViewById(R.id.rvProducts)

        // 1. Banner slider
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

        // 2. Categories (horizontal, from products)
        categoryAdapter = CategoryAdapter(categoryList) { category ->
            // later: filter products by category.title if you want
        }

        binding.rvCategories.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategories.adapter = categoryAdapter

        // 3. Products (grid) from Firestore
        productsAdapter = mainAdapter(productList)
        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProducts.adapter = productsAdapter

        loadProductsFromFirestore()

        // 4. Item click + Add to Cart (per item)
        binding.rvProducts.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                val addToCartButton = view.findViewById<View>(R.id.btnAddToCart)

                // 🔹 Add to cart button
                addToCartButton?.setOnClickListener {
                    val pos = binding.rvProducts.getChildAdapterPosition(view)
                    if (pos == RecyclerView.NO_POSITION || pos >= productList.size) return@setOnClickListener

                    val product = productList[pos]

                    CartManager.addToCart(
                        CartItem(
                            productId = product.productId,
                            imageResId = 0,                 // we rely on imageUrl
                            imageUrl = product.imageUrl,
                            name = product.name,
                            category = product.category,
                            price = product.price,
                            quantity = 1,
                            stockQuantity = product.stockQuantity
                        )
                    )
                    Toast.makeText(requireContext(), "${product.name} added to cart", Toast.LENGTH_SHORT).show()
                }

                // 🔹 Whole card click → open product_detail
                view.setOnClickListener {
                    val pos = binding.rvProducts.getChildAdapterPosition(view)
                    if (pos == RecyclerView.NO_POSITION || pos >= productList.size) return@setOnClickListener

                    val product = productList[pos]

                    val intent = Intent(requireContext(), product_detail::class.java).apply {
                        putExtra("productId", product.productId)
                        putExtra("name", product.name)
                        putExtra("category", product.category)
                        putExtra("price", product.price)
                        putExtra("imageUrl", product.imageUrl)
                        putExtra("description", product.description)
                        putExtra("stockQuantity", product.stockQuantity)
                    }
                    startActivity(intent)
                }
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                view.findViewById<View>(R.id.btnAddToCart)?.setOnClickListener(null)
                view.setOnClickListener(null)
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

                val categoryStockMap = mutableMapOf<String, Int>()

                for (doc in querySnapshot) {
                    val name = doc.getString("name") ?: continue
                    val priceDouble = doc.getDouble("price") ?: 0.0
                    val category = doc.getString("category") ?: "General"
                    val stockQty = (doc.getLong("stockQuantity") ?: 0L).toInt()
                    val productId = doc.id
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val description = doc.getString("description") ?: ""

                    productList.add(
                        Product(
                            name = name,
                            imageUrl = imageUrl,
                            price = priceDouble,
                            productId = productId,
                            stockQuantity = stockQty,
                            category = category,
                            description = description
                        )
                    )

                    categoryStockMap[category] = (categoryStockMap[category] ?: 0) + stockQty
                }

                productsAdapter.notifyDataSetChanged()

                categoryList.clear()
                categoryStockMap.forEach { (catName, totalStock) ->
                    categoryList.add(
                        Category(
                            title = catName,
                            itemCount = totalStock,
                            iconResId = R.drawable.ic_crop_tonics
                        )
                    )
                }
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
                    val fullName = snapshot.getString("fullName")
                        ?: snapshot.getString("name")
                        ?: snapshot.getString("email")
                        ?: "User"

                    binding.tvUserName.text = "$fullName 👋"
                }
            }
            .addOnFailureListener {
                // keep default if needed
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
