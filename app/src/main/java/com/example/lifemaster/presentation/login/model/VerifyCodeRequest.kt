package com.example.lifemaster.presentation.login.model

data class VerifyCodeRequest(
    val email: String,
    val code: String
)