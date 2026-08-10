package com.example.lifemaster.presentation.total.mypage.model

import com.google.gson.annotations.SerializedName

/**
 * Google Play 구독 영수증 검증 요청 모델 (POST /payments/googlePay/verify).
 */
data class GooglePayVerifyRequest(
    @SerializedName("subscriptionId")
    val subscriptionId: String,

    @SerializedName("purchaseToken")
    val purchaseToken: String,

    @SerializedName("productName")
    val productName: String,

    @SerializedName("amount")
    val amount: Int,

    @SerializedName("currency")
    val currency: String,
)
