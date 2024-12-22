package com.example.lifemaster_xml.total.detox.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.databinding.DialogDetoxRepeatLockTargetAppBinding
import com.example.lifemaster_xml.dialogFragmentResize

class DetoxRepeatLockTargetAppDialog: DialogFragment(R.layout.dialog_detox_repeat_lock_target_app) {

    private lateinit var binding: DialogDetoxRepeatLockTargetAppBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxRepeatLockTargetAppBinding.bind(view)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
            showParentDialog()
        }
        binding.btnApply.setOnClickListener {
            dismiss()
            showParentDialog()
        }
    }

    private fun showParentDialog() {
        val dialog = DetoxRepeatLockDialog()
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, DetoxRepeatLockDialog.TAG)
    }

    companion object {
        const val TAG = "DetoxRepeatLockTargetAppDialog"
    }
}