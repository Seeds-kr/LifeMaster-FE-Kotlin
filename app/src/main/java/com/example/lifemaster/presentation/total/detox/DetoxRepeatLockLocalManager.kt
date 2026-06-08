package com.example.lifemaster.presentation.total.detox

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

object DetoxRepeatLockLocalManager {

    private const val PREF_NAME = "detox_repeat_lock_pref"
    private const val KEY_REPEAT_BLOCK_INFOS = "repeat_block_infos"

    data class RepeatBlockInfo(
        val id: Long,
        val packageName: String,
        val sessionUsageLimit: Int,
        val lockDuration: Int,
        val dailyMaxUsageLimit: Int
    )

    data class RepeatLockState(
        val locked: Boolean,
        val todayUsedMinutes: Int,
        val remainingUnlockMinutes: Int,
        val exceededDailyLimit: Boolean,
        val escapeAvailable: Boolean,
        val isLastLockSection: Boolean,
        val currentLockThresholdMinutes: Int,
        val isDailyLimitLock: Boolean
    )

    fun saveRepeatBlockInfos(
        context: Context,
        infos: List<RepeatBlockInfo>
    ) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val encodedInfos = infos.map {
            "${it.id}|${it.packageName}|${it.sessionUsageLimit}|${it.lockDuration}|${it.dailyMaxUsageLimit}"
        }.toSet()

        val editor = prefs.edit()
            .putStringSet(KEY_REPEAT_BLOCK_INFOS, encodedInfos)

        val today = LocalDate.now().toString()

        infos.forEach { info ->
            val savedStartMillis = prefs.getLong(usageStartKey(info.id), 0L)

            if (savedStartMillis == 0L) {
                editor
                    .putString(dateKey(info.id), today)
                    .putLong(usageStartKey(info.id), System.currentTimeMillis())
                    .putInt(completedThresholdKey(info.id), 0)
                    .putBoolean(disabledTodayKey(info.id), false)
                    .remove(lockStartKey(info.id))
                    .remove(currentThresholdKey(info.id))
            }
        }

        editor.apply()
    }

    fun loadRepeatBlockInfos(context: Context): Map<String, RepeatBlockInfo> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        return prefs.getStringSet(KEY_REPEAT_BLOCK_INFOS, emptySet())
            .orEmpty()
            .mapNotNull { info ->
                val parts = info.split("|")
                if (parts.size != 5) return@mapNotNull null

                val id = parts[0].toLongOrNull()
                val packageName = parts[1]
                val sessionUsageLimit = parts[2].toIntOrNull()
                val lockDuration = parts[3].toIntOrNull()
                val dailyMaxUsageLimit = parts[4].toIntOrNull()

                if (
                    id != null &&
                    packageName.isNotBlank() &&
                    sessionUsageLimit != null &&
                    lockDuration != null &&
                    dailyMaxUsageLimit != null
                ) {
                    packageName to RepeatBlockInfo(
                        id = id,
                        packageName = packageName,
                        sessionUsageLimit = sessionUsageLimit,
                        lockDuration = lockDuration,
                        dailyMaxUsageLimit = dailyMaxUsageLimit
                    )
                } else {
                    null
                }
            }.toMap()
    }

    fun getTodayUsedMinutes(
        context: Context,
        id: Long,
        packageName: String
    ): Int {
        return (getTodayUsedMillis(context, id, packageName) / 1000L / 60L).toInt()
    }

    fun getRemainingMillisUntilNextLock(
        context: Context,
        id: Long,
        packageName: String,
        nextLockThresholdMinutes: Int
    ): Long {
        val usedMillis = getTodayUsedMillis(context, id, packageName)
        val thresholdMillis = TimeUnit.MINUTES.toMillis(nextLockThresholdMinutes.toLong())
        return (thresholdMillis - usedMillis).coerceAtLeast(0L)
    }

    private fun getTodayUsedMillis(
        context: Context,
        id: Long,
        packageName: String
    ): Long {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val startMillis = getOrCreateUsageStartMillis(context, id)
        val endMillis = System.currentTimeMillis()

        val events = usageStatsManager.queryEvents(startMillis, endMillis)
        val event = UsageEvents.Event()

        var lastResumeTime = 0L
        var totalForegroundMillis = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            if (event.packageName != packageName) continue

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED,
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    lastResumeTime = event.timeStamp
                }

                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    if (lastResumeTime > 0L && event.timeStamp > lastResumeTime) {
                        totalForegroundMillis += event.timeStamp - lastResumeTime
                        lastResumeTime = 0L
                    }
                }
            }
        }

        if (lastResumeTime > 0L) {
            totalForegroundMillis += endMillis - lastResumeTime
        }

        Log.d(
            "RepeatUsage",
            "USED id=$id, package=$packageName, millis=$totalForegroundMillis, minutes=${totalForegroundMillis / 1000L / 60L}"
        )

        return totalForegroundMillis
    }

    fun getRepeatLockState(
        context: Context,
        id: Long,
        packageName: String,
        sessionUsageLimit: Int,
        lockDuration: Int,
        dailyMaxUsageLimit: Int
    ): RepeatLockState {
        resetIfNewDay(context, id)

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val todayUsedMinutes = getTodayUsedMinutes(
            context = context,
            id = id,
            packageName = packageName
        )

        if (prefs.getBoolean(disabledTodayKey(id), false)) {
            return RepeatLockState(
                locked = false,
                todayUsedMinutes = todayUsedMinutes,
                remainingUnlockMinutes = 0,
                exceededDailyLimit = false,
                escapeAvailable = false,
                isLastLockSection = false,
                currentLockThresholdMinutes = 0,
                isDailyLimitLock = false
            )
        }

        if (dailyMaxUsageLimit > 0 && todayUsedMinutes >= dailyMaxUsageLimit) {
            return RepeatLockState(
                locked = true,
                todayUsedMinutes = todayUsedMinutes,
                remainingUnlockMinutes = getRemainingMinutesUntilTomorrow(),
                exceededDailyLimit = true,
                escapeAvailable = true,
                isLastLockSection = true,
                currentLockThresholdMinutes = dailyMaxUsageLimit,
                isDailyLimitLock = true
            )
        }

        val completedThresholdMinutes = prefs.getInt(completedThresholdKey(id), 0)
        val nextLockThresholdMinutes = completedThresholdMinutes + sessionUsageLimit

        if (todayUsedMinutes < nextLockThresholdMinutes) {
            return RepeatLockState(
                locked = false,
                todayUsedMinutes = todayUsedMinutes,
                remainingUnlockMinutes = 0,
                exceededDailyLimit = false,
                escapeAvailable = false,
                isLastLockSection = false,
                currentLockThresholdMinutes = nextLockThresholdMinutes,
                isDailyLimitLock = false
            )
        }

        val now = System.currentTimeMillis()
        var lockStartMillis = prefs.getLong(lockStartKey(id), 0L)

        if (lockStartMillis == 0L) {
            lockStartMillis = now

            prefs.edit()
                .putLong(lockStartKey(id), lockStartMillis)
                .putInt(currentThresholdKey(id), nextLockThresholdMinutes)
                .apply()
        }

        val lockEndMillis =
            lockStartMillis + TimeUnit.MINUTES.toMillis(lockDuration.toLong())

        if (now >= lockEndMillis) {
            prefs.edit()
                .putInt(completedThresholdKey(id), nextLockThresholdMinutes)
                .remove(lockStartKey(id))
                .remove(currentThresholdKey(id))
                .apply()

            return RepeatLockState(
                locked = false,
                todayUsedMinutes = todayUsedMinutes,
                remainingUnlockMinutes = 0,
                exceededDailyLimit = false,
                escapeAvailable = false,
                isLastLockSection = false,
                currentLockThresholdMinutes = nextLockThresholdMinutes + sessionUsageLimit,
                isDailyLimitLock = false
            )
        }

        val remainingUnlockMinutes =
            ceil((lockEndMillis - now) / 1000.0 / 60.0).toInt()

        return RepeatLockState(
            locked = true,
            todayUsedMinutes = todayUsedMinutes,
            remainingUnlockMinutes = remainingUnlockMinutes,
            exceededDailyLimit = false,
            escapeAvailable = true,
            isLastLockSection = false,
            currentLockThresholdMinutes = nextLockThresholdMinutes,
            isDailyLimitLock = false
        )
    }

    fun escapeRepeatLock(
        context: Context,
        id: Long,
        currentLockThresholdMinutes: Int,
        isDailyLimitLock: Boolean
    ) {
        resetIfNewDay(context, id)

        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        if (isDailyLimitLock) {
            prefs.edit()
                .putBoolean(disabledTodayKey(id), true)
                .remove(lockStartKey(id))
                .remove(currentThresholdKey(id))
                .apply()
        } else {
            prefs.edit()
                .putInt(completedThresholdKey(id), currentLockThresholdMinutes)
                .remove(lockStartKey(id))
                .remove(currentThresholdKey(id))
                .apply()
        }
    }

    fun resetUsageStartMillis(
        context: Context,
        id: Long
    ) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(dateKey(id), LocalDate.now().toString())
            .putLong(usageStartKey(id), System.currentTimeMillis())
            .putInt(completedThresholdKey(id), 0)
            .putBoolean(disabledTodayKey(id), false)
            .remove(lockStartKey(id))
            .remove(currentThresholdKey(id))
            .apply()
    }

    fun isDisabledToday(
        context: Context,
        id: Long
    ): Boolean {
        resetIfNewDay(context, id)

        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(disabledTodayKey(id), false)
    }

    private fun getOrCreateUsageStartMillis(
        context: Context,
        id: Long
    ): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val today = LocalDate.now().toString()
        val savedDate = prefs.getString(dateKey(id), null)
        val savedStartMillis = prefs.getLong(usageStartKey(id), 0L)

        if (savedDate == today && savedStartMillis > 0L) {
            return savedStartMillis
        }

        val startMillis = if (savedStartMillis == 0L) {
            System.currentTimeMillis()
        } else {
            LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }

        prefs.edit()
            .putString(dateKey(id), today)
            .putLong(usageStartKey(id), startMillis)
            .putInt(completedThresholdKey(id), 0)
            .putBoolean(disabledTodayKey(id), false)
            .remove(lockStartKey(id))
            .remove(currentThresholdKey(id))
            .apply()

        return startMillis
    }

    private fun resetIfNewDay(context: Context, id: Long) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val today = LocalDate.now().toString()
        val savedDate = prefs.getString(dateKey(id), null)

        if (savedDate != today) {
            val todayStartMillis = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            prefs.edit()
                .putString(dateKey(id), today)
                .putLong(usageStartKey(id), todayStartMillis)
                .putInt(completedThresholdKey(id), 0)
                .putBoolean(disabledTodayKey(id), false)
                .remove(lockStartKey(id))
                .remove(currentThresholdKey(id))
                .apply()
        }
    }

    private fun getRemainingMinutesUntilTomorrow(): Int {
        val now = LocalDateTime.now()
        val tomorrowStart = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT)
        return ceil(java.time.Duration.between(now, tomorrowStart).toMillis() / 1000.0 / 60.0).toInt()
    }

    private fun dateKey(id: Long) = "repeat_${id}_date"
    private fun usageStartKey(id: Long) = "repeat_${id}_usage_start_millis"
    private fun completedThresholdKey(id: Long) = "repeat_${id}_completed_threshold"
    private fun lockStartKey(id: Long) = "repeat_${id}_lock_start"
    private fun currentThresholdKey(id: Long) = "repeat_${id}_current_threshold"
    private fun disabledTodayKey(id: Long) = "repeat_${id}_disabled_today"
}