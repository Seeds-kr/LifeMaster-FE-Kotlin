package com.example.lifemaster.presentation.community.model

data class CommentDto(
    val commentId: Long?,
    val memberId: Long?,
    val comment: String?,
    val commentDate: String?,
    val likeCount: Int?,
    val liked: Boolean?,
    val isMine: Boolean?,
    val nickname: String?
)

data class NewCommentRequest(val comment: String)