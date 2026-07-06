package com.example.lifemaster.presentation.group.model

data class GroupResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val icon: String?,
    val creatorId: Long? = null,
    val memberCount: Int? = 0,
    val accessType: String? = null
)

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
    val goalType: String,
    val goalCondition: String,
    val value: Int,
    val duration: String
)