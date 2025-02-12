package com.example.lifemaster.presentation.group.fragment.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogAlarmRandomMissionBinding

class AlarmRandomMissionDialog: DialogFragment(R.layout.dialog_alarm_random_mission) {

    private lateinit var binding: DialogAlarmRandomMissionBinding
    private val randomMissionType by lazy { listOf(binding.cvMath, binding.cvClick, binding.cvWrite) }
    private val randomMissionLevel by lazy { listOf(binding.cvLevelHigh, binding.cvLevelMedium, binding.cvLevelLow)}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogAlarmRandomMissionBinding.bind(view)
        initViews()
        initListeners()
    }

    private fun initViews() {
        with(binding) {
            cvLevel.visibility = View.GONE // default
            cvLevelMedium.isSelected = true // default
        }
    }

    private fun initListeners() {
        with(binding) {
            randomMissionType.forEach { type ->
                type.setOnClickListener {
                    it.isSelected = !it.isSelected
                    if (cvMath.isSelected) cvLevel.visibility = View.VISIBLE else cvLevel.visibility = View.GONE
                }
            }
            randomMissionLevel.forEach { level ->
                level.setOnClickListener {
                    if(it.isSelected == false) it.isSelected = true
                    if(it.isSelected) {
                        randomMissionLevel.filterNot { it == level }.forEach { otherLevel ->
                            if(otherLevel.isSelected) otherLevel.isSelected = false
                        }
                    }
                }
            }
            btnApply.setOnClickListener { dismiss() }
        }
    }

    companion object {
        const val TAG = "AlarmRandomMissionDialog"
    }

}