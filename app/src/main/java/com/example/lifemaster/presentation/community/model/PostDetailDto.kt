package com.example.lifemaster.presentation.community.model

data class PostDetailDto(
    val title: String?,
    val content: String?,
    val file: String?,
    val type: String?,
    val createdAt: String?,
    val likeCount: Int?,
    val liked: Boolean?,
    val isMine: Boolean?,
    val nickname: String?,
    val viewCount: Int?
)