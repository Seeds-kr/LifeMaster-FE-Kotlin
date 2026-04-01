package com.example.lifemaster

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Calendar

object Utils {
    /**
     * 자정을 기준으로 하루 앱 사용 시간을 측정하는 함수
     */
    fun getDailyUsageStats(context: Context): Map<String, Long> {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val eventMap = mutableMapOf<String, Long>()

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()

        var currentForegroundApp: String? = null
        var lastEventTime = 0L

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)

            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    currentForegroundApp = event.packageName
                    lastEventTime = event.timeStamp
                }

                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    if (currentForegroundApp != null && lastEventTime != 0L) {
                        val usageTime = event.timeStamp - lastEventTime
                        eventMap[currentForegroundApp] =
                            (eventMap[currentForegroundApp] ?: 0) + usageTime
                    }
                    currentForegroundApp = null
                    lastEventTime = 0L
                }
            }
        }

        return eventMap
    }
}