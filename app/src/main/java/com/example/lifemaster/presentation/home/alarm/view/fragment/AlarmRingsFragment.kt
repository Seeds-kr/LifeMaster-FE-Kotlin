package com.example.lifemaster.presentation.home.alarm.view.fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmRingBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.home.alarm.model.AlarmModel
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionType
import com.example.lifemaster.presentation.home.alarm.view.service.AlarmService
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModel
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModelFactory
import java.time.Instant
import java.time.ZoneId
import kotlin.math.min

class AlarmRingsFragment : Fragment(R.layout.fragment_alarm_ring) {

    lateinit var binding: FragmentAlarmRingBinding
    private val alarmViewModel: AlarmViewModel by activityViewModels(
        factoryProducer = { AlarmViewModelFactory(RetrofitInstance.networkService) }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmRingBinding.bind(view)

        val alarmItem = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("ALARM_DATA", AlarmModel::class.java)
        } else {
            arguments?.getParcelable<AlarmModel>("ALARM_DATA")
        }

        if(alarmItem == null) return

        alarmViewModel.alarmTriggeredAt = arguments?.getLong("time") // 알람이 울린 시간 (알람이 울린 시간과 핸드폰 화면에 들어온 시간은 다르다) 내 기억 상으로 수면 연동 때문에 적은 듯
        initViews(alarmItem)
        initListeners(alarmItem)
    }

    private fun initViews(alarmItem: AlarmModel) = with(binding) {
        setAlarmTime()
        setDelayAlarm(alarmItem)
        btnDismissAlarm.text = if(alarmItem.randomMissionType == null) "알람 끄기" else "랜덤 미션 수행하기"
    }

    private fun FragmentAlarmRingBinding.setAlarmTime() {
        val localTime = Instant.now().atZone(ZoneId.systemDefault()).toLocalTime()
        val hour = localTime.hour
        val minute = localTime.minute
        val timeType = when (hour) {
            in 6..11 -> "Morning"
            in 12..18 -> "Afternoon"
            in 19..23 -> "Night"
            else -> "Dawn" // 0시(자정) ~ 오전 5시
        }
        tvTimeType.text = "Good\n${timeType},\nit's"
        tvCurrentTime.text = String.format("%02d:%02d", hour, minute)
        tvDayOrNight.text = if (hour in 0..11) "AM" else "PM"
    }

    private fun FragmentAlarmRingBinding.setDelayAlarm(alarmItem: AlarmModel) {
        if(alarmItem.snoozed) {
            tvDelayAlarm.isVisible = true
            tvDelayAlarm.text = "${alarmItem.snoozeMinute}분 미루기 (2 / ${alarmItem.snoozeCount})" // TODO: 현재 몇번째 미루기 단계인지 설정하는 필드 필요 (2 부분 실제 필드로 변경해야 함)
        } else {
            tvDelayAlarm.isVisible = false
        }
    }

    private fun initListeners(alarmItem: AlarmModel) = with(binding) {

        btnDismissAlarm.setOnClickListener {
            if(alarmItem.randomMissionType == null) {
                requireActivity().stopService(Intent(requireContext(), AlarmService::class.java))
                // TODO: 현재 액티비티 끄고 메인 액티비티 화면으로 이동하기 (nav_graph_main 연결??)
                // TODO: 만약 알람이 일회성 알람인 경우 (반복 요일이 없는 경우) 스위치 상태 OFF 로 변경하기 (업데이트) + 세부 화면 들어가면 이미 꺼진 알람입니다.. (이미 지난 시간인 지 구별하는 지금보다 더 명확한 로직 작성 필요)
                // TODO: 추가 설정 필요한 지 고민해보기
            } else {
                when(alarmItem.randomMissionType) {
                    RandomMissionType.MATH_PROBLEM -> {
                        val action = AlarmRingsFragmentDirections.actionAlarmRingsFragmentToAlarmRandomMissionMathFragment(alarmItem = alarmItem, currentPageNum = 1)
                        findNavController().navigate(action)
                    }
                    RandomMissionType.FOLLOW_CLICK -> {
                        val action = AlarmRingsFragmentDirections.actionAlarmRingsFragmentToAlarmRandomMissionTapFragment(alarmItem = alarmItem, currentPageNum = 1)
                        findNavController().navigate(action)
                    }
                    RandomMissionType.TYPING_SENTENCE -> {
                        val action = AlarmRingsFragmentDirections.actionAlarmRingsFragmentToAlarmRandomMissionTypingFragment(alarmItem = alarmItem, currentPageNum = 1)
                        findNavController().navigate(action)
                    }
                }
            }
        }

        tvDelayAlarm.setOnClickListener {
            // TODO: 만약 현재 미루기 횟수가 총 미루기 횟수보다 적은 경우에는 알람을 미루는 로직 수행 (snoozeMinute 후에 다시 울림 + 현재 미루기 횟수 +1 반영
            // TODO: 현재 미루기 숫자가 총 미루기 횟수랑 같은 경우 -> TextView 를 안보이게 하거나 눌렀을 때 "더이상 알람을 미룰 수 없습니다" 토스트 메세지 띄우기
        }

        // 뒤로가기 버튼(백버튼) 막기
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() { Toast.makeText(context, "뒤로갈 수 없습니다!", Toast.LENGTH_SHORT).show() }
            }
        )
    }
}