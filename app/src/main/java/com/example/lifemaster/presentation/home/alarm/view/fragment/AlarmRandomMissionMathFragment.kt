package com.example.lifemaster.presentation.home.alarm.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmRandomMissionMathBinding
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModel
import com.google.android.material.card.MaterialCardView
import java.time.LocalDate
import java.time.LocalTime
import android.util.Log
import com.example.lifemaster.presentation.Constants
import com.example.lifemaster.presentation.home.alarm.view.service.AlarmService
import com.example.lifemaster.presentation.home.sleep.model.AlarmInfo
import com.example.lifemaster.presentation.home.sleep.model.AlarmSettingInfo
import com.example.lifemaster.presentation.home.sleep.model.Result
import com.example.lifemaster.presentation.home.sleep.model.SleepRequest
import com.example.lifemaster.presentation.home.sleep.viewmodel.SleepViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant

@AndroidEntryPoint
class AlarmRandomMissionMathFragment : Fragment(R.layout.fragment_alarm_random_mission_math) {

    private lateinit var binding: FragmentAlarmRandomMissionMathBinding
    private val alarmViewModel: AlarmViewModel by activityViewModels()
    private val sleepViewModel: SleepViewModel by activityViewModels()

    private lateinit var numberPadList: List<MaterialCardView> // 1 ~ 9

    private var currentPage: Int = 1
    private var correctAnswer: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("Lifecycle", "onCreate")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmRandomMissionMathBinding.bind(view)
        fetchRemoteData()
        initObservers()
        initViews()
        initListeners()
    }

    private fun fetchRemoteData() {
        alarmViewModel.generateMathProblem(level = MATH_LEVEL_HIGH)
    }

    private fun initObservers() = with(binding) {
        // alarm
        alarmViewModel.mathProblemInfo.observe(viewLifecycleOwner) { mathProblemInfo ->
            tvAlarmRandomMissionMathQuestion.text = mathProblemInfo.question
            correctAnswer = mathProblemInfo.correctAnswer
        }
        // sleep
        sleepViewModel.userSleepRecordList.observe(viewLifecycleOwner) { result ->
                when(result) {
                    is Result.Success -> {
                        alarmViewModel.alarmDismissedAt = System.currentTimeMillis()
                        val data = result.data
                        val todayRecord = data.find { it.sleepDate == LocalDate.now().toString() }
                        if(todayRecord == null) {
                            // POST
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
                            // PATCH
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
                        R.id.action_alarmRandomMissionMathFragment_to_alarmListFragment,
                        bundleOf("origin" to "alarm_random_mission") // TODO: value 값 변경해야하는가? (랜덤 미션 종류별로 분기를 쳐야하는가?)
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
                        R.id.action_alarmRandomMissionMathFragment_to_alarmListFragment,
                        bundleOf("origin" to "alarm_random_mission") // TODO: value 값 변경해야하는가? (랜덤 미션 종류별로 분기를 쳐야하는가?)
                    )
                }
                is Result.Error -> {
                    Toast.makeText(context, "네트워크 연결이 불안정합니다.", Toast.LENGTH_SHORT).show()
                }
                is Result.Loading -> {}
            }
        }

    }

    private fun initViews() = with(binding) {
        // late initialization
        numberPadList = listOf(cvMathNumber1, cvMathNumber2, cvMathNumber3, cvMathNumber4, cvMathNumber5, cvMathNumber6, cvMathNumber7, cvMathNumber8, cvMathNumber9)
        currentPage = arguments?.getInt(PAGE_KEY)!!

        // UI binding
        tvAlarmRandomMissionMathDate.text = "${LocalDate.now().year}년 ${LocalDate.now().monthValue}월 ${LocalDate.now().dayOfMonth}일"
        tvAlarmRandomMissionMathTime.text = "${LocalTime.now().hour}:${LocalTime.now().minute}"
        tvAlarmRandomMissionMathAmPm.text = if(LocalTime.now().hour in 0..11) "am" else "pm"
        tvAlarmRandomMissionMathPage.text = "${currentPage}/${TOTAL_PAGE_NUM}"
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
                findNavController().navigate(
                    R.id.alarmRandomMissionMathFragment,
                    bundleOf(PAGE_KEY to currentPage+1),
                    NavOptions.Builder().setLaunchSingleTop(true).build() // 최상단에 같은 프래그먼트가 있으면 쌓이지 않게 하기
                )
                Toast.makeText(context, "답이 맞았습니다!", Toast.LENGTH_SHORT).show()
            } else if(userAnswer == correctAnswer && currentPage == TOTAL_PAGE_NUM) {
                sleepViewModel.getUserSleepInfo(Constants.USER_ID)
            } else {
                Toast.makeText(context, "답이 틀렸습니다!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val MATH_LEVEL_HIGH = "상"
        private const val MATH_LEVEL_MEDIUM = "중"
        private const val MATH_LEVEL_LOW = "하"
        private const val PAGE_KEY = "currentPageNum"
        private const val TOTAL_PAGE_NUM = 3
    }

}