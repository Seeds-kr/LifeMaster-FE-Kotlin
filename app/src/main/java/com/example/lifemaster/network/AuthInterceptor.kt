package com.example.lifemaster.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(private val tokenManager: TokenManager): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithHeader = originalRequest.newBuilder().header("Authorization", "Bearer ${tokenManager.accessToken}").header("Content-Type", "application/json").method(originalRequest.method, originalRequest.body).build()
        return chain.proceed(requestWithHeader)
    }
}