package com.example.lifemaster.presentation.home.alarm.util

import android.content.Intent
import androidx.fragment.app.Fragment
import com.example.lifemaster.presentation.MainActivity
import com.example.lifemaster.presentation.home.alarm.view.AlarmDisplayActivity
import com.example.lifemaster.presentation.home.alarm.view.service.AlarmService

/**
 * 알람 서비스를 멈추고 [AlarmDisplayActivity]를 종료한 뒤 메인 화면으로 돌아갑니다.
 */
fun Fragment.dismissAlarmDisplayFlow() {
    val activity = requireActivity()
    if (activity !is AlarmDisplayActivity || activity.isFinishing) return

    val appCtx = activity.applicationContext
    appCtx.stopService(Intent(appCtx, AlarmService::class.java))

    appCtx.startActivity(
        Intent(appCtx, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }
    )
    activity.finish()
}
