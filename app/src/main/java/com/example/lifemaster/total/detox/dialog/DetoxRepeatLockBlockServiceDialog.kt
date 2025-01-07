package com.example.lifemaster.total.detox.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogDetoxRepeatLockBlockServiceBinding
import com.example.lifemaster.total.detox.adapter.DetoxServiceSettingAdapter
import com.example.lifemaster.total.detox.viewmodel.DetoxRepeatLockViewModel
import com.example.lifemaster.total.detox.viewmodel.DetoxTimeLockViewModel

class DetoxRepeatLockBlockServiceDialog: DialogFragment(R.layout.dialog_detox_repeat_lock_block_service) {
    private lateinit var binding: DialogDetoxRepeatLockBlockServiceBinding
    private val repeatLockViewModel: DetoxRepeatLockViewModel by activityViewModels()
    private val timeLockViewModel: DetoxTimeLockViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxRepeatLockBlockServiceBinding.bind(view)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        binding.recyclerview.layoutManager = GridLayoutManager(context, 5)
        binding.recyclerview.adapter = DetoxServiceSettingAdapter(repeatLockViewModel.blockServiceApplications, repeatLockViewModel, timeLockViewModel)
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnApply.setOnClickListener {
            val allowServices = repeatLockViewModel.blockServiceApplications.filter { app -> app.isClicked }
            repeatLockViewModel.updateBlockServices(allowServices)
            dismiss()
        }
    }

    companion object {
        const val TAG = "DetoxRepeatLockBlockServiceDialog"
    }
}