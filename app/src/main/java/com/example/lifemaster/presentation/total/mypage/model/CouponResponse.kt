package com.example.lifemaster.presentation.total.mypage.model

import com.google.gson.annotations.SerializedName

import java.io.Serializable

data class CouponResponse(
    val couponId: Long,
    val couponCode: String,
    val couponPercent: Int,
    val couponType: String,
    val couponStatus: String,
    val createdAt: String?,
    val updatedAt: String?,
    @SerializedName("user")
    val user: UserData? = null
) : Serializable
