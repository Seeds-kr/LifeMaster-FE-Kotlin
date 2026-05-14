package com.example.lifemaster.presentation.total.challenge.model

/**
 * @param participatingIds [GET /challenge/my] 등으로 알아낸 내가 참여 중인 챌린지 ID 집합.
 * 목록 API에 challMe가 없을 때 참여 여부를 맞추기 위해 사용합니다.
 */
fun ChallengeItemDto.toPresentation(participatingIds: Set<Long> = emptySet()): ChallengeItem {
    val joined = challMe == true || challId in participatingIds
    return ChallengeItem(
        challId = challId,
        challName = challName,
        challTitle = challDesc,
        challImg = challImg,
        challJoinCnt = challCnt,
        createdAt = createdAt,
        isJoined = joined
    )
}