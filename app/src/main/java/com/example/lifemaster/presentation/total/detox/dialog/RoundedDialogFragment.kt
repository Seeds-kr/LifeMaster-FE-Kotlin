package com.example.lifemaster.presentation.total.detox.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment

open class RoundedDialogFragment(
    @LayoutRes layoutRes: Int
) : DialogFragment(layoutRes) {

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}