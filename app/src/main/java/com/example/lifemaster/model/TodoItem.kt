package com.example.lifemaster.model

import com.google.gson.annotations.SerializedName

data class TodoItem(
    val id: Int = 0,
    val date: String = "",
    val title: String = "",
    val description: String = "",
    @SerializedName("completed") val isCompleted: Boolean = false
)
