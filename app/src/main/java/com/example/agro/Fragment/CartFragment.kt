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
import android.widget.Toast

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

        val adapter = CartAdapter(CartManager.cartItems) {
            updateTotals()
        }
        recyclerView.adapter = adapter

        updateTotals()

        // --- Checkout Button ---
        checkoutButton.setOnClickListener {
            if (CartManager.cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "Your cart is empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(requireContext(), checkout::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun updateTotals() {

        // ❌ If cart is empty
        if (CartManager.cartItems.isEmpty()) {
            tvSubtotal.text = "₹0"
            tvTax.text = "+ ₹0"
            tvTotal.text = "₹0"
            checkoutButton.isEnabled = false   // Disable checkout
            return
        }

        // ♻ Check stock availability
        var outOfStock = false
        for (item in CartManager.cartItems) {
            if (item.quantity > item.stockQuantity) {
                outOfStock = true
                break
            }
        }

        if (outOfStock) {
            tvSubtotal.text = "–"
            tvTax.text = "–"
            tvTotal.text = "Out of Stock"
            checkoutButton.isEnabled = false
            return
        }

        checkoutButton.isEnabled = true

        // ✔ Calculate totals normally
        val subtotal = CartManager.getTotal()
        val tax = subtotal * 0.02
        val total = subtotal + tax

        tvSubtotal.text = "₹${subtotal.toInt()}"
        tvTax.text = "+ ₹${tax.toInt()}"
        tvTotal.text = "₹${total.toInt()}"
    }

    companion object {
        fun newInstance(): CartFragment = CartFragment()
    }
}
