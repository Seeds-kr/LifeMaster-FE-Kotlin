package com.example.lifemaster.presentation.home.todo

import android.os.Parcelable
import com.example.lifemaster.model.PomodoroTimerType
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TodoItem(
    val id: Int = 0,
    val date: String = "",
    val title: String = "",
    @SerializedName("completed") var isCompleted: Boolean = false,
    var timer: PomodoroTimerType = PomodoroTimerType.NONE
) : Parcelable

@Parcelize
data class CalendarItem(
    val id: Int = 0,
    val date: String = "",
    val day: String = "",
    val events: List<String>
) : Parcelable
