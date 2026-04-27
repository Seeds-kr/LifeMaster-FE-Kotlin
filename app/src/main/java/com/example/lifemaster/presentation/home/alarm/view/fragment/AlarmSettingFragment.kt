package com.example.lifemaster.presentation.home.alarm.view.fragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmSettingBinding
import com.example.lifemaster.presentation.home.alarm.model.AlarmModel
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionType
import com.example.lifemaster.presentation.home.alarm.view.dialog.AlarmRandomMissionDialog
import com.example.lifemaster.presentation.home.alarm.view.receiver.AlarmReceiver
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModel
import com.example.lifemaster.presentation.total.detox.dialog.SelectTimeDialog
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@AndroidEntryPoint
class AlarmSettingFragment : Fragment(R.layout.fragment_alarm_setting) {

    private lateinit var binding: FragmentAlarmSettingBinding
    private val alarmViewModel: AlarmViewModel by activityViewModels()
    private var randomMissionList = arrayListOf<RandomMissionType>()
//    private var isDelaySet: Boolean = false
    private var alarmRepeatDays = arrayListOf<String>()
    private val dayLayouts by lazy {
        listOf(
            binding.layoutMonday,
            binding.layoutTuesday,
            binding.layoutWednesday,
            binding.layoutThursday,
            binding.layoutFriday,
            binding.layoutSaturday,
            binding.layoutSunday
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmSettingBinding.bind(view)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        binding.apply {
//            layoutSwitch.widget.setOnCheckedChangeListener { _, isChecked ->
//                if (isChecked) {
//                    llDelayStatusOn.visibility = View.VISIBLE
//                    isDelaySet = true
//                } else {
//                    llDelayStatusOn.visibility = View.GONE
//                    isDelaySet = false
//                }
//            }

            val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")
            dayLayouts.zip(dayLabels).forEach { it.first.tvDayType.text = it.second }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun initListeners() {
        with(binding) {
            ivBack.setOnClickListener {
                findNavController().navigate(R.id.alarmListFragment)
            }
            ivRandomMission.setOnClickListener {
                val dialog = AlarmRandomMissionDialog()
                dialog.isCancelable = true
                dialog.show(childFragmentManager, AlarmRandomMissionDialog.TAG)
            }
            dayLayouts.forEach { eachDay ->
                eachDay.cardview.setOnClickListener {
                    it.isSelected = !it.isSelected
                    if (it.isSelected) alarmRepeatDays.add(eachDay.tvDayType.text.toString()) else alarmRepeatDays.remove(
                        eachDay.tvDayType.text.toString()
                    )
                }
            }
            llDelayStatusOn.setOnClickListener {
                val dialog = SelectTimeDialog("delay")
                dialog.isCancelable = false
                dialog.show(childFragmentManager, SelectTimeDialog.TAG)
            }

            btnSave.setOnClickListener {

                if (etAlarmTitle.text.toString().isBlank()) {
                    Toast.makeText(requireContext(), "제목을 입력 해주세요!", Toast.LENGTH_SHORT).show()
                } else {

                    val alarmItem = AlarmModel(
                        id = alarmViewModel.alarmItems.value?.size ?: 0,
                        alarmTitle = etAlarmTitle.text.toString(),
                        alarmTime = LocalDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(timePicker.hour, timePicker.minute)
                        ).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        alarmMon = alarmRepeatDays.contains("월"),
                        alarmTue = alarmRepeatDays.contains("화"),
                        alarmWed = alarmRepeatDays.contains("수"),
                        alarmThu = alarmRepeatDays.contains("목"),
                        alarmFri = alarmRepeatDays.contains("금"),
                        alarmSat = alarmRepeatDays.contains("토"),
                        alarmSun = alarmRepeatDays.contains("일"),
                        snoozed = false,
                        antiSnoozed = false,
                        randomMissionType = randomMissionList.firstOrNull()
                    )
                    alarmViewModel.updateAlarmItems(alarmItem)
                    findNavController().navigate(R.id.alarmListFragment)
                }
            }
        }
    }

    private fun initObservers() {
        // This legacy fragment is no longer the primary alarm creation screen.
    }
}
