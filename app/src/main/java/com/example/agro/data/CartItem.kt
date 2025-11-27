package com.example.agro.Model

data class CartItem(
    val productId: String? = null,
    val imageResId: Int? = null,
    val imageUrl: String? = null,
    val name: String,
    val category: String,
    val price: Double,
    var quantity: Int,
    val stockQuantity: Int
)
