package com.example.lifemaster.presentation.community.model

data class CommentDto(
    val commentId: Long?,
    val memberId: Long?,
    val comment: String?,
    val nickname: String?,
    val commentDate: String?,
    val liked: Boolean?
)

data class NewCommentRequest(
    val comment: String
)