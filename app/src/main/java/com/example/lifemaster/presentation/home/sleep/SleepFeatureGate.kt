package com.example.lifemaster.presentation.home.sleep

import android.content.Context
import android.widget.Toast
import com.example.lifemaster.R

/**
 * 수면 기능 활성화 여부.
 */
object SleepFeatureGate {

    const val IS_ENABLED = true

    fun showUnavailableToast(context: Context) {
        Toast.makeText(
            context,
            context.getString(R.string.sleep_feature_in_development),
            Toast.LENGTH_SHORT
        ).show()
    }

    inline fun runIfEnabled(context: Context, block: () -> Unit) {
        if (IS_ENABLED) {
            block()
        } else {
            showUnavailableToast(context)
        }
    }
}
