package com.example.lifemaster.presentation.group.view.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.lifemaster.presentation.group.view.service.AlarmService
import kotlin.math.min

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("ttest", ""+intent?.action)
        if(intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // 앱 강제 종료 후에도 알람 유지 → AlarmManager를 통해 알람을 다시 설정
            // 기기 재 부팅 시 알람 재 등록
        }

        val alarmService = Intent(context, AlarmService::class.java)
        context?.startForegroundService(alarmService)
    }
}

