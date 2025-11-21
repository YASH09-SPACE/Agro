package com.example.agro.Fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.compose.ui.unit.IntRect
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.agro.*
import com.example.agro.R
import com.example.agro.data.BannerItem
import com.example.agro.data.Product
import com.example.agro.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var bannerSlider: ViewPager2
    private lateinit var recyclerView: RecyclerView
    private val handler = Handler(Looper.getMainLooper())
    private val scrollDelay = 3000L // 3 seconds

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            if (_binding != null) { // Check if binding is not null
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


        // --- Initialize Views ---
        bannerSlider = view.findViewById(R.id.bannerSlider)
        recyclerView = view.findViewById(R.id.rvProducts)

        // --- Banner List ---
        val listOfBanners = listOf(
            BannerItem("Happy Weekend", "20% OFF", R.color.banner_blue),
            BannerItem("New Arrivals", "Free Shipping", R.color.banner_green),
            BannerItem("Summer Sale", "Up to 50% Off", R.color.banner_red)
        )
        binding.bannerSlider.adapter = BannerAdapter(listOfBanners)

        // --- Auto-scroll pause/resume ---
        binding.bannerSlider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    ViewPager2.SCROLL_STATE_DRAGGING -> handler.removeCallbacks(autoScrollRunnable)
                    ViewPager2.SCROLL_STATE_IDLE -> handler.postDelayed(autoScrollRunnable, scrollDelay)
                }
            }
        })


        // --- RecyclerView setup ---
        val items = listOf(
            Product("Zincacea", R.drawable.ic_product, "₹1000", "Add to Cart"),
            Product("Fertilizer A", R.drawable.ic_product, "₹750", "Add to Cart"),
            Product("Fertilizer B", R.drawable.ic_product, "₹500", "Add to Cart"),
            Product("Crop Booster", R.drawable.ic_product, "₹1200", "Add to Cart"),
            Product("Seeds Pack", R.drawable.ic_product, "₹300", "Add to Cart")
        )

        val adapter = mainAdapter(items)
        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProducts.adapter = adapter
        

        // =================================================================
        // === NEW CODE ADDED FOR REDIRECTION ==============================
        // =================================================================
        binding.rvProducts.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                val addToCartButton = view.findViewById<View>(R.id.btnAddToCart)
                addToCartButton?.setOnClickListener {
                    // Start your Cart Activity
                    val intent = Intent(requireContext(), CartFragment::class.java)
                    startActivity(intent)
                }
            }

            override fun onChildViewDetachedFromWindow(view: View) {
                view.findViewById<View>(R.id.btnAddToCart)?.setOnClickListener(null)
            }
        })

        return view
    }
        // =================================================================
        // === END OF NEW CODE =============================================
        // =================================================================

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
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}