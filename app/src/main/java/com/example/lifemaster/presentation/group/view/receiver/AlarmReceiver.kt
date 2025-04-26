package com.example.lifemaster.presentation.group.view.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.lifemaster.presentation.group.view.service.AlarmService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // 앱 강제 종료 후에도 알람 유지 → AlarmManager를 통해 알람을 다시 설정
        }

        val alarmService = Intent(context, AlarmService::class.java)
        context?.startForegroundService(alarmService)
    }
}

