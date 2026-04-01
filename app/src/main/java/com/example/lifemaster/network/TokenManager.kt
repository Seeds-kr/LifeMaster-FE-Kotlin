package com.example.lifemaster.network

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var cachedToken: String? = null

    var accessToken: String?
        get() {
            val stored = TokenProvider.getAccessToken(context)?.trim()
            cachedToken = stored
            return stored?.takeIf { it.isNotBlank() }
        }
        set(value) {
            val normalized = value?.trim()
            cachedToken = normalized
            TokenProvider.saveAccessToken(context, normalized)
        }

    fun getBearerToken(): String? {
        val raw = accessToken?.trim()
        if (raw.isNullOrBlank()) return null
        return if (raw.startsWith("Bearer ", ignoreCase = true)) raw else "Bearer $raw"
    }

    fun clear() {
        cachedToken = null
        TokenProvider.clear(context)
    }

    fun refreshFromStorage() {
        cachedToken = TokenProvider.getAccessToken(context)?.trim()
    }

    fun getMemberId(): Long? = TokenProvider.getMemberId(context)

    fun getJoinTargetId(): Long? = TokenProvider.getJoinTargetId(context)
}