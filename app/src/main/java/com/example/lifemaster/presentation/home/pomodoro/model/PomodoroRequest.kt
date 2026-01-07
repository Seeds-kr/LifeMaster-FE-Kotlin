package com.example.lifemaster.presentation.home.pomodoro.model

data class PomodoroRequest(
    val todoId: Int,
    val taskName: String,
    val focusTime: Int,
    val breakTime: Int,
    val currentTimer: Int,
    val date: String,
)