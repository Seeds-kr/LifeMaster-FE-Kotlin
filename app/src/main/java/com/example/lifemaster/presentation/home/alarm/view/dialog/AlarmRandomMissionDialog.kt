package com.example.lifemaster.presentation.home.alarm.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogAlarmRandomMissionBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModel
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModelFactory
import com.google.android.material.card.MaterialCardView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmGenerateViewModel

class AlarmRandomMissionDialog : DialogFragment(R.layout.dialog_alarm_random_mission) {

    private lateinit var binding: DialogAlarmRandomMissionBinding
    private val alarmGenerateViewModel: AlarmGenerateViewModel by activityViewModels()
    private val randomMissionType by lazy {
        listOf(
            binding.cvMath,
            binding.cvClick,
            binding.cvWrite
        )
    }
    private val randomMissionLevel by lazy {
        listOf(
            binding.cvLevelHigh,
            binding.cvLevelMedium,
            binding.cvLevelLow
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogAlarmRandomMissionBinding.bind(view)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() = with(binding) {
        cvLevelMedium.isSelected = true // 수학 난이도 기본 값 - 중
    }

    private fun initListeners() = with(binding) {
        randomMissionType.forEach { randomMission ->
            randomMission.setOnClickListener {
                val previousRandomMission = randomMissionType.find { it.isSelected == true }
                previousRandomMission?.let { it.isSelected = false }
                it.isSelected = true
                cvMathLevel.isVisible = cvMath.isSelected
            }
        }
        randomMissionLevel.forEach { level ->
            level.setOnClickListener {
                val previousLevel = randomMissionLevel.find { it.isSelected == true }
                previousLevel?.isSelected = false
                it.isSelected = true
            }
        }
        btnApply.setOnClickListener {
            val selectedRandomMission = getSelectedMissions()
            selectedRandomMission?.let {
                alarmGenerateViewModel.setRandomMission(it)
                dismiss()
            } ?: Toast.makeText(context, "미션을 설정해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initObservers() = with(binding) {
        alarmGenerateViewModel.randomMission.observe(viewLifecycleOwner) { mission ->
            when(mission) {
                is Map<*, *> -> {
                    cvMath.isSelected = true
                    cvMathLevel.isVisible = true
                    when(mission.entries.first().value) {
                        tvLevelHigh.text -> {
                            cvLevelMedium.isSelected = false
                            cvLevelHigh.isSelected = true
                        }
                        tvLevelLow.text -> {
                            cvLevelMedium.isSelected = false
                            cvLevelLow.isSelected = true
                        }
                    }
                }
                is String -> {
                    when(mission) {
                        tvClick.text -> cvClick.isSelected = true
                        tvWrite.text -> cvWrite.isSelected = true
                    }
                }
            }
        }
    }

    // 타입이 Any인 이유: Map, String 타입 중 무엇이 저장될 지 알 수 없어서
    private fun getSelectedMissions(): Any? = with(binding) {
        return if (cvMath.isSelected) {
            val selectedLevel = randomMissionLevel.single { it.isSelected }
            val selectedLevelText = getLevelText(selectedLevel) // ex) "상"
            mapOf(tvMath.text.toString() to selectedLevelText) // ex) {수학 문제 풀기 = 상}
        } else if (cvClick.isSelected) {
            tvClick.text.toString() // ex) "따라 누르기"
        } else if (cvWrite.isSelected) {
            tvWrite.text.toString() // ex) "글 따라쓰기"
        } else {
            null
        }
    }

    private fun getLevelText(selectedLevel: MaterialCardView): String = with(binding) {
        return when (selectedLevel) {
            cvLevelHigh -> tvLevelHigh.text.toString()
            cvLevelMedium -> tvLevelMedium.text.toString()
            cvLevelLow -> tvLevelLow.text.toString()
            else -> ""
        }
    }

    companion object {
        const val TAG = "AlarmRandomMissionDialog"
    }

}