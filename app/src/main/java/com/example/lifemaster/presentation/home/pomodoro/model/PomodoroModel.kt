package com.example.lifemaster.presentation.home.pomodoro.model

data class PomodoroModel(
    val id: Int,
    val taskName: String,
    val focusTime: Int,
    val breakTime: Int,
    val currentTimer: Int,
    val date: String
)

