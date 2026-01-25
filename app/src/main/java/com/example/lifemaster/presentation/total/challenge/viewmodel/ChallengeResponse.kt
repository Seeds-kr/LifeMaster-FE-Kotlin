package com.example.lifemaster.presentation.total.challenge.viewmodel

import com.google.gson.annotations.SerializedName
import com.kakao.sdk.user.model.User

data class ChallengeResponse(
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("challd") val challd: Long,
    @SerializedName("challName") val challName: String,
    @SerializedName("challDesc") val challDesc: String,
    @SerializedName("challImg") val challImg: String,
    @SerializedName("user") val user: User,
    @SerializedName("challCnt") val challCnt: Int

)