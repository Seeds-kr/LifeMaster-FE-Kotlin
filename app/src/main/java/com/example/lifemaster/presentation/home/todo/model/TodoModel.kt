package com.example.lifemaster.presentation.home.todo.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TodoModel(
    val id: Int,
    val date: String,
    val title: String,
    @SerializedName("completed") val isCompleted: Boolean,
) : Parcelable
