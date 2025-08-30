package com.example.lifemaster.presentation.home.sleep.model

import java.time.Duration
import java.time.Instant

data class SleepResponse(
    val sleepId: Int,
    val sleepDate: String, // 2025-08-28
    val sleepStart: String, // 2025-08-27T14:30:44.345151
    val sleepEnd: String, // 2025-08-27T22:30:44.345176
    val sleepMood: String, // BAD
    val alarmSnoozeCnt: Int, // 0
    val timeToWakeUp: Int, // 0
    val antiSleepMode: Boolean, // false
    val sleepScore: Float // 60.0
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