package com.example.lifemaster.presentation.total.challenge.model

/**
 * 앱의 비즈니스 로직(Domain) 또는 UI(Presentation)에서 사용할
 * 챌린지 아이템을 나타내는 데이터 모델.
 * PagingSource가 API 응답(DTO)을 이 클래스로 변환하여 전달합니다.
 */
data class ChallengeItem(
    val challId: Long,
    val challName: String,
    val challTitle: String, // UI에 표시할 제목 (API의 challDesc를 여기에 매핑)
    val challImg: String,
    val challJoinCnt: Int,   // UI에 표시할 참여자 수 (API의 challCnt를 여기에 매핑)
    val createdAt: String? = null  // 정렬을 위해 추가
)
