package com.example.lifemaster.presentation.total.detox.model

import com.google.gson.annotations.SerializedName

/**
 * 디톡스 시간 잠금 요청을 위한 데이터 모델
 * @property cycle 주기 (예: "WEEKLY")
 * @property day 요일 (예: "MONDAY")
 * @property startTime 시작 시간 (HH:mm:ss 형식)
 * @property endTime 종료 시간 (HH:mm:ss 형식)
 * @property lockedAppPackageName 잠금 설정된 앱 이름 리스트
 */
data class DetoxTimeLockRequest(
    val type: DetoxType,
    val cycle: TimeLockRepeatPeriod,
    val day: TimeLockRepeatDay,
    val startTime: String,
    val endTime: String,
    @SerializedName("lockedApps")
    val lockedAppPackageName: String
)

data class DetoxTimeLockResponse(
    val id: Long,
    val cycle: TimeLockRepeatPeriod,
    val day: TimeLockRepeatDay,
    val startTime: String,
    val endTime: String,
    @SerializedName("lockedApps")
    val lockedAppPackageName: String // packageName
) {
    val startHour: Int get() {
        val hour = startTime.split(":")[0].toInt()
        return when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
    }
    val startMinutes: Int get() {
        return startTime.split(":")[1].toInt()
    }
    val startAmPm: String get() {
        val hour = startTime.split(":")[0].toInt()
        return if(hour < 12) "AM" else "PM"
    }
    val endHour: Int get() {
        val hour = endTime.split(":")[0].toInt()
        return when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
    }
    val endMinutes: Int get() {
        return endTime.split(":")[1].toInt()
    }
    val endAmPm: String get() {
        val hour = endTime.split(":")[0].toInt()
        return if(hour < 12) "AM" else "PM"
    }
}

