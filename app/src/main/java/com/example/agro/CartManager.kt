package com.example.agro

import com.example.agro.Model.CartItem

object CartManager {
    val cartItems = mutableListOf<CartItem>()

    fun addToCart(item: CartItem) {
        val existingItem = cartItems.find { it.name == item.name }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            cartItems.add(item)
        }
    }

    fun getTotal(): Double {
        return cartItems.sumOf { it.price * it.quantity }
    }
}
