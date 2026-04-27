package com.example.lifemaster.network

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor() {
    var accessToken: String? = null
}