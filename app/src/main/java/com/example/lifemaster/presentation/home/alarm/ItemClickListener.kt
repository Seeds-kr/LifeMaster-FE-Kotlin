package com.example.lifemaster.presentation.home.alarm

interface ItemClickListener {
    fun onItemClick(alarmId: Int)
    fun onItemLongClick(alarmId: Int)
    fun onSwitchToggle(alarmId: Int, alarmStatus: Boolean)
}