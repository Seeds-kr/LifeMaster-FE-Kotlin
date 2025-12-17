package com.example.lifemaster.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val token: String): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithHeader = originalRequest.newBuilder().header("Authorization", "Bearer $token").header("Content-Type", "application/json").method(originalRequest.method, originalRequest.body).build()
        return chain.proceed(requestWithHeader)
    }
}