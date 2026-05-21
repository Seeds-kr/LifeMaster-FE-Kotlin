package com.example.lifemaster.presentation.home.alarm.view.fragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmRandomMissionMathBinding
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.network.TokenManager
import com.example.lifemaster.presentation.Constants
import com.example.lifemaster.presentation.home.alarm.AlarmConstants.LEVEL_HIGH
import com.example.lifemaster.presentation.home.alarm.model.AlarmModel
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.alarm.util.dismissAlarmDisplayFlow
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class AlarmRandomMissionMathFragment : Fragment(R.layout.fragment_alarm_random_mission_math) {

    private lateinit var binding: FragmentAlarmRandomMissionMathBinding

    @Inject lateinit var networkService: NetworkService
    @Inject lateinit var tokenManager: TokenManager

    private val alarmViewModel: AlarmViewModel by activityViewModels(
        factoryProducer = { AlarmViewModelFactory(networkService) }
    )
    private val sleepViewModel: SleepViewModel by activityViewModels(
        factoryProducer = { SleepViewModelFactory(networkService) }
    )
    private val alarmMissionViewModel: AlarmMissionViewModel by activityViewModels()

    private val numberPadList: List<MaterialCardView> by lazy {
        listOf(binding.cvMathNumber1, binding.cvMathNumber2, binding.cvMathNumber3, binding.cvMathNumber4, binding.cvMathNumber5, binding.cvMathNumber6, binding.cvMathNumber7, binding.cvMathNumber8, binding.cvMathNumber9)
    }
    private val currentPage by lazy {
        val args: AlarmRandomMissionMathFragmentArgs by navArgs()
        args.currentPageNum
    }

    private val alarmItem by lazy {
        val args: AlarmRandomMissionMathFragmentArgs by navArgs()
        args.alarmItem
    }
    private var correctAnswer: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmRandomMissionMathBinding.bind(view)
        fetchRemoteData()
        initObservers()
        initViews()
        initListeners()
    }

    private fun fetchRemoteData() {
        alarmMissionViewModel.generateMathProblem(alarmId = alarmItem.id, level = alarmItem.randomMissionType!!.name)
    }

    private fun initObservers() = with(binding) {

        // 알람 데이터
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    alarmMissionViewModel.mathProblemInfo.collect { resource ->
                        when(resource) {
                            is DataResource.Error -> {}
                            DataResource.Idle -> {}
                            DataResource.Loading -> {}
                            is DataResource.Success -> {
                                val mathProblemInfo = resource.data
                                tvAlarmRandomMissionMathQuestion.text = mathProblemInfo.question
                                correctAnswer = mathProblemInfo.correctAnswer
                            }
                        }
                    }
                }
            }
        }

        // 수면 데이터
        sleepViewModel.userSleepRecordList.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Result.Success -> {
                    alarmViewModel.alarmDismissedAt = System.currentTimeMillis()
                    val data = result.data
                    val todayRecord = data.find { it.sleepDate == LocalDate.now().toString() }
                    val memberId = tokenManager.getMemberId() ?: Constants.USER_ID.toLong()
                    if(todayRecord == null) {
                        // POST
                        sleepViewModel.registerUserSleepInfo(
                            sleepRequest = SleepRequest(
                                userId = memberId,
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
                        // PATCH
                        sleepViewModel.updateUserSleepInfo(
                            sleepRequest = SleepRequest(
                                userId = memberId,
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
                    dismissAlarmDisplayFlow()
                } else {
                    Toast.makeText(context, "네트워크 연결이 불안정합니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        sleepViewModel.userSleepUpdatedRecord.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    Toast.makeText(context, "수면 기록이 업데이트 되었습니다", Toast.LENGTH_SHORT).show()
                    dismissAlarmDisplayFlow()
                }
                is Result.Error -> {
                    Toast.makeText(context, "네트워크 연결이 불안정합니다.", Toast.LENGTH_SHORT).show()
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun initViews() = with(binding) {
        val localDateTime = LocalDateTime.now()
        tvAlarmRandomMissionMathDate.text = "${localDateTime.year}년 ${localDateTime.monthValue}월 ${localDateTime.dayOfMonth}일"
        tvAlarmRandomMissionMathTime.text = String.format("%02d:%02d", localDateTime.hour, localDateTime.minute)
        tvAlarmRandomMissionMathAmPm.text = if(localDateTime.hour in 0..11) "am" else "pm"
        tvAlarmRandomMissionMathPage.text = "$currentPage/$TOTAL_PAGE_NUM"
    }

    private fun initListeners() = with(binding) {

        // 1 ~ 9 버튼 클릭
        for(numberPad in numberPadList) {
            numberPad.setOnClickListener {
                val number = (numberPad.getChildAt(0) as TextView).text
                if(tvAlarmRandomMissionUserAnswer.text.length < 7) {
                    tvAlarmRandomMissionUserAnswer.append(number)
                } else {
                    Toast.makeText(context, "글자 수는 7자리로 제한되어 있습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 0 버튼 클릭
        cvMathNumberZero.setOnClickListener {
            if(tvAlarmRandomMissionUserAnswer.text.length == 0) {
                Toast.makeText(context, "0을 처음에 넣을 수 없습니다", Toast.LENGTH_SHORT).show()
            } else if(tvAlarmRandomMissionUserAnswer.text.length < 7) {
                tvAlarmRandomMissionUserAnswer.append("0")
            } else {
                // length >= 7
                Toast.makeText(context, "글자 수는 7자리로 제한되어 있습니다", Toast.LENGTH_SHORT).show()
            }
        }

        // 지우기 버튼 클릭
        cvMathBackSpace.setOnClickListener {
            if(tvAlarmRandomMissionUserAnswer.text.length > 0) {
                val removedText = tvAlarmRandomMissionUserAnswer.text.dropLast(1)
                tvAlarmRandomMissionUserAnswer.text = removedText
            } else {
                Toast.makeText(context, "더이상 지울 수 없습니다", Toast.LENGTH_SHORT).show()
            }
        }

        // 제출 버튼 클릭
        cvMathSubmitAnswer.setOnClickListener {
            val userAnswer = tvAlarmRandomMissionUserAnswer.text.toString().toIntOrNull()
            if(userAnswer == null) {
                Toast.makeText(context, "답을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if(userAnswer == correctAnswer && currentPage < TOTAL_PAGE_NUM) {
                Toast.makeText(context, "답이 맞았습니다.", Toast.LENGTH_SHORT).show()
                val action = AlarmRandomMissionMathFragmentDirections.actionAlarmRandomMissionMathFragmentSelf(alarmItem = alarmItem, currentPageNum = currentPage + 1)
                findNavController().navigate(action)
            } else if(userAnswer == correctAnswer && currentPage == TOTAL_PAGE_NUM) {
                Toast.makeText(context, "고생하셨습니다.", Toast.LENGTH_SHORT).show()
//                sleepViewModel.getUserSleepInfo(Constants.USER_ID) 수면 연동
                dismissAlarmDisplayFlow()
            } else {
                Toast.makeText(context, "답이 틀렸습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TOTAL_PAGE_NUM = 3
    }

}