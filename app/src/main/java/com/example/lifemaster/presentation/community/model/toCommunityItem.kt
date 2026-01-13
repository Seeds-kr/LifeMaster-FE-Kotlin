package com.example.lifemaster.presentation.community.model

fun PostSummaryDto.toCommunityItem(
    idStr: String,
    type: String? = null
): CommunityItem =
    CommunityItem(
        id = idStr,
        title = this.title.orEmpty(),
        content = "",
        author = this.nickName.orEmpty(),
        likes = this.likeCount ?: 0,
        liked = this.liked == true,
        views = this.viewCount ?: 0,
        createdAt = this.createdAt,
        fileUri = null,
        type = type ?: "FREE",
        commentCount = this.commentCount ?: 0,
    )