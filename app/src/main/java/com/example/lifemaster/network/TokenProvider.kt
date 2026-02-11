package com.example.lifemaster.network

import android.content.Context

object TokenProvider {
    private const val PREF = "auth"
    private const val KEY_TOKEN = "token"

    fun getAccessToken(context: Context): String? {
        return context
            .getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)
    }
}