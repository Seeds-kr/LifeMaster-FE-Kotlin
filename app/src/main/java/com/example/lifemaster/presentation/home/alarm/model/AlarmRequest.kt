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
    val alarmStatus: Boolean,
    @SerializedName("alarmSound")
    val alarmSoundUri: String? = null,
    val snoozed: Boolean,
    val snoozeTime: Int? = null, // 분 단위. 서버 수정 예정.
    val snoozeCount: Int? = null,
    @SerializedName("reSleptPrevention")
    val antiSnoozed: Boolean,
    @SerializedName("reSleptPreventionTime")
    val antiSnoozeTime: Int? = null, // 분 단위. 서버 수정 예정.
    val randomMissionType: RandomMissionType? = null,
    @SerializedName("missionLevel")
    val randomMissionLevel: RandomMissionLevel? = null
)
