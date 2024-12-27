package com.example.lifemaster_xml.total.detox.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.databinding.DialogDetoxRepeatLockTargetAppBinding
import com.example.lifemaster_xml.total.detox.adapter.DetoxRepeatLockTargetAppAdapter
import com.example.lifemaster_xml.total.detox.model.DetoxTargetApp

class DetoxRepeatLockTargetAppDialog: DialogFragment(R.layout.dialog_detox_repeat_lock_target_app) {

    private lateinit var binding: DialogDetoxRepeatLockTargetAppBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxRepeatLockTargetAppBinding.bind(view)
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