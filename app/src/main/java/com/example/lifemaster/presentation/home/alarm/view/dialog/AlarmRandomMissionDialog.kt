package com.example.lifemaster.presentation.home.alarm.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogAlarmRandomMissionBinding
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.get
import androidx.core.view.isVisible
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.FOLLOW_CLICK
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.HIGH
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.LOW
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.MATH_PROBLEM
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.TYPING_SENTENCE
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
    private val mathRandomMissionLevel by lazy {
        listOf(
            binding.cvMathLevelHigh,
            binding.cvMathLevelMedium,
            binding.cvMathLevelLow
        )
    }
    private val clickRandomMissionLevel by lazy {
        listOf(
            binding.cvClickLevelHigh,
            binding.cvClickLevelMedium,
            binding.cvClickLevelLow
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
        cvMathLevelMedium.isSelected = true // 수학 난이도 기본 값 - 중
        cvClickLevelMedium.isSelected = true // 따라 누르기 기본 값 - 중
    }

    private fun initListeners() = with(binding) {
        randomMissionType.forEach { randomMission ->
            randomMission.setOnClickListener {
                val previousRandomMission = randomMissionType.find { it.isSelected == true }
                previousRandomMission?.let { it.isSelected = false }
                it.isSelected = true
                cvMathLevel.isVisible = cvMath.isSelected
                cvClickLevel.isVisible = cvClick.isSelected
            }
        }
        mathRandomMissionLevel.forEach { level ->
            level.setOnClickListener {
                val previousLevel = mathRandomMissionLevel.find { it.isSelected == true }
                previousLevel?.isSelected = false
                it.isSelected = true
            }
        }
        clickRandomMissionLevel.forEach { level ->
            level.setOnClickListener {
                val previousLevel = clickRandomMissionLevel.find { it.isSelected == true }
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
            when (mission) {
                is Map<*, *> -> {
                    when (mission.entries.first().key) {
                        MATH_PROBLEM -> {
                            cvMath.isSelected = true
                            cvMathLevel.isVisible = true
                            when (mission.entries.first().value) {
                                HIGH -> {
                                    cvMathLevelMedium.isSelected = false
                                    cvMathLevelHigh.isSelected = true
                                }
                                LOW -> {
                                    cvMathLevelMedium.isSelected = false
                                    cvMathLevelLow.isSelected = true
                                }
                            }
                        }
                        FOLLOW_CLICK -> {
                            cvClick.isSelected = true
                            cvClickLevel.isVisible = true
                            when (mission.entries.first().value) {
                                HIGH -> {
                                    cvClickLevelMedium.isSelected = false
                                    cvClickLevelHigh.isSelected = true
                                }
                                LOW -> {
                                    cvClickLevelMedium.isSelected = false
                                    cvClickLevelLow.isSelected = true
                                }
                            }
                        }
                    }
                }
                is String -> cvWrite.isSelected = true
            }
        }
    }


    // 타입이 Any인 이유: Map, String 타입 중 무엇이 저장될 지 알 수 없어서
    private fun getSelectedMissions(): Any? = with(binding) {
        return if (cvMath.isSelected) {
            val selectedLevelText =
                (mathRandomMissionLevel.single { it.isSelected }[0] as TextView).text // "상", "중", "하"
            mapOf(MATH_PROBLEM to selectedLevelText) // ex) {수학 문제 풀기 = 상}
        } else if (cvClick.isSelected) {
            val selectedLevelText =
                (clickRandomMissionLevel.single { it.isSelected }[0] as TextView).text
            mapOf(FOLLOW_CLICK to selectedLevelText) // ex) {따라 누르기 = 중}
        } else if (cvWrite.isSelected) {
            TYPING_SENTENCE // ex) "글 따라쓰기"
        } else {
            null
        }
    }

    companion object {
        const val TAG = "AlarmRandomMissionDialog"
    }

}