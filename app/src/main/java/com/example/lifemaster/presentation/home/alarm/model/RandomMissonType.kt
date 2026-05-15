package com.example.lifemaster.presentation.home.alarm.model

import com.google.gson.annotations.SerializedName

enum class RandomMissionType {
    @SerializedName("MATH_PROBLEM")
    MATH_PROBLEM,
    @SerializedName("TYPING_SENTENCE")
    TYPING_SENTENCE,
    @SerializedName("FOLLOW_CLICK")
    FOLLOW_CLICK
}