package com.example.agro.data


data class Post(val postId: String,
                val userName: String,
                val postDate: String,
                val description: String,
                val imageUrl: String,
                var isLiked: Boolean = false)
