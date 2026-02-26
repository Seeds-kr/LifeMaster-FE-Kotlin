package com.example.lifemaster.presentation.total.detox.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogDetoxTargetAppBinding
import com.example.lifemaster.presentation.total.detox.adapter.DetoxRepeatLockTargetAppAdapter
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxRepeatLockViewModel

class DetoxRepeatLockTargetDialog: DialogFragment(R.layout.dialog_detox_target_app) {

    private lateinit var binding: DialogDetoxTargetAppBinding
    private val viewModel: DetoxRepeatLockViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxTargetAppBinding.bind(view)
        setupViews()
        setupListeners()
    }

    private fun setupViews() = with(binding) {
        tvDetoxTargetServiceTitle.text = "반복 잠금 대상 어플"
        tvDetoxTargetServiceSubTitle.text = "반복 잠금을 설정할 어플을 선택해주세요"
        binding.rvDetoxTargetService.layoutManager = GridLayoutManager(context, 5)
        binding.rvDetoxTargetService.adapter = DetoxRepeatLockTargetAppAdapter()
        (binding.rvDetoxTargetService.adapter as DetoxRepeatLockTargetAppAdapter).setItems(viewModel.repeatLockTargetApplications)
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
            showParentDialog()
        }
        binding.btnApply.setOnClickListener {
//            val clickedApplication = viewModel.repeatLockTargetApplications.find { it.isClicked }
//            if (clickedApplication != null) {
//                viewModel.updateRepeatLockTargetApp(clickedApplication)
//            }
//            dismiss()
//            showParentDialog()
        }
    }

    private fun showParentDialog() {
        val dialog = DetoxRepeatLockSettingDialog()
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, DetoxRepeatLockSettingDialog.TAG)
    }

    companion object {
        const val TAG = "DetoxRepeatLockTargetAppDialog"
    }
}