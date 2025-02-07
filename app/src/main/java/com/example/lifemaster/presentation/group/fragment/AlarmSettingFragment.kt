package com.example.lifemaster.presentation.group.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmSettingBinding
import com.example.lifemaster.presentation.common.SelectTimeDialog
import com.example.lifemaster.presentation.common.setAlarm
import com.example.lifemaster.presentation.group.model.AlarmItem
import com.example.lifemaster.presentation.group.viewmodel.AlarmViewModel
import java.util.Calendar

class AlarmSettingFragment : Fragment(R.layout.fragment_alarm_setting) {

    private lateinit var binding: FragmentAlarmSettingBinding
    private val alarmViewModel: AlarmViewModel by activityViewModels()
    private var isDelaySet: Boolean = false

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmSettingBinding.bind(view)
        setupViews()
        setupListeners()
        setupObservers()
    }

    private fun setupViews() {
        binding.apply {
            layoutSwitch.widget.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    llDelayStatusOn.visibility = View.VISIBLE
                    isDelaySet = true
                } else {
                    llDelayStatusOn.visibility = View.GONE
                    isDelaySet = false
                }
            }
            initRepeatDayView()
        }
    }

    private fun initRepeatDayView() {
        val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")
        val dayLayouts = with(binding) {
            listOf(
                layoutMonday,
                layoutTuesday,
                layoutWednesday,
                layoutThursday,
                layoutFriday,
                layoutSaturday,
                layoutSunday
            )
        } // scope function 의 차이점을 분명하게 이해할 수 있는 코드 -> run, with 만 가능
        dayLayouts.zip(dayLabels).forEach { it.first.tvDayType.text = it.second }
    }

    @RequiresApi(Build.VERSION_CODES.S)
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
                val amPm = if (timePicker.hour in 0..11) "AM" else "PM"
                val alarmTime = Triple<String, Int, Int>(
                    amPm,
                    timePicker.hour,
                    timePicker.minute
                )

                if (alarmTitle.isBlank()) {
                    Toast.makeText(requireContext(), "제목을 입력 해주세요!", Toast.LENGTH_SHORT).show()
                } else if (isDelaySet && tvDelayCounts.text.isNotBlank()) {
                    val alarmItem = AlarmItem(
                        id = alarmViewModel.alarmItems.value?.size ?: 0,
                        title = alarmTitle,
                        time = alarmTime,
                        isDelaySet = true,
                        delayMinute = tvDelayMinutes.text.toString().toInt(),
                        delayCount = tvDelayCounts.text.toString().toInt()
                    )
                    alarmViewModel.updateAlarmItems(alarmItem)
                    setAlarm(requireContext(), convertTimeToMillis(alarmTime))
                    Toast.makeText(requireContext(), "알람이 추가되었습니다!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_alarmSettingFragment_to_alarmListFragment)
                } else if (isDelaySet && tvDelayCounts.text.isBlank()) {
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

fun convertTimeToMillis(alarmTime: Triple<String, Int, Int>): Long {
    val (_, hour, minute) = alarmTime
    val calender = Calendar.getInstance()
    calender.set(Calendar.HOUR_OF_DAY, hour)
    calender.set(Calendar.MINUTE, minute)
    calender.set(Calendar.SECOND, 0)
    calender.set(Calendar.MILLISECOND, 0)

    if (calender.timeInMillis < System.currentTimeMillis()) {
        calender.add(Calendar.DAY_OF_MONTH, 1) // 시간이 이미 지난 경우, 다음날에 알람이 울리도록 해줌
    }

    return calender.timeInMillis
}