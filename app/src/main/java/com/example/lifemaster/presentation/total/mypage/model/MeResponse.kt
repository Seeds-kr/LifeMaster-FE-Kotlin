package com.example.lifemaster.presentation.total.mypage.model

import com.google.gson.annotations.SerializedName

data class MeResponse(
    val id: Long,
    val nickName: String? = null,
    val email: String? = null,
    @SerializedName(value = "profileImageUrl", alternate = ["profileImage", "imageUrl", "avatarUrl", "profile_image_url"])
    val profileImageUrl: String? = null,
    @SerializedName(value = "subscriptionPlan", alternate = ["plan", "membershipType", "tier", "subscriptionTier"])
    val subscriptionPlan: String? = null,
    @SerializedName(
        value = "subscriptionDescription",
        alternate = ["subscriptionRenewalDescription", "nextRenewal", "subscriptionEndText", "premiumUntil"],
    )
    val subscriptionDescription: String? = null,
)
