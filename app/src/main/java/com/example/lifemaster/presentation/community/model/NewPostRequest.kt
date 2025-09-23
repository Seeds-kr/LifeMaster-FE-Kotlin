package com.example.lifemaster.presentation.community.model

data class NewPostRequest(
    val title: String,
    val content: String,
    val file: String?,
    val type: String
)