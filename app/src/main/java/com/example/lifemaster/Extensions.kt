package com.example.lifemaster

import android.content.Context
import android.view.WindowManager
import androidx.fragment.app.DialogFragment

fun Context.dialogFragmentResize(dialogFragment: DialogFragment, width: Float, height: Float) {
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val rect = windowManager.currentWindowMetrics.bounds
    val x = (rect.width()*width).toInt()
    val y = (rect.height()*height).toInt()
    val window = dialogFragment.dialog?.window
    window?.setLayout(x,y)
}