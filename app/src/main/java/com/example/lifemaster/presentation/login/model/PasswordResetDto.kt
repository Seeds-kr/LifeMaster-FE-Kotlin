package com.example.lifemaster.presentation.login.model

data class PasswordResetDto(
    val token: String,
    val newPassword: String,
    val checkPassword: String
)