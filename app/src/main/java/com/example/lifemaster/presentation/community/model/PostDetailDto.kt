package com.example.lifemaster.presentation.community.model

data class PostDetailDto(
    val title: String?,
    val content: String?,
    val file: String?,
    val type: String?,
    val memberId: Long?,
    val createdAt: String?,
    val likeCount: Int?,
    val liked: Boolean?,
    val isMine: Boolean?,
    val nickname: String?,
    val viewCount: Int?,
    val calendarShared: Boolean?
)