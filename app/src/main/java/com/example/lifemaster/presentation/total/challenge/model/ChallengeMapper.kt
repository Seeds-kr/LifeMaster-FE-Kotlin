package com.example.lifemaster.presentation.total.challenge.model

fun ChallengeItemDto.toPresentation(): ChallengeItem {
    return ChallengeItem(
        challId = challId,
        challName = challName,
        challTitle = challDesc,
        challImg = challImg,
        challJoinCnt = challCnt,
        createdAt = createdAt
    )
}