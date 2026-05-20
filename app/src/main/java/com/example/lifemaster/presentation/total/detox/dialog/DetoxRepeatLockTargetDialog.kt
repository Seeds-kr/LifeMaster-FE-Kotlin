package com.example.lifemaster.presentation.total.detox.dialog

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogDetoxTargetAppBinding
import com.example.lifemaster.presentation.total.detox.adapter.DetoxRepeatLockTargetAppAdapter
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxRepeatLockViewModel
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxViewModel

class DetoxRepeatLockTargetDialog : RoundedDialogFragment(R.layout.dialog_detox_target_app) {

    private lateinit var binding: DialogDetoxTargetAppBinding

    private val viewModel: DetoxViewModel by activityViewModels()
    private val repeatLockViewModel: DetoxRepeatLockViewModel by activityViewModels()
    private var targetApp: DetoxTargetApp? = null
    private val adapter by lazy {
        DetoxRepeatLockTargetAppAdapter { item ->
            targetApp = item
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxTargetAppBinding.bind(view)
        setupViews()
        setupListeners()
    }

    private fun setupViews() = with(binding) {
        tvDetoxTargetServiceTitle.text = "반복 잠금 대상 어플"
        tvDetoxTargetServiceSubTitle.text = "반복 잠금을 설정할 어플을 선택해주세요"
        rvDetoxTargetService.layoutManager = GridLayoutManager(context, 5)
        rvDetoxTargetService.adapter = adapter
        adapter.submitList(viewModel.installedApps.value)
    }

    private fun setupListeners() = with(binding) {
        btnCancel.setOnClickListener {
            dismiss()
            showParentDialog()
        }
        btnApply.setOnClickListener {
            val selectedApp = targetApp
            if (selectedApp == null) {
                Toast.makeText(context, "반복 잠금할 앱을 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            repeatLockViewModel.updateRepeatLockTargetApp(selectedApp)

            dismiss()
            showParentDialog(selectedApp)
        }
    }

    private fun showParentDialog(targetApp: DetoxTargetApp? = null) {
        val dialog = DetoxRepeatLockSettingDialog(targetApp)
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, DetoxRepeatLockSettingDialog.TAG)
    }

    companion object {
        const val TAG = "DetoxRepeatLockTargetAppDialog"
    }
}