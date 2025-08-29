package com.example.lifemaster.presentation.community.model

import java.util.UUID

data class CommunityItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val author: String,
    val views: Int,
    val likes: Int,
    val dateText: String,
    val createdAt: Long = System.currentTimeMillis(),
    val imageResId: Int? = null,
    val fileUri: String? = null,
    val shareCalendar: Boolean = false
)
