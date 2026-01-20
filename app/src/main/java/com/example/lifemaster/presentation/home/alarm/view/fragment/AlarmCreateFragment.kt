package com.example.lifemaster.presentation.home.alarm.view.fragment

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmSettingBinding
import com.example.lifemaster.presentation.home.alarm.model.AlarmRequest
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionLevel
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionType
import com.example.lifemaster.presentation.home.alarm.util.formatAlarmDateLabel
import com.example.lifemaster.presentation.home.alarm.util.formatRemainingTime
import com.example.lifemaster.presentation.home.alarm.util.getRemainingDaysUntilAlarmRings
import com.example.lifemaster.presentation.home.alarm.util.randomMissionLevelMapper
import com.example.lifemaster.presentation.home.alarm.util.randomMissionTypeMapper
import com.example.lifemaster.presentation.home.alarm.view.dialog.AlarmRandomMissionDialog
import com.example.lifemaster.presentation.home.alarm.view.dialog.AlarmSnoozeDialog
import com.example.lifemaster.presentation.home.alarm.view.dialog.AlarmSnoozeLockDialog
import com.example.lifemaster.presentation.home.alarm.view.fragment.AlarmListFragment.Companion.ALARM
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmGenerateViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.getValue

@AndroidEntryPoint
class AlarmCreateFragment : Fragment(R.layout.fragment_alarm_setting) {
    private lateinit var binding: FragmentAlarmSettingBinding
    private val alarmGenerateViewModel: AlarmGenerateViewModel by activityViewModels()
    private val daysOfWeek by lazy {
        listOf(
            binding.cvAlarmRepeatDayMon,
            binding.cvAlarmRepeatDayTue,
            binding.cvAlarmRepeatDayWed,
            binding.cvAlarmRepeatDayThu,
            binding.cvAlarmRepeatDayFri,
            binding.cvAlarmRepeatDaySat,
            binding.cvAlarmRepeatDaySun
        )
    }
    private val selectedDays = mutableSetOf<Int>()
    private var alarmTime: String = ""
    private var randomMissionType: RandomMissionType? = null
    private var randomMissionLevel: RandomMissionLevel? = null
    private lateinit var ringtonePickerLauncher: ActivityResultLauncher<Intent>
    private var alarmSoundUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmSettingBinding.bind(view)
        initStates()
        initViews()
        initListeners()
        initObservers()
    }

    private fun initStates() = with(binding) {
        ringtonePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedUri: Uri? = data?.getParcelableExtra(
                    RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                    Uri::class.java
                )
                val ringtone = RingtoneManager.getRingtone(context, selectedUri)
                val ringtoneTitle = ringtone.getTitle(context)
                tvAlarmSettingMusicTitle.text = ringtoneTitle
                alarmSoundUri = selectedUri
            }
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed() // 뒤로 가기 동작 -> 디스페처에 전달
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun initViews() = with(binding) {
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation).isVisible = false
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun initListeners() = with(binding) {
        ivAlarmSettingBack.setOnClickListener {
            findNavController().popBackStack()
            alarmGenerateViewModel.resetAlarmData()
        }
        ivAlarmSettingRandomMission.setOnClickListener {
            val dialog = AlarmRandomMissionDialog()
            dialog.isCancelable = true
            dialog.show(childFragmentManager, AlarmRandomMissionDialog.TAG)
        }
        daysOfWeek.forEach { dayOfWeek ->
            dayOfWeek.setOnClickListener {
                it.isSelected = !it.isSelected
                val dayInt = it.tag.toString().toIntOrNull() ?: return@setOnClickListener
                if (it.isSelected) {
                    selectedDays.add(dayInt)
                } else {
                    selectedDays.remove(dayInt)
                }
            }
        }
        // 알람 미루기 스위치
        alarmSettingLayoutSwitchSnooze.alarmSwitch.setOnCheckedChangeListener { view, isChecked ->
            llAlarmSettingDelayStatus.isVisible = isChecked
        }

        // 알람 미루기 세부 사항
        llAlarmSettingDelayStatus.setOnClickListener {
            val dialog = AlarmSnoozeDialog()
            dialog.isCancelable = true
            dialog.show(childFragmentManager, AlarmSnoozeDialog.TAG)
        }

        // 다시 잠들기 방지 스위치
        alarmSettingLayoutSwitchAntiSnooze.alarmSwitch.setOnCheckedChangeListener { view, isChecked ->
            llAlarmSettingAntiSnoozeStatus.isVisible = isChecked
        }

        // 다시 잠들기 세부 사항
        llAlarmSettingAntiSnoozeStatus.setOnClickListener {
            val dialog = AlarmSnoozeLockDialog()
            dialog.isCancelable = true
            dialog.show(childFragmentManager, AlarmSnoozeLockDialog.TAG)
        }

        // 사운드 설정
        ivAlarmSettingSound.setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, alarmSoundUri)
            }
            ringtonePickerLauncher.launch(intent)
        }

        btnSave.setOnClickListener {

            // 알람 제목을 무조건 설정하도록 강제
            if (etAlarmSettingTitle.text.isBlank()) {
                Toast.makeText(requireContext(), "알람 제목을 입력 해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 첫 알람이 울리는 시각(alarmTime) 구하기
            if (selectedDays.isEmpty()) {
                // 반복 요일 없음
                val now = LocalTime.now()
                val selected =
                    LocalTime.of(alarmSettingTimePicker.hour, alarmSettingTimePicker.minute)
                alarmTime = if (selected.isAfter(now)) {
                    LocalDate.now()
                        .atTime(alarmSettingTimePicker.hour, alarmSettingTimePicker.minute)
                        .atZone(ZoneId.systemDefault()).toInstant().toString()
                } else {
                    LocalDate.now().plusDays(1)
                        .atTime(alarmSettingTimePicker.hour, alarmSettingTimePicker.minute)
                        .atZone(ZoneId.systemDefault()).toInstant().toString()
                }
            } else {
                // 반복 요일 있음
                val daysUntilAlarm = getRemainingDaysUntilAlarmRings(
                    LocalTime.of(
                        alarmSettingTimePicker.hour,
                        alarmSettingTimePicker.minute
                    ), selectedDays
                )
                alarmTime = LocalDate.now().plusDays(daysUntilAlarm.toLong())
                    .atTime(alarmSettingTimePicker.hour, alarmSettingTimePicker.minute)
                    .atZone(ZoneId.systemDefault()).toInstant().toString()
            }
            alarmGenerateViewModel.createNewAlarm(
                alarmRequest = AlarmRequest(
                    alarmTitle = etAlarmSettingTitle.text.toString(),
                    alarmTime = alarmTime,
                    alarmMon = cvAlarmRepeatDayMon.isSelected,
                    alarmTue = cvAlarmRepeatDayTue.isSelected,
                    alarmWed = cvAlarmRepeatDayWed.isSelected,
                    alarmThu = cvAlarmRepeatDayThu.isSelected,
                    alarmFri = cvAlarmRepeatDayFri.isSelected,
                    alarmSat = cvAlarmRepeatDaySat.isSelected,
                    alarmSun = cvAlarmRepeatDaySun.isSelected,
                    alarmSoundUri = alarmSoundUri?.toString(),
                    snoozed = alarmSettingLayoutSwitchSnooze.alarmSwitch.isChecked,
                    snoozeMinute = if (alarmSettingLayoutSwitchSnooze.alarmSwitch.isChecked) {
                        tvAlarmSettingSnoozeMinutes.text.toString().toInt()
                    } else null,
                    snoozeCount = if (alarmSettingLayoutSwitchSnooze.alarmSwitch.isChecked) {
                        tvAlarmSettingSnoozeCount.text.toString().toInt()
                    } else null,
                    antiSnoozed = alarmSettingLayoutSwitchAntiSnooze.alarmSwitch.isChecked,
                    antiSnoozeMinute = if (alarmSettingLayoutSwitchAntiSnooze.alarmSwitch.isChecked) {
                        tvAlarmSettingSnoozeLockMinutes.text.toString().toInt()
                    } else null,
                    randomMissionType = randomMissionType,
                    randomMissionLevel = randomMissionLevel,
                    alarmStatus = true
                )
            )
        }
    }

    private fun initObservers() = with(binding) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    alarmGenerateViewModel.randomMission.collect { mission ->
                        if(mission != null) {
                            val missionType = mission.entries.first().key
                            val missionLevel = mission.entries.first().value
                            if (missionType == RandomMissionType.MATH_PROBLEM || missionType == RandomMissionType.FOLLOW_CLICK) {
                                tvAlarmSettingSelectedRandomMission.text = "${randomMissionTypeMapper[missionType]}-${randomMissionLevelMapper[missionLevel]}"
                            } else if(missionType == RandomMissionType.TYPING_SENTENCE) {
                                tvAlarmSettingSelectedRandomMission.text = "${randomMissionTypeMapper[missionType]}"
                            }
                            randomMissionType = missionType
                            randomMissionLevel = missionLevel
                        }
                    }
                }
                launch {
                    alarmGenerateViewModel.snoozeDuration.collect { snoozeDuration ->
                        tvAlarmSettingSnoozeMinutes.text = snoozeDuration.first.toString()
                        tvAlarmSettingSnoozeCount.text = snoozeDuration.second.toString()
                    }
                }
                launch {
                    alarmGenerateViewModel.snoozeLockMinute.collect { snoozeLockMinute ->
                        tvAlarmSettingSnoozeLockMinutes.text = snoozeLockMinute.toString()
                    }
                }
                launch {
                    alarmGenerateViewModel.alarmCreationState.collect { resource ->
                        Log.e("TEST", "collect")
                        when (resource) {
                            is DataResource.Idle -> {}
                            is DataResource.Loading -> {}
                            is DataResource.Success -> {
                                Log.e("TEST", "success")
                                val alarmTime = resource.data
                                val targetDateTime = Instant.parse(alarmTime).atZone(ZoneId.systemDefault()).toLocalDateTime()
                                val currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
                                val toastMessage = formatRemainingTime(start = currentDateTime, end = targetDateTime)
                                Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack()
                            }
                            is DataResource.Error -> {
                                Log.e("TEST", "" + resource.throwable.message)
                                Log.e("TEST", "" + resource.throwable.localizedMessage)
                                Log.e("TEST", "" + resource.throwable.cause)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation).isVisible = true // 하단 바 상태 변경
    }
}