package com.example.lifemaster

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MyService : AccessibilityService() {

    var blockServicePackageNames: ArrayList<String>? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            blockServicePackageNames = intent?.getStringArrayListExtra("BLOCK_SERVICE_APPLICATIONS")
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter("com.example.lifemaster.BROADCAST_RECEIVER")
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            blockServicePackageNames?.forEach {
                if (event.packageName.toString() == it) {
                    preventUsingApp()
                }
            }
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

    override fun onInterrupt() {
        TODO("Not yet implemented")
    }
}