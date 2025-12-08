package com.example.lifemaster.presentation.home.alarm.model

import java.time.Instant
import java.time.ZoneId

// UI, Presentation Layer
data class AlarmModel(
    val id: Int,
    val alarmTitle: String,
    val alarmTime: String,
    val alarmMon: Boolean,
    val alarmTue: Boolean,
    val alarmWed: Boolean,
    val alarmThu: Boolean,
    val alarmFri: Boolean,
    val alarmSat: Boolean,
    val alarmSun: Boolean,
    val alarmSound: String,
    val snoozed: Boolean,
    val snoozeMinute: Int? = null,
    val snoozeCount: Int? = null,
    val antiSnoozed: Boolean,
    val antiSnoozeMinute: Int? = null,
    val randomMissionType: RandomMissionType,
    val randomMissionLevel: RandomMissionLevel? = null,
    var switchOnOff: Boolean = true, // 처음 알람 등록하면 켜지도록 설정 (기본값)
) {
    val hour: Int
        get() {
            val hour = Instant.parse(alarmTime).atZone(ZoneId.systemDefault()).hour
            return hour
        }

    val minute: Int
        get() {
            val minute = Instant.parse(alarmTime).atZone(ZoneId.systemDefault()).minute
            return minute
        }

    val timeText: String
        get() {
            val h = "%02d".format(
                if (hour < 12) hour else hour - 12
            )
            val m = "%02d".format(minute)
            return "$h:$m"
        }

    val ampm: String
        get() {
            return if (hour < 12) "AM" else "PM"
        }

}
