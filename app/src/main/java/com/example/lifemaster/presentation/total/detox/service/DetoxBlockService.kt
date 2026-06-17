package com.example.lifemaster.presentation.total.detox.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.lifemaster.presentation.total.detox.DetoxBlockActivity
import com.example.lifemaster.presentation.total.detox.DetoxPermanentBlockActivity
import com.example.lifemaster.presentation.total.detox.DetoxRepeatLockLocalManager
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DetoxBlockService : AccessibilityService() {

    private var permanentBlockServicePackageNames: ArrayList<String>? = null
    private var timeBlockServicePackageNames: ArrayList<String>? = null
    private var repeatBlockServicePackageNames: ArrayList<String>? = null

    private var lastBlockedPackageName: String? = null
    private var lastBlockedTime: Long = 0L

    private var currentForegroundPackageName: String? = null

    private val repeatHandler = Handler(Looper.getMainLooper())
    private var repeatCheckRunnable: Runnable? = null

    private val timeHandler = Handler(Looper.getMainLooper())
    private var timeCheckRunnable: Runnable? = null

    private var timeBlockInfoMap: Map<String, Pair<String, String>> = emptyMap()
    private var repeatBlockInfoMap: Map<String, RepeatBlockInfo> = emptyMap()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.example.lifemaster.BROADCAST_RECEIVER" -> {
                    val permanentApps =
                        intent.getStringArrayListExtra("PERMANENT_BLOCK_SERVICE_APPLICATIONS")
                            ?: arrayListOf()

                    val timeApps =
                        intent.getStringArrayListExtra("TIME_BLOCK_SERVICE_APPLICATIONS")
                            ?: arrayListOf()

                    val timeInfos =
                        intent.getStringArrayListExtra("TIME_BLOCK_SERVICE_INFOS")
                            ?: arrayListOf()

                    permanentBlockServicePackageNames = permanentApps
                    timeBlockServicePackageNames = timeApps

                    timeBlockInfoMap = timeInfos.mapNotNull { info ->
                        val parts = info.split("|")

                        if (parts.size == 3) {
                            parts[0] to (parts[1] to parts[2])
                        } else {
                            null
                        }
                    }.toMap()

                    getSharedPreferences("detox_pref", Context.MODE_PRIVATE)
                        .edit()
                        .clear()
                        .putStringSet("PERMANENT_BLOCK_SERVICE_APPLICATIONS", permanentApps.toSet())
                        .putStringSet("TIME_BLOCK_SERVICE_APPLICATIONS", timeApps.toSet())
                        .putStringSet("TIME_BLOCK_SERVICE_INFOS", timeInfos.toSet())
                        .apply()

                    val repeatInfos =
                        intent.getStringArrayListExtra("REPEAT_BLOCK_SERVICE_INFOS")
                            ?: arrayListOf()

                    applyRepeatBlockInfos(repeatInfos)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        restoreRepeatBlockInfos()

        val filter = IntentFilter().apply {
            addAction("com.example.lifemaster.BROADCAST_RECEIVER")
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

        Log.d("DetoxBlockService", "DetoxBlockService 실행됨")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val currentPackageName = event.packageName?.toString() ?: return
        currentForegroundPackageName = currentPackageName

        if (currentPackageName == packageName) return

        cancelRepeatCheck()
        cancelTimeCheck()

        if (permanentBlockServicePackageNames?.contains(currentPackageName) == true) {
            preventUsingPermanentBlockApp(currentPackageName)
            return
        }

        if (timeBlockServicePackageNames?.contains(currentPackageName) == true) {
            handleTimeBlockApp(currentPackageName)
            return
        }

        if (repeatBlockServicePackageNames?.contains(currentPackageName) == true) {
            handleRepeatBlockApp(currentPackageName)
            return
        }
    }

    private fun handleTimeBlockApp(blockedPackageName: String) {
        val timeInfo = timeBlockInfoMap[blockedPackageName]

        if (timeInfo == null) {
            Log.d("DetoxBlockService", "시간잠금 정보 없음 package=$blockedPackageName")
            return
        }

        val startTime = runCatching {
            LocalTime.parse(timeInfo.first, timeFormatter)
        }.getOrNull()

        val endTime = runCatching {
            LocalTime.parse(timeInfo.second, timeFormatter)
        }.getOrNull()

        if (startTime == null || endTime == null) {
            Log.d(
                "DetoxBlockService",
                "시간잠금 시간 파싱 실패 package=$blockedPackageName, start=${timeInfo.first}, end=${timeInfo.second}"
            )
            return
        }

        val nowTime = LocalTime.now()
        val isBlockedNow = isTimeBlockedNow(nowTime, startTime, endTime)

        Log.d(
            "DetoxBlockService",
            "시간잠금 판단 package=$blockedPackageName, now=$nowTime, start=$startTime, end=$endTime, isBlockedNow=$isBlockedNow"
        )

        if (isBlockedNow) {
            startTimeBlockActivity(blockedPackageName, timeInfo.first, timeInfo.second)
        } else {
            scheduleTimeBlockCheck(blockedPackageName, startTime)
        }
    }

    private fun isTimeBlockedNow(
        nowTime: LocalTime,
        startTime: LocalTime,
        endTime: LocalTime
    ): Boolean {
        return if (endTime.isAfter(startTime)) {
            nowTime >= startTime && nowTime < endTime
        } else {
            nowTime >= startTime || nowTime < endTime
        }
    }

    private fun scheduleTimeBlockCheck(
        blockedPackageName: String,
        startTime: LocalTime
    ) {
        cancelTimeCheck()

        val nowTime = LocalTime.now()
        var delayMillis = Duration.between(nowTime, startTime).toMillis()

        if (delayMillis < 0L) {
            delayMillis += 24L * 60L * 60L * 1000L
        }

        timeCheckRunnable = Runnable {
            if (currentForegroundPackageName == blockedPackageName) {
                Log.d("DetoxBlockService", "예약된 시간잠금 검사 실행 package=$blockedPackageName")
                handleTimeBlockApp(blockedPackageName)
            }
        }

        timeHandler.postDelayed(timeCheckRunnable!!, delayMillis + 300L)

        Log.d(
            "DetoxBlockService",
            "시간잠금 예약 package=$blockedPackageName, delayMillis=$delayMillis"
        )
    }

    private fun cancelTimeCheck() {
        timeCheckRunnable?.let {
            timeHandler.removeCallbacks(it)
        }
        timeCheckRunnable = null
    }

    private fun startTimeBlockActivity(
        blockedPackageName: String,
        startTime: String,
        endTime: String
    ) {
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
            putExtra("blockType", "TIME")
            putExtra("startTime", startTime)
            putExtra("endTime", endTime)
        }

        startActivity(intent)
    }

    private fun handleRepeatBlockApp(blockedPackageName: String) {
        val repeatInfo = repeatBlockInfoMap[blockedPackageName] ?: return

        val state = DetoxRepeatLockLocalManager.getRepeatLockState(
            context = this,
            id = repeatInfo.id,
            packageName = repeatInfo.packageName,
            sessionUsageLimit = repeatInfo.sessionUsageLimit,
            lockDuration = repeatInfo.lockDuration,
            dailyMaxUsageLimit = repeatInfo.dailyMaxUsageLimit
        )

        Log.d(
            "DetoxBlockService",
            "반복잠금 판단 package=$blockedPackageName, locked=${state.locked}, used=${state.todayUsedMinutes}, threshold=${state.currentLockThresholdMinutes}"
        )

        if (state.locked) {
            startRepeatBlockActivity(blockedPackageName, repeatInfo, state)
        } else {
            scheduleRepeatCheck(blockedPackageName, repeatInfo, state.currentLockThresholdMinutes)
        }
    }

    private fun scheduleRepeatCheck(
        blockedPackageName: String,
        repeatInfo: RepeatBlockInfo,
        nextLockThresholdMinutes: Int
    ) {
        if (nextLockThresholdMinutes <= 0) return

        val remainingMillis = DetoxRepeatLockLocalManager.getRemainingMillisUntilNextLock(
            context = this,
            id = repeatInfo.id,
            packageName = repeatInfo.packageName,
            nextLockThresholdMinutes = nextLockThresholdMinutes
        )

        val delayMillis = remainingMillis.coerceAtLeast(1000L) + 500L

        repeatCheckRunnable = Runnable {
            if (currentForegroundPackageName == blockedPackageName) {
                handleRepeatBlockApp(blockedPackageName)
            }
        }

        repeatHandler.postDelayed(repeatCheckRunnable!!, delayMillis)

        Log.d(
            "DetoxBlockService",
            "반복잠금 예약 package=$blockedPackageName, delayMillis=$delayMillis"
        )
    }

    private fun cancelRepeatCheck() {
        repeatCheckRunnable?.let {
            repeatHandler.removeCallbacks(it)
        }
        repeatCheckRunnable = null
    }

    private fun startRepeatBlockActivity(
        blockedPackageName: String,
        repeatInfo: RepeatBlockInfo,
        state: DetoxRepeatLockLocalManager.RepeatLockState
    ) {
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
            putExtra("currentLockThresholdMinutes", state.currentLockThresholdMinutes)
            putExtra("isDailyLimitLock", state.isDailyLimitLock)
        }

        startActivity(intent)
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

    private fun restoreRepeatBlockInfos() {
        val savedInfos = DetoxRepeatLockLocalManager.loadRepeatBlockInfos(this)

        repeatBlockInfoMap = savedInfos.mapValues { (_, info) ->
            RepeatBlockInfo(
                id = info.id,
                packageName = info.packageName,
                sessionUsageLimit = info.sessionUsageLimit,
                lockDuration = info.lockDuration,
                dailyMaxUsageLimit = info.dailyMaxUsageLimit
            )
        }

        repeatBlockServicePackageNames = ArrayList(repeatBlockInfoMap.keys)

        Log.d("DetoxBlockService", "저장된 반복 차단 정보 복원 = $repeatBlockInfoMap")
    }

    private fun applyRepeatBlockInfos(infos: ArrayList<String>) {
        repeatBlockServicePackageNames = ArrayList(
            infos.mapNotNull { info ->
                val parts = info.split("|")
                if (parts.size == 5) parts[1] else null
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

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        cancelRepeatCheck()
        cancelTimeCheck()
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