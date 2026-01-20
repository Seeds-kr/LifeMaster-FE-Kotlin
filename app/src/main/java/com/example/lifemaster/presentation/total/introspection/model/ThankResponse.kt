package com.example.lifemaster.presentation.total.introspection.model

data class ThankResponse(
    val id: Long,
    val thankDate: String,
    val createdAt: String,
    val thankOne: String,
    val thankTwo: String,
    val thankThree: String,
    val thankFour: String,
    val thankFive: String,
    val member: MemberInfo? = null
)

data class MemberInfo(
    val id: Long,
    val email: String,
    val nickname: String,
    val imageUrl: String? = null
)


