package com.example.lifemaster.presentation.home.alarm

import com.example.lifemaster.presentation.home.alarm.model.AlarmModel

interface ItemClickListener {
    fun onItemClick(alarm: AlarmModel)
    fun onItemLongClick()
    fun onSwitchToggle(alarm: AlarmModel)
}