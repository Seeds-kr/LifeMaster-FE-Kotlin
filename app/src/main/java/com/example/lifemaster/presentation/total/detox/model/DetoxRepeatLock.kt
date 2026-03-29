package com.example.lifemaster.presentation.total.detox.model

data class DetoxRepeatLock(
    val lockedApp: String,
    val sessionUsageLimit: Int,
    val lockDuration: Int,
    val dailyMaxUsageLimit: Int
)
