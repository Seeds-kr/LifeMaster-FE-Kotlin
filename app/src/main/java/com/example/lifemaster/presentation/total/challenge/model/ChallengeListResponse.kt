package com.example.lifemaster.presentation.total.challenge.model

import com.google.gson.annotations.SerializedName

/**
 * 챌린지 목록 API(/challenge)의 전체 응답을 담는 데이터 클래스(DTO)
 */
data class ChallengeListResponse(
    @SerializedName("content")
    val content: List<ChallengeItemDto>,
    @SerializedName("pageable")
    val pageable: PageableDto? = null,
    @SerializedName("last")
    val last: Boolean,
    @SerializedName("totalPages")
    val totalPages: Int,
    @SerializedName("totalElements")
    val totalElements: Int,
    @SerializedName("size")
    val size: Int,
    @SerializedName("number")
    val number: Int,
    @SerializedName("sort")
    val sort: SortDto? = null,
    @SerializedName("first")
    val first: Boolean,
    @SerializedName("numberOfElements")
    val numberOfElements: Int,
    @SerializedName("empty")
    val empty: Boolean
)

/**
 * ChallengeListResponse의 content 배열에 포함된 개별 챌린지 아이템 DTO
 */
data class ChallengeItemDto(
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("challId")
    val challId: Long,
    @SerializedName("challName")
    val challName: String,
    @SerializedName("challDesc")
    val challDesc: String,
    @SerializedName("challImg")
    val challImg: String,
    @SerializedName("user")
    val user: ChallengeUserDto? = null,
    @SerializedName("challCnt")
    val challCnt: Int,
    @SerializedName("challMe")
    val challMe: Boolean? = null // 검색 결과에만 포함되는 필드 (내가 참여한 챌린지 여부)
)

/**
 * 챌린지 생성자 정보를 담는 DTO
 */
data class ChallengeUserDto(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("nickname")
    val nickname: String? = null,
    @SerializedName("imageUrl")
    val imageUrl: String? = null
)

/**
 * 페이징 정보를 담는 DTO
 */
data class PageableDto(
    @SerializedName("pageNumber")
    val pageNumber: Int,
    @SerializedName("pageSize")
    val pageSize: Int,
    @SerializedName("sort")
    val sort: SortDto? = null,
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("paged")
    val paged: Boolean,
    @SerializedName("unpaged")
    val unpaged: Boolean
)

/**
 * 정렬 정보를 담는 DTO
 */
data class SortDto(
    @SerializedName("sorted")
    val sorted: Boolean,
    @SerializedName("empty")
    val empty: Boolean,
    @SerializedName("unsorted")
    val unsorted: Boolean
)
