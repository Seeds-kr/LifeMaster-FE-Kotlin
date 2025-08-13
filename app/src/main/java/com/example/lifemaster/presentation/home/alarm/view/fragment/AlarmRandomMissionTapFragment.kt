package com.example.lifemaster.presentation.home.alarm.view.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmRandomMissionTapBinding
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


class AlarmRandomMissionTapFragment : Fragment(R.layout.fragment_alarm_random_mission_tap) {

    private lateinit var binding: FragmentAlarmRandomMissionTapBinding
    private lateinit var taps: List<MaterialCardView>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmRandomMissionTapBinding.bind(view)
        initViews()
        initListeners()
    }

    private fun initViews() = with(binding) {
        taps = listOf(
            cvAlarmRandomMissionTap1,
            cvAlarmRandomMissionTap2,
            cvAlarmRandomMissionTap3,
            cvAlarmRandomMissionTap4,
            cvAlarmRandomMissionTap5,
            cvAlarmRandomMissionTap6,
            cvAlarmRandomMissionTap7,
            cvAlarmRandomMissionTap8,
            cvAlarmRandomMissionTap9,
            cvAlarmRandomMissionTap10,
            cvAlarmRandomMissionTap11,
            cvAlarmRandomMissionTap12,
            cvAlarmRandomMissionTap13,
            cvAlarmRandomMissionTap14,
            cvAlarmRandomMissionTap15,
            cvAlarmRandomMissionTap16,
            cvAlarmRandomMissionTap17,
            cvAlarmRandomMissionTap18,
            cvAlarmRandomMissionTap19,
            cvAlarmRandomMissionTap20,
            cvAlarmRandomMissionTap21,
            cvAlarmRandomMissionTap22,
            cvAlarmRandomMissionTap23,
            cvAlarmRandomMissionTap24,
            cvAlarmRandomMissionTap25
        )
        lifecycleScope.launch {
            repeat(10) {
                val i = Random.nextInt(0, 25) // 0 ~ 24 (중복 허용)
                taps[i].apply {
                    isSelected = true
                    setCardBackgroundColor(
                        resources.getColor(
                            R.color.alarm_primary,
                            context?.theme
                        )
                    )
                }
            }
            delay(1000)
            tvAlarmRandomMissionTapCount.text = "2"
            delay(1000)
            tvAlarmRandomMissionTapCount.text = "1"
            delay(1000)
            tvAlarmRandomMissionTapCount.isVisible = false
            for (tap in taps) {
                tap.isSelected = false
                tap.setCardBackgroundColor(
                    resources.getColor(
                        R.color.light_gray_100,
                        context?.theme
                    )
                )
            }
        }
    }

    private fun initListeners() = with(binding) {
        for (tap in taps) {
            tap.setOnClickListener {
                tap.isSelected = !tap.isSelected
                if (tap.isSelected) {
                    tap.setCardBackgroundColor(
                        resources.getColor(
                            R.color.alarm_primary,
                            context?.theme
                        )
                    )
                } else {
                    tap.setCardBackgroundColor(
                        resources.getColor(
                            R.color.light_gray_100,
                            context?.theme
                        )
                    )
                }
            }
        }
    }
}