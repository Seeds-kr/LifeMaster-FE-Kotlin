package com.example.lifemaster.presentation.total.detox.dialog

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogSelectTimesBinding
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxRepeatLockViewModel

class DetoxRepeatLockSelectTimeDialog(private val type: String) :
    DialogFragment(R.layout.dialog_select_times) {
    private lateinit var binding: DialogSelectTimesBinding
    private val viewModel: DetoxRepeatLockViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogSelectTimesBinding.bind(view)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        val values = Array(12) { (it * 5).toString() }
        binding.npHour.minValue = 0
        binding.npHour.maxValue = 12
        binding.npMinutes.minValue = 0 // 배열의 첫번째 인덱스
        binding.npMinutes.maxValue = 11 // 배열의 마지막 인덱스
        binding.npMinutes.displayedValues = values
        when (type) {
            "useTime" -> {
                binding.npHour.value = viewModel.useTime.value?.first ?: 0
                binding.npMinutes.value = (viewModel.useTime.value?.second?.div(5)) ?: 0
            }

            "lockTime" -> {
                binding.npHour.value = viewModel.lockTime.value?.first ?: 0
                binding.npMinutes.value = viewModel.lockTime.value?.second?.div(5) ?: 0
            }

            "maxUseTime" -> {
                binding.npHour.value = viewModel.maxUseTime.value?.first ?: 0
                binding.npMinutes.value = viewModel.maxUseTime.value?.second?.div(5) ?: 0
            }
        }
    }

    private fun setupListeners() {
        binding.btnSelect.setOnClickListener {
            Log.d("ttest", ""+binding.npMinutes.value)
            val time = Pair(binding.npHour.value, binding.npMinutes.value*5)
            when (type) {
                "useTime" -> viewModel.setUseTime(time)
                "lockTime" -> viewModel.setLockTime(time)
                "maxUseTime" -> viewModel.setMaxUseTime(time)
            }
            dismiss()
            showParentDialog()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
            showParentDialog()
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