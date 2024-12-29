package com.example.lifemaster_xml.total.detox.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.databinding.DialogDetoxRepeatLockAllowServiceBinding
import com.example.lifemaster_xml.total.detox.adapter.DetoxAllowServiceSettingAdapter
import com.example.lifemaster_xml.total.detox.viewmodel.DetoxViewModel

class DetoxRepeatLockAllowServiceDialog: DialogFragment(R.layout.dialog_detox_repeat_lock_allow_service) {

    private lateinit var binding: DialogDetoxRepeatLockAllowServiceBinding
    private val viewModel: DetoxViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxRepeatLockAllowServiceBinding.bind(view)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        binding.recyclerview.layoutManager = GridLayoutManager(context, 5)
        binding.recyclerview.adapter = DetoxAllowServiceSettingAdapter(viewModel.tempAppLogos)
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnApply.setOnClickListener {
            val allowServices = viewModel.tempAppLogos.filter { app -> app.isClicked }
            viewModel.updateAllowServices(allowServices)
            dismiss()
        }
    }

    companion object {
        const val TAG = "DetoxRepeatLockBlockServiceDialog"
    }
}