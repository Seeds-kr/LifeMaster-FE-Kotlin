package com.example.lifemaster_xml.total.detox.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.databinding.DialogDetoxTimeLockBinding

class DetoxTimeLockDialog: DialogFragment(R.layout.dialog_detox_time_lock) {

    private lateinit var binding: DialogDetoxTimeLockBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxTimeLockBinding.bind(view)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnAdd.setOnClickListener {
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        const val TAG = "DetoxTimeLockDialog"
    }
}