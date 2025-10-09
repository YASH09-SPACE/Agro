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

class CartFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

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
