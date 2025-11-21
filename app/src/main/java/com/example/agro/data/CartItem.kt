package com.example.agro.Model

data class CartItem(
    val imageResId: Int,
    val name: String,
    val category: String,
    val price: String,
    val oldPrice: String,
    var quantity: Int
)
