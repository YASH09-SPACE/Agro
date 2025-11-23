package com.example.agro.data

data class OrderItem(
    val id: String,
    val orderDateText: String,
    val totalAmount: Double,
    val status: String,
    val itemCount: Int,
    val iconResId: Int = com.example.agro.R.drawable.ic_product // placeholder
)
