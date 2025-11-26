package com.example.lifemaster.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * 챌린지 목록 API(/challenge)의 전체 응답을 담는 데이터 클래스(DTO)
 */
data class ChallengeListResponse(
    @SerializedName("content")
    val content: List<ChallengeItemDto>
)

/**
 * ChallengeListResponse의 content 배열에 포함된 개별 챌린지 아이템 DTO
 */
data class ChallengeItemDto(
    @SerializedName("challId")
    val challId: Long,
    @SerializedName("challName")
    val challName: String,
    @SerializedName("challDesc")
    val challDesc: String,
    @SerializedName("challImg")
    val challImg: String,
    @SerializedName("challCnt")
    val challCnt: Int
)
