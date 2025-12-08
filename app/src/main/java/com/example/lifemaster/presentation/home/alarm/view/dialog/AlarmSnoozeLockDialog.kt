package com.example.lifemaster.presentation.home.alarm.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogAlarmSnoozeLockBinding
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmGenerateViewModel
import kotlin.getValue

class AlarmSnoozeLockDialog: DialogFragment(R.layout.dialog_alarm_snooze_lock) {

    private lateinit var binding: DialogAlarmSnoozeLockBinding
    private val alarmGenerateViewModel: AlarmGenerateViewModel by activityViewModels()
    private val snoozeLockMinuteIds = listOf<Pair<Int, Int>>(
        R.id.rbAlarmSettingSnoozeLockMinutes1 to 1,
        R.id.rbAlarmSettingSnoozeLockMinutes2 to 2,
        R.id.rbAlarmSettingSnoozeLockMinutes3 to 3,
        R.id.rbAlarmSettingSnoozeLockMinutes4 to 4,
        R.id.rbAlarmSettingSnoozeLockMinutes5 to 5,
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogAlarmSnoozeLockBinding.bind(view)
        initObservers()
        initListeners()
    }

    private fun initObservers() = with(binding) {
        alarmGenerateViewModel.snoozeLockMinute.observe(viewLifecycleOwner) { minute ->
            rgAlarmSettingSnoozeLockMinutes.clearCheck()
            val snoozeLockMinuteId = snoozeLockMinuteIds.single { it.second == minute }.first
            rgAlarmSettingSnoozeLockMinutes.findViewById<RadioButton>(snoozeLockMinuteId).isChecked = true
        }
    }

    private fun initListeners() = with(binding) {
        btnAlarmSnoozeLockApply.setOnClickListener {
            val snoozeLockMinute = snoozeLockMinuteIds.find { it.first == rgAlarmSettingSnoozeLockMinutes.checkedRadioButtonId }?.second ?: 2
            alarmGenerateViewModel.setSnoozeLockMinute(snoozeLockMinute = snoozeLockMinute)
            dismiss()
        }
    }

    companion object {
        const val TAG = "AlarmSnoozeAntiDialog"
    }
}