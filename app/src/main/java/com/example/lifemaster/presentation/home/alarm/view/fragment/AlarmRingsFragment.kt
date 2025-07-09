package com.example.lifemaster.presentation.home.alarm.view.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmRingBinding

class AlarmRingsFragment : Fragment(R.layout.fragment_alarm_ring) {

    lateinit var binding: FragmentAlarmRingBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmRingBinding.bind(view)
    }
}