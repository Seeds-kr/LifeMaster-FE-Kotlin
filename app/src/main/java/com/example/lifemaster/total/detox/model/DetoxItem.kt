package com.example.lifemaster.total.detox.model

import android.graphics.drawable.Drawable

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
    val appIcon: Drawable,
    val appName: String,
    val appPackageName: String,
    val accumulatedTime: Long = 0L,
    val isClicked: Boolean = false
)

data class DetoxRepeatLockItem(
    val appIcon: Drawable,
    val appName: String,
    val useTime: Int = 0,
    val lockTime: Int = 0,
    val maxTime: Int = 0,
    val accumulatedTime: Long = 0L, // 사용 시간 (unit: milliseconds)
    val isMaxTimeLimitSet: Boolean = false
)
