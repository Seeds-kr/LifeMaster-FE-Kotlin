package com.example.lifemaster.presentation.home.sleep

data class SleepItem(
    val id: Int, // 곡 id
    val thumbnail: Int,
    val title: String,
    val duration: String,
    val description: String
)