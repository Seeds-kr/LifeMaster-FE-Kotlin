package com.example.lifemaster.presentation.group.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmListBinding
import com.example.lifemaster.presentation.group.adapter.AlarmAdapter
import com.example.lifemaster.presentation.group.viewmodel.AlarmViewModel

class AlarmListFragment : Fragment(R.layout.fragment_alarm_list) {

    private lateinit var binding: FragmentAlarmListBinding
    private val alarmViewModel: AlarmViewModel by activityViewModels()
    private val alarmAdapter = AlarmAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmListBinding.bind(view)
        setupViews()
        setupListeners()
        setupObservers()
    }

    private fun setupViews() {
        with(binding) {
            recyclerview.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL)) // 구분선 넣기
            recyclerview.adapter = alarmAdapter
        }
    }

    private fun setupListeners() {
        with(binding) {
            tvAddAlarmItem.setOnClickListener {
                findNavController().navigate(R.id.action_alarmListFragment_to_alarmSettingFragment)
            }
        }
    }

    private fun setupObservers() {
        alarmViewModel.alarmItems.observe(viewLifecycleOwner) { items ->
            if(binding.llNoAlarmItem.visibility == View.VISIBLE) { // 리팩토링하기
                binding.llNoAlarmItem.visibility = View.GONE
                binding.recyclerview.visibility = View.VISIBLE
            }
            alarmAdapter.submitList(items.toMutableList())
        }
    }
}