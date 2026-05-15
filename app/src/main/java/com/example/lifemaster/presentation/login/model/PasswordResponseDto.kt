package com.example.lifemaster.presentation.login.model

data class PasswordResponseDto(
    val success: Boolean,
    val message: String,
    val token: String? = null
)