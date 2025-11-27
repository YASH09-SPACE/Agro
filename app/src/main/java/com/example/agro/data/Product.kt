package com.example.agro.data

// com.example.agro.data.Product
data class Product(
    val name: String,
    val imageUrl: String,        // <-- image from Firestore
    val price: Double,
    val productId: String,
    val stockQuantity: Int,
    val category: String,
    val description: String = "" // optional
)
