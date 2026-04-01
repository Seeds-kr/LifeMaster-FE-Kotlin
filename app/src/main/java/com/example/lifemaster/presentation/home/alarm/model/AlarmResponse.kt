package com.example.lifemaster.presentation.home.alarm.model

import com.google.gson.annotations.SerializedName

data class AlarmResponse(
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
    val alarmStatus: Boolean,
    @SerializedName("alarmSound")
    val alarmSoundUri: String? = null,
    val snoozed: Boolean,
    @SerializedName("snoozeTime")
    val snoozeMinute: Int? = null,
    val snoozeCount: Int? = null,
    @SerializedName("reSleptPrevention")
    val antiSnoozed: Boolean,
    @SerializedName("reSleptPreventionTime")
    val antiSnoozeMinute: Int? = null,
    val randomMissionType: RandomMissionType? = null,
    @SerializedName("missionLevel")
    val randomMissionLevel: RandomMissionLevel? = null
)
