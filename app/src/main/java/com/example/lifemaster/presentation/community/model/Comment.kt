package com.example.lifemaster.presentation.community.model

data class Comment(
    val id: Long,
    val memberId: Long? = null,
    val nickname: String,
    val content: String,
    val createdAt: Long,
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val isEdited: Boolean = false,
    val isMine: Boolean = false
)