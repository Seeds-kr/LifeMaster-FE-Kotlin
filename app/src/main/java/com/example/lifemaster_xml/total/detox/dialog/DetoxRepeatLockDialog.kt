package com.example.lifemaster_xml.total.detox.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.databinding.DialogDetoxRepeatLockBinding

class DetoxRepeatLockDialog: DialogFragment(R.layout.dialog_detox_repeat_lock) {

    private lateinit var binding: DialogDetoxRepeatLockBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxRepeatLockBinding.bind(view)
        setupListeners()
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
            val dialog = DetoxRepeatLockTargetAppDialog()
            dialog.isCancelable = false
            dialog.show(parentFragmentManager, DetoxRepeatLockTargetAppDialog.TAG)
        }
        binding.btnDelete.setOnClickListener {
            dismiss()
        }
        binding.btnSave.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        const val TAG = "DetoxRepeatLockDialog"
    }
}