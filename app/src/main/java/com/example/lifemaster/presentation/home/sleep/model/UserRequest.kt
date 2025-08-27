package com.example.lifemaster.presentation.home.sleep.model

data class UserRequest(
    val userId: Int,
    val sleepDate: String, // "2025-08-26"
    val sleepStart: String, // "2025-08-26T22:04:35.513Z"
    val sleepEnd: String, // "2025-08-27T06:04:35.513Z"
    val sleepMood: String, // "BAD"
    val alarmSnoozeCnt: Int,
    val timeToWakeUp: Int,
    val antiSleepMode: Boolean
)