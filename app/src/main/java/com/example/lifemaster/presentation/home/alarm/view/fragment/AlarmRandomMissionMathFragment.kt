package com.example.lifemaster.presentation.home.alarm.view.fragment

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmRandomMissionMathBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModel
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModelFactory
import com.google.android.material.card.MaterialCardView
import java.time.LocalDate
import java.time.LocalTime

class AlarmRandomMissionMathFragment : Fragment(R.layout.fragment_alarm_random_mission_math) {

    private lateinit var binding: FragmentAlarmRandomMissionMathBinding
    private val alarmViewModel: AlarmViewModel by activityViewModels(
        factoryProducer = { AlarmViewModelFactory(RetrofitInstance.networkService) }
    )
    private lateinit var numberPadList: List<MaterialCardView> // 1 ~ 9

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
        alarmViewModel.generateMathProblem(level = MATH_LEVEL_HIGH)
    }

    private fun initObservers() = with(binding) {
        alarmViewModel.mathProblemInfo.observe(viewLifecycleOwner) { mathProblemInfo ->
            tvAlarmRandomMissionMathQuestion.text = mathProblemInfo.question
            correctAnswer = mathProblemInfo.correctAnswer
        }
    }

    private fun initViews() = with(binding) {
        // late initialization
        numberPadList = listOf(cvMathNumber1, cvMathNumber2, cvMathNumber3, cvMathNumber4, cvMathNumber5, cvMathNumber6, cvMathNumber7, cvMathNumber8, cvMathNumber9)

        // UI binding
        tvAlarmRandomMissionMathDate.text = "${LocalDate.now().year}년 ${LocalDate.now().monthValue}월 ${LocalDate.now().dayOfMonth}일"
        tvAlarmRandomMissionMathTime.text = "${LocalTime.now().hour}:${LocalTime.now().minute}"
        tvAlarmRandomMissionMathAmPm.text = if(LocalTime.now().hour in 0..11) "am" else "pm"
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
            } else if(userAnswer == correctAnswer) {
                // TODO: 다음 페이지로 넘어가기 + 문제 갱신하기 + 답변 칸 지우기
                Toast.makeText(context, "답이 맞았습니다!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "답이 틀렸습니다!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val MATH_LEVEL_HIGH = "상"
        private const val MATH_LEVEL_MEDIUM = "중"
        private const val MATH_LEVEL_LOW = "하"
    }

}