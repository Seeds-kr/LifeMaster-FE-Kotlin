package com.example.lifemaster.presentation.total.detox.model

/**
 * 디톡스 시간 잠금 요청을 위한 데이터 모델
 * @property cycle 주기 (예: "WEEKLY")
 * @property day 요일 (예: "MONDAY")
 * @property startTime 시작 시간 (HH:mm:ss 형식)
 * @property endTime 종료 시간 (HH:mm:ss 형식)
 * @property active 활성화 상태 여부
 * @property lockedApps 잠금 설정된 앱 이름 리스트
 */
data class DetoxTimeLockRequest(
    val cycle: TimeLockRepeatPeriod,
    val day: TimeLockRepeatDay,
    val startTime: String,
    val endTime: String,
    val active: Boolean,
    val lockedApps: String
)

data class DetoxTimeLockResponse(
    val id: Long,
    val cycle: TimeLockRepeatPeriod,
    val day: TimeLockRepeatDay,
    val startTime: String,
    val endTime: String,
    val active: Boolean,
    val lockedApps: List<String>
)

