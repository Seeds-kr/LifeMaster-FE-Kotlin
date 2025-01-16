package com.example.lifemaster.presentation.total.detox.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogDetoxRepeatLockTargetAppBinding
import com.example.lifemaster.presentation.total.detox.adapter.DetoxRepeatLockTargetAppAdapter
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxRepeatLockViewModel

class DetoxRepeatLockSettingTargetDialog: DialogFragment(R.layout.dialog_detox_repeat_lock_target_app) {

    private lateinit var binding: DialogDetoxRepeatLockTargetAppBinding
    private val viewModel: DetoxRepeatLockViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxRepeatLockTargetAppBinding.bind(view)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        binding.recyclerview.layoutManager = GridLayoutManager(context, 5)
        binding.recyclerview.adapter = DetoxRepeatLockTargetAppAdapter()
        (binding.recyclerview.adapter as DetoxRepeatLockTargetAppAdapter).setItems(viewModel.repeatLockTargetApplications)
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
            showParentDialog()
        }
        binding.btnApply.setOnClickListener {
            dismiss()
            val clickedApplication = viewModel.repeatLockTargetApplications.find { it.isClicked }
            if (clickedApplication != null) {
                viewModel.updateRepeatLockTargetApp(clickedApplication)
            }
            showParentDialog()
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