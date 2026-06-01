package com.example.lifemaster.presentation.total.detox.dialog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogDetoxRepeatLockSettingBinding
import com.example.lifemaster.presentation.total.detox.model.DetoxRepeatLockItem
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp
import com.example.lifemaster.presentation.total.detox.model.DetoxRepeatLock
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxRepeatLockViewModel

class DetoxRepeatLockSettingDialog(
    private var targetApp: DetoxTargetApp? = null
) : RoundedDialogFragment(R.layout.dialog_detox_repeat_lock_setting) {

    private lateinit var binding: DialogDetoxRepeatLockSettingBinding
    private val viewModel: DetoxRepeatLockViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxRepeatLockSettingBinding.bind(view)
        bindSelectedTargetApp()
        initListeners()
        initObservers()
    }

    private fun bindSelectedTargetApp() {
        val selectedApp = targetApp ?: viewModel.repeatLockTargetApp.value ?: return
        targetApp = selectedApp
        binding.ivSelectTargetApp.setImageDrawable(selectedApp.appIcon)
        binding.tvSelectTargetApp.visibility = View.GONE
        binding.ivSelectTargetApp.visibility = View.VISIBLE
    }

    private fun initListeners() = with(binding) {
        tvSelectTargetApp.setOnClickListener {
            openTargetAppDialog()
        }

        ivSelectTargetApp.setOnClickListener {
            openTargetAppDialog()
        }

        ivOpenMaxTimeSetting.setOnClickListener {
            llMaxTimeClose.visibility = View.GONE
            llMaxTimeOpen.visibility = View.VISIBLE
        }

        ivCloseMaxTimeSetting.setOnClickListener {
            llMaxTimeOpen.visibility = View.GONE
            llMaxTimeClose.visibility = View.VISIBLE
        }

        btnUseTimeHour.setOnClickListener {
            openSelectTimeDialog("useTime")
        }

        btnUseTimeMinutes.setOnClickListener {
            openSelectTimeDialog("useTime")
        }

        btnLockTimeHour.setOnClickListener {
            openSelectTimeDialog("lockTime")
        }

        btnLockTimeMinutes.setOnClickListener {
            openSelectTimeDialog("lockTime")
        }

        btnMaxTimeHour.setOnClickListener {
            openSelectTimeDialog("maxUseTime")
        }

        btnMaxTimeMinutes.setOnClickListener {
            openSelectTimeDialog("maxUseTime")
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnAdd.setOnClickListener {
            val selectedApp = targetApp

            if (selectedApp == null) {
                Toast.makeText(context, "앱을 선택해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val useTimeHour = btnUseTimeHour.text.toString().toIntOrNull() ?: 0
            val useTimeMinutes = btnUseTimeMinutes.text.toString().toIntOrNull() ?: 0

            val lockTimeHour = btnLockTimeHour.text.toString().toIntOrNull() ?: 0
            val lockTimeMinutes = btnLockTimeMinutes.text.toString().toIntOrNull() ?: 0

            val maxUseTimeHour = btnMaxTimeHour.text.toString().toIntOrNull() ?: 0
            val maxUseTimeMinutes = btnMaxTimeMinutes.text.toString().toIntOrNull() ?: 0

            val useTime = useTimeHour * 60 + useTimeMinutes
            val lockTime = lockTimeHour * 60 + lockTimeMinutes
            val maxUseTime = maxUseTimeHour * 60 + maxUseTimeMinutes
            val isMaxTimeLimitSet = maxUseTime != 0

            val oneDayMinutes = 24 * 60

            if (useTime == 0) {
                Toast.makeText(context, "사용 시간을 설정해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (lockTime == 0) {
                Toast.makeText(context, "잠금 시간을 설정해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (useTime + lockTime > oneDayMinutes) {
                Toast.makeText(
                    context,
                    "사용 시간과 잠금 시간의 합은 24시간을 넘을 수 없습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (isMaxTimeLimitSet && maxUseTime > oneDayMinutes) {
                Toast.makeText(
                    context,
                    "하루 최대 사용 시간은 24시간을 넘을 수 없습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (isMaxTimeLimitSet && maxUseTime < useTime) {
                Toast.makeText(
                    context,
                    "하루 최대 사용 시간은 1회 사용 시간보다 짧을 수 없습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val request = DetoxRepeatLock(
                lockedApp = selectedApp.appPackageName,
                sessionUsageLimit = useTime,
                lockDuration = lockTime,
                dailyMaxUsageLimit = maxUseTime
            )

            viewModel.generateRepeatLock(request)

            dismiss()
        }
    }

    private fun openTargetAppDialog() {
        dismiss()
        val dialog = DetoxRepeatLockTargetDialog()
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, DetoxRepeatLockTargetDialog.TAG)
    }

    private fun openSelectTimeDialog(type: String) {
        dismiss()
        val dialog = SelectTimeDialog(type)
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, SelectTimeDialog.TAG)
    }

    private fun initObservers() {
        viewModel.repeatLockTargetApp.observe(viewLifecycleOwner) {
            targetApp = it
            bindSelectedTargetApp()
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