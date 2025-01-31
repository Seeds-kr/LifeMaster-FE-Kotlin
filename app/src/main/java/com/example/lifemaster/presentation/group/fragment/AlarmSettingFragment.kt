package com.example.lifemaster.presentation.group.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmSettingBinding
import com.example.lifemaster.presentation.common.SelectTimeDialog
import com.example.lifemaster.presentation.group.viewmodel.AlarmViewModel

class AlarmSettingFragment : Fragment(R.layout.fragment_alarm_setting) {

    private lateinit var binding: FragmentAlarmSettingBinding
    private val alarmViewModel: AlarmViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmSettingBinding.bind(view)
        setupViews()
        setupListeners()
        setupObservers()
    }

    private fun setupViews() {
        binding.layoutSwitch.widget.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                binding.llDelayStatusOn.visibility = View.VISIBLE
            } else {
                binding.llDelayStatusOn.visibility = View.GONE
            }
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().navigate(R.id.action_alarmSettingFragment_to_alarmListFragment)
        }
        binding.llDelayStatusOn.setOnClickListener {
            val dialog = SelectTimeDialog("delay")
            dialog.isCancelable = false
            dialog.show(childFragmentManager, SelectTimeDialog.TAG)
        }
        binding.btnSave.setOnClickListener {
            val alarmTitle = binding.etAlarmName.text.toString()

        }
    }

    private fun setupObservers() {
        alarmViewModel.delayMinutesAndCount.observe(viewLifecycleOwner) {
            binding.tvDelayMinutes.text = "${it.first}"
            binding.tvDelayCounts.text = "${it.second}"
        }
    }

}