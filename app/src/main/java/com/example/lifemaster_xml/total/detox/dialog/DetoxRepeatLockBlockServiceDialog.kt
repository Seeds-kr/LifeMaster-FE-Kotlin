package com.example.lifemaster_xml.total.detox.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.databinding.DialogDetoxRepeatLockBlockServiceBinding
import com.example.lifemaster_xml.total.detox.adapter.DetoxServiceSettingAdapter
import com.example.lifemaster_xml.total.detox.viewmodel.DetoxRepeatLockViewModel

class DetoxRepeatLockBlockServiceDialog: DialogFragment(R.layout.dialog_detox_repeat_lock_block_service) {
    private lateinit var binding: DialogDetoxRepeatLockBlockServiceBinding
    private val viewModel: DetoxRepeatLockViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxRepeatLockBlockServiceBinding.bind(view)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        binding.recyclerview.layoutManager = GridLayoutManager(context, 5)
        binding.recyclerview.adapter = DetoxServiceSettingAdapter(viewModel.blockServiceApplications)
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnApply.setOnClickListener {
            val allowServices = viewModel.blockServiceApplications.filter { app -> app.isClicked }
            viewModel.updateblockServices(allowServices)
            dismiss()
        }
    }

    companion object {
        const val TAG = "DetoxRepeatLockBlockServiceDialog"
    }
}