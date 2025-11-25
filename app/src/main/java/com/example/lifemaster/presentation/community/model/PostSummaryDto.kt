package com.example.lifemaster.presentation.community.model

data class PostSummaryDto(
    val id: Long?,
    val title: String?,
    val createdAt: String?,
    val viewCount: Int?,
    val commentCount: Int?,
    val nickName: String?,
    val likeCount: Int?,
    val liked: Boolean?
)