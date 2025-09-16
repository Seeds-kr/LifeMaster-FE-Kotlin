package com.example.lifemaster.presentation.total.challenge.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: Long,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("imageUrl") val imageUrl: String? // null일 수도 있으므로 Nullable '?'
)