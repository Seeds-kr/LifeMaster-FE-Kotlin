package com.example.lifemaster.network

import android.content.Context
import android.util.Base64
import org.json.JSONObject

object TokenProvider {
    private const val PREF = "auth"
    private const val KEY_TOKEN = "token"
    private const val KEY_MEMBER_ID = "memberId"

    private fun sp(context: Context) =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun saveAccessToken(context: Context, token: String?) {
        val normalized = token?.trim()

        val editor = sp(context).edit()
        editor.putString(KEY_TOKEN, normalized)
        editor.remove(KEY_MEMBER_ID)
        editor.apply()

        val extracted = normalized?.let { extractUserIdFromJwt(it) }
        if (extracted != null && extracted > 0L) {
            saveMemberId(context, extracted)
        }
    }

    fun clear(context: Context) {
        sp(context).edit().clear().apply()
    }

    fun saveMemberId(context: Context, memberId: Long?) {
        if (memberId == null || memberId <= 0L) return
        sp(context).edit().putLong(KEY_MEMBER_ID, memberId).apply()
    }

    fun getMemberId(context: Context): Long? {
        val v = sp(context).getLong(KEY_MEMBER_ID, -1L)
        return if (v > 0L) v else null
    }

    fun getAccessToken(context: Context): String? =
        sp(context).getString(KEY_TOKEN, null)?.trim()

    fun getBearerToken(context: Context): String? {
        val raw = getAccessToken(context)
        if (raw.isNullOrBlank()) return null
        return if (raw.startsWith("Bearer ", ignoreCase = true)) raw else "Bearer $raw"
    }

    fun getJoinTargetId(context: Context): Long? {
        val token = getAccessToken(context)
        val fromJwt = token?.let { extractUserIdFromJwt(it) }
        return fromJwt ?: getMemberId(context)
    }

    private fun extractUserIdFromJwt(rawToken: String): Long? {
        val token = rawToken.trim().removePrefix("Bearer ").trim()
        val parts = token.split(".")
        if (parts.size < 2) return null

        return runCatching {
            val payload = parts[1]
            val decoded = String(
                Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            )
            val json = JSONObject(decoded)

            findLong(json, listOf("memberId", "userId", "id", "uid", "user_id", "member_id"))
                ?: run {
                    if (json.has("sub")) {
                        when (val sub = json.opt("sub")) {
                            is Number -> sub.toLong()
                            is String -> sub.toLongOrNull()
                            else -> null
                        }
                    } else null
                }
                ?: run {
                    extractNestedId(json, "member")
                        ?: extractNestedId(json, "user")
                        ?: extractNestedId(json, "account")
                }
        }.getOrNull()
    }

    private fun findLong(json: JSONObject, keys: List<String>): Long? {
        for (k in keys) {
            if (!json.has(k)) continue
            when (val v = json.opt(k)) {
                is Number -> return v.toLong()
                is String -> v.toLongOrNull()?.let { return it }
            }
        }
        return null
    }

    private fun extractNestedId(json: JSONObject, objKey: String): Long? {
        val obj = json.optJSONObject(objKey) ?: return null
        return findLong(obj, listOf("memberId", "userId", "id", "uid"))
    }
}