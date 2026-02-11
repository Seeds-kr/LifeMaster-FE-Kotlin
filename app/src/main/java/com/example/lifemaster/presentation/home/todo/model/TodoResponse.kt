package com.example.lifemaster.presentation.home.todo.model

data class TodoResponse(
    val id: Int,
    val date: String,
    val title: String,
    val completed: Boolean
)
