package com.example.lifemaster.presentation.total.introspection.model

/**
 * 날짜별 자아성찰(다이어리 + 5감사) 조회 응답 모델
 */
data class SelfReflectionResponse(
    val diaryId: Long?,
    val thankId: Long?,
    val diaryContent: String?,
    val thankOne: String?,
    val thankTwo: String?,
    val thankThree: String?,
    val thankFour: String?,
    val thankFive: String?
)

