package com.example.lifemaster_xml.total.detox.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.databinding.DialogDitoxRepeatLockBinding

class DetoxRepeatLockDialog: DialogFragment(R.layout.dialog_ditox_repeat_lock) {

    lateinit var binding: DialogDitoxRepeatLockBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDitoxRepeatLockBinding.bind(view)
        setupListeners()
    }

    private fun setupListeners() {
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