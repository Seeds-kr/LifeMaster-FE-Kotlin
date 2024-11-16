package com.example.lifemaster_xml.total.pomodoro.emergency_escape

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.activity.viewModels
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.data.Datas
import com.example.lifemaster_xml.databinding.ActivityEmergencyEscapeBinding
import com.google.android.material.internal.TextWatcherAdapter

class EmergencyEscapeActivity : AppCompatActivity() {
    lateinit var binding: ActivityEmergencyEscapeBinding
    private val viewModel by viewModels<EmergencyEscapeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyEscapeBinding.inflate(layoutInflater)

        val sentenceList =
            listOf(binding.tvSentenceFirst, binding.tvSentenceSecond, binding.tvSentenceThird)
        val answerList =
            listOf(binding.etAnswerFirst, binding.etAnswerSecond, binding.etAnswerThird)

        sentenceList.forEachIndexed { idx, sentence ->
            sentence.text = Datas.pomodoroEmergecyEscapeList[idx]
        }

        viewModel.buttonCount.observe(this) { buttonCount ->
            when (buttonCount) {
                1 -> {
                    // 2페이지
                    sentenceList.forEachIndexed { idx, sentence ->
                        sentence.text = Datas.pomodoroEmergecyEscapeList[idx + 3] // [?] 좋지 않은 코드
                    }
                    answerList.forEach {
                        it.text.clear()
                    }
                }
                2 -> {
                    // 3페이지
                    sentenceList.forEachIndexed { idx, sentence ->
                        sentence.text = Datas.pomodoroEmergecyEscapeList[idx + 6]
                    }
                    answerList.forEach {
                        it.text.clear()
                    }
                }
                3 -> {
                    // 4페이지
                    sentenceList.forEachIndexed { idx, sentence ->
                        sentence.text = Datas.pomodoroEmergecyEscapeList[idx + 9]
                    }
                    answerList.forEach {
                        it.text.clear()
                    }
                }
                4 -> {
                    // 5페이지
                    sentenceList.forEachIndexed { idx, sentence ->
                        sentence.text = Datas.pomodoroEmergecyEscapeList[idx + 12]
                    }
                    answerList.forEach {
                        it.text.clear()
                    }
                }
                else -> {
                    // 뽀모도로 화면으로 돌아오기
                    finish()
                }
            }
        }

        viewModel.writtenSentence.observe(this) { sentence ->
            binding.btnNextPage.text = "${sentence}/${Datas.pomodoroEmergecyEscapeList.size} 진행 중"
        }

        edittextWatcher()

        setContentView(binding.root)

        binding.btnNextPage.setOnClickListener {
            if (binding.etAnswerFirst.text.isNotEmpty() && binding.etAnswerSecond.text.isNotEmpty() && binding.etAnswerThird.text.isNotEmpty()) { // [?] CharSequence vs String
                answerList.forEach {
                    if (it.isFocused) it.clearFocus()
                }
                viewModel.clickButton()
            } else {
                Toast.makeText(this, "아직 입력하지 않은 문장이 있습니다!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun edittextWatcher() {

        binding.etAnswerFirst.addTextChangedListener(@SuppressLint("RestrictedApi")
        object: TextWatcherAdapter() {
            override fun afterTextChanged(s: Editable) {
                when(viewModel.btnCount) {
                    0 -> checkUserInputSentence(s, 0, 1)
                    1 -> checkUserInputSentence(s, 3, 1)
                    2 -> checkUserInputSentence(s, 6, 1)
                    3 -> checkUserInputSentence(s, 9, 1)
                    4 -> checkUserInputSentence(s, 12, 1)
                }
            }
        })

        binding.etAnswerSecond.addTextChangedListener(@SuppressLint("RestrictedApi")
        object: TextWatcherAdapter() {
            override fun afterTextChanged(s: Editable) {
                when(viewModel.btnCount) {
                    0 -> checkUserInputSentence(s, 1, 2)
                    1 -> checkUserInputSentence(s, 4, 2)
                    2 -> checkUserInputSentence(s, 7, 2)
                    3 -> checkUserInputSentence(s, 10, 2)
                    4 -> checkUserInputSentence(s, 13, 2)
                }
            }
        })

        binding.etAnswerThird.addTextChangedListener(@SuppressLint("RestrictedApi")
        object: TextWatcherAdapter() {
            override fun afterTextChanged(s: Editable) {
                when(viewModel.btnCount) {
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
        when(etPosition) {
            1 -> {
                val size = s.length
                if(s.contentEquals(Datas.pomodoroEmergecyEscapeList[sentencePosition].take(size))) {
                    binding.cvSentenceFirst.strokeWidth = resources.getDimensionPixelSize(R.dimen.text_watcher_success)
                    binding.etAnswerFirst.setTextColor(getColor(R.color.edit_text_gray))
                } else {
                    binding.cvSentenceFirst.strokeColor = getColor(R.color.red_100)
                    binding.cvSentenceFirst.strokeWidth = resources.getDimensionPixelSize(R.dimen.text_watcher_error)
                    binding.etAnswerFirst.setTextColor(getColor(R.color.red_100))
                }
            }
            2 -> {
                val size = s.length
                if(s.contentEquals(Datas.pomodoroEmergecyEscapeList[sentencePosition].take(size))) {
                    binding.cvSentenceSecond.strokeWidth = resources.getDimensionPixelSize(R.dimen.text_watcher_success)
                    binding.etAnswerSecond.setTextColor(getColor(R.color.edit_text_gray))
                } else {
                    binding.cvSentenceSecond.strokeColor = getColor(R.color.red_100)
                    binding.cvSentenceSecond.strokeWidth = resources.getDimensionPixelSize(R.dimen.text_watcher_error)
                    binding.etAnswerSecond.setTextColor(getColor(R.color.red_100))
                }
            }
            3 -> {
                val size = s.length
                if(s.contentEquals(Datas.pomodoroEmergecyEscapeList[sentencePosition].take(size))) {
                    binding.cvSentenceThird.strokeWidth = resources.getDimensionPixelSize(R.dimen.text_watcher_success)
                    binding.etAnswerThird.setTextColor(getColor(R.color.edit_text_gray))
                } else {
                    binding.cvSentenceThird.strokeColor = getColor(R.color.red_100)
                    binding.cvSentenceThird.strokeWidth = resources.getDimensionPixelSize(R.dimen.text_watcher_error)
                    binding.etAnswerThird.setTextColor(getColor(R.color.red_100))
                }
            }
        }
    }
}