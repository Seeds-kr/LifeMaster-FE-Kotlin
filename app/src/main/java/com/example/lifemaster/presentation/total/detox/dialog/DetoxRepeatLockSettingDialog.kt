package com.example.lifemaster.presentation.total.detox.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogDetoxRepeatLockSettingBinding
import com.example.lifemaster.presentation.total.detox.model.DetoxRepeatLockItem
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxRepeatLockViewModel
import kotlin.math.max

class DetoxRepeatLockSettingDialog : DialogFragment(R.layout.dialog_detox_repeat_lock_setting) {

    private lateinit var binding: DialogDetoxRepeatLockSettingBinding
    private lateinit var targetApp: DetoxTargetApp
    private val viewModel: DetoxRepeatLockViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxRepeatLockSettingBinding.bind(view)
        setupListeners()
        observing()
    }

    private fun setupListeners() {
        binding.ivOpenMaxTimeSetting.setOnClickListener {
            binding.llMaxTimeClose.visibility = View.GONE
            binding.llMaxTimeOpen.visibility = View.VISIBLE
        }
        binding.ivCloseMaxTimeSetting.setOnClickListener {
            binding.llMaxTimeOpen.visibility = View.GONE
            binding.llMaxTimeClose.visibility = View.VISIBLE
        }

        binding.tvSelectTargetApp.setOnClickListener {
            dismiss()
            val dialog = DetoxRepeatLockSettingTargetDialog()
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, DetoxRepeatLockSettingTargetDialog.TAG)
        }
        binding.ivSelectTargetApp.setOnClickListener {
            dismiss()
            val dialog = DetoxRepeatLockSettingTargetDialog()
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, DetoxRepeatLockSettingTargetDialog.TAG)
        }

        binding.btnUseTimeHour.setOnClickListener {
            dismiss()
            val dialog = DetoxRepeatLockSelectTimeDialog("useTime")
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, DetoxRepeatLockSelectTimeDialog.TAG)
        }
        binding.btnUseTimeMinutes.setOnClickListener {
            dismiss()
            val dialog = DetoxRepeatLockSelectTimeDialog("useTime")
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, DetoxRepeatLockSelectTimeDialog.TAG)
        }
        binding.btnLockTimeHour.setOnClickListener {
            dismiss()
            val dialog = DetoxRepeatLockSelectTimeDialog("lockTime")
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, DetoxRepeatLockSelectTimeDialog.TAG)
        }
        binding.btnLockTimeMinutes.setOnClickListener {
            dismiss()
            val dialog = DetoxRepeatLockSelectTimeDialog("lockTime")
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, DetoxRepeatLockSelectTimeDialog.TAG)
        }
        binding.btnMaxTimeHour.setOnClickListener {
            dismiss()
            val dialog = DetoxRepeatLockSelectTimeDialog("maxUseTime")
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, DetoxRepeatLockSelectTimeDialog.TAG)
        }
        binding.btnMaxTimeMinutes.setOnClickListener {
            dismiss()
            val dialog = DetoxRepeatLockSelectTimeDialog("maxUseTime")
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, DetoxRepeatLockSelectTimeDialog.TAG)
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnAdd.setOnClickListener {
            if (binding.tvSelectTargetApp.visibility == View.VISIBLE) {
                Toast.makeText(context, "앱을 선택해주세요!", Toast.LENGTH_SHORT).show()
            } else {
                binding.apply {
                    val appIcon = targetApp.appIcon
                    val appName = targetApp.appName
                    val accumulatedTime = targetApp.accumulatedTime

                    val useTime = btnUseTimeHour.text.toString().toInt()*60 + btnUseTimeMinutes.text.toString().toInt()
                    val lockTime = btnLockTimeHour.text.toString().toInt()*60 + btnLockTimeMinutes.text.toString().toInt()
                    val maxTime = btnMaxTimeHour.text.toString().toInt()*60 + btnMaxTimeMinutes.text.toString().toInt()

                    val isMaxTimeLimitSet = if(btnMaxTimeHour.text.equals("0") && btnMaxTimeMinutes.text.equals("0")) false else true

                    viewModel.addRepeatLockApp(DetoxRepeatLockItem(
                        appIcon, appName, useTime, lockTime, maxTime, accumulatedTime, isMaxTimeLimitSet
                    ))

                }
                dismiss()
            }
        }
    }

    private fun observing() {
        viewModel.repeatLockTargetApp.observe(viewLifecycleOwner) {
            targetApp = it
            binding.ivSelectTargetApp.setImageDrawable(it.appIcon)
            binding.tvTargetAppName.text = it.appName
            binding.tvSelectTargetApp.visibility = View.GONE
            binding.ivSelectTargetApp.visibility = View.VISIBLE
            binding.tvTargetAppName.visibility = View.VISIBLE
        }
        viewModel.useTime.observe(viewLifecycleOwner) { useTime ->
            binding.btnUseTimeHour.text = "${useTime.first}"
            binding.btnUseTimeMinutes.text = "${useTime.second}"
        }
        viewModel.lockTime.observe(viewLifecycleOwner) { lockTime ->
            binding.btnLockTimeHour.text = "${lockTime.first}"
            binding.btnLockTimeMinutes.text = "${lockTime.second}"
        }
        viewModel.maxUseTime.observe(viewLifecycleOwner) { maxUseTime ->
            binding.btnMaxTimeHour.text = "${maxUseTime.first}"
            binding.btnMaxTimeMinutes.text = "${maxUseTime.second}"
            binding.llMaxTimeClose.visibility = View.GONE
            binding.llMaxTimeOpen.visibility = View.VISIBLE
        }
    }

    companion object {
        const val TAG = "DetoxRepeatLockDialog"
    }
}