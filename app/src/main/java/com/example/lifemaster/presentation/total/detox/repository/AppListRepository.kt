package com.example.lifemaster.presentation.total.detox.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import com.example.lifemaster.Utils.getDailyUsageStats
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp
import javax.inject.Inject

class AppListRepository @Inject constructor(
   private val context: Context
) {

    /**
     * 실제 디바이스에 설치된 어플리케이션을 가져오는 함수
     */
    fun fetchInstalledApps(): List<DetoxTargetApp> {

        val packageManager = context.packageManager
        val totalApps = packageManager.getInstalledApplications(0)
        val requiredApps = totalApps.filter { app ->
            val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystemApp = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            !isSystemApp && !isUpdatedSystemApp
        }

        val applicationList = arrayListOf<DetoxTargetApp>()
        val usageStatsMap = getDailyUsageStats(context)

        for (app in requiredApps) {
            val appName = app.loadLabel(packageManager).toString()
            val appIcon = app.loadUnbadgedIcon(packageManager)
            val appPackageName = app.packageName
            val accumulatedTime = usageStatsMap[app.packageName] ?: 0L // 누적 사용 시간은 실시간으로 변동되지 않음 (리팩토링 필요)
            applicationList.add(DetoxTargetApp(appIcon, appName, appPackageName, accumulatedTime))
        }

        return applicationList

//        detoxRepeatLockViewModel.blockServiceApplications = ArrayList(applicationList)
//        detoxRepeatLockViewModel.repeatLockTargetApplications = ArrayList(applicationList)
//        detoxTimeLockViewModel.allowServiceApplications = ArrayList(applicationList)
    }
}