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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmRandomMissionTapBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.Constants
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.alarm.view.service.AlarmService
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmMissionViewModel
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


class AlarmRandomMissionTapFragment : Fragment(R.layout.fragment_alarm_random_mission_tap) {

    private lateinit var binding: FragmentAlarmRandomMissionTapBinding
    private val alarmViewModel: AlarmViewModel by activityViewModels(
        factoryProducer = { AlarmViewModelFactory(RetrofitInstance.networkService) }
    )
    private val sleepViewModel: SleepViewModel by activityViewModels {
        SleepViewModelFactory(RetrofitInstance.networkService)
    }

    private val alarmMissionViewModel: AlarmMissionViewModel by activityViewModels()

    private val taps: List<MaterialCardView> by lazy {
        listOf(
            binding.cvAlarmRandomMissionTap1,
            binding.cvAlarmRandomMissionTap2,
            binding.cvAlarmRandomMissionTap3,
            binding.cvAlarmRandomMissionTap4,
            binding.cvAlarmRandomMissionTap5,
            binding.cvAlarmRandomMissionTap6,
            binding.cvAlarmRandomMissionTap7,
            binding.cvAlarmRandomMissionTap8,
            binding.cvAlarmRandomMissionTap9,
            binding.cvAlarmRandomMissionTap10,
            binding.cvAlarmRandomMissionTap11,
            binding.cvAlarmRandomMissionTap12,
            binding.cvAlarmRandomMissionTap13,
            binding.cvAlarmRandomMissionTap14,
            binding.cvAlarmRandomMissionTap15,
            binding.cvAlarmRandomMissionTap16,
            binding.cvAlarmRandomMissionTap17,
            binding.cvAlarmRandomMissionTap18,
            binding.cvAlarmRandomMissionTap19,
            binding.cvAlarmRandomMissionTap20,
            binding.cvAlarmRandomMissionTap21,
            binding.cvAlarmRandomMissionTap22,
            binding.cvAlarmRandomMissionTap23,
            binding.cvAlarmRandomMissionTap24,
            binding.cvAlarmRandomMissionTap25
        )
    }
    private var answerTapPositions: MutableSet<Int> = hashSetOf()
    private var userTapPositions: MutableSet<Int> = hashSetOf()
    private var currentPage: Int = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmRandomMissionTapBinding.bind(view)
        initData()
        setupBackPressHandler()
        initViews()
        fetchRemoteData()
        initObservers()
        initListeners()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Toast.makeText(context, "뒤로 갈 수 없습니다!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun initData() {
        val args: AlarmRandomMissionTapFragmentArgs by navArgs()
        currentPage = args.currentPageNum
    }

    private fun fetchRemoteData() = with(binding) {
        // TODO: 실제 alarmId, level 받아와서 연결하기
        alarmMissionViewModel.generateFollowClickProblem(alarmId = 1105, level = "상")
    }

    private fun initViews() = with(binding) {
        tvAlarmRandomMissionTapPage.text = "$currentPage/$TOTAL_PAGE_NUM"
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
                if (tap.isSelected) userTapPositions.add(position) else userTapPositions.remove(position)
            }

            if (answerTapPositions == userTapPositions) {
                if (currentPage == 3) {
//                    sleepViewModel.getUserSleepInfo(Constants.USER_ID)
                    Toast.makeText(context, "수고하셨습니다!", Toast.LENGTH_SHORT).show()
                } else {
                    val action = AlarmRandomMissionTapFragmentDirections.actionAlarmRandomMissionTapFragmentSelf(currentPageNum = currentPage + 1)
                    findNavController().navigate(action)
                }
            } else {
                Toast.makeText(context, "답이 틀렸습니다! 다시 입력해주세요!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initObservers() = with(binding) {
        // 알람 미션
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    alarmMissionViewModel.followClickInfo.collect { resource ->
                        when (resource) {
                            is DataResource.Error -> {
                                Toast.makeText(context, "네트워크가 불안정합니다.", Toast.LENGTH_SHORT).show()
                                cvAlarmRandomMissionNextPage.isEnabled = false
                            }
                            DataResource.Idle -> {
                                cvAlarmRandomMissionNextPage.isEnabled = false
                            }
                            DataResource.Loading -> {
                                cvAlarmRandomMissionNextPage.isEnabled = false
                            }
                            is DataResource.Success<List<List<Int>>> -> {
                                for (tap in taps) {
                                    tap.isEnabled = false
                                }
                                cvAlarmRandomMissionNextPage.isEnabled = false

                                val question = resource.data.flatten()
                                question.forEachIndexed { position, status ->
                                    if(status == SELECTED) {
                                        taps[position].apply {
                                            isSelected = true
                                            setCardBackgroundColor(
                                                resources.getColor(
                                                    R.color.alarm_primary,
                                                    context?.theme
                                                )
                                            )
                                        }
                                        answerTapPositions.add(position)
                                    }
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
                                    tap.isEnabled = true
                                }
                                cvAlarmRandomMissionNextPage.isEnabled = true
                            }
                        }
                    }
                }
            }
        }

        // 수면
        sleepViewModel.userSleepRecordList.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    alarmViewModel.alarmDismissedAt = System.currentTimeMillis()
                    val data = result.data
                    val todayRecord = data.find { it.sleepDate == LocalDate.now().toString() }
                    if (todayRecord == null) {
                        // TODO: POST
                        sleepViewModel.registerUserSleepInfo(
                            sleepRequest = SleepRequest(
                                userId = Constants.USER_ID,
                                sleepDate = LocalDate.now().toString(),
                                sleepStart = Instant.ofEpochMilli(sleepViewModel.rawSleepTime ?: 0L)
                                    .toString(),
                                sleepEnd = Instant.ofEpochMilli(
                                    alarmViewModel.alarmDismissedAt ?: 0L
                                ).toString(),
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
                                sleepStart = Instant.ofEpochMilli(sleepViewModel.rawSleepTime ?: 0L)
                                    .toString(),
                                sleepEnd = Instant.ofEpochMilli(
                                    alarmViewModel.alarmDismissedAt ?: 0L
                                ).toString(),
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

    companion object {
        private const val SELECTED = 1
        private const val UNSELECTED = 0
        private const val TOTAL_PAGE_NUM = 3
    }

}
