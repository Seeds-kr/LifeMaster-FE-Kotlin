package com.example.lifemaster.presentation.home.pomodoro.model

import com.example.lifemaster.presentation.home.todo.model.TodoResponse
import com.example.lifemaster.presentation.home.todo.model.toPresentation

data class PomodoroResponse(
    val id: Int,
    val taskName: String,
    val focusTime: Int,
    val breakTime: Int,
    val currentTimer: Int,
    val date: String,
    val todo: TodoResponse
)

fun PomodoroResponse.toPresentation(): PomodoroModel = PomodoroModel(
    id = id,
    taskName = taskName,
    focusTime = focusTime,
    breakTime = breakTime,
    currentTimer = currentTimer,
    date = date,
    todo = todo.toPresentation()
)

