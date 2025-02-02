package com.example.lifemaster.presentation.total.detox.dialog

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogDetoxRepeatLockTestBinding
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxRepeatLockViewModel

class DetoxRepeatLockTestDialog(private val type: String) : DialogFragment(R.layout.dialog_detox_repeat_lock_test) {

    private lateinit var binding: DialogDetoxRepeatLockTestBinding
    private val detoxRepeatLockViewModel: DetoxRepeatLockViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxRepeatLockTestBinding.bind(view)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        val values = Array(12) { ((it + 1) * 5).toString() }
        binding.apply {
            npSecond.minValue = 0
            npSecond.maxValue = 11
            npSecond.displayedValues = values
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnSelect.setOnClickListener {
                val tempValue = (binding.npSecond.value+1)*5
                Log.d("ttest",""+tempValue)
                when(type) {
                    "useTime" -> { detoxRepeatLockViewModel.setTempUseTime(tempValue)}
                    "lockTime" -> { detoxRepeatLockViewModel.setTempLockTime(tempValue)}
                    "maxUseTime" -> { detoxRepeatLockViewModel.setTempMaxUseTime(tempValue)}
                }
                dismiss()
                showParentDialog()
            }
            btnCancel.setOnClickListener {
                dismiss()
                showParentDialog()
            }
        }
    }

    private fun showParentDialog() {
        val dialog = DetoxRepeatLockSettingDialog()
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, DetoxRepeatLockSettingDialog.TAG)
    }

    companion object {
        const val TAG = "DetoxRepeatLockTestDialog"
    }
}