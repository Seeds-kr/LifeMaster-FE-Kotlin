package com.example.lifemaster.presentation.home.alarm

import com.example.lifemaster.presentation.home.alarm.model.AlarmModel

interface ItemClickListener {
    fun onItemClick(alarm: AlarmModel)
    fun onItemLongClick(alarmId: Int)
    fun onSwitchToggle(alarm: AlarmModel)
}