package com.example.lifemaster.presentation.home.sleep

import android.content.Context
import android.widget.Toast
import com.example.lifemaster.R

/**
 * 수면 기능 임시 차단. 재개 시 [IS_ENABLED]를 true로 변경한다.
 */
object SleepFeatureGate {

    const val IS_ENABLED = false

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
