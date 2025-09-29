package com.example.lifemaster.presentation.home.todo.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TodoModel(
    val id: Int = 0,
    val date: String = "",
    val title: String = "",
    @SerializedName("completed") val isCompleted: Boolean = false,
    val calendar: CalendarModel = CalendarModel(),
) : Parcelable

@Parcelize
data class CalendarModel(
    val id: Int = 0,
    val date: String = "",
    val day: String = "",
    val events: List<String> = emptyList()
) : Parcelable
