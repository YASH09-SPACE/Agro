package com.example.agro.data

import com.google.firebase.Timestamp

data class Comment(
    val commentId: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val createdAt: Timestamp? = null
)
