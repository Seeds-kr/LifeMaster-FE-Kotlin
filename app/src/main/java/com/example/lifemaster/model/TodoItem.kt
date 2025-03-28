package com.example.lifemaster.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TodoItem(
    val id: Int = 0,
    val date: String = "",
    val title: String = "",
    @SerializedName("completed") var isCompleted: Boolean = false,
    var timer: PomodoroTimer,
    val calendar: CalendarItem
) : Parcelable

@Parcelize
data class CalendarItem(
    val id: Int = 0,
    val date: String = "",
    val day: String = "",
    val events: List<String>
) : Parcelable

// enum class 작성하는 위치?
enum class PomodoroTimer {
    NONE,
    TIMER_25,
    TIMER_50
}
