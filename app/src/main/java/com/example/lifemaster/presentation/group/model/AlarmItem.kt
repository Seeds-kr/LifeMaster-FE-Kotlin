package com.example.lifemaster.presentation.group.model

data class AlarmItem(
    val id: Int,
    val title: String,
    val time: Triple<String, Int, Int>,
    val isDelaySet: Boolean,
    val delayMinute: Int = 0,
    val delayCount: Int = 0
)
