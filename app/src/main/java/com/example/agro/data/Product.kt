package com.example.agro.data

data class Product(
    val name: String = "",
    val image: Int = 0,
    val price: String = "",
    val buttonText: String = "",
    val productId: String = "",      // 🔹 new
    val stockQuantity: Int = 0,      // 🔹 new (you already use this)
    val category: String = ""        // 🔹 new (you already fetch this)
)
