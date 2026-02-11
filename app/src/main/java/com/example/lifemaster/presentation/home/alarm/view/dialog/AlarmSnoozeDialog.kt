package com.example.lifemaster.presentation.home.alarm.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogAlarmSnoozeBinding
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmGenerateViewModel
import com.google.android.material.radiobutton.MaterialRadioButton
import kotlinx.coroutines.launch
import kotlin.getValue

class AlarmSnoozeDialog: DialogFragment(R.layout.dialog_alarm_snooze) {

    private lateinit var binding: DialogAlarmSnoozeBinding
    private val alarmGenerateViewModel: AlarmGenerateViewModel by activityViewModels()
    private val snoozeMinuteIds = listOf<Pair<Int, Int>>(
        R.id.rbAlarmSettingSnoozeMinutes5 to 5,
        R.id.rbAlarmSettingSnoozeMinutes10 to 10,
        R.id.rbAlarmSettingSnoozeMinutes15 to 15,
        R.id.rbAlarmSettingSnoozeMinutes20 to 20
    )
    private val snoozeCountIds = listOf<Pair<Int,Int>>(
        R.id.rbAlarmSettingSnoozeCountOne to 1,
        R.id.rbAlarmSettingSnoozeCountTwo to 2,
        R.id.rbAlarmSettingSnoozeCountThree to 3,
        R.id.rbAlarmSettingSnoozeCountFour to 4
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogAlarmSnoozeBinding.bind(view)
        initObservers()
        initListeners()
    }

    private fun initObservers() = with(binding) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                alarmGenerateViewModel.snoozeDuration.collect { duration ->
                    rgAlarmSettingSnoozeMinutes.clearCheck()
                    val snoozeMinute = duration.first
                    val snoozeMinuteId = snoozeMinuteIds.single { it.second == snoozeMinute }.first
                    rgAlarmSettingSnoozeMinutes.findViewById<MaterialRadioButton>(snoozeMinuteId).isChecked =
                        true
                    val snoozeCount = duration.second
                    val snoozeCountId = snoozeCountIds.single { it.second == snoozeCount }.first
                    rgAlarmSettingSnoozeCount.findViewById<MaterialRadioButton>(snoozeCountId).isChecked =
                        true
                }
            }
        }
    }

    private fun initListeners() = with(binding) {
        btnAlarmSnoozeApply.setOnClickListener {
            val snoozeMinutes = snoozeMinuteIds.find { it.first == rgAlarmSettingSnoozeMinutes.checkedRadioButtonId }?.second ?: 10
            val snoozeCount = snoozeCountIds.find { it.first == rgAlarmSettingSnoozeCount.checkedRadioButtonId}?.second ?: 2
            alarmGenerateViewModel.setSnoozeDuration(snoozeDuration = snoozeMinutes to snoozeCount)
            dismiss()
        }
    }

    companion object {
        const val TAG = "AlarmSnoozeDialog"
    }
}