package com.example.lifemaster

import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Calendar

class Dummy {
    // 하루 앱 사용 시간을 측정하는 함수(자정 이전의 앱 사용 시간을 포함하여 계산함)
    private fun getDailyUsageStats(context: Context): Map<String, Long> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        return usageStats.associate { it.packageName to it.totalTimeInForeground }
    }
}