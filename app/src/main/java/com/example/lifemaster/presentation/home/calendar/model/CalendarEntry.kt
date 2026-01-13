package com.example.lifemaster.presentation.home.calendar.model

data class CalendarEntry(
    val id: Long,
    val date: String,
    val day: String?,
    val events: List<String> = emptyList()
)