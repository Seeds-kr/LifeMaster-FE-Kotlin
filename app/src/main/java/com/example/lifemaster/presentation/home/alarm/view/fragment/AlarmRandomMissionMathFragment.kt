package com.example.lifemaster.presentation.home.alarm.view.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmRandomMissionMathBinding

class AlarmRandomMissionMathFragment : Fragment(R.layout.fragment_alarm_random_mission_math) {

    private lateinit var binding: FragmentAlarmRandomMissionMathBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmRandomMissionMathBinding.bind(view)
    }

}