package com.example.lifemaster.presentation.total.detox.model

import android.graphics.drawable.Drawable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

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

@Parcelize
data class DetoxRepeatLockItem(
    val appIcon: @RawValue Drawable,
    val appName: String,
    val appPackageName: String,
    val useTime: Int = 0,
    val lockTime: Int = 0,
    val maxTime: Int = 0,
    val accumulatedTime: Long = 0L, // 사용 시간 (unit: milliseconds)
    val isMaxTimeLimitSet: Boolean = false
): Parcelable

data class TestItem(
    val appPackageName: String,
    val useTime: Int = 0,
    val lockTime: Int = 0,
    val maxTime: Int = 0,
    val isMaxTimeLimitSet: Boolean = false
)


