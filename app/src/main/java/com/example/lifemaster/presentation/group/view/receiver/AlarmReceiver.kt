package com.example.lifemaster.presentation.group.view.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.lifemaster.presentation.group.view.service.AlarmService
import kotlin.math.min

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // 앱 강제 종료 후에도 알람 유지 → AlarmManager를 통해 알람을 다시 설정
        }

        val dayOrNight = intent?.getStringExtra("day_or_night")
        val hour = intent?.getIntExtra("hour(24)", 0)
        val minutes = intent?.getIntExtra("minutes", 0)

//        Log.d("ttest(AlarmReceiver)", "$dayOrNight, $hour, $minutes")

        val alarmService = Intent(context, AlarmService::class.java).apply {
            putExtra("day_or_night", dayOrNight)
            putExtra("hour(24)", hour)
            putExtra("minutes", minutes)
        }

        context?.startForegroundService(alarmService)
    }
}

