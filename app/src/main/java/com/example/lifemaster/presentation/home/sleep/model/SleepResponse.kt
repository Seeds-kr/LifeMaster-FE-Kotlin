package com.example.lifemaster.presentation.home.sleep.model

data class SleepResponse(
    val sleepId: Int,
    val sleepDate: String,
    val sleepStart: String,
    val sleepEnd: String,
    val sleepMood: String,
    val sleepAwakeCnt: Int,
    val sleepScore: Int
)
