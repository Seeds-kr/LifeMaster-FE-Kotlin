package com.example.lifemaster.presentation.total.mypage.model

import com.google.gson.annotations.SerializedName

/**
 * PayPal "create-order" 응답 모델.
 *
 * 서버에서 내려주는 JSON 키에 맞춰 매핑합니다.
 */
data class PayPalCreateOrderResponse(
    @SerializedName("orderId")
    val orderId: String,

    @SerializedName("approveUrl")
    val approveUrl: String,
)

