package com.example.lifemaster.presentation.home.pomodoro.model

data class PomodoroResponse(
    val id: Int,
    val taskName: String,
    val focusTime: Int,
    val breakTime: Int,
    val currentTimer: Int,
    val date: String
)

fun PomodoroResponse.toPresentation(): PomodoroModel = PomodoroModel(
    id = id,
    taskName = taskName,
    focusTime = focusTime,
    breakTime = breakTime,
    currentTimer = currentTimer,
    date = date
)

