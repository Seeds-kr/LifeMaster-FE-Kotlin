package com.example.lifemaster.presentation.total.mypage.model

import com.google.gson.annotations.SerializedName

data class PaymentHistoryResponse(
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName(value = "amount", alternate = ["paymentAmount", "price"])
    val amount: String? = null,
    @SerializedName(value = "description", alternate = ["paymentDesc", "productName", "item_name", "type"])
    val description: String? = null,
    @SerializedName(value = "paymentDate", alternate = ["at", "createdAt", "payment_date", "date"])
    val paymentDate: String? = null,
    @SerializedName(value = "status", alternate = ["payment_status"])
    val status: String? = null
)
