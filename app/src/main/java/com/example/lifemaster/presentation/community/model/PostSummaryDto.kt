package com.example.lifemaster.presentation.community.model

data class PostSummaryDto(
    val id: Long?,
    val title: String?,
    val nickName: String?,
    val viewCount: Int?,
    val commentCount: Int?,
    val createdAt: String?,
    val liked: Boolean?
)
