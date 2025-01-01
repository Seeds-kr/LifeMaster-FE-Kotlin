package com.example.lifemaster_xml.total.detox.model

data class DetoxTimeLockItem(
    val itemId: Int,
    val weekType: String,
    val day: String,
    val startHour: String,
    val startMinutes: String,
    val startType: String,
    val endHour: String,
    val endMinutes: String,
    val endType: String
)

data class DetoxTargetApp(
    val appIcon: Int,
    val appName: String,
    val isClicked: Boolean = false
)

data class DetoxRepeatLockItem(
    val appIcon: Int,
    val appName: String,
    val useTime: Int,
    val lockTime: Int,
    val maxTime: Int,
    val isMaxTimeLimitSet: Boolean = false
)
