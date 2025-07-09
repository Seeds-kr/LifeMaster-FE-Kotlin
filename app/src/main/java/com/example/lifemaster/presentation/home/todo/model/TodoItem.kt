package com.example.lifemaster.presentation.home.todo.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TodoItem(
    val id: Int = 0,
    val date: String? = "",
    val title: String = "",
    @SerializedName("completed") var isCompleted: Boolean = false,
    val calendar: CalendarItem = CalendarItem(),
    var timer25Number: Int = 0,
    var timer50Number: Int = 0
) : Parcelable

@Parcelize
data class CalendarItem(
    val id: Int = 0,
    val date: String = ""
) : Parcelable
