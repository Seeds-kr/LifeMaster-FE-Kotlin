package com.example.lifemaster.presentation.home.alarm.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmRandomMissionTapBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.Constants
import com.example.lifemaster.presentation.home.alarm.view.service.AlarmService
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModel
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModelFactory
import com.example.lifemaster.presentation.home.sleep.model.AlarmInfo
import com.example.lifemaster.presentation.home.sleep.model.AlarmSettingInfo
import com.example.lifemaster.presentation.home.sleep.model.Result
import com.example.lifemaster.presentation.home.sleep.model.SleepRequest
import com.example.lifemaster.presentation.home.sleep.viewmodel.SleepViewModel
import com.example.lifemaster.presentation.home.sleep.viewmodel.SleepViewModelFactory
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import kotlin.random.Random


class AlarmRandomMissionTapFragment : Fragment(R.layout.fragment_alarm_random_mission_tap) {

    private lateinit var binding: FragmentAlarmRandomMissionTapBinding
    private val alarmViewModel: AlarmViewModel by activityViewModels(
        factoryProducer = { AlarmViewModelFactory(RetrofitInstance.networkService) }
    )
    private val sleepViewModel: SleepViewModel by activityViewModels {
        SleepViewModelFactory(RetrofitInstance.networkService)
    }

    private lateinit var taps: List<MaterialCardView>
    private var answerTapPositions: MutableSet<Int> = hashSetOf()
    private var userTapPositions: MutableSet<Int> = hashSetOf()
    private var currentPage: Int = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmRandomMissionTapBinding.bind(view)
        currentPage = arguments?.getInt("currentPageNum")!! // 파라미터 전달 받기
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뒤로가기 버튼 비활성화
            }
        })
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() = with(binding) {
        tvAlarmRandomMissionTapPage.text = "${currentPage}/3"
        taps = listOf(
            cvAlarmRandomMissionTap1,
            cvAlarmRandomMissionTap2,
            cvAlarmRandomMissionTap3,
            cvAlarmRandomMissionTap4,
            cvAlarmRandomMissionTap5,
            cvAlarmRandomMissionTap6,
            cvAlarmRandomMissionTap7,
            cvAlarmRandomMissionTap8,
            cvAlarmRandomMissionTap9,
            cvAlarmRandomMissionTap10,
            cvAlarmRandomMissionTap11,
            cvAlarmRandomMissionTap12,
            cvAlarmRandomMissionTap13,
            cvAlarmRandomMissionTap14,
            cvAlarmRandomMissionTap15,
            cvAlarmRandomMissionTap16,
            cvAlarmRandomMissionTap17,
            cvAlarmRandomMissionTap18,
            cvAlarmRandomMissionTap19,
            cvAlarmRandomMissionTap20,
            cvAlarmRandomMissionTap21,
            cvAlarmRandomMissionTap22,
            cvAlarmRandomMissionTap23,
            cvAlarmRandomMissionTap24,
            cvAlarmRandomMissionTap25
        )
        lifecycleScope.launch {
            for (tap in taps) { tap.isEnabled = false } // 사용자 터치 임시 비활성화
            // repeat 코드 실행 시간 거의 0ms에 가까움
            repeat(10) {
                val i = Random.nextInt(0, 25) // 0 ~ 24 (중복 허용)
                taps[i].apply {
                    isSelected = true
                    setCardBackgroundColor(
                        resources.getColor(
                            R.color.alarm_primary,
                            context?.theme
                        )
                    )
                }
                answerTapPositions.add(i)
            }
            delay(1000)
            tvAlarmRandomMissionTapCount.text = "2"
            delay(1000)
            tvAlarmRandomMissionTapCount.text = "1"
            delay(1000)
            tvAlarmRandomMissionTapCount.isVisible = false
            for (tap in taps) {
                tap.isSelected = false
                tap.setCardBackgroundColor(
                    resources.getColor(
                        R.color.light_gray_100,
                        context?.theme
                    )
                )
            }
            for (tap in taps) { tap.isEnabled = true } // 사용자 터치 재활성화
        }
    }

    private fun initListeners() = with(binding) {
        for (tap in taps) {
            tap.setOnClickListener {
                tap.isSelected = !tap.isSelected
                if (tap.isSelected) {
                    tap.setCardBackgroundColor(
                        resources.getColor(
                            R.color.alarm_primary,
                            context?.theme
                        )
                    )
                } else {
                    tap.setCardBackgroundColor(
                        resources.getColor(
                            R.color.light_gray_100,
                            context?.theme
                        )
                    )
                }
            }
        }
        cvAlarmRandomMissionNextPage.setOnClickListener {
            taps.forEachIndexed { position, tap ->
                if(tap.isSelected) userTapPositions.add(position) else userTapPositions.remove(position)
            }

            if(answerTapPositions.equals(userTapPositions)) {
                if(currentPage == 3) {
                    sleepViewModel.getUserSleepInfo(Constants.USER_ID)
                } else {
                    findNavController().navigate(
                        R.id.alarmRandomMissionTapFragment,
                        bundleOf("currentPageNum" to ++currentPage),
                        NavOptions.Builder()
                            .setLaunchSingleTop(true) // 최상단이 같은 프래그먼트인 경우 쌓지 않고 교체함
                            .build()
                    )
                }
            }
            else { Toast.makeText(context, "답이 틀렸습니다! 다시 입력해주세요!", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun initObservers() = with(binding) {
        sleepViewModel.userSleepRecordList.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Result.Success -> {
                    alarmViewModel.alarmDismissedAt = System.currentTimeMillis()
                    val data = result.data
                    val todayRecord = data.find { it.sleepDate == LocalDate.now().toString() }
                    if(todayRecord == null) {
                        // TODO: POST
                        sleepViewModel.registerUserSleepInfo(
                            sleepRequest = SleepRequest(
                                userId = Constants.USER_ID,
                                sleepDate = LocalDate.now().toString(),
                                sleepStart = Instant.ofEpochMilli(sleepViewModel.rawSleepTime ?: 0L).toString(),
                                sleepEnd =  Instant.ofEpochMilli(alarmViewModel.alarmDismissedAt ?: 0L).toString(),
                                sleepMood = "GOOD",
                                alarmInfo = AlarmInfo(
                                    isWakeUpAlarmSet = true,
                                    alarmSettings = AlarmSettingInfo(
                                        alarmSnoozeCnt = 0, // TODO: 실제 알람 데이터로 변경하기
                                        timeToWakeUp = 0, // TODO: 실제 알람 데이터로 변경하기
                                        antiSleepMode = false // TODO: 실제 알람 데이터로 변경하기
                                    )
                                )
                            )
                        )
                    } else {
                        // TODO: PATCH
                        sleepViewModel.updateUserSleepInfo(
                            sleepRequest = SleepRequest(
                                userId = Constants.USER_ID,
                                sleepDate = LocalDate.now().toString(),
                                sleepStart = Instant.ofEpochMilli(sleepViewModel.rawSleepTime ?: 0L).toString(),
                                sleepEnd =  Instant.ofEpochMilli(alarmViewModel.alarmDismissedAt ?: 0L).toString(),
                                sleepMood = "GOOD",
                                alarmInfo = AlarmInfo(
                                    isWakeUpAlarmSet = true,
                                    alarmSettings = AlarmSettingInfo(
                                        alarmSnoozeCnt = 0, // TODO: 실제 알람 데이터로 변경하기
                                        timeToWakeUp = 0, // TODO: 실제 알람 데이터로 변경하기
                                        antiSleepMode = false // TODO: 실제 알람 데이터로 변경하기
                                    )
                                )
                            )
                        )
                    }
                }
                is Result.Error -> {
                    Toast.makeText(context, "네트워크 연결이 불안정합니다.", Toast.LENGTH_SHORT).show()
                }
                is Result.Loading -> {}
            }
        }

        sleepViewModel.isUserSleepRecordGenerated.observe(viewLifecycleOwner) { event ->
            event.getDataIfNotHandled()?.let { isSuccess ->
                if (isSuccess) {
                    Toast.makeText(context, "수면 기록 전송이 성공했습니다", Toast.LENGTH_SHORT).show()
                    // 알람 소리 멈추기
                    val serviceIntent = Intent(context, AlarmService::class.java)
                    requireContext().stopService(serviceIntent)
                    findNavController().navigate(
                        R.id.action_alarmRandomMissionTapFragment_to_alarmListFragment,
                        bundleOf("origin" to "alarm_random_mission")
                    )
                } else {
                    Toast.makeText(context, "네트워크 연결이 불안정합니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        sleepViewModel.userSleepUpdatedRecord.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    Toast.makeText(context, "수면 기록이 업데이트 되었습니다", Toast.LENGTH_SHORT).show()
                    // 알람 소리 멈추기
                    val serviceIntent = Intent(context, AlarmService::class.java)
                    requireContext().stopService(serviceIntent)
                    findNavController().navigate(
                        R.id.action_alarmRandomMissionTapFragment_to_alarmListFragment,
                        bundleOf("origin" to "alarm_random_mission")
                    )
                }
                is Result.Error -> {
                    Toast.makeText(context, "네트워크 연결이 불안정합니다.", Toast.LENGTH_SHORT).show()
                }
                is Result.Loading -> {}
            }
        }
    }

}
