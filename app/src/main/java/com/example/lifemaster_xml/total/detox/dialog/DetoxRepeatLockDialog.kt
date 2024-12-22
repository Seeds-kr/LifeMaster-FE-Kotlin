package com.example.lifemaster_xml.total.detox.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.databinding.DialogDitoxRepeatLockBinding
import com.example.lifemaster_xml.dialogFragmentResize

class DetoxRepeatLockDialog: DialogFragment(R.layout.dialog_ditox_repeat_lock) {

    private lateinit var binding: DialogDitoxRepeatLockBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDitoxRepeatLockBinding.bind(view)
        setupListeners()
    }

    private fun setupListeners() {
        binding.tvSelectTargetApp.setOnClickListener {
            val dialog = DetoxRepeatLockTargetAppDialog()
            dialog.isCancelable = false
            dialog.show(childFragmentManager, DetoxRepeatLockTargetAppDialog.TAG)
        }
        binding.btnDelete.setOnClickListener {
            dismiss()
        }
        binding.btnSave.setOnClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        context?.dialogFragmentResize(this@DetoxRepeatLockDialog, 0.9f, 0.9f)
    }

    companion object {
        const val TAG = "DetoxRepeatLockDialog"
    }
}