package com.example.lifemaster.presentation.group.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmListBinding

class AlarmListFragment : Fragment(R.layout.fragment_alarm_list) {
    private lateinit var binding: FragmentAlarmListBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmListBinding.bind(view)
        setupViews()
        setupListeners()
        setupObservers()
    }

    private fun setupViews() {
        binding.apply {
            recyclerview.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL)) // 구분선 넣기
        }
    }

    private fun setupListeners() {
        binding.apply {
            tvAddAlarmItem.setOnClickListener {
                findNavController().navigate(R.id.action_alarmListFragment_to_alarmSettingFragment)
            }
        }
    }

    private fun setupObservers() {

    }
}