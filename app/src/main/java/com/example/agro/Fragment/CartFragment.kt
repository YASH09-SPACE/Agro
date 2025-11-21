package com.example.agro.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.Adapter.CartAdapter
import com.example.agro.CartManager
import com.example.agro.R
import com.example.agro.checkout

class CartFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvTotal: TextView
    private lateinit var checkoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        // --- Find views ---
        recyclerView = view.findViewById(R.id.cart_items_recycler_view)
        tvSubtotal = view.findViewById(R.id.tvSubtotal)
        tvTax = view.findViewById(R.id.tvTax)
        tvTotal = view.findViewById(R.id.tvTotal)
        checkoutButton = view.findViewById(R.id.CheckoutButton)

        // --- RecyclerView setup ---
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Use CartManager's global list instead of hardcoded items
        val adapter = CartAdapter(CartManager.cartItems) {
            updateTotals()
        }
        recyclerView.adapter = adapter

        // Calculate totals initially
        updateTotals()

        // --- Checkout Button ---
        checkoutButton.setOnClickListener {
            val intent = Intent(requireContext(), checkout::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun updateTotals() {
        // 🔥 1. Check stock for all items
        var outOfStock = false

        for (item in CartManager.cartItems) {
            if (item.quantity > item.stockQuantity) {
                outOfStock = true
                break
            }
        }

        if (outOfStock) {
            // 🔴 Over-stock: show message and disable checkout
            tvSubtotal.text = "–"
            tvTax.text = "–"
            tvTotal.text = "Not in stock"
            checkoutButton.isEnabled = false
            return
        } else {
            checkoutButton.isEnabled = true
        }

        // 🔥 2. Normal total calculation
        val subtotal = CartManager.getTotal()
        val tax = subtotal * 0.02       // 2% tax
        val total = subtotal + tax

        tvSubtotal.text = "₹${subtotal.toInt()}"
        tvTax.text = "+ ₹${tax.toInt()}"
        tvTotal.text = "₹${total.toInt()}"
    }

    companion object {
        fun newInstance(): CartFragment = CartFragment()
    }
}
