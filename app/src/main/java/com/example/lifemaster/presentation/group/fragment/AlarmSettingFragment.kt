package com.example.lifemaster.presentation.group.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmSettingBinding
import com.example.lifemaster.presentation.common.SelectTimeDialog
import com.example.lifemaster.presentation.group.model.AlarmItem
import com.example.lifemaster.presentation.group.viewmodel.AlarmViewModel

class AlarmSettingFragment : Fragment(R.layout.fragment_alarm_setting) {

    private lateinit var binding: FragmentAlarmSettingBinding
    private val alarmViewModel: AlarmViewModel by activityViewModels()
    private var isDelaySet: Boolean = false

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
                isDelaySet = true
            } else {
                binding.llDelayStatusOn.visibility = View.GONE
                isDelaySet = false
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            ivBack.setOnClickListener {
                findNavController().navigate(R.id.action_alarmSettingFragment_to_alarmListFragment)
            }
            llDelayStatusOn.setOnClickListener {
                val dialog = SelectTimeDialog("delay")
                dialog.isCancelable = false
                dialog.show(childFragmentManager, SelectTimeDialog.TAG)
            }

            btnSave.setOnClickListener {
                val alarmTitle = etAlarmName.text.toString() // 알람 이름
                val amPm = if(timePicker.hour in 0.. 11) "AM" else "PM"
                val hour = if(timePicker.hour in 13..23) timePicker.hour-12 else timePicker.hour
                val alarmTime = Triple<String, Int, Int>(
                    amPm,
                    hour,
                    timePicker.minute
                )

                if(alarmTitle.isBlank()) {
                    Toast.makeText(requireContext(), "제목을 입력 해주세요!", Toast.LENGTH_SHORT).show()
                } else if(isDelaySet && tvDelayCounts.text.isNotBlank()) {
                    val alarmItem = AlarmItem(
                        id = alarmViewModel.alarmItems.value?.size ?: 0,
                        title = alarmTitle,
                        time = alarmTime,
                        isDelaySet = true,
                        delayMinute = tvDelayMinutes.text.toString().toInt(),
                        delayCount = tvDelayCounts.text.toString().toInt()
                    )
                    alarmViewModel.updateAlarmItems(alarmItem)
                    Toast.makeText(requireContext(), "알람이 추가되었습니다!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_alarmSettingFragment_to_alarmListFragment)
                } else if(isDelaySet && tvDelayCounts.text.isBlank()) {
                    Toast.makeText(requireContext(), "미루기 설정을 해주세요!", Toast.LENGTH_SHORT).show()
                } else {
                    val alarmItem = AlarmItem(
                        id = alarmViewModel.alarmItems.value?.size ?: 0,
                        title = alarmTitle,
                        time = alarmTime,
                        isDelaySet = false
                    )
                    alarmViewModel.updateAlarmItems(alarmItem)
                    Toast.makeText(requireContext(), "알람이 추가되었습니다!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_alarmSettingFragment_to_alarmListFragment)
                }
            }

        }
    }

    private fun setupObservers() {
        alarmViewModel.delayMinutesAndCount.observe(viewLifecycleOwner) {
            binding.tvDelayMinutes.text = "${it.first}"
            binding.tvDelayCounts.text = "${it.second}"
        }
    }

}