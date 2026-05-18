package com.example.lifemaster.presentation.total.mypage.model

import com.google.gson.annotations.SerializedName

data class MeResponse(
    @SerializedName("user")
    val user: UserData? = null,
    // Top-level fallbacks
    val id: Long = 0,
    @SerializedName(value = "nickName", alternate = ["nickname", "nick_name", "userName", "user_name", "name"])
    val nickName: String? = null,
    val email: String? = null,
    val profileImageUrl: String? = null,
    @SerializedName(value = "subscriptionPlan", alternate = ["plan", "subscription", "membership"])
    val subscriptionPlan: String? = null,
    @SerializedName(value = "subscriptionDescription", alternate = ["subscriptionExpirationDate", "expirationDate"])
    val subscriptionDescription: String? = null,
    val expirationDate: String? = null,
    val paymentStatus: String? = null
)

data class UserData(
    val id: Long,
    @SerializedName(value = "nickName", alternate = ["nickname", "nick_name", "userName", "user_name", "name"])
    val nickName: String? = null,
    val email: String? = null,
    @SerializedName(value = "imageUrl", alternate = ["profileImageUrl", "profileImage", "image_url"])
    val profileImageUrl: String? = null,
    @SerializedName(value = "subscriptionPlan", alternate = ["plan", "subscription", "membership"])
    val subscriptionPlan: String? = null,
    @SerializedName(value = "subscriptionExpirationDate", alternate = ["expirationDate"])
    val subscriptionDescription: String? = null,
    val expirationDate: String? = null,
    val paymentStatus: String? = null
)
