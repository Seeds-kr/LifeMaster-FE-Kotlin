package com.example.lifemaster.presentation.home.alarm.view.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.lifemaster.R
import com.example.lifemaster.presentation.home.alarm.model.AlarmModel
import com.example.lifemaster.presentation.home.alarm.view.AlarmDisplayActivity

/**
 * 1.
 * onStartCommand에서 반환되는 값은 "서비스가 시스템에 의해 강제종료 되었을 때, 시스템이 서비스를 어떻게 재시작할 것인가"를 결정한다.
 * START_STICKY는 시스템이 메모리 부족 등의 이유로 서비스를 강제 종료하면, 메모리 여유가 생겼을 때 시스템이 서비스를 자동으로 다시 생성하고 onStartCommand를 호출한다.
 * 이때(재생성) 전달되는 Intent는 null이 된다.
 * START_NOT_STICKY는 강제 종료되어도 서비스가 자동으로 재시작되지 않는다.
 */
class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmItem = intent?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelableExtra("ALARM_DATA", AlarmModel::class.java)
            } else {
                it.getParcelableExtra<AlarmModel>("ALARM_DATA")
            }
        }

        if (alarmItem != null) {
            showForegroundNotification(alarmItem)
            playAlarmSound(alarmItem.alarmSoundUri)
            return START_STICKY
        } else {
            // Context.startForegroundService() 호출 후 5초 이내에 startForeground()를 호출하지 않으면
            // ForegroundServiceDidNotStartInTimeException 발생. 
            // intent가 null이거나 alarmItem이 null인 경우에도 fallback 알림을 띄워 서비스를 유지하거나 종료해야 함.
            showFallbackForegroundNotification()
            stopSelf()
            return START_NOT_STICKY
        }
    }

    /**
     * 1. 모든 알림이 특정 채널에 할당되어 있어야 한다. NotificationChannel(id, name, importance)
     * id: 채널을 식별하는 고유한 문자열. 앱 내에서 중복되면 안 되며, 나중에 NotificationCompat.Builder에서 이 알림이 어떤 채널에 속할지 지정할 때 사용함
     * 채널은 알림의 '카테고리' 개념이다. "알람"이라는 카테고리의 알림은 모두 같은 소리 설정, 같은 중요도를 가지기에 하나의 채널만 만들어두고 모든 알람 알림이 이 채널을 타게 만드는게 일반적이다.
     * name: 사용자에게 실제로 보이는 채널 이름. 시스템 설정의 알림 관리 화면에서 사용자가 이 이름을 보고 채널별로 알림을 켤지 끌지 결정함.
     * importance: 이 채널의 중요도 수준. IMPORTANCE_HIGH 는 소리가 나고 헤드업 알림으로 표시된다
     * 2. 알림 자체 소리는 끄고 MediaPlayer 사용
     * 3. 새로운 화면을 어떻게 띄우고, 기존에 열려있던 화면들은 어떻게 정리할 것인가를 결정
     * FLAG_ACTIVITY_NEW_TASK(서비스에서의 실행 보장): 새로운 태스크를 생성하여 그 위에서 액티비티를 실행하라
     * 화면이 없는 곳(Service)에서 액티비티를 실행할 때, "어떤 화면 흐름(Task) 위에 해당 액티비티를 두어야 할지" 알 수 없다.
     * 이 플래그를 주지 않으면 서비스에서 startActivity 호출 시 에러가 발생한다. 이 설정을 통해 독립적인 새로운 작업 흐름으로 알람 화면을 띄울 수 있다.
     * FLAG_ACTIVITY_CLEAR_TOP(화면 집중): 실행하려는 액티비티가 이미 메모리에 올라와 있다면, 그 액티비티 위에 쌓여 있는 다른 모든 액티비티를 모두 종료하라.
     * 사용자가 앱을 사용 중이었거나, 이전 알람이 아직 꺼지지 않은 상태에서 새로운 알람이 울리는 상황에서 기존의 화면들을 다 지우고 지금 울리는 알람 화면을 가장 최상단으로 띄운다.
     * 4. requestCode: PendingIntent를 식별하는 고유한 숫자이다.
     * alarmId를 넣어줌으로써 각 알람마다 고유한 대기 인텐트를 가지게 하여, 여러 알람이 겹치더라도 각각 올바른 데이터를 유지하도록 한다.
     * 5. FLAG_UPDATE_CURRENT
     * 이미 동일한 requestCode로 생성된 PendingIntent가 존재한다면, 새로 만든 Intent의 Extra 데이터(alarmItem)만 최신으로 교체하라
     * 알람 시간을 수정하거나, 같은 ID의 알람이 다시 울릴 때 최신 정보를 정확하게 전달하기 위해 필수적인 플래그
     * FLAG_IMMUTABLE: 해당 PendingIntent를 전달받은 시스템이나 다른 앱이 Intent 내용을 수정할 수 없게 하겠다.
     * 6. 잠금화면 위로 즉시 실행한다.
     * 7. 알람 id
     * 고유한 id를 전달하면 알람마다 다른 알림이 생성된다. (= 알람마다 별개로 알림 바가 쌓인다)
     * 매번 동일한 id를 전달하면 기존 알림이 새로운 알람 내용으로 덮어씌워져서 알림바에 알림이 1개만 유지된다.
     * 참고로, 안드로이드 시스템은 0을 유효한 알림 ID로 보지 않아 에러가 날 수 있따.
     */
    private fun showForegroundNotification(alarmItem: AlarmModel) {

        // 1. 알림 채널 만들기
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel( // 1
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null) // 2
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(this, AlarmDisplayActivity::class.java).apply {
            putExtra("ALARM_DATA", alarmItem)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP // 3
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmItem.id, // 4
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // 5
        )

        // 2. 알림 생성 이후 포그라운드 알림 실행
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_alert)
            .setContentTitle(alarmItem.alarmTitle)
            .setContentText("알람이 울리고 있어요!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true) // 6
            .build()

        val notificationId = if (alarmItem.id == 0) NOTIFICATION_ID_FALLBACK else alarmItem.id
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(notificationId, notification) // 7
        }
    }

    private fun showFallbackForegroundNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_alert)
            .setContentTitle("알람 서비스")
            .setContentText("알람을 준비 중입니다.")
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID_FALLBACK, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID_FALLBACK, notification)
        }
    }

    /**
     * 미디어 플레이어로 알람 소리 재생하는 메소드
     * 1. 사용자가 소리를 지정하지 않은 경우 무음으로 처리
     * 2. 알람 용도라 명시 → 시스템의 알람 볼륨 조절기에 반응하게 함
     */
    private fun playAlarmSound(soundUriString: String?) {

        if(soundUriString == null) return // 1

        try {
            val uri = Uri.parse(soundUriString)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmService, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM) // 2
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    companion object {
        const val CHANNEL_ID = "ALARM_SERVICE_CHANNEL_ID"
        const val CHANNEL_NAME = "ALARM_SERVICE_CHANNEL_NAME"
        private const val NOTIFICATION_ID_FALLBACK = 1001
    }

    override fun onBind(intent: Intent?) = null
}
