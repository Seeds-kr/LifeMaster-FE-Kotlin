package com.example.lifemaster.presentation.total.detox.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogSelectTimesBinding
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModel
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModelFactory
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxRepeatLockViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SelectTimeDialog(private val type: String) :
    RoundedDialogFragment(R.layout.dialog_select_times) {
    private lateinit var binding: DialogSelectTimesBinding
    private val detoxRepeatLockViewModel: DetoxRepeatLockViewModel by activityViewModels()

    @Inject lateinit var networkService: NetworkService

    private val alarmViewModel: AlarmViewModel by activityViewModels(
        factoryProducer = { AlarmViewModelFactory(networkService) }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogSelectTimesBinding.bind(view)
        setupViews()
        setupListeners()
    }

    private fun setupViews() = with(binding) {
        when (type) {
            "delay" -> {
                val minuteValues = Array(5) { ((it + 2) * 5).toString() }
                val delayValues = Array(3) { (it + 1).toString() }

                tvTitle.text = "시간(분), 횟수(미루기)를 선택해주세요."

                npFirst.minValue = 0
                npFirst.maxValue = 4
                npFirst.displayedValues = minuteValues

                npSecond.minValue = 0
                npSecond.maxValue = 2
                npSecond.displayedValues = delayValues
            }

            "useTime", "lockTime", "maxUseTime" -> {
                tvTitle.text = "시간과 분을 설정해주세요."

                npFirst.minValue = 0
                npFirst.maxValue = 23
                npFirst.wrapSelectorWheel = true

                val minuteValues = Array(12) { (it * 5).toString() }

                npSecond.minValue = 0
                npSecond.maxValue = 11
                npSecond.displayedValues = minuteValues
                npSecond.wrapSelectorWheel = true

                val currentValue = when (type) {
                    "useTime" -> detoxRepeatLockViewModel.useTime.value
                    "lockTime" -> detoxRepeatLockViewModel.lockTime.value
                    "maxUseTime" -> detoxRepeatLockViewModel.maxUseTime.value
                    else -> null
                }

                npFirst.value = currentValue?.first ?: 0
                npSecond.value = (currentValue?.second ?: 0) / 5
            }
        }
    }

    private fun setupListeners() = with(binding) {
        btnSelect.setOnClickListener {
            when (type) {
                "useTime" -> {
                    val value = Pair(npFirst.value, npSecond.value * 5)
                    detoxRepeatLockViewModel.setUseTime(value)
                    dismiss()
                    showParentDialog()
                }
                "lockTime" -> {
                    val value = Pair(npFirst.value, npSecond.value * 5)
                    detoxRepeatLockViewModel.setLockTime(value)
                    dismiss()
                    showParentDialog()
                }
                "maxUseTime" -> {
                    val value = Pair(npFirst.value, npSecond.value * 5)
                    detoxRepeatLockViewModel.setMaxUseTime(value)
                    dismiss()
                    showParentDialog()
                }
                "delay" -> {
                    val value = Pair((npFirst.value + 2) * 5, npSecond.value + 1)
                    alarmViewModel.setDelayMinutesAndCount(value)
                    dismiss()
                }
            }
        }

        btnCancel.setOnClickListener {
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
        dialog.show(parentFragmentManager, DetoxRepeatLockSettingDialog.TAG)
    }

    companion object {
        const val TAG = "DetoxRepeatLockSelectTimeDialog"
    }
}