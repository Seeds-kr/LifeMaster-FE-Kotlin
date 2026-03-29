package com.example.lifemaster.presentation.home.alarm.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.FOLLOW_CLICK
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.LEVEL_HIGH
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.LEVEL_LOW
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.LEVEL_MEDIUM
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.MATH_PROBLEM
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.TYPING_SENTENCE
import com.example.lifemaster.presentation.home.alarm.model.AlarmModel
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionLevel
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionType
import com.example.lifemaster.presentation.home.alarm.view.receiver.AlarmReceiver
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * 알람이 오늘 또는 내일 울리는 경우 그에 맞는 텍스트를 표기하는 메소드
 */
fun formatAlarmDateLabel(referenceTime: String): String {
    var alarmDateLabel = ""
    when (referenceTime) {
        "TODAY" -> {
            val today = LocalDateTime.now()
            alarmDateLabel = "오늘 - ${today.monthValue}월 ${today.dayOfMonth}일 (${
                today.dayOfWeek.getDisplayName(
                    TextStyle.SHORT, Locale.KOREAN
                )
            })"
        }

        "TOMORROW" -> {
            val tomorrow = LocalDateTime.now().plusDays(1)
            alarmDateLabel = "내일 - ${tomorrow.monthValue}월 ${tomorrow.dayOfMonth}일 (${
                tomorrow.dayOfWeek.getDisplayName(
                    TextStyle.SHORT, Locale.KOREAN
                )
            })"
        }
    }
    return alarmDateLabel
}

/**
 * 현재 시점과 알람이 울리기 전까지 며칠 남았는 지 값을 반환하는 메소드
 */
fun getRemainingDaysUntilAlarmRings(alarmTime: LocalTime, selectedDays: MutableSet<Int>): Int {

    val now = LocalDateTime.now()
    val todayValue = now.dayOfWeek.value // 월(1) ~ 일(7)
    val currentTime = now.toLocalTime()

    val sortedDays = selectedDays.sorted()

    for (dayValue in sortedDays) {
        if (dayValue > todayValue) {
            return dayValue - todayValue
        } else if (dayValue == todayValue) {
            if (alarmTime.isAfter(currentTime)) {
                return 0
            }
        }
    }

    // 만약 리스트의 모든 요일이 오늘보다 이전이거나, 오늘인데 시간이 이미 지났다면
    // 리스트의 첫 번째 요일(가장 작은 값) = 다음 주에 돌아오는 가장 빠른 날
    val nextWeekDay = sortedDays.first()
    return nextWeekDay + 7 - todayValue
}

/**
 * 두 시간의 차이를 분석하여 언제 울리는 지 텍스트를 생성하는 메소드 ex. "4일 5시간 12분 뒤에 울려요"
 */
fun formatRemainingTime(alarmTime: String): String {

    val start = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
    val end = Instant.parse(alarmTime).atZone(ZoneId.systemDefault()).toLocalDateTime()

    val duration = Duration.between(start, end)
    val days = duration.toDays()
    val hours = duration.toHours() % 24
    val minutes = duration.toMinutes() % 60

    val parts = mutableListOf<String>()
    if (days > 0) parts.add("${days}일")
    if (hours > 0) parts.add("${hours}시간")
    if (minutes > 0 || (days == 0L && hours == 0L)) parts.add("${minutes}분")

    return "${parts.joinToString(" ")} 뒤에 알람이 울려요"
}

/**
 * 1. 각각의 알람을 식별하는 고유 ID
 * 2-1. FLAG_IMMUTABLE: 해당 PendingIntent를 받은 외부 앱이나 시스템이 Intent 내부 내용을 변경할 수 없다.
 * 2-2. FLAG_UPDATE_CURRENT: 동일한 고유 ID(alarmId)로 이미 생성된 PendingIntent가 있다면, 새로 만든 Intent의 데이터로 내용을 업데이트하라. (알람 시간 수정 시 사용)
 * 2-3. or: 두 가지 속성을 동시에 적용하겠다는 비트 연산자
 * 3. AlarmManager은 알람이 울릴 시간을 "1970년 1월 1일 0시부터 현재까지 흐른 밀리초(ms)" 단위로 받는다.
 * alarmTimeIso는 "2024-05-20T07:30:00Z" 와 같이 ISO-8601 표준 형식의 문자열이다.
 * 이 표준 시간 문자열을 시간 객체(Instant)로 변환한 뒤 AlarmManager가 사용하는 '밀리초'로 변환하는 과정이다.
 * 4. Android 12 이상에서는 정확한 알람 권한 확인이 필요하다. 권한이 없는 경우 일반 알람으로 설정하거나 권한 요청이 필요하다.
 * 5. 알람의 기준 시계와 작동 방식을 결정함
 * RTC: Real Time Clock. 실제 세상의 시각을 기준으로 한다. (반대 개념인 ELAPSED_REALTIME은 핸드폰이 켜진 후 흐른 시간이다.)
 * WAKEUP: 핸드폰 배터리 절약을 위해 절전 모드(Doze Mode)에 빠져 있더라도, 알람이 울릴 시간이 되면 CPU를 깨워서 알람 수신기(BroadcastReceiver)을 실행하라는 뜻이다.
 * 알람 앱이면 반드시 이 타입을 써야 제시간에 울린다.
 */
fun scheduleAlarm(context: Context, alarm: AlarmModel) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("ALARM_DATA", alarm)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        alarm.id, // 1
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // 2
    )

    val triggerTime = Instant.parse(alarm.alarmTime).toEpochMilli() // 3

    // 4
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if(alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, // 5
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    } else {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}

val randomMissionTypeMapper = mapOf(
    RandomMissionType.MATH_PROBLEM to MATH_PROBLEM,
    RandomMissionType.FOLLOW_CLICK to FOLLOW_CLICK,
    RandomMissionType.TYPING_SENTENCE to TYPING_SENTENCE
)
val randomMissionLevelMapper = mapOf(
    RandomMissionLevel.HIGH to LEVEL_HIGH,
    RandomMissionLevel.MEDIUM to LEVEL_MEDIUM,
    RandomMissionLevel.LOW to LEVEL_LOW
)
