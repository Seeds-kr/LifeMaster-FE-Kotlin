package com.example.lifemaster.presentation.total.challenge.model

data class ChallengeResponse(
    val content: List<ChallengeItem>,
    val empty: Boolean,
    val first: Boolean,
    val last: Boolean,
    val number: Int,
    val numberOfElements: Int,
    val pageable: Pageable,
    val size: Int,
    val sort: SortX,
    val totalElements: Int,
    val totalPages: Int
)