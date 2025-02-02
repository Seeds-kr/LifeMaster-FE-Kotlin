package com.example.lifemaster.presentation.group.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.lifemaster.presentation.MainActivity

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("fragment type", "AlarmRingsFragment")
        }
        context?.startActivity(intent)
    }

}