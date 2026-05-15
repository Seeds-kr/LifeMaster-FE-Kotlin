package com.example.lifemaster.presentation.total.introspection.model

data class SelfReflectionByDateResponse(
    val diaryId: Long,
    val thankId: Long,
    val diaryContent: String,
    val thankOne: String,
    val thankTwo: String,
    val thankThree: String,
    val thankFour: String,
    val thankFive: String
)