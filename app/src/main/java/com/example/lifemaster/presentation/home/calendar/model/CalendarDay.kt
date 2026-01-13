package com.example.lifemaster.presentation.home.calendar.model

data class CalendarDay(
    val day: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean = false,
    val features: List<StarType> = emptyList()
)

enum class StarType(val colorHex: String) {
    TODO("#5C76C3"),
    ALARM("#BBAB94"),
    SLEEP("#333333"),
    DETOX("#8DB83F"),
    INTROSPECTION("#FFA500"),
    CHALLENGE("#84CAE2")
}