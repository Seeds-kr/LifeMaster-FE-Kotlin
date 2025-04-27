package com.example.lifemaster.presentation.group.view.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.lifemaster.R
import com.example.lifemaster.presentation.group.view.activity.AlarmRingsActivity

class AlarmService : Service() {

    private var player: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val dayOrNight = intent?.getStringExtra("day_or_night")
        val hour = intent?.getIntExtra("hour(24)", 0)
        val minutes = intent?.getIntExtra("minutes", 0)

        Log.d("ttest(AlarmService)", "$dayOrNight, $hour, $minutes")

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "alarm_channel_id"
        val channel = NotificationChannel(channelId, "Alarm Channel", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
        val intent = Intent(this, AlarmRingsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("day_or_night", dayOrNight)
            putExtra("hour(24)", hour)
            putExtra("minutes", minutes)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE) // requestCode를 다르게 줘서 여러 PendingIntent를 구분하는 방법 도 고려하기
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_alarm_alert)
            .setContentTitle("알람이 울려요!")
            .setContentText("알람이 울러요! 눌러서 확인해보세요!")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1001, notification)

        val soundUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
        player = MediaPlayer.create(this, soundUri).apply {
            isLooping = true
            start()
        }

        return START_STICKY // 작동 안하면 return START_REDELIVER_INTENT 로 바꿔보기
    }


    override fun onDestroy() {
        super.onDestroy()
        player?.stop()
        player?.release()
        player = null
    }

    override fun onBind(intent: Intent): IBinder? = null
}