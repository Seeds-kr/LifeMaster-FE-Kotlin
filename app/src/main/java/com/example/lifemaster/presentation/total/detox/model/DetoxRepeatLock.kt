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