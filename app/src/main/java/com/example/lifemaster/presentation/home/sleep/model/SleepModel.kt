package com.example.lifemaster.presentation.home.sleep.model

import java.time.Duration
import java.time.Instant

data class SleepRequest(
    val userId: Int,
    val sleepId: Int = 0,
    val sleepDate: String, // "2025-08-26"
    val sleepStart: String, // "2025-08-26T22:04:35.513Z"
    val sleepEnd: String, // "2025-08-27T06:04:35.513Z"
    val sleepMood: String?, // "BAD"
    val alarmInfo: AlarmInfo
)

data class SleepResponse(
    val sleepId: Int,
    val sleepDate: String, // 2025-08-28
    val sleepStart: String, // 2025-08-27T14:30:44.345151
    val sleepEnd: String, // 2025-08-27T22:30:44.345176
    val sleepMood: String, // VERY_GOOD, GOOD, BAD, VERY_BAD
    val alarmInfo: AlarmInfo,
    val sleepScore: Float // 수면 점수 60.0
) {
    val sleepDurationText: String
        get() {
            val sleepStartMillis = Instant.parse(sleepStart+"Z").toEpochMilli()
            val sleepEndMillis = Instant.parse(sleepEnd+"Z").toEpochMilli()
            val sleepDurationMillis = sleepEndMillis - sleepStartMillis

            val duration = Duration.ofMillis(sleepDurationMillis)

            val hour = duration.toHours()
            val minute = duration.toMinutes()%60
            return "${hour}시간 ${minute}분"
        }
}

data class AlarmInfo(
    val isWakeUpAlarmSet: Boolean,
    val alarmSettings: AlarmSettingInfo? = null
)

data class AlarmSettingInfo(
    val alarmSnoozeCnt: Int? = null,
    val timeToWakeUp: Int? = null,
    val antiSleepMode: Boolean? = null
)