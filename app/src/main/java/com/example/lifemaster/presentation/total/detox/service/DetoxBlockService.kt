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

class DetoxBlockService : AccessibilityService() {

    private var permanentBlockServicePackageNames: ArrayList<String>? = null
    private var timeBlockServicePackageNames: ArrayList<String>? = null

    private var lastBlockedPackageName: String? = null
    private var lastBlockedTime: Long = 0L

    private var timeBlockInfoMap: Map<String, Pair<String, String>> = emptyMap()

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

        if (currentPackageName == packageName) return

        if (permanentBlockServicePackageNames?.contains(currentPackageName) == true) {
            preventUsingPermanentBlockApp(currentPackageName)
            return
        }

        if (timeBlockServicePackageNames?.contains(currentPackageName) == true) {
            preventUsingTimeBlockApp(currentPackageName)
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
            putExtra("blockType", "PERMANENT")
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

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }
}