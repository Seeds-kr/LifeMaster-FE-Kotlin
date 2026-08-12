package com.example.lifemaster.presentation.tutorial

import android.content.Context
import androidx.core.content.edit

/**
 * 할일(홈)/챌린지 화면에 처음 진입했을 때 튜토리얼을 한 번만 보여주기 위한 플래그 저장소.
 */
object TutorialPrefs {

    private const val PREF_NAME = "tutorial_pref"
    private const val KEY_HOME_TODO_TUTORIAL_SHOWN = "home_todo_tutorial_shown"
    private const val KEY_CHALLENGE_TUTORIAL_SHOWN = "challenge_tutorial_shown"

    fun hasSeenHomeTodoTutorial(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HOME_TODO_TUTORIAL_SHOWN, false)

    fun markHomeTodoTutorialSeen(context: Context) {
        prefs(context).edit { putBoolean(KEY_HOME_TODO_TUTORIAL_SHOWN, true) }
    }

    fun hasSeenChallengeTutorial(context: Context): Boolean =
        prefs(context).getBoolean(KEY_CHALLENGE_TUTORIAL_SHOWN, false)

    fun markChallengeTutorialSeen(context: Context) {
        prefs(context).edit { putBoolean(KEY_CHALLENGE_TUTORIAL_SHOWN, true) }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}
