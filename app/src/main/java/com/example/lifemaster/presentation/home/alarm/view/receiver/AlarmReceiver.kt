package com.example.lifemaster.presentation.home.alarm.view.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.lifemaster.presentation.home.alarm.model.AlarmModel
import com.example.lifemaster.presentation.home.alarm.repository.AlarmRepository
import com.example.lifemaster.presentation.home.alarm.model.mapper.toPresentation
import com.example.lifemaster.presentation.home.alarm.util.scheduleAlarm
import com.example.lifemaster.presentation.home.alarm.view.service.AlarmService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmRepository: AlarmRepository

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "onReceive action: ${intent.action}")

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAlarms(context)
            return
        }

        val alarmData = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("ALARM_DATA", AlarmModel::class.java)
        } else {
            intent.getParcelableExtra<AlarmModel>("ALARM_DATA")
        }

        if (alarmData == null) {
            Log.e("AlarmReceiver", "alarmData is null. Skipping service start.")
            return
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

    private fun rescheduleAlarms(context: Context) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = alarmRepository.fetchAlarmList()
                result.onSuccess { alarmList ->
                    alarmList.forEach { alarmResponse ->
                        if (alarmResponse.alarmStatus) {
                            val alarmModel = alarmResponse.toPresentation()
                            scheduleAlarm(context, alarmModel)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Failed to reschedule alarms", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

