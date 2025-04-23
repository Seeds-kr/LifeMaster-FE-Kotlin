package com.example.lifemaster.presentation.group.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.lifemaster.presentation.group.view.activity.AlarmRingsActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val intent = Intent(context, AlarmRingsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context?.startActivity(intent)
    }
}

