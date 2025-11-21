package com.example.agro.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.agro.R
import com.example.agro.checkout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.agro.Adapter.CartAdapter
import com.example.agro.Model.CartItem


class CartFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        // --- RecyclerView setup ---
        val recyclerView = view.findViewById<RecyclerView>(R.id.cart_items_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val cartItems = mutableListOf(
            CartItem(R.drawable.ic_product, "Nitrocea", "Fruit", "₹400", "₹440", 3),
            CartItem(R.drawable.ic_product, "Mango", "Fruit", "₹250", "₹280", 2),
            CartItem(R.drawable.ic_product, "Tomato Seeds", "Seed", "₹120", "₹150", 1)
        )

        recyclerView.adapter = CartAdapter(cartItems)



        // --- Checkout Button ---
        val checkoutButton = view.findViewById<Button>(R.id.CheckoutButton)
        checkoutButton.setOnClickListener {
            val intent = Intent(requireContext(), checkout::class.java)
            startActivity(intent)
        }

        return view
    }

    companion object {
        fun newInstance(): CartFragment = CartFragment()
    }
}
