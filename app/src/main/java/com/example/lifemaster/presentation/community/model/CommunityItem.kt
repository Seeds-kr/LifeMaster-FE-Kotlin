package com.example.lifemaster.presentation.community.model

data class CommunityItem(
    val id: String,
    val title: String,
    val content: String,
    val author: String,
    val authorImage: String? = null,
    val likes: Int = 0,
    val liked: Boolean = false,
    val views: Int = 0,
    val createdAt: Long = 0L,
    val fileUri: String? = null,
    val type: String = "FREE",
    val commentCount: Int = 0,
    val comments: List<Comment> = emptyList()
)