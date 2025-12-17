package com.example.lifemaster.presentation.home.alarm.model.mapper

import com.example.lifemaster.presentation.home.alarm.model.AlarmModel
import com.example.lifemaster.presentation.home.alarm.model.AlarmResponse

fun AlarmResponse.toPresentation(): AlarmModel = AlarmModel(
    id = id,
    alarmTitle = alarmTitle,
    alarmTime = alarmTime,
    alarmMon = alarmMon,
    alarmTue = alarmTue,
    alarmWed = alarmWed,
    alarmThu = alarmThu,
    alarmFri = alarmFri,
    alarmSat = alarmSat,
    alarmSun = alarmSun,
    alarmSoundUri = alarmSoundUri,
    snoozed = snoozed,
    snoozeMinute = snoozeMinute,
    snoozeCount = snoozeCount,
    antiSnoozed = antiSnoozed,
    antiSnoozeMinute = antiSnoozeMinute,
    randomMissionType = randomMissionType,
    randomMissionLevel = randomMissionLevel,
    switchOnOff = alarmStatus
)