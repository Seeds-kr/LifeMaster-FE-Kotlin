package com.example.lifemaster.presentation.home.alarm.view.fragment

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmSettingBinding
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.FOLLOW_CLICK
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.HIGH
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.LOW
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.MATH_PROBLEM
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.MEDIUM
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.TYPING_SENTENCE
import com.example.lifemaster.presentation.home.alarm.model.AlarmModel
import com.example.lifemaster.presentation.home.alarm.model.AlarmRequest
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionLevel
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionType
import com.example.lifemaster.presentation.home.alarm.view.dialog.AlarmRandomMissionDialog
import com.example.lifemaster.presentation.home.alarm.view.dialog.AlarmSnoozeDialog
import com.example.lifemaster.presentation.home.alarm.view.dialog.AlarmSnoozeLockDialog
import com.example.lifemaster.presentation.home.alarm.view.fragment.AlarmListFragment.Companion.ALARM
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmGenerateViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
import androidx.core.net.toUri

@AndroidEntryPoint
class AlarmSettingFragment : Fragment(R.layout.fragment_alarm_setting) {

    private lateinit var binding: FragmentAlarmSettingBinding
    private val alarmGenerateViewModel: AlarmGenerateViewModel by activityViewModels()
    private val args: AlarmSettingFragmentArgs by navArgs()

    // 소리 URI
    private lateinit var ringtonePickerLauncher: ActivityResultLauncher<Intent>

    // 알람 정보
    private var alarmSoundUri: Uri? = null // 음원 URI 정보
    private var randomMissionType: RandomMissionType? = null
    private var randomMissionLevel: RandomMissionLevel? = null
    private var alarmTime: String = ""

    // 반복 요일
    private val daysOfWeek by lazy {
        listOf(
            binding.alarmSettingLayoutMonday,
            binding.alarmSettingLayoutTuesday,
            binding.alarmSettingLayoutWednesday,
            binding.alarmSettingLayoutThursday,
            binding.alarmSettingLayoutFriday,
            binding.alarmSettingLayoutSaturday,
            binding.alarmSettingLayoutSunday
        )
    }
    private val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")

    // 요일 변환기
    private val dayToValueMapper = mapOf(
        "월" to DayOfWeek.MONDAY.value,
        "화" to DayOfWeek.TUESDAY.value,
        "수" to DayOfWeek.WEDNESDAY.value,
        "목" to DayOfWeek.THURSDAY.value,
        "금" to DayOfWeek.FRIDAY.value,
        "토" to DayOfWeek.SATURDAY.value,
        "일" to DayOfWeek.SUNDAY.value
    )
    private val valueToDayMapper = mapOf(
        DayOfWeek.MONDAY.value to "월",
        DayOfWeek.TUESDAY.value to "화",
        DayOfWeek.WEDNESDAY.value to "수",
        DayOfWeek.THURSDAY.value to "목",
        DayOfWeek.FRIDAY.value to "금",
        DayOfWeek.SATURDAY.value to "토",
        DayOfWeek.SUNDAY.value to "일"
    )
    private val randomMissionTypeMapper = mapOf(
        RandomMissionType.MATH_PROBLEM to MATH_PROBLEM,
        RandomMissionType.FOLLOW_CLICK to FOLLOW_CLICK,
        RandomMissionType.TYPING_SENTENCE to TYPING_SENTENCE
    )
    private val randomMissionLevelMapper = mapOf(
        RandomMissionLevel.HIGH to HIGH,
        RandomMissionLevel.MEDIUM to MEDIUM,
        RandomMissionLevel.LOW to LOW
    )

    private val selectedDays = mutableSetOf<Int>()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmSettingBinding.bind(view)
        val alarmItem: AlarmModel? = args.alarmModel
        initViews(alarmItem)
        initStates()
        initListeners(alarmItem)
        initObservers()
    }

    // 공통
    private fun initStates() = with(binding) {
        ringtonePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    data?.getParcelableExtra(
                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                        Uri::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                }
                val ringtone = RingtoneManager.getRingtone(context, selectedUri)
                val ringtoneTitle = ringtone.getTitle(context)
                tvAlarmSettingMusicTitle.text = ringtoneTitle
                alarmSoundUri = selectedUri
            }
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                clearUI()
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed() // 뒤로 가기 동작 -> 디스페처에 전달
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun initViews(alarm: AlarmModel?) = with(binding) {
        // 공통 기능
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation).isVisible =
            false
        daysOfWeek.zip(dayLabels).forEach {
            it.first.tvDayType.text = it.second
            it.first.cardview.tag = dayToValueMapper[it.second]
        }
        if (alarm != null) {
            // 기존 알람 아이템을 클릭한 경우
            etAlarmSettingTitle.setText(alarm.alarmTitle)
            alarmSettingTimePicker.hour = alarm.hour
            alarmSettingTimePicker.minute = alarm.minute
            setAlarmSettingRingTime(alarm)
            if (alarm.randomMissionType != null) {
                alarmGenerateViewModel.setRandomMission(randomMission = mapOf(alarm.randomMissionType to alarm.randomMissionLevel))
            }
            daysOfWeek.forEach { dayOfWeek ->
                if (selectedDays.contains(dayOfWeek.cardview.tag)) dayOfWeek.cardview.isSelected =
                    true
            }
            if (alarm.snoozed) {
                alarmSettingLayoutSwitchSnooze.alarmSwitch.isChecked = true
                llAlarmSettingDelayStatus.isVisible = true
                alarmGenerateViewModel.setSnoozeDuration(snoozeDuration = alarm.snoozeMinute!! to alarm.snoozeCount!!)
            }
            if (alarm.antiSnoozed) {
                alarmSettingLayoutSwitchAntiSnooze.alarmSwitch.isChecked = true
                llAlarmSettingAntiSnoozeStatus.isVisible = true
                alarmGenerateViewModel.setSnoozeLockMinute(snoozeLockMinute = alarm.antiSnoozeMinute!!)
            }
            alarmSoundUri = alarm.alarmSoundUri?.toUri()
            tvAlarmSettingMusicTitle.text = RingtoneManager.getRingtone(context, alarmSoundUri).getTitle(context)
        } else {
            // 새로운 알람을 추가하는 경우
            tvAlarmSettingRingTime.text = formatAlarmDateLabel(referenceTime = TOMORROW)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun initListeners(alarm: AlarmModel?) = with(binding) {

        // 뒤로가기 버튼
        ivAlarmSettingBack.setOnClickListener {
            findNavController().popBackStack()
            clearUI()
        }

        // 랜덤미션 설정 버튼
        ivAlarmSettingRandomMission.setOnClickListener {
            val dialog = AlarmRandomMissionDialog()
            dialog.isCancelable = true
            dialog.show(childFragmentManager, AlarmRandomMissionDialog.TAG)
        }

        // 알람 반복 요일 설정
        daysOfWeek.forEach { dayOfWeek ->
            dayOfWeek.cardview.setOnClickListener {
                it.isSelected = !it.isSelected

                val dayInt = it.tag.toString().toIntOrNull() ?: return@setOnClickListener

                if (it.isSelected) {
                    selectedDays.add(dayInt)
                } else {
                    selectedDays.remove(dayInt)
                }

                if (selectedDays.isEmpty()) {
                    val now = LocalTime.now()
                    val selected =
                        LocalTime.of(alarmSettingTimePicker.hour, alarmSettingTimePicker.minute)
                    val reference = if (selected.isAfter(now)) TODAY else TOMORROW
                    tvAlarmSettingRingTime.text = formatAlarmDateLabel(referenceTime = reference)
                } else if (selectedDays.size == ALL_DAYS_COUNT) {
                    tvAlarmSettingRingTime.text = "매일"
                } else {
                    val sortedDays = selectedDays.sorted()
                    val textDays = sortedDays.mapNotNull { valueToDayMapper[it] }
                    tvAlarmSettingRingTime.text = "매주 " + textDays.joinToString(", ")
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

        // 알람 저장하기
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
                val todayValue = LocalDate.now().dayOfWeek.value
                val convertedDays = selectedDays.map { dayValue ->
                    if (dayValue > todayValue) dayValue - todayValue
                    else if (dayValue < todayValue) dayValue + 7 - todayValue
                    else {
                        if (LocalTime.of(alarmSettingTimePicker.hour, alarmSettingTimePicker.minute)
                                .isAfter(LocalTime.now())
                        ) 0 else 7
                    }
                }
                val daysUntilAlarm = convertedDays.min()
                alarmTime = LocalDate.now().plusDays(daysUntilAlarm.toLong())
                    .atTime(alarmSettingTimePicker.hour, alarmSettingTimePicker.minute)
                    .atZone(ZoneId.systemDefault()).toInstant().toString()
            }

            if(alarm != null) {
                // 알람 수정하기
                alarmGenerateViewModel.updateAlarm(alarmId = alarm.id, request = AlarmRequest(
                    alarmTitle = etAlarmSettingTitle.text.toString(),
                    alarmTime = alarmTime,
                    alarmMon = alarmSettingLayoutMonday.cardview.isSelected,
                    alarmTue = alarmSettingLayoutTuesday.cardview.isSelected,
                    alarmWed = alarmSettingLayoutWednesday.cardview.isSelected,
                    alarmThu = alarmSettingLayoutThursday.cardview.isSelected,
                    alarmFri = alarmSettingLayoutFriday.cardview.isSelected,
                    alarmSat = alarmSettingLayoutSaturday.cardview.isSelected,
                    alarmSun = alarmSettingLayoutSunday.cardview.isSelected,
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
                ))
            } else {
                // 알람 생성하기
                alarmGenerateViewModel.createNewAlarm(
                    alarmRequest = AlarmRequest(
                        alarmTitle = etAlarmSettingTitle.text.toString(),
                        alarmTime = alarmTime,
                        alarmMon = alarmSettingLayoutMonday.cardview.isSelected,
                        alarmTue = alarmSettingLayoutTuesday.cardview.isSelected,
                        alarmWed = alarmSettingLayoutWednesday.cardview.isSelected,
                        alarmThu = alarmSettingLayoutThursday.cardview.isSelected,
                        alarmFri = alarmSettingLayoutFriday.cardview.isSelected,
                        alarmSat = alarmSettingLayoutSaturday.cardview.isSelected,
                        alarmSun = alarmSettingLayoutSunday.cardview.isSelected,
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

            // TODO: DB 저장 및 내부 로직 처리

//            val alarmModel = AlarmModel(
//                id = alarmViewModel.alarmItems.value?.size ?: 0,
//                title = etAlarmSettingTitle.text.toString(),
//                hour = alarmSettingTimePicker.hour,
//                minute = alarmSettingTimePicker.minute
//            )
//
//            /**
//             * 1) timeInMillis = System.currentTimeMillis()
//             * 현재 시각을 기준으로 Calendar 객체를 초기화한다. 이 코드를 작성하지 않으면 Calendar 객체는 기본적으로 1970년 1월 1일 0시 0분으로 설정되어,
//             * 사용자가 직접 알람 시간을 올바르게 계산하지 못할 수 있다.
//             * 2) before(Calendar.getInstance())
//             * 만약 사용자가 설정한 시각(오후 2시)이 현재 시각(오후 4시)보다 이전(before)이면 알람이 현재 시각에 바로 울리게 된다.
//             * 따라서, 이런 경우 1일을 증가시켜 다음날 해당 시각에 알람이 울리도록 한다.
//             */
//            val calendar = Calendar.getInstance().apply {
//                timeInMillis = System.currentTimeMillis()
//                set(Calendar.HOUR_OF_DAY, alarmModel.hour)
//                set(Calendar.MINUTE, alarmModel.minute)
//                set(Calendar.SECOND, 0)
//                set(Calendar.MILLISECOND, 0)
//                if (before(Calendar.getInstance())) {
//                    add(Calendar.DATE, 1)
//                }
//            }
//
//            /**
//             * 1) alarmManager.canScheduleExactAlarms()
//             * 앱이 정확한 시각에 알람을 예약할 수 있는 "권한"이 있는지 확인하는 메소드
//             * 왜 exact라고 표기되어 있는가? 배터리 최적화를 위해 시스템이 알람 시간을 조정하는 경우인 일반 알람(inexact)도 존재하기 때문
//             * 2) alarmManager.setExactAndAllowWhileIdle
//             * 정확한 시간에 알람을 예약하고, 기기가 Doze 모드(절전 모드)에 있어도 알람이 울리도록 한다.
//             * 참고로, 잦은 반복 사용은 배터리 소모가 커서 권장하지 않는다.
//             * 3) requestCode (★)
//             * 여러 개의 알람을 예약할 때, PendingIntent를 구분하는 식별자로 쓰인다.
//             * 같은 Intent여도 requestCode가 다르면 서로 다른 알람으로 취급되고, 같으면 기존 것을 갱신(FLAG_UPDATE_CURRENT)한다.
//             * 즉, 알람을 여러 개 등록할 필요가 있으면 0 대신 uniqueId 같은 값을 넣어야 하고, 단 하나의 알람만 쓰면 0 고정으로 둬도 된다.
//             * 요약) requestCode 를 0처럼 단일로 준다 → 한개의 알람만 등록 가능, requestCode를 다르게 준다 → 여러 개의 알람 등록 가능
//             * 4) PendingIntent.FLAG_UPDATE_CURRENT
//             * requestCode가 같은 경우 동일한 pendingIntent로 취급하여 기존 값을 갱신한다(덮어쓰기)
//             * 5) PendingIntent.FLAG_IMMUTABLE
//             * 생성된 PendingIntent는 이후 수정이 불가능하다는 보안 설정
//             * 6) AlarmManager.RTC_WAKEUP
//             * 실제 시간(System.currentTimeMillis 기준)으로 알람을 예약하고, 필요하면 디바이스를 깨움 (알람이 무조건 울리게 할 때 꼭 써야하는 옵션)
//             */
//            // TODO: 알림 권한 설정 안되어있으면 권한 허용하도록 강제해야함
//            val alarmManager =
//                requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
//            if (alarmManager.canScheduleExactAlarms()) { // 주석 1
//                val intent = Intent(requireContext(), AlarmReceiver::class.java)
//                val pendingIntent = PendingIntent.getBroadcast(
//                    requireContext(),
//                    SINGLE_ALARM_REQUEST_CODE, // 주석 3, 일단은 단일 알람 기능 먼저 구현
//                    intent,
//                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // 주석 4, 주석 5
//                )
//                alarmManager.setExactAndAllowWhileIdle( // 주석 2
//                    AlarmManager.RTC_WAKEUP, // 주석 6
//                    calendar.timeInMillis,
//                    pendingIntent
//                )
//            } else {
//                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
//                    data = Uri.parse("package:${requireContext().packageName}")
//                }
//                requireContext().startActivity(intent)
//            }
//            alarmViewModel.updateAlarmItems(alarmModel)
//            alarmViewModel.clearRandomMissions()
//            Toast.makeText(requireContext(), "알람이 추가되었습니다!", Toast.LENGTH_SHORT).show()
//            findNavController().popBackStack()

        }
    }

    private fun initObservers() = with(binding) {
        alarmSettingTimePicker.setOnTimeChangedListener { timePicker, timePickerHour, timePickerMinute ->
            if (selectedDays.isEmpty()) {
                val now = LocalTime.now()
                val selected = LocalTime.of(timePickerHour, timePickerMinute)
                val reference = if (selected.isAfter(now)) TODAY else TOMORROW
                tvAlarmSettingRingTime.text = formatAlarmDateLabel(referenceTime = reference)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                alarmGenerateViewModel.snoozeDuration.collect { snoozeDuration ->
                    tvAlarmSettingSnoozeMinutes.text = snoozeDuration.first.toString()
                    tvAlarmSettingSnoozeCount.text = snoozeDuration.second.toString()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                alarmGenerateViewModel.snoozeLockMinute.collect { snoozeLockMinute ->
                    tvAlarmSettingSnoozeLockMinutes.text = snoozeLockMinute.toString()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                alarmGenerateViewModel.alarmCreationState.collect { resource ->
                    when (resource) {
                        is DataResource.Idle -> {}
                        is DataResource.Loading -> {}
                        is DataResource.Success -> {
                            Toast.makeText(context, "알람 생성이 완료되었어요.", Toast.LENGTH_SHORT).show()
                            clearUI()
                            findNavController().popBackStack()
                        }

                        is DataResource.Error -> {
                            Toast.makeText(context, "알람 생성이 실패하였습니다.", Toast.LENGTH_SHORT).show()
                            Log.e(ALARM, "" + resource.throwable.message)
                        }
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                alarmGenerateViewModel.alarmUpdateState.collect { resource ->
                    when(resource) {
                        is DataResource.Error -> {
                            Toast.makeText(context, "알람 업데이트가 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                        DataResource.Idle -> {}
                        DataResource.Loading -> {}
                        is DataResource.Success<*> -> {
                            Toast.makeText(context, "알람 업데이트가 완료되었어요.", Toast.LENGTH_SHORT).show()
                            clearUI()
                            findNavController().popBackStack()
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

    private fun setAlarmSettingRingTime(alarm: AlarmModel) = with(binding) {
        if (alarm.alarmMon) selectedDays.add(DayOfWeek.MONDAY.value)
        if (alarm.alarmTue) selectedDays.add(DayOfWeek.TUESDAY.value)
        if (alarm.alarmWed) selectedDays.add(DayOfWeek.WEDNESDAY.value)
        if (alarm.alarmThu) selectedDays.add(DayOfWeek.THURSDAY.value)
        if (alarm.alarmFri) selectedDays.add(DayOfWeek.FRIDAY.value)
        if (alarm.alarmSat) selectedDays.add(DayOfWeek.SATURDAY.value)
        if (alarm.alarmSun) selectedDays.add(DayOfWeek.SUNDAY.value)

        if (selectedDays.isEmpty()) {
            val today: LocalDate = LocalDate.now(ZoneId.systemDefault())
            val targetDate: LocalDate =
                Instant.parse(alarm.alarmTime).atZone(ZoneId.systemDefault()).toLocalDate()
            val daysDifference =
                ChronoUnit.DAYS.between(today, targetDate).toInt() // targetDate - today
            when (daysDifference) {
                0 -> tvAlarmSettingRingTime.text = formatAlarmDateLabel(referenceTime = TODAY)
                1 -> tvAlarmSettingRingTime.text = formatAlarmDateLabel(referenceTime = TOMORROW)
                else -> tvAlarmSettingRingTime.text =
                    "${targetDate.year}년 ${targetDate.dayOfMonth}월 ${targetDate.dayOfMonth}일 (${
                        targetDate.dayOfWeek.getDisplayName(
                            TextStyle.SHORT, Locale.KOREAN
                        )
                    })"
            }
        } else if (selectedDays.size == ALL_DAYS_COUNT) {
            tvAlarmSettingRingTime.text = "매일"
        } else {
            val sortedDays = selectedDays.sorted() // [1, 3, 4]
            val textDays = sortedDays.map { valueToDayMapper[it] } // [월, 수, 목]
            tvAlarmSettingRingTime.text = "매주" + textDays.joinToString(", ") // 매주 월, 수. 목
        }
    }

    private fun formatAlarmDateLabel(referenceTime: String): String {
        var alarmDateLabel = ""
        when (referenceTime) {
            TODAY -> {
                val today = LocalDateTime.now()
                alarmDateLabel = "오늘 - ${today.monthValue}월 ${today.dayOfMonth}일 (${
                    today.dayOfWeek.getDisplayName(
                        TextStyle.SHORT, Locale.KOREAN
                    )
                })"
            }

            TOMORROW -> {
                val tomorrow = LocalDateTime.now().plusDays(1)
                alarmDateLabel = "내일 - ${tomorrow.monthValue}월 ${tomorrow.dayOfMonth}일 (${
                    tomorrow.dayOfWeek.getDisplayName(
                        TextStyle.SHORT, Locale.KOREAN
                    )
                })"
            }
        }
        return alarmDateLabel
    }

    private fun clearUI() {
        alarmGenerateViewModel.resetRandomMission()
        alarmGenerateViewModel.resetSnoozeDuration()
        alarmGenerateViewModel.resetSnoozeAntiMinute()
    }

    companion object {
        private const val SINGLE_ALARM_REQUEST_CODE = 1000
        private const val TODAY = "today"
        private const val TOMORROW = "tomorrow"
        private const val ALL_DAYS_COUNT = 7
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