package com.example.lifemaster.presentation.group.model

data class GroupChatReadEvent(
    val groupId: Long,
    val messageId: Long,
    val readerId: Long
)