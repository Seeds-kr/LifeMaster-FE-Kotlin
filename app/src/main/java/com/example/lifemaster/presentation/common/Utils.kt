package com.example.lifemaster.presentation.common

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.example.lifemaster.presentation.group.receiver.AlarmReceiver

@RequiresApi(Build.VERSION_CODES.S)
fun setAlarm(context: Context, triggerTimeMillis: Long) {
    val intent = Intent(context, AlarmReceiver::class.java) // broadcast receiver 를 실행하는 intent
    val pendingIntent = PendingIntent.getBroadcast( // 일정 시간 뒤(pending)에 broadcast receiver 를 실행하는 intent
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if(alarmManager.canScheduleExactAlarms()) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
    } else {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
}
