package com.example.agro.Model

data class CartItem(
    val imageResId: Int,
    val name: String,
    val category: String,
    val price: Double,
    var quantity: Int = 1,
    val stockQuantity: Int = Int.MAX_VALUE  // 🔥 available stock
)
