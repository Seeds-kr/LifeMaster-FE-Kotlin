package com.example.lifemaster.presentation.total.challenge.model

import com.google.gson.annotations.SerializedName

data class ChallengeCompleteRequest(
    @SerializedName("challId")
    val challId: Long
)

data class ChallengeCompleteResponse(
    @SerializedName("challId")
    val challId: Long,

    @SerializedName("completed")
    val completed: Boolean,

    @SerializedName("completedAt")
    val completedAt: String?
)