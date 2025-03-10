package com.example.lifemaster.model

import com.google.gson.annotations.SerializedName

data class TodoItem(
    val id: Int = 0,
    val date: String = "",
    val title: String = "",
    @SerializedName("completed") val isCompleted: Boolean = false,
    val calendar: CalendarItem
)

data class CalendarItem(
    val id: Int = 0,
    val date: String = "",
    val day: String = "",
    val events: List<String>
)
