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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.FOLLOW_CLICK
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.HIGH
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.MEDIUM
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.LOW
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.MATH_PROBLEM
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.TYPING_SENTENCE
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionLevel
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionType
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmGenerateViewModel
import kotlinx.coroutines.launch

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
    private val randomMissionLevelMapper = mapOf(
        HIGH to RandomMissionLevel.HIGH,
        MEDIUM to RandomMissionLevel.MEDIUM,
        LOW to RandomMissionLevel.LOW
    )

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
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                alarmGenerateViewModel.randomMission.collect { mission ->
                    if(mission != null) {
                        val missionType = mission.entries.first().key
                        val missionLevel = mission.entries.first().value
                        when (missionType) {
                            RandomMissionType.MATH_PROBLEM -> {
                                cvMath.isSelected = true
                                cvMathLevel.isVisible = true
                                when (missionLevel) {
                                    RandomMissionLevel.HIGH -> {
                                        cvMathLevelMedium.isSelected = false
                                        cvMathLevelHigh.isSelected = true
                                    }
                                    RandomMissionLevel.LOW -> {
                                        cvMathLevelMedium.isSelected = false
                                        cvMathLevelLow.isSelected = true
                                    }
                                    else -> { /* initView에서 기본값 처리 */ }
                                }
                            }
                            RandomMissionType.FOLLOW_CLICK -> {
                                cvClick.isSelected = true
                                cvClickLevel.isVisible = true
                                when (missionLevel) {
                                    RandomMissionLevel.HIGH -> {
                                        cvClickLevelMedium.isSelected = false
                                        cvClickLevelHigh.isSelected = true
                                    }
                                    RandomMissionLevel.LOW -> {
                                        cvClickLevelMedium.isSelected = false
                                        cvClickLevelLow.isSelected = true
                                    }
                                    else -> { /* initView에서 기본값 처리 */ }
                                }
                            }
                            RandomMissionType.TYPING_SENTENCE -> cvWrite.isSelected = true
                        }
                    }
                }
            }
        }
    }

    private fun getSelectedMissions(): Map<RandomMissionType, RandomMissionLevel?>? = with(binding) {
        return if (cvMath.isSelected) {
            val selectedLevelText = (mathRandomMissionLevel.single { it.isSelected }[0] as TextView).text.toString()
            val randomMissionLevel = randomMissionLevelMapper[selectedLevelText]
            mapOf(RandomMissionType.MATH_PROBLEM to randomMissionLevel)
        } else if (cvClick.isSelected) {
            val selectedLevelText =
                (clickRandomMissionLevel.single { it.isSelected }[0] as TextView).text.toString()
            val randomMissionLevel = randomMissionLevelMapper[selectedLevelText]
            mapOf(RandomMissionType.FOLLOW_CLICK to randomMissionLevel)
        } else if (cvWrite.isSelected) {
            mapOf(RandomMissionType.TYPING_SENTENCE to null)
        } else {
            null
        }
    }

    companion object {
        const val TAG = "AlarmRandomMissionDialog"
    }

}