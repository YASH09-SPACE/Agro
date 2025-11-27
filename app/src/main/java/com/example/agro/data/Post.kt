package com.example.agro.data

import com.google.firebase.Timestamp

data class Post(
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val createdAt: com.google.firebase.Timestamp? = null,
    val likeCount: Long = 0,
    val commentCount: Long = 0,
    val likedBy: List<String> = emptyList(),

    @Transient
    var isLikedByCurrentUser: Boolean = false
)

