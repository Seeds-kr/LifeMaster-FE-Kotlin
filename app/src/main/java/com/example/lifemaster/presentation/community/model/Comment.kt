package com.example.lifemaster.presentation.community.model

data class Comment(
    val id: Long = System.currentTimeMillis(),
    val nickname: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val isEdited: Boolean = false
)