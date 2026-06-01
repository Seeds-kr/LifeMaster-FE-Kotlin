package com.example.lifemaster.presentation.total.detox.model

data class DetoxRepeatLock(
    val lockedApp: String,
    val sessionUsageLimit: Int,
    val lockDuration: Int,
    val dailyMaxUsageLimit: Int
)

data class DetoxRepeatLockResponse(
    val lockedApps: List<DetoxRepeatLockResponseItem>
)

data class DetoxRepeatLockResponseItem(
    val id: Long,
    val lockedApp: String,
    val sessionUsageLimit: Int,
    val lockDuration: Int,
    val dailyMaxUsageLimit: Int
)

data class RepeatLockStatusResponse(
    val id: Long,
    val lockedApp: String,
    val locked: Boolean
)

data class RepeatLockDetailResponse(
    val id: Long,
    val lockedApp: String,
    val todayUsedMinutes: Int,
    val remainingUnlockMinutes: Int,
    val exceededDailyLimit: Boolean
)

data class RepeatPhraseResponse(
    val phrase: String
)