package com.example.lifemaster.presentation.home.pomodoro.emergency_escape

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.lifemaster.R
import com.example.lifemaster.presentation.data.SharedData
import com.example.lifemaster.databinding.ActivityEmergencyEscapeBinding
import com.example.lifemaster.presentation.home.pomodoro.PomodoroStatus
import com.google.android.material.internal.TextWatcherAdapter

class EmergencyEscapeActivity : AppCompatActivity() {
    lateinit var binding: ActivityEmergencyEscapeBinding
    private val viewModel by viewModels<EmergencyEscapeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Activity_EmergencyEscape", "onCreate")
        binding = ActivityEmergencyEscapeBinding.inflate(layoutInflater)

        val remainTimer = intent.getStringExtra("remain_timer")
        binding.tvPomodoroTimer.text = remainTimer

        val sentenceList =
            listOf(binding.tvSentenceFirst, binding.tvSentenceSecond, binding.tvSentenceThird)
        val answerList =
            listOf(binding.etAnswerFirst, binding.etAnswerSecond, binding.etAnswerThird)

        sentenceList.forEachIndexed { idx, sentence ->
            sentence.text = SharedData.pomodoroEmergecyEscapeList[idx]
        }

        viewModel.buttonCount.observe(this) { buttonCount ->
            when (buttonCount) {
                1 -> {
                    // 2페이지
                    sentenceList.forEachIndexed { idx, sentence ->
                        sentence.text = SharedData.pomodoroEmergecyEscapeList[idx + 3] // [?] 좋지 않은 코드
                    }
                    answerList.forEach {
                        it.text.clear()
                    }
                }
                2 -> {
                    // 3페이지
                    sentenceList.forEachIndexed { idx, sentence ->
                        sentence.text = SharedData.pomodoroEmergecyEscapeList[idx + 6]
                    }
                    answerList.forEach {
                        it.text.clear()
                    }
                }
                3 -> {
                    // 4페이지
                    sentenceList.forEachIndexed { idx, sentence ->
                        sentence.text = SharedData.pomodoroEmergecyEscapeList[idx + 9]
                    }
                    answerList.forEach {
                        it.text.clear()
                    }
                }
                4 -> {
                    // 5페이지
                    sentenceList.forEachIndexed { idx, sentence ->
                        sentence.text = SharedData.pomodoroEmergecyEscapeList[idx + 12]
                    }
                    answerList.forEach {
                        it.text.clear()
                    }
                }
                else -> {
                    // 뽀모도로 화면으로 돌아오기
                    SharedData.pomodoroStatus = PomodoroStatus.ESCAPE_SUCCESS
                    finish()
                }
            }
        }

        viewModel.writtenSentence.observe(this) { sentence ->
            binding.btnNextPage.text = "${sentence}/${SharedData.pomodoroEmergecyEscapeList.size} 진행 중"
        }

        edittextWatcher()

        setContentView(binding.root)

        binding.btnNextPage.setOnClickListener {
            if (binding.etAnswerFirst.text.toString() == binding.tvSentenceFirst.text.toString()
                && binding.etAnswerSecond.text.toString() == binding.tvSentenceSecond.text.toString()
                && binding.etAnswerThird.text.toString() == binding.tvSentenceThird.text.toString()
            ) {
                answerList.forEach {
                    if (it.isFocused) it.clearFocus()
                }
                viewModel.clickButton()
            } else if (binding.etAnswerFirst.text.isBlank() || binding.etAnswerSecond.text.isBlank() || binding.etAnswerThird.text.isBlank()) {
                Toast.makeText(this, "아직 입력하지 않은 문장이 있습니다!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "문장을 정확하게 입력해주세요!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivBackToPomodoro.setOnClickListener {
            val dialog = EmergencyEscapeDialog()
            dialog.isCancelable = false
            dialog.show(
                supportFragmentManager, EmergencyEscapeDialog.TAG
            )
        }
    }

    fun edittextWatcher() {

        binding.etAnswerFirst.addTextChangedListener(@SuppressLint("RestrictedApi")
        object : TextWatcherAdapter() {
            override fun afterTextChanged(s: Editable) {
                when (viewModel.btnCount) {
                    0 -> checkUserInputSentence(s, 0, 1)
                    1 -> checkUserInputSentence(s, 3, 1)
                    2 -> checkUserInputSentence(s, 6, 1)
                    3 -> checkUserInputSentence(s, 9, 1)
                    4 -> checkUserInputSentence(s, 12, 1)
                }
            }
        })

        binding.etAnswerSecond.addTextChangedListener(@SuppressLint("RestrictedApi")
        object : TextWatcherAdapter() {
            override fun afterTextChanged(s: Editable) {
                when (viewModel.btnCount) {
                    0 -> checkUserInputSentence(s, 1, 2)
                    1 -> checkUserInputSentence(s, 4, 2)
                    2 -> checkUserInputSentence(s, 7, 2)
                    3 -> checkUserInputSentence(s, 10, 2)
                    4 -> checkUserInputSentence(s, 13, 2)
                }
            }
        })

        binding.etAnswerThird.addTextChangedListener(@SuppressLint("RestrictedApi")
        object : TextWatcherAdapter() {
            override fun afterTextChanged(s: Editable) {
                when (viewModel.btnCount) {
                    0 -> checkUserInputSentence(s, 2, 3)
                    1 -> checkUserInputSentence(s, 5, 3)
                    2 -> checkUserInputSentence(s, 8, 3)
                    3 -> checkUserInputSentence(s, 11, 3)
                    4 -> checkUserInputSentence(s, 14, 3)
                }
            }
        })

        binding.etAnswerFirst.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (binding.etAnswerFirst.text.isEmpty()) viewModel.increaseSentence()
            } else {
                if (binding.etAnswerFirst.text.isEmpty()) viewModel.decreaseSentence()
            }
        }
        binding.etAnswerSecond.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (binding.etAnswerSecond.text.isEmpty()) viewModel.increaseSentence() // [PM] 진행 중 숫자 상태를 empty 에 맞춰야 할까요 아니면 blank 에 맞춰야 할까요?
            } else {
                if (binding.etAnswerSecond.text.isEmpty()) viewModel.decreaseSentence()
            }
        }
        binding.etAnswerThird.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (binding.etAnswerThird.text.isEmpty()) viewModel.increaseSentence()
            } else {
                if (binding.etAnswerThird.text.isEmpty()) viewModel.decreaseSentence()
            }
        }
    }

    fun checkUserInputSentence(s: Editable, sentencePosition: Int, etPosition: Int) {
        when (etPosition) {
            1 -> {
                val size = s.length
                if (s.contentEquals(SharedData.pomodoroEmergecyEscapeList[sentencePosition].take(size))) {
                    binding.cvSentenceFirst.strokeWidth =
                        resources.getDimensionPixelSize(R.dimen.text_watcher_success)
                    binding.etAnswerFirst.setTextColor(getColor(R.color.edit_text_gray))
                } else {
                    binding.cvSentenceFirst.strokeColor = getColor(R.color.red_100)
                    binding.cvSentenceFirst.strokeWidth =
                        resources.getDimensionPixelSize(R.dimen.text_watcher_error)
                    binding.etAnswerFirst.setTextColor(getColor(R.color.red_100))
                }
            }
            2 -> {
                val size = s.length
                if (s.contentEquals(SharedData.pomodoroEmergecyEscapeList[sentencePosition].take(size))) {
                    binding.cvSentenceSecond.strokeWidth =
                        resources.getDimensionPixelSize(R.dimen.text_watcher_success)
                    binding.etAnswerSecond.setTextColor(getColor(R.color.edit_text_gray))
                } else {
                    binding.cvSentenceSecond.strokeColor = getColor(R.color.red_100)
                    binding.cvSentenceSecond.strokeWidth =
                        resources.getDimensionPixelSize(R.dimen.text_watcher_error)
                    binding.etAnswerSecond.setTextColor(getColor(R.color.red_100))
                }
            }
            3 -> {
                val size = s.length
                if (s.contentEquals(SharedData.pomodoroEmergecyEscapeList[sentencePosition].take(size))) {
                    binding.cvSentenceThird.strokeWidth =
                        resources.getDimensionPixelSize(R.dimen.text_watcher_success)
                    binding.etAnswerThird.setTextColor(getColor(R.color.edit_text_gray))
                } else {
                    binding.cvSentenceThird.strokeColor = getColor(R.color.red_100)
                    binding.cvSentenceThird.strokeWidth =
                        resources.getDimensionPixelSize(R.dimen.text_watcher_error)
                    binding.etAnswerThird.setTextColor(getColor(R.color.red_100))
                }
            }
        }
    }

    // text watcher 에서 오류 판정 기준을 한글자가 아닌 자음과 모음까지 분리하기 위해 만든 함수인데 제대로 작동안해서 주석 처리함
//    fun isUserInputValid(input: String, target: String): Boolean {
//        val inputLength = input.length
//        if (input.length > target.length) return false
//
//        val targetSub = target.take(inputLength)
//        if (input == targetSub) return true
//
//        for (char in input) {
//            if(!isValidHangulChar(char)) return false
//        }
//
//        return true
//    }

//    fun isValidHangulChar(char: Char): Boolean {
//        return (char in '\uAC00'..'\uD7A3') ||
//                (char in '\u1100'..'\u11FF') ||
//                (char in '\u3130'..'\u318F')
//    }

    override fun onStart() {
        super.onStart()
        Log.d("Activity_EmergencyEscape", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Activity_EmergencyEscape", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("Activity_EmergencyEscape", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Activity_EmergencyEscape", "onStop")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("Activity_EmergencyEscape", "onRestart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Activity_EmergencyEscape", "onDestroy")
    }
}