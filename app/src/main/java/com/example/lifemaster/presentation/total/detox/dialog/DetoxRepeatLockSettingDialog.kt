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

    private fun initListeners() = with(binding)  {
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
            dismiss()
            val dialog = SelectTimeDialog("useTime")
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, SelectTimeDialog.TAG)
        }

        btnUseTimeMinutes.setOnClickListener {
            dismiss()
            val dialog = DetoxRepeatLockTestDialog("useTime")
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, DetoxRepeatLockTestDialog.TAG)
        }

        btnLockTimeMinutes.setOnClickListener {
            dismiss()
            val dialog = DetoxRepeatLockTestDialog("lockTime")
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, DetoxRepeatLockTestDialog.TAG)
        }

        btnMaxTimeMinutes.setOnClickListener {
            dismiss()
            val dialog = DetoxRepeatLockTestDialog("maxUseTime")
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, DetoxRepeatLockTestDialog.TAG)
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

            val useTime = btnUseTimeMinutes.text.toString().toInt()
            val lockTime = btnLockTimeMinutes.text.toString().toInt()
            val maxUseTime = btnMaxTimeMinutes.text.toString().toInt()
            val isMaxTimeLimitSet = maxUseTime != 0

            val repeatLockItem = DetoxRepeatLockItem(
                selectedApp.appIcon,
                selectedApp.appName,
                selectedApp.appPackageName,
                useTime,
                lockTime,
                maxUseTime,
                selectedApp.accumulatedTime,
                isMaxTimeLimitSet
            )

            viewModel.addRepeatLockApp(repeatLockItem)

            val intent = Intent("com.example.lifemaster.BROADCAST_RECEIVER")
            intent.putExtra("TEMPORARY_BLOCK_APP", repeatLockItem)
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)

            dismiss()
        }
    }

    private fun openTargetAppDialog() {
        dismiss()
        val dialog = DetoxRepeatLockTargetDialog()
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, DetoxRepeatLockTargetDialog.TAG)
    }

    private fun initObservers() {
        viewModel.repeatLockTargetApp.observe(viewLifecycleOwner) {
            targetApp = it
            bindSelectedTargetApp()
        }
        viewModel.useTime.observe(viewLifecycleOwner) { useTime ->
//            binding.btnUseTimeHour.text = "${useTime.first}"
            binding.btnUseTimeMinutes.text = "${useTime.second}"
        }
        viewModel.lockTime.observe(viewLifecycleOwner) { lockTime ->
//            binding.btnLockTimeHour.text = "${lockTime.first}"
            binding.btnLockTimeMinutes.text = "${lockTime.second}"
        }
        viewModel.maxUseTime.observe(viewLifecycleOwner) { maxUseTime ->
//            binding.btnMaxTimeHour.text = "${maxUseTime.first}"
            binding.btnMaxTimeMinutes.text = "${maxUseTime.second}"
            binding.llMaxTimeClose.visibility = View.GONE
            binding.llMaxTimeOpen.visibility = View.VISIBLE
        }

        // test 용
        viewModel.tempUseTime.observe(viewLifecycleOwner) { useTime ->
            binding.btnUseTimeMinutes.text = "$useTime"
        }
        viewModel.tempLockTime.observe(viewLifecycleOwner) { lockTime ->
            binding.btnLockTimeMinutes.text = "$lockTime"
        }
        viewModel.tempMaxUseTime.observe(viewLifecycleOwner) { maxUseTime ->
            binding.btnMaxTimeMinutes.text = "$maxUseTime"
            binding.llMaxTimeClose.visibility = View.GONE
            binding.llMaxTimeOpen.visibility = View.VISIBLE
        }
    }

    companion object {
        const val TAG = "DetoxRepeatLockDialog"
    }
}