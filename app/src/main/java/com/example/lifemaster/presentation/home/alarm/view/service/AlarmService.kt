package com.example.lifemaster.presentation.home.alarm.view.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.lifemaster.R
import com.example.lifemaster.presentation.MainActivity

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    /**
     * 1) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
     * 2) requestCode를 다르게 줘서 여러 PendingIntent를 구분하는 방법 도 고려하기
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 알림 채널 생성
        createNotificationChannel()
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("destination", "alarm")
            putExtra("time", System.currentTimeMillis())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP) // 주석 1
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, // 주석 2
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // TODO: 알람이 울리는 시점에 알림만 떠야할까 아니면 앱을 실행시켜야할까? (현재: 알림만 뜨는 상황)
        val notification = NotificationCompat.Builder(this, "notification_channel_id").apply {
            setContentTitle("띠리리링~🎶🎶")
            setContentText("알람이 울려요! 얼른 일어나세요!")
            setSmallIcon(R.drawable.ic_alarm_alert)
            setCategory(NotificationCompat.CATEGORY_ALARM) // ?
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // ?
            priority = NotificationCompat.PRIORITY_MAX // ?
            setContentIntent(pendingIntent)
            setFullScreenIntent(pendingIntent, true) // ?
            setAutoCancel(true) // ?
        }.build()

        startForeground(System.currentTimeMillis().toInt(), notification) // ?

        /**
         * MediaPlayer를 알림음에 넣지 않고 따로 분리한 이유 → 알림을 클릭하고나서도 계속 울려야하기 때문
         */
        mediaPlayer = MediaPlayer().apply {
            isLooping = true
            setOnPreparedListener { // ?
                it.start()
            }
            setDataSource(
                this@AlarmService,
                RingtoneManager.getActualDefaultRingtoneUri(this@AlarmService, RingtoneManager.TYPE_ALARM) // 나중에 사용자가 소리 설정할 수 있을 때 바꾸기
            )
            prepareAsync() // 비동기 준비
        }

        return START_STICKY // ??
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "notification_channel_id",
            "notification_channel_name",
            NotificationManager.IMPORTANCE_HIGH // 무조건 High 어야 기능 제약 없이 동작함
        ).apply {
            description = "notification_channel_description" // 꼭 설정해야하나?
            setSound(null, null) // 알람 소리로 따로 제어
        }
        notificationManager.createNotificationChannel(channel)
    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onBind(intent: Intent): IBinder? = null
}