package com.example.lifemaster_xml.total.detox.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.databinding.DialogDetoxRepeatLockAllowServiceBinding
import com.example.lifemaster_xml.total.detox.adapter.DetoxRepeatLockTargetAppAdapter
import com.example.lifemaster_xml.total.detox.model.DetoxTargetApp

class DetoxRepeatLockAllowServiceDialog: DialogFragment(R.layout.dialog_detox_repeat_lock_allow_service) {

    private lateinit var binding: DialogDetoxRepeatLockAllowServiceBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxRepeatLockAllowServiceBinding.bind(view)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        val tempAppLogos = arrayListOf(
            DetoxTargetApp(R.drawable.ic_insta),
            DetoxTargetApp(R.drawable.ic_kakaotalk),
            DetoxTargetApp(R.drawable.ic_twitter),
            DetoxTargetApp(R.drawable.ic_line),
            DetoxTargetApp(R.drawable.ic_netflix),
            DetoxTargetApp(R.drawable.ic_spotify),
            DetoxTargetApp(R.drawable.ic_ticktock),
            DetoxTargetApp(R.drawable.ic_spotify),
            DetoxTargetApp(R.drawable.ic_ticktock),
            DetoxTargetApp(R.drawable.ic_line),
            DetoxTargetApp(R.drawable.ic_netflix),
            DetoxTargetApp(R.drawable.ic_spotify),
            DetoxTargetApp(R.drawable.ic_line),
            DetoxTargetApp(R.drawable.ic_netflix),
            DetoxTargetApp(R.drawable.ic_spotify),
            DetoxTargetApp(R.drawable.ic_line),
            DetoxTargetApp(R.drawable.ic_netflix),
            DetoxTargetApp(R.drawable.ic_spotify),
            DetoxTargetApp(R.drawable.ic_insta),
            DetoxTargetApp(R.drawable.ic_kakaotalk),
            DetoxTargetApp(R.drawable.ic_twitter),
            DetoxTargetApp(R.drawable.ic_line),
            DetoxTargetApp(R.drawable.ic_netflix),
            DetoxTargetApp(R.drawable.ic_spotify),
            DetoxTargetApp(R.drawable.ic_ticktock),
            DetoxTargetApp(R.drawable.ic_spotify),
            DetoxTargetApp(R.drawable.ic_ticktock),
            DetoxTargetApp(R.drawable.ic_line),
            DetoxTargetApp(R.drawable.ic_netflix),
            DetoxTargetApp(R.drawable.ic_spotify),
            DetoxTargetApp(R.drawable.ic_line),
            DetoxTargetApp(R.drawable.ic_netflix),
            DetoxTargetApp(R.drawable.ic_spotify),
            DetoxTargetApp(R.drawable.ic_line),
            DetoxTargetApp(R.drawable.ic_netflix),
            DetoxTargetApp(R.drawable.ic_spotify),
            DetoxTargetApp(R.drawable.ic_spotify),
            DetoxTargetApp(R.drawable.ic_line),
            DetoxTargetApp(R.drawable.ic_netflix),
            DetoxTargetApp(R.drawable.ic_spotify)
        )
        binding.recyclerview.layoutManager = GridLayoutManager(context, 5)
        binding.recyclerview.adapter = DetoxRepeatLockTargetAppAdapter(tempAppLogos)
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnApply.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        const val TAG = "DetoxRepeatLockBlockServiceDialog"
    }
}