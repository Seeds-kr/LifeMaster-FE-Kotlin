package com.example.lifemaster.presentation.community.model

data class PostDetailDto(
    val title: String? = null,
    val content: String? = null,
    val file: String? = null,
    val type: String? = null,
    val nickname: String? = null,
    val createdAt: String? = null,
    val likeCount: Int? = null,
    val liked: Boolean? = null,
    val memberId: Long? = null   // ✅ 서버가 주면 사용, 없으면 null
)
