package com.example.lifemaster.presentation.home.alarm.model

import com.google.gson.annotations.SerializedName

data class AlarmRequest(
    val alarmTitle: String,
    val alarmTime: String,
    val alarmMon: Boolean,
    val alarmTue: Boolean,
    val alarmWed: Boolean,
    val alarmThu: Boolean,
    val alarmFri: Boolean,
    val alarmSat: Boolean,
    val alarmSun: Boolean,
    @SerializedName("alarmSound")
    val alarmSoundUri: String,
    val snoozed: Boolean,
    val snoozeTime: String? = null, // 분 단위. 서버 수정 예정. String -> Int
    val snoozeCount: Int? = null,
    @SerializedName("reSlept")
    val antiSnoozed: Boolean,
    @SerializedName("reSleptTime")
    val antiSnoozeTime: String? = null, // 분 단위. 서버 수정 예정. String -> Int
    val randomMissionType: RandomMissionType
)
