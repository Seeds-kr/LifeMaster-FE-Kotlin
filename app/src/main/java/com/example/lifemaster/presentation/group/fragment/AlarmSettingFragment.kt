package com.example.lifemaster.presentation.group.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmSettingBinding

class AlarmSettingFragment : Fragment(R.layout.fragment_alarm_setting) {

    private lateinit var binding: FragmentAlarmSettingBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmSettingBinding.bind(view)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {

    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().navigate(R.id.action_alarmSettingFragment_to_alarmListFragment)
        }
    }

}