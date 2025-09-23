package com.example.lifemaster.presentation.home.alarm.view.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.lifemaster.R
import com.example.lifemaster.presentation.MainActivity

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    companion object {
        const val NOTIFICATION_ID = 100
        const val NOTIFICATION_CHANNEL_ID = "notification_channel_id"
        const val NOTIFICATION_CHANNEL_NAME = "notification_channel_name"
    }

    /**
     * 1) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
     * 2) requestCode를 다르게 줘서 여러 PendingIntent를 구분하는 방법 도 고려하기
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel(this)
        notifyNotification()
        playMediaPlayer()
        return START_STICKY
    }

    private fun createNotificationChannel(context: Context) {
        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH // 무조건 High 어야 기능 제약 없이 동작함
        ).apply {
            setSound(null, null) // 알람 소리로 따로 제어
        }
        NotificationManagerCompat.from(context).createNotificationChannel(notificationChannel)
    }

    private fun notifyNotification() {

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("destination", "alarm")
            putExtra("time", System.currentTimeMillis())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP) // 주석 1
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).apply {
            setContentTitle("띠리리링~🎶🎶")
            setContentText("알람이 울려요! 얼른 일어나세요!")
            setSmallIcon(R.drawable.ic_alarm_alert)
            setCategory(NotificationCompat.CATEGORY_ALARM)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            priority = NotificationCompat.PRIORITY_MAX
            setContentIntent(pendingIntent)
            setFullScreenIntent(pendingIntent, true)
            setAutoCancel(true)
        }.build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun playMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            isLooping = true
            setOnPreparedListener {
                it.start()
            }
            setDataSource(
                this@AlarmService,
                RingtoneManager.getActualDefaultRingtoneUri(
                    this@AlarmService,
                    RingtoneManager.TYPE_ALARM
                )
            )
            prepareAsync()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onBind(intent: Intent): IBinder? = null
}