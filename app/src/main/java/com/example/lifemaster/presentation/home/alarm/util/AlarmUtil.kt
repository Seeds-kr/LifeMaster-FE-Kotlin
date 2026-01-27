package com.example.lifemaster.presentation.home.alarm.util

import com.example.lifemaster.presentation.home.alarm.AlarmConstants.FOLLOW_CLICK
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.LEVEL_HIGH
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.LEVEL_LOW
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.LEVEL_MEDIUM
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.MATH_PROBLEM
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.TYPING_SENTENCE
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionLevel
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionType
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.TextStyle
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
fun formatRemainingTime(start: LocalDateTime, end: LocalDateTime): String {
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
