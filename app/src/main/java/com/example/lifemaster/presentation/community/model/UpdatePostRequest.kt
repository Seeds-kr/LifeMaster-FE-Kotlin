package com.example.lifemaster.presentation.community.model

data class UpdatePostRequest(
    val title: String,
    val content: String,
    val file: String?,
    val type: String = "FREE",
    val calendarShared: Boolean = false
)