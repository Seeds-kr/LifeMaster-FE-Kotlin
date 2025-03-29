package com.example.lifemaster.presentation.total.detox.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class DetoxBlockService : AccessibilityService() {

    var permanentBlockServicePackageNames: ArrayList<String>? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.example.lifemaster.BROADCAST_RECEIVER" -> {
                    permanentBlockServicePackageNames =
                        intent.getStringArrayListExtra("PERMANENT_BLOCK_SERVICE_APPLICATIONS")
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
    }

    //     나는 디스코드 앱에 대한 반복 잠금을 구현하려 한다.
//    private var temporaryBlockServices = arrayListOf<TestItem>(
//        TestItem("com.discord", 10000L, 5000L, 20000L, true)
//    )

//    private val appTotalUsageTime = mutableMapOf<String, Long>()
//    private var currentPackageName: String? = null
//    private var p: Long = 0L
//    private var t: Long = 0L
//    private var previousPackageName: String? = null
//    private var startTime: Long = 0L
//    private var elapsedTime: Long = 0L

//    private var blockItem: TestItem? = null

//    private val handler = Handler(Looper.getMainLooper())
//    private val updateRunnable = object : Runnable {
//        override fun run() {
//
//            t += 1000L
//            Log.d("ttest t", ""+t)
//            Log.d("ttest p", ""+p)
//
//            if (t >= blockItem.useTime) {
//                preventUsingApp()
//                p += t
//                t = 0L
//                blockItem.isBlocked = true
//                handler.postDelayed({blockItem.isBlocked = false}, blockItem.lockTime) // 얘 동작 안함
//            }
//
//            handler.postDelayed(this, 1000)

//            elapsedTime = System.currentTimeMillis() - startTime
//            appTotalUsageTime[currentPackageName!!] =
//                appTotalUsageTime.getOrDefault(currentPackageName!!, -1L) + 1000
//
//            Log.d("ttest(elapsed Time)", "" + elapsedTime / 1000)
//            Log.d("ttest(total Time)", "" + (appTotalUsageTime[currentPackageName!!] ?: 0L) / 1000)
//
    // 일회 이용 시간 넘으면 일시 차단
//            if (elapsedTime / 1000 >= appItem!!.useTime) {
//                val i =
//                    temporaryBlockServices.indexOfFirst { it.appPackageName == currentPackageName }
//                temporaryBlockServices[i] =
//                    temporaryBlockServices[i].copy(isBlocked = true) // 내가 appItem.isBlocked = true 를 선뜻 하지 못하는 이유 (복사본일까봐)
//                startTime = 0L
//                preventUsingApp()
//                Log.d("ttest", "isRun?")
//                handler.postDelayed({temporaryBlockServices[i] = temporaryBlockServices[i].copy(isBlocked = false)}, appItem!!.lockTime.toLong()*1000)
//            }
//
//             최대 사용 시간 넘으면 차단
//            if (appItem!!.isMaxTimeLimitSet && ((appTotalUsageTime[currentPackageName]
//                    ?: 0L) / 1000) >= appItem!!.maxTime
//            ) {
//                val i =
//                    temporaryBlockServices.indexOfFirst { it.appPackageName == currentPackageName }
//                temporaryBlockServices[i] = temporaryBlockServices[i].copy(isBlocked = true)
//                preventUsingApp()
//                startTime = 0L
//            }

//        }
//    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            permanentBlockServicePackageNames?.forEach {
                if (it == event.packageName.toString()) {
                    preventUsingApp()
                }
            }

//            currentPackageName = event.packageName.toString() // 현재 접속한 앱
//
//            blockItem = temporaryBlockServices.find { it.appPackageName == currentPackageName } ?: return
//
//            if (blockItem.isBlocked) preventUsingApp() // 해당 앱 누적 사용 시간이 최대 사용 시간을 초과한 경우 → 앱 접속 막기 (단위: 하루)
//
//            else {
//                // 해당 앱 누적 사용 시간이 최대 사용 시간을 아직 초과하지 않은 경우
//                startTimer()
//            }

//            if(startTime == 0L) {
//                startTime = System.currentTimeMillis()
//                startTimer()
//            }

//            if (currentPackageName != previousPackageName && startTime != 0L) {
//                stopTimer()
//                previousPackageName = currentPackageName
//                startTime = 0L // 초기화
//            }
//
//            // 현재 들어간 앱이 반복 차단 대상의 앱인 경우
//            appItem = temporaryBlockServices.find { it.appPackageName == currentPackageName } ?: return
//
//            if (appItem!!.isBlocked) {
//                Toast.makeText(this, "해당 앱은 오늘 더이상 사용할 수 없습니다!", Toast.LENGTH_SHORT).show()
//                preventUsingApp()
//                return
//            }
//
//            // 처음 앱을 실행한 경우 또는 기존 앱과 다른 앱을 실행한 경우
//            if (previousPackageName != currentPackageName && startTime == 0L) {
//                startTime = System.currentTimeMillis()
//                previousPackageName = currentPackageName
//                startTimer()
//            }
//        }
        }
    }

    // 차단 앱의 실행을 막는 메소드
    private fun preventUsingApp() {
        val intent = Intent()
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_FORWARD_RESULT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        startActivity(intent)
    }

    override fun onInterrupt() {}
}