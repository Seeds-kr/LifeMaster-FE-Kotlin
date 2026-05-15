package com.example.lifemaster.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath

        val builder = original.newBuilder()

        if (original.body != null) {
            builder.header("Content-Type", "application/json")
        }

        val skipAuth = path == "/user/login" ||
                path == "/user/register" ||
                path == "/user/register/nickname" ||
                path.startsWith("/auth/password")

        val alreadyHasAuth = !original.header("Authorization").isNullOrBlank()

        if (!skipAuth && !alreadyHasAuth) {
            val bearerToken = tokenManager.getBearerToken()
            if (!bearerToken.isNullOrBlank()) {
                builder.header("Authorization", bearerToken)
            }
        }

        return chain.proceed(builder.build())
    }
}