package com.example.lifemaster.presentation.home.calendar.model

data class CalendarDay(
    val day: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean = false,
    val features: List<StarType> = emptyList()
)

// 추후에 기능별로 색깔 적용
enum class StarType(val colorHex: String) {
    ALARM("#E95A5A"),
    SLEEP("#333333"),
    DETOX("#B4D7D5"),
    INTROSPECTION("#FFB943"),
    CHALLENGE("#B4C4E2"),
    ETC("#9C9C9C")
}