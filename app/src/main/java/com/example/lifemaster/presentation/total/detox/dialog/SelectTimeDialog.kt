package com.example.lifemaster.presentation.total.detox.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogSelectTimesBinding
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModel
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxRepeatLockViewModel

class SelectTimeDialog(private val type: String) :
    DialogFragment(R.layout.dialog_select_times) {
    private lateinit var binding: DialogSelectTimesBinding
    private val detoxRepeatLockViewModel: DetoxRepeatLockViewModel by activityViewModels()
    private val alarmViewModel: AlarmViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogSelectTimesBinding.bind(view)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        val values = Array(12) { (it * 5).toString() }
        binding.npFirst.minValue = 0
        binding.npFirst.maxValue = 12
        binding.npSecond.minValue = 0 // 배열의 첫번째 인덱스
        binding.npSecond.maxValue = 11 // 배열의 마지막 인덱스
        binding.npSecond.displayedValues = values
        when (type) {
            "delay" -> {
                val minuteValues = Array(5) { ((it+2)*5).toString() } // 10, 15, 20, 25, 30
                val delayValues = Array(3) {(it+1).toString()}
                binding.apply {
                    tvTitle.text = "시간(분), 횟수(미루기)를 선택해주세요."
                    npFirst.minValue = 0
                    npFirst.maxValue = 4
                    npFirst.displayedValues = minuteValues
                    npSecond.minValue = 0
                    npSecond.maxValue = 2
                    npSecond.displayedValues = delayValues
                }
            }
            "useTime" -> {
                binding.npFirst.value = detoxRepeatLockViewModel.useTime.value?.first ?: 0
                binding.npSecond.value = (detoxRepeatLockViewModel.useTime.value?.second?.div(5)) ?: 0
            }

            "lockTime" -> {
                binding.npFirst.value = detoxRepeatLockViewModel.lockTime.value?.first ?: 0
                binding.npSecond.value = detoxRepeatLockViewModel.lockTime.value?.second?.div(5) ?: 0
            }

            "maxUseTime" -> {
                binding.npFirst.value = detoxRepeatLockViewModel.maxUseTime.value?.first ?: 0
                binding.npSecond.value = detoxRepeatLockViewModel.maxUseTime.value?.second?.div(5) ?: 0
            }
        }
    }

    private fun setupListeners() {
        binding.btnSelect.setOnClickListener {
            val detoxValue = Pair(binding.npFirst.value, binding.npSecond.value*5)
            val alarmValue = Pair((binding.npFirst.value+2)*5, binding.npSecond.value+1)
            when (type) {
                "useTime" -> {
                    detoxRepeatLockViewModel.setUseTime(detoxValue)
                    dismiss()
                    showParentDialog()
                }
                "lockTime" -> {
                    detoxRepeatLockViewModel.setLockTime(detoxValue)
                    dismiss()
                    showParentDialog()
                }
                "maxUseTime" -> {
                    detoxRepeatLockViewModel.setMaxUseTime(detoxValue)
                    dismiss()
                    showParentDialog()
                }
                "delay" -> {
                    alarmViewModel.setDelayMinutesAndCount(alarmValue)
                    dismiss()
                }
            }

        }
        binding.btnCancel.setOnClickListener {
            when(type) {
                "useTime", "lockTime", "maxUseTime" -> {
                    dismiss()
                    showParentDialog()
                }
                "delay" -> dismiss()
            }
        }
    }

    private fun showParentDialog() {
        val dialog = DetoxRepeatLockSettingDialog()
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, DetoxRepeatLockSettingDialog.Companion.TAG)
    }

    companion object {
        const val TAG = "DetoxRepeatLockSelectTimeDialog"
    }
}