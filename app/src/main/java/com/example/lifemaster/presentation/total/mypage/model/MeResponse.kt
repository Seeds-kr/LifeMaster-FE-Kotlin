package com.example.lifemaster.presentation.total.mypage.model

import com.google.gson.annotations.SerializedName

data class MeResponse(
    @SerializedName("user")
    val user: UserData? = null,
    // Top-level fallbacks
    val id: Long = 0,
    @SerializedName(value = "nickName", alternate = ["nickname", "nick_name", "userName", "user_name", "name", "display_name"])
    val nickName: String? = null,
    val email: String? = null,
    @SerializedName(value = "profileImageUrl", alternate = ["profile_image", "avatar"])
    val profileImageUrl: String? = null,
    @SerializedName(value = "subscriptionPlan", alternate = ["plan", "subscription", "membership", "grade", "user_grade", "role"])
    val subscriptionPlan: String? = null,
    @SerializedName(value = "subscriptionDescription", alternate = ["subscriptionExpirationDate", "subscription_desc", "plan_description"])
    val subscriptionDescription: String? = null,
    @SerializedName(value = "expirationDate", alternate = ["expired_at", "expire_date"])
    val expirationDate: String? = null,
    @SerializedName(value = "paymentStatus", alternate = ["payment_status", "is_paid", "paid"])
    val paymentStatus: String? = null
)

data class UserData(
    val id: Long,
    @SerializedName(value = "nickName", alternate = ["nickname", "nick_name", "userName", "user_name", "name", "display_name"])
    val nickName: String? = null,
    val email: String? = null,
    @SerializedName(value = "imageUrl", alternate = ["profileImageUrl", "profile_image", "image_url", "avatar"])
    val profileImageUrl: String? = null,
    @SerializedName(value = "subscriptionPlan", alternate = ["plan", "subscription", "membership", "grade", "user_grade", "role"])
    val subscriptionPlan: String? = null,
    @SerializedName(value = "subscriptionExpirationDate", alternate = ["subscription_desc", "plan_description"])
    val subscriptionDescription: String? = null,
    @SerializedName(value = "expirationDate", alternate = ["expired_at", "expire_date"])
    val expirationDate: String? = null,
    @SerializedName(value = "paymentStatus", alternate = ["payment_status", "is_paid", "paid"])
    val paymentStatus: String? = null
)
