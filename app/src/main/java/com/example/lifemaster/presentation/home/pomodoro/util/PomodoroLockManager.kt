package com.example.lifemaster.presentation.home.pomodoro.util

import android.content.Context

object PomodoroLockManager {
    private const val PREF_NAME = "pomodoro_lock"
    private const val KEY_IS_LOCKED = "isLocked"
    private const val KEY_END_TIME_MILLIS = "endTimeMillis"

    fun lock(context: Context, totalSeconds: Int) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_LOCKED, true)
            .putLong(KEY_END_TIME_MILLIS, System.currentTimeMillis() + totalSeconds * 1000L)
            .apply()
    }

    fun unlock(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
