package com.example.lifemaster.presentation.home.pomodoro.view

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.lifemaster.presentation.home.pomodoro.model.SharedData
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroStatus

class EmergencyEscapeDialog: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setMessage("비상탈출을 그만두시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                SharedData.pomodoroStatus = PomodoroStatus.ESCAPE_FAILURE
                requireActivity().finish()
            }
            .setNegativeButton("아니요") { _, _ -> dismiss() }
            .create()
    }

    companion object {
        const val TAG = "EmergencyEscapeDialog"
    }
}