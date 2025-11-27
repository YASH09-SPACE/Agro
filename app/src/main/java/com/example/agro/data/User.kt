package com.example.agro.data

data class User(
    val fullName: String? = null,
    val email: String? = null,
    val password: String? = null,
    val provider: String? = null,
    val createdAt: Long? = null
)
