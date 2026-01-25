package com.example.lifemaster.presentation.community.model

data class CommunityItem(
    val id: String,
    val title: String,
    val content: String,
    val author: String,
    val likes: Int,
    val liked: Boolean,
    val views: Int,
    val createdAt: String?,
    val fileUri: String?,
    val type: String?,
    val commentCount: Int
)