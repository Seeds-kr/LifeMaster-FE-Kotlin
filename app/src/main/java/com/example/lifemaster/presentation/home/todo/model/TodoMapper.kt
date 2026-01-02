package com.example.lifemaster.presentation.home.todo.model

fun TodoResponse.toPresentation(): TodoModel = TodoModel(
    id = id,
    date = date,
    title = title,
    isCompleted = completed
)