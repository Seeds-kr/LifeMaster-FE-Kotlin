package com.example.lifemaster.presentation.home.calendar.model

data class CalendarDay(
    val day: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean = false
)