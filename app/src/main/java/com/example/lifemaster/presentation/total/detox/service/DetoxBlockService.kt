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
import com.example.lifemaster.presentation.total.detox.model.TestItem

class DetoxBlockService : AccessibilityService() {

    var blockServicePackageNames: ArrayList<String>? = null
    var temporaryBlockServices = arrayListOf<TestItem>(
        TestItem("com.example.lifemaster", 10, 5, 30, true)
    )
    private val appTotalUsageTime = mutableMapOf<String, Long>() // map[패키지명] = 총 누적 시간

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            blockServicePackageNames = intent?.getStringArrayListExtra("BLOCK_SERVICE_APPLICATIONS")
            permanentBlockServicePackageNames = intent?.getStringArrayListExtra("PERMANENT_BLOCK_SERVICE_APPLICATIONS")
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter("com.example.lifemaster.BROADCAST_RECEIVER")
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }

    private var previousPackageName: String? = null
    private var startTime: Long = 0L
    private var endTime: Long = 0L
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            blockServicePackageNames?.forEach {
                if (event.packageName.toString() == it) {
            val newPackageName = event.packageName.toString() // 안드로이드 기본 화면 앱 패키지 이름 : com.sec.android.app.launcher
            Log.d("ttest(package name)", newPackageName)

            // 영구 차단 서비스
                    preventUsingApp()
                }
            }
        }
    }

            if(newPackageName != previousPackageName && startTime != 0L) {
                // 바로 직전에 사용한 앱의 사용 시간 계산하기
                endTime = System.currentTimeMillis()
                val usedTime = endTime - startTime
                Log.d("ttest(single usage time)", ""+usedTime/1000) // 왜 두번 불리지?
                appTotalUsageTime[previousPackageName!!] = appTotalUsageTime.getOrDefault(previousPackageName!!, 0L) + usedTime
                Log.d("ttest(total usage time)", ""+appTotalUsageTime[previousPackageName!!]!!/1000) // 왜 두번 불리지?
                startTime = 0L // 초기화
                previousPackageName = newPackageName
            }



            if (previousPackageName != newPackageName && startTime == 0L) {
//                 처음 앱을 실행한 경우, 기존 앱과 다른 앱을 실행한 경우
                previousPackageName = newPackageName // 변수값 갱신
                startTrackingCurrentAppTime()
            }
        }
    }

    private fun startTrackingCurrentAppTime() {
        startTime = System.currentTimeMillis()
        Log.d("ttest(start time)", ""+startTime)
    }


    // 차단 앱의 실행을 막는 메소드
    private fun preventUsingApp() {
        val intent = Intent()
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_FORWARD_RESULT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        startActivity(intent)
    }

    override fun onInterrupt() {
        TODO("Not yet implemented")
    }
}