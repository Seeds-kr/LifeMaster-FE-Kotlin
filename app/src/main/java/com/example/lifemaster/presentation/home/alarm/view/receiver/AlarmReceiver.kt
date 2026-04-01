package com.example.lifemaster.presentation.home.alarm.view.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.lifemaster.presentation.home.alarm.model.AlarmModel
import com.example.lifemaster.presentation.home.alarm.view.service.AlarmService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val alarmData = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("ALARM_DATA", AlarmModel::class.java)
        } else {
            intent.getParcelableExtra<AlarmModel>("ALARM_DATA")
        }

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("ALARM_DATA", alarmData)
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}

