package com.example.lifemaster.presentation.home.alarm.model

import com.google.gson.annotations.SerializedName

// Remote Layer
data class AlarmResponse(
    val id: Int,
    val alarmTitle: String,
    val alarmTime: String, // ISO 8601 형식의 문자열 (예: "2025-12-08T07:00:05.595Z") Instant.toString()
    val alarmMon: Boolean,
    val alarmTue: Boolean,
    val alarmWed: Boolean,
    val alarmThu: Boolean,
    val alarmFri: Boolean,
    val alarmSat: Boolean,
    val alarmSun: Boolean,
    val alarmSound: String, // URI
    val snoozed: Boolean, // 알람 미루기
    @SerializedName("snoozeTime")
    val snoozeMinute: Int, // 몇분씩 미룰 것인가?
    val snoozeCount: Int,
    @SerializedName("reSlept")
    val antiSnoozed: Boolean, // 다시 잠들기 방지
    @SerializedName("reSleptTime")
    val antiSnoozeMinute: Int, // 알람이 꺼진 후 몇분 뒤에 울릴 것인가?
    val randomMissionType: RandomMissionType, // MATH_PROBLEM, TYPING, FOLLOW_CLICK 등
    @SerializedName("missionLevel")
    val randomMissionLevel: RandomMissionLevel, // HIGH, MEDIUM, LOW
)
