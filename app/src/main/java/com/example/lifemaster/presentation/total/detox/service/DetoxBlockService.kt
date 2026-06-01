package com.example.lifemaster.presentation.total.detox.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.lifemaster.presentation.total.detox.DetoxBlockActivity
import com.example.lifemaster.presentation.total.detox.DetoxPermanentBlockActivity
import com.example.lifemaster.presentation.total.detox.DetoxRepeatLockLocalManager

class DetoxBlockService : AccessibilityService() {

    private var permanentBlockServicePackageNames: ArrayList<String>? = null
    private var timeBlockServicePackageNames: ArrayList<String>? = null
    private var repeatBlockServicePackageNames: ArrayList<String>? = null

    private var lastBlockedPackageName: String? = null
    private var lastBlockedTime: Long = 0L

    private var timeBlockInfoMap: Map<String, Pair<String, String>> = emptyMap()
    private var repeatBlockInfoMap: Map<String, RepeatBlockInfo> = emptyMap()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.example.lifemaster.BROADCAST_RECEIVER" -> {
                    intent.getStringArrayListExtra("PERMANENT_BLOCK_SERVICE_APPLICATIONS")?.let {
                        permanentBlockServicePackageNames = it
                    }

                    intent.getStringArrayListExtra("TIME_BLOCK_SERVICE_APPLICATIONS")?.let {
                        timeBlockServicePackageNames = it
                    }

                    intent.getStringArrayListExtra("TIME_BLOCK_SERVICE_INFOS")?.let { infos ->
                        timeBlockInfoMap = infos.mapNotNull { info ->
                            val parts = info.split("|")

                            if (parts.size == 3) {
                                val packageName = parts[0]
                                val startTime = parts[1]
                                val endTime = parts[2]

                                packageName to (startTime to endTime)
                            } else {
                                null
                            }
                        }.toMap()
                    }

                    intent.getStringArrayListExtra("REPEAT_BLOCK_SERVICE_INFOS")?.let { infos ->
                        repeatBlockServicePackageNames = ArrayList(
                            infos.mapNotNull { info ->
                                val parts = info.split("|")

                                if (parts.size == 5) {
                                    parts[1]
                                } else {
                                    null
                                }
                            }
                        )

                        repeatBlockInfoMap = infos.mapNotNull { info ->
                            val parts = info.split("|")

                            if (parts.size == 5) {
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
                            } else {
                                null
                            }
                        }.toMap()
                    }

                    Log.d(
                        "DetoxBlockService",
                        "영구 차단 앱 목록 = $permanentBlockServicePackageNames"
                    )

                    Log.d(
                        "DetoxBlockService",
                        "시간 차단 앱 목록 = $timeBlockServicePackageNames"
                    )

                    Log.d(
                        "DetoxBlockService",
                        "시간 차단 정보 = $timeBlockInfoMap"
                    )

                    Log.d(
                        "DetoxBlockService",
                        "반복 차단 앱 목록 = $repeatBlockServicePackageNames"
                    )

                    Log.d(
                        "DetoxBlockService",
                        "반복 차단 정보 = $repeatBlockInfoMap"
                    )
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        val filter = IntentFilter().apply {
            addAction("com.example.lifemaster.BROADCAST_RECEIVER")
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

        Log.d("DetoxBlockService", "DetoxBlockService 실행됨")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val currentPackageName = event.packageName?.toString() ?: return

        Log.d("DetoxBlockService", "현재 실행 앱 = $currentPackageName")
        Log.d("DetoxBlockService", "현재 영구 차단 앱 목록 = $permanentBlockServicePackageNames")
        Log.d("DetoxBlockService", "현재 시간 차단 앱 목록 = $timeBlockServicePackageNames")
        Log.d("DetoxBlockService", "현재 시간 차단 정보 = $timeBlockInfoMap")
        Log.d("DetoxBlockService", "현재 반복 차단 앱 목록 = $repeatBlockServicePackageNames")
        Log.d("DetoxBlockService", "현재 반복 차단 정보 = $repeatBlockInfoMap")

        if (currentPackageName == packageName) return

        if (permanentBlockServicePackageNames?.contains(currentPackageName) == true) {
            preventUsingPermanentBlockApp(currentPackageName)
            return
        }

        if (timeBlockServicePackageNames?.contains(currentPackageName) == true) {
            preventUsingTimeBlockApp(currentPackageName)
            return
        }

        if (repeatBlockServicePackageNames?.contains(currentPackageName) == true) {
            preventUsingRepeatBlockApp(currentPackageName)
            return
        }
    }

    private fun preventUsingPermanentBlockApp(blockedPackageName: String) {
        val now = System.currentTimeMillis()

        if (
            lastBlockedPackageName == blockedPackageName &&
            now - lastBlockedTime < 1500L
        ) {
            return
        }

        lastBlockedPackageName = blockedPackageName
        lastBlockedTime = now

        val intent = Intent(this, DetoxPermanentBlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            putExtra("blockedPackageName", blockedPackageName)
        }

        startActivity(intent)
    }

    private fun preventUsingTimeBlockApp(blockedPackageName: String) {
        val now = System.currentTimeMillis()

        if (
            lastBlockedPackageName == blockedPackageName &&
            now - lastBlockedTime < 1500L
        ) {
            return
        }

        lastBlockedPackageName = blockedPackageName
        lastBlockedTime = now

        val timeInfo = timeBlockInfoMap[blockedPackageName]

        val intent = Intent(this, DetoxBlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            putExtra("blockedPackageName", blockedPackageName)
            putExtra("blockType", "TIME")
            putExtra("startTime", timeInfo?.first)
            putExtra("endTime", timeInfo?.second)
        }

        startActivity(intent)
    }

    private fun preventUsingRepeatBlockApp(blockedPackageName: String) {
        val repeatInfo = repeatBlockInfoMap[blockedPackageName] ?: return

        val state = DetoxRepeatLockLocalManager.getRepeatLockState(
            context = this,
            id = repeatInfo.id,
            packageName = repeatInfo.packageName,
            sessionUsageLimit = repeatInfo.sessionUsageLimit,
            lockDuration = repeatInfo.lockDuration,
            dailyMaxUsageLimit = repeatInfo.dailyMaxUsageLimit
        )

        if (!state.locked) return

        val now = System.currentTimeMillis()

        if (
            lastBlockedPackageName == blockedPackageName &&
            now - lastBlockedTime < 1500L
        ) {
            return
        }

        lastBlockedPackageName = blockedPackageName
        lastBlockedTime = now

        val intent = Intent(this, DetoxBlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            putExtra("blockedPackageName", blockedPackageName)
            putExtra("blockType", "REPEAT")
            putExtra("repeatLockId", repeatInfo.id)
            putExtra("todayUsedMinutes", state.todayUsedMinutes)
            putExtra("remainingUnlockMinutes", state.remainingUnlockMinutes)
            putExtra("exceededDailyLimit", state.exceededDailyLimit)
            putExtra("escapeAvailable", state.escapeAvailable)
        }

        startActivity(intent)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    data class RepeatBlockInfo(
        val id: Long,
        val packageName: String,
        val sessionUsageLimit: Int,
        val lockDuration: Int,
        val dailyMaxUsageLimit: Int
    )
}