package com.example.lifemaster.presentation.group.model

import com.google.gson.annotations.SerializedName

data class GroupChatMessage(
    val id: Long? = null,
    val groupId: Long,
    val senderId: Long,
    val senderName: String? = null,
    @SerializedName(value = "content", alternate = ["message"])
    val content: String,
    val type: String = "CHAT",
    val timestamp: String? = null
)