package com.example.lifemaster.presentation.group.view.fragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import com.example.lifemaster.presentation.group.view.dialog.AlarmRandomMissionDialog
import com.example.lifemaster.presentation.group.model.AlarmItem
import com.example.lifemaster.presentation.group.model.MathProblemLevel
import com.example.lifemaster.presentation.group.model.RandomMissionType
import com.example.lifemaster.presentation.group.view.receiver.AlarmReceiver
import com.example.lifemaster.presentation.group.viewmodel.AlarmViewModel
import java.util.Calendar

class AlarmSettingFragment : Fragment(R.layout.fragment_alarm_setting) {

    private lateinit var binding: FragmentAlarmSettingBinding
    private val alarmViewModel: AlarmViewModel by activityViewModels()
    private var randomMissionList = arrayListOf<RandomMissionType>()
    private var randomMissionMathLevel = MathProblemLevel.NONE
    private var isDelaySet: Boolean = false
    private var alarmRepeatDays = arrayListOf<String>()
    private val dayLayouts by lazy { listOf(
        binding.layoutMonday,
        binding.layoutTuesday,
        binding.layoutWednesday,
        binding.layoutThursday,
        binding.layoutFriday,
        binding.layoutSaturday,
        binding.layoutSunday
    ) }

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
        dayLayouts.zip(dayLabels).forEach { it.first.tvDayType.text = it.second }
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
                    if (it.isSelected) alarmRepeatDays.add(eachDay.tvDayType.text.toString()) else alarmRepeatDays.remove(eachDay.tvDayType.text.toString())
                }
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
                    // 미루기 설정 o
                    val alarmItem = AlarmItem(
                        id = alarmViewModel.alarmItems.value?.size ?: 0,
                        title = alarmTitle,
                        time = alarmTime,
                        randomMissions = randomMissionList,
                        randomMissionMathLevel = randomMissionMathLevel,
                        alarmRepeatDays = alarmRepeatDays,
                        isDelaySet = true,
                        delayMinute = tvDelayMinutes.text.toString().toInt(),
                        delayCount = tvDelayCounts.text.toString().toInt()
                    )

                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = System.currentTimeMillis()
                        set(Calendar.HOUR_OF_DAY, alarmTime.second)
                        set(Calendar.MINUTE, alarmTime.third)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
//                        if(before(Calendar.getInstance())) { add(Calendar.DATE, 1) }
                    }

                    val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if(alarmManager.canScheduleExactAlarms()) {
                        val intent = Intent(requireContext(), AlarmReceiver::class.java)
                        val pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                        val goesOffTime = calendar.timeInMillis
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, goesOffTime, pendingIntent)
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
                } else if (isDelaySet && tvDelayCounts.text.isBlank()) {
                    Toast.makeText(requireContext(), "미루기 설정을 해주세요!", Toast.LENGTH_SHORT).show()
                } else {
                    // 미루기 설정 x
                    val alarmItem = AlarmItem(
                        id = alarmViewModel.alarmItems.value?.size ?: 0,
                        title = alarmTitle,
                        time = alarmTime,
                        randomMissions = randomMissionList,
                        randomMissionMathLevel = randomMissionMathLevel,
                        alarmRepeatDays = alarmRepeatDays,
                        isDelaySet = false
                    )

                    Log.d("ttest", ""+alarmTime)

                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = System.currentTimeMillis()
                        set(Calendar.HOUR_OF_DAY, alarmTime.second)
                        set(Calendar.MINUTE, alarmTime.third)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
//                        if(before(Calendar.getInstance())) { add(Calendar.DATE, 1) }
                    }

                    val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    if(alarmManager.canScheduleExactAlarms()) {
                        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
                            putExtra("day_or_night", alarmTime.first) // string
                            putExtra("hour(24)", alarmTime.second)
                            putExtra("minutes", alarmTime.third)
                        }
                        val pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                        val goesOffTime = calendar.timeInMillis
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, goesOffTime, pendingIntent)
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
                when(mission) {
                    is Map<*, *> -> mission.entries.first().let { "${it.key}-${it.value}" }
                    is String -> mission
                    else -> ""
                }
            }
            binding.tvSelectedRandomMission.text = formattedText

            randomMissions.forEach { randomMission ->
                when(randomMission) {
                    is Map<*,*> -> {
                        randomMissionList.add(RandomMissionType.MATHEMATICAL_PROBLEM_SOLVING)
                        when(randomMission.entries.first().value) {
                            "상" -> randomMissionMathLevel = MathProblemLevel.HIGH
                            "중" -> randomMissionMathLevel = MathProblemLevel.MEDIUM
                            "하" -> randomMissionMathLevel = MathProblemLevel.LOW
                        }
                    }
                    is String -> {
                        when(randomMission) {
                            "따라 누르기" -> randomMissionList.add(RandomMissionType.TOUCH_ALONG)
                            "글 따라쓰기" -> randomMissionList.add(RandomMissionType.WRITE_ALONG)
                        }
                    }
                }
            }
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