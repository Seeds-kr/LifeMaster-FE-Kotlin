package com.example.lifemaster.model

data class PomodoroItem(
    val id: Int = 0,
    val taskName: String = "",
    val focusTime: Int = 0, // 분 단위
    val breakTime: Int = 0, // 분 단위
    val cycles: Int = 0, // 이해 못함
    val date: String = "",
    val currentTimer: Int = 0 // 이해 못함
)