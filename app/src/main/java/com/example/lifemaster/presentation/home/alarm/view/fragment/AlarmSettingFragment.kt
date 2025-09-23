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
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.home.alarm.model.AlarmItem
import com.example.lifemaster.presentation.home.alarm.model.MathProblemLevel
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionType
import com.example.lifemaster.presentation.home.alarm.view.dialog.AlarmRandomMissionDialog
import com.example.lifemaster.presentation.home.alarm.view.receiver.AlarmReceiver
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModel
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModelFactory
import com.example.lifemaster.presentation.total.detox.dialog.SelectTimeDialog
import java.util.Calendar

class AlarmSettingFragment : Fragment(R.layout.fragment_alarm_setting) {

    private lateinit var binding: FragmentAlarmSettingBinding
    private val alarmViewModel: AlarmViewModel by activityViewModels(
        factoryProducer = { AlarmViewModelFactory(RetrofitInstance.networkService) }
    )
    private var randomMissionList = arrayListOf<RandomMissionType>()
    private var randomMissionMathLevel = MathProblemLevel.NONE
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
                findNavController().navigate(R.id.action_alarmSettingFragment_to_alarmListFragment)
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

                    val alarmItem = AlarmItem(
                        id = alarmViewModel.alarmItems.value?.size ?: 0,
                        title = etAlarmTitle.text.toString(),
                        hour = timePicker.hour,
                        minute = timePicker.minute
                    )

                    /**
                     * 1) timeInMillis = System.currentTimeMillis()
                     * 현재 시각을 기준으로 Calendar 객체를 초기화한다. 이 코드를 작성하지 않으면 Calendar 객체는 기본적으로 1970년 1월 1일 0시 0분으로 설정되어,
                     * 사용자가 직접 알람 시간을 올바르게 계산하지 못할 수 있다.
                     * 2) before(Calendar.getInstance())
                     * 만약 사용자가 설정한 시각(오후 2시)이 현재 시각(오후 4시)보다 이전(before)이면 알람이 현재 시각에 바로 울리게 된다.
                     * 따라서, 이런 경우 1일을 증가시켜 다음날 해당 시각에 알람이 울리도록 한다.
                     */
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = System.currentTimeMillis()
                        set(Calendar.HOUR_OF_DAY, alarmItem.hour)
                        set(Calendar.MINUTE, alarmItem.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        if (before(Calendar.getInstance())) {
                            add(Calendar.DATE, 1)
                        }
                    }

                    /**
                     * 1) alarmManager.canScheduleExactAlarms()
                         * 앱이 정확한 시각에 알람을 예약할 수 있는 "권한"이 있는지 확인하는 메소드
                         * 왜 exact라고 표기되어 있는가? 배터리 최적화를 위해 시스템이 알람 시간을 조정하는 경우인 일반 알람(inexact)도 존재하기 때문
                     * 2) alarmManager.setExactAndAllowWhileIdle
                         * 정확한 시간에 알람을 예약하고, 기기가 Doze 모드(절전 모드)에 있어도 알람이 울리도록 한다.
                         * 참고로, 잦은 반복 사용은 배터리 소모가 커서 권장하지 않는다.
                     * 3) requestCode (★)
                        * 여러 개의 알람을 예약할 때, PendingIntent를 구분하는 식별자로 쓰인다.
                        * 같은 Intent여도 requestCode가 다르면 서로 다른 알람으로 취급되고, 같으면 기존 것을 갱신(FLAG_UPDATE_CURRENT)한다.
                        * 즉, 알람을 여러 개 등록할 필요가 있으면 0 대신 uniqueId 같은 값을 넣어야 하고, 단 하나의 알람만 쓰면 0 고정으로 둬도 된다.
                        * 요약) requestCode 를 0처럼 단일로 준다 → 한개의 알람만 등록 가능, requestCode를 다르게 준다 → 여러 개의 알람 등록 가능
                     * 4) PendingIntent.FLAG_UPDATE_CURRENT
                        * requestCode가 같은 경우 동일한 pendingIntent로 취급하여 기존 값을 갱신한다(덮어쓰기)
                     * 5) PendingIntent.FLAG_IMMUTABLE
                        * 생성된 PendingIntent는 이후 수정이 불가능하다는 보안 설정
                     * 6) AlarmManager.RTC_WAKEUP
                        * 실제 시간(System.currentTimeMillis 기준)으로 알람을 예약하고, 필요하면 디바이스를 깨움 (알람이 무조건 울리게 할 때 꼭 써야하는 옵션)
                     */
                    // TODO: 알림 권한 설정 안되어있으면 권한 허용하도록 강제해야함
                    val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if (alarmManager.canScheduleExactAlarms()) { // 주석 1
                        val intent = Intent(requireContext(), AlarmReceiver::class.java)
                        val pendingIntent = PendingIntent.getBroadcast(
                            requireContext(),
                            SINGLE_ALARM_REQUEST_CODE, // 주석 3, 일단은 단일 알람 기능 먼저 구현
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // 주석 4, 주석 5
                        )
                        alarmManager.setExactAndAllowWhileIdle( // 주석 2
                            AlarmManager.RTC_WAKEUP, // 주석 6
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${requireContext().packageName}")
                        }
                        requireContext().startActivity(intent)
                    }
                    alarmViewModel.updateAlarmItems(alarmItem)
                    alarmViewModel.clearRandomMissions()
                    Toast.makeText(requireContext(), "알람이 추가되었습니다!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_alarmSettingFragment_to_alarmListFragment)
                }
            }
        }
    }

    private fun initObservers() {
        alarmViewModel.delayMinutesAndCount.observe(viewLifecycleOwner) {
            binding.tvDelayMinutes.text = "${it.first}"
            binding.tvDelayCounts.text = "${it.second}"
        }
        alarmViewModel.randomMissions.observe(viewLifecycleOwner) { randomMissions ->
            val formattedText = randomMissions.joinToString(", ") { mission ->
                when (mission) {
                    is Map<*, *> -> mission.entries.first().let { "${it.key}-${it.value}" }
                    is String -> mission
                    else -> ""
                }
            }
            binding.tvSelectedRandomMission.text = formattedText

            randomMissions.forEach { randomMission ->
                when (randomMission) {
                    is Map<*, *> -> {
                        randomMissionList.add(RandomMissionType.MATHEMATICAL_PROBLEM_SOLVING)
                        when (randomMission.entries.first().value) {
                            "상" -> randomMissionMathLevel = MathProblemLevel.HIGH
                            "중" -> randomMissionMathLevel = MathProblemLevel.MEDIUM
                            "하" -> randomMissionMathLevel = MathProblemLevel.LOW
                        }
                    }

                    is String -> {
                        when (randomMission) {
                            "따라 누르기" -> randomMissionList.add(RandomMissionType.TOUCH_ALONG)
                            "글 따라쓰기" -> randomMissionList.add(RandomMissionType.WRITE_ALONG)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val SINGLE_ALARM_REQUEST_CODE = 1000
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