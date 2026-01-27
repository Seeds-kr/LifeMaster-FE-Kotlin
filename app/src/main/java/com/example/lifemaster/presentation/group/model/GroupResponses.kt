package com.example.lifemaster.presentation.group.model

data class GroupCreateResponse(
    val id: Long,
    val name: String?,
    val description: String?,
    val icon: String?,
    val password: String?
)

data class GroupGoalResponse(
    val id: Long,
    val name: String?,
    val goalCondition: String?
)

data class GroupGoalCreateRequest(
    val name: String,
    val goalCondition: String,
    val duration: String? = null,
    val value: Int? = null
)