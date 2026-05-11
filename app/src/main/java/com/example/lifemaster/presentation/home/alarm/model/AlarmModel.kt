package com.example.lifemaster.presentation.home.alarm.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.time.ZoneId

// UI, Presentation Layer
@Parcelize
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
    val alarmSoundUri: String? = null, // null = 무음
    val snoozed: Boolean,
    val snoozeMinute: Int? = null,
    val snoozeCount: Int? = null,
    val antiSnoozed: Boolean,
    val antiSnoozeMinute: Int? = null,
    val randomMissionType: RandomMissionType? = null,
    val randomMissionLevel: RandomMissionLevel? = null,
    val switchOnOff: Boolean = true, // 처음 알람 등록하면 켜지도록 설정 (기본값)
): Parcelable {
    val hour: Int
        get() {
            val time = if (alarmTime.endsWith("Z")) alarmTime else "${alarmTime}Z"
            return Instant.parse(time).atZone(ZoneId.systemDefault()).hour
        }

    val minute: Int
        get() {
            val time = if (alarmTime.endsWith("Z")) alarmTime else "${alarmTime}Z"
            return Instant.parse(time).atZone(ZoneId.systemDefault()).minute
        }

    val timeText: String
        get() {
            val h = if (hour == 0 || hour == 12) 12 else hour % 12
            return "%02d:%02d".format(h, minute)
        }

    val ampm: String
        get() {
            return if (hour < 12) "AM" else "PM"
        }

    val formattedAlarmTime: String
        get() {
            return if (alarmTime.endsWith("Z")) alarmTime else "${alarmTime}Z"
        }
}
