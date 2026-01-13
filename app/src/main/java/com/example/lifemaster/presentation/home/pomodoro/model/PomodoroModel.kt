package com.example.lifemaster.presentation.home.pomodoro.model

import com.example.lifemaster.presentation.home.todo.model.TodoModel

data class PomodoroModel(
    val id: Int,
    val taskName: String,
    val focusTime: Int,
    val breakTime: Int,
    val currentTimer: Int,
    val date: String,
    val todo: TodoModel
)

