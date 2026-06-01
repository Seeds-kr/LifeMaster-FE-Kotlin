package com.example.lifemaster.presentation.total.detox

import android.app.usage.UsageStatsManager
import android.content.Context
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object DetoxRepeatLockLocalManager {

    private const val PREF_NAME = "detox_repeat_lock_local_pref"

    fun getTodayUsedMinutes(context: Context, packageName: String): Int {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val now = LocalDateTime.now()
        val startOfDay = LocalDate.now().atStartOfDay()

        val startMillis = startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startMillis,
            endMillis
        )

        val totalMillis = usageStats
            .filter { it.packageName == packageName }
            .sumOf { it.totalTimeInForeground }

        return (totalMillis / 1000 / 60).toInt()
    }

    fun getRepeatLockState(
        context: Context,
        id: Long,
        packageName: String,
        sessionUsageLimit: Int,
        lockDuration: Int,
        dailyMaxUsageLimit: Int
    ): RepeatLockLocalState {
        val todayUsedMinutes = getTodayUsedMinutes(context, packageName)
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val today = LocalDate.now().toString()
        val savedDate = prefs.getString("${id}_date", "")

        if (savedDate != today) {
            prefs.edit()
                .putString("${id}_date", today)
                .putInt("${id}_lastLockedUsageMinutes", 0)
                .putLong("${id}_lockEndTimeMillis", 0L)
                .putString("${id}_escapeUsedDate", "")
                .apply()
        }

        val nowMillis = System.currentTimeMillis()
        val lockEndTimeMillis = prefs.getLong("${id}_lockEndTimeMillis", 0L)
        val lastLockedUsageMinutes = prefs.getInt("${id}_lastLockedUsageMinutes", 0)
        val escapeUsedDate = prefs.getString("${id}_escapeUsedDate", "")

        val isDailyLimitReached = dailyMaxUsageLimit > 0 && todayUsedMinutes >= dailyMaxUsageLimit

        if (isDailyLimitReached) {
            return RepeatLockLocalState(
                todayUsedMinutes = todayUsedMinutes,
                remainingUnlockMinutes = 0,
                exceededDailyLimit = true,
                locked = true,
                escapeAvailable = false
            )
        }

        if (lockEndTimeMillis > nowMillis) {
            val remainingMillis = lockEndTimeMillis - nowMillis
            val remainingUnlockMinutes = ((remainingMillis + 59999L) / 60000L).toInt()

            return RepeatLockLocalState(
                todayUsedMinutes = todayUsedMinutes,
                remainingUnlockMinutes = remainingUnlockMinutes,
                exceededDailyLimit = false,
                locked = true,
                escapeAvailable = escapeUsedDate != today
            )
        }

        val usedAfterLastLock = todayUsedMinutes - lastLockedUsageMinutes

        if (usedAfterLastLock >= sessionUsageLimit) {
            val newLockEndTimeMillis = nowMillis + lockDuration * 60L * 1000L

            prefs.edit()
                .putString("${id}_date", today)
                .putInt("${id}_lastLockedUsageMinutes", todayUsedMinutes)
                .putLong("${id}_lockEndTimeMillis", newLockEndTimeMillis)
                .apply()

            return RepeatLockLocalState(
                todayUsedMinutes = todayUsedMinutes,
                remainingUnlockMinutes = lockDuration,
                exceededDailyLimit = false,
                locked = true,
                escapeAvailable = escapeUsedDate != today
            )
        }

        return RepeatLockLocalState(
            todayUsedMinutes = todayUsedMinutes,
            remainingUnlockMinutes = 0,
            exceededDailyLimit = false,
            locked = false,
            escapeAvailable = escapeUsedDate != today
        )
    }

    fun successEscape(
        context: Context,
        id: Long,
        packageName: String
    ) {
        val today = LocalDate.now().toString()
        val todayUsedMinutes = getTodayUsedMinutes(context, packageName)
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        prefs.edit()
            .putString("${id}_date", today)
            .putInt("${id}_lastLockedUsageMinutes", todayUsedMinutes)
            .putLong("${id}_lockEndTimeMillis", 0L)
            .putString("${id}_escapeUsedDate", today)
            .apply()
    }
}

data class RepeatLockLocalState(
    val todayUsedMinutes: Int,
    val remainingUnlockMinutes: Int,
    val exceededDailyLimit: Boolean,
    val locked: Boolean,
    val escapeAvailable: Boolean
)