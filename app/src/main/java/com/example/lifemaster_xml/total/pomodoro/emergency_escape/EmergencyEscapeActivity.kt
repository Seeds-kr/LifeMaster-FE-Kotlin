package com.example.lifemaster_xml.total.pomodoro.emergency_escape

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.viewModels
import com.example.lifemaster_xml.data.Datas
import com.example.lifemaster_xml.databinding.ActivityEmergencyEscapeBinding

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
            answerList.forEach {
                if(it.isFocused) it.clearFocus()
            }
            viewModel.clickButton()
        }
    }

    // https://velog.io/@simsubeen/Android-Kotlin-TextWatcher%EC%99%80-%EC%A0%95%EA%B7%9C%EC%8B%9D%EC%9D%84-%EC%82%AC%EC%9A%A9%ED%95%9C-%ED%9A%8C%EC%9B%90-%EA%B0%80%EC%9E%85-%ED%99%94%EB%A9%B4
    // 숫자 +1 / 문자(한글, 영어) +2
    fun edittextWatcher() {
//        binding.etAnswerFirst.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                // "the count characters beginning at start" are about to be replaced by new text with length after. (이해 완료)
////                Log.d("textWatcher", "s:$s, start:$start, count: $count, after: $after")
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
////                if (s?.isNotEmpty() == true) {
////                    viewModel.increaseSentence()
////                } else {
////                    viewModel.decreaseSentence()
////                }
//                // within s, "the count characters beginning at start" have just replaced old text that had length before. (이해 완료)
////                Log.d("textWatcher", "s: $s, start: $start, before: $before, count: $count")
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                // somewhere within s, the text has been changed. (이해 완료)
////                Log.d("textWatcher", "s: $s")
//            }
//        })

        binding.etAnswerFirst.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                Log.d("focus_first","true")
                if(binding.etAnswerFirst.text.isEmpty()) viewModel.increaseSentence()
            } else {
                Log.d("focus_first","false")
                if(binding.etAnswerFirst.text.isEmpty()) viewModel.decreaseSentence()
            }
        }

        binding.etAnswerSecond.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                Log.d("focus_second","true")
                if(binding.etAnswerSecond.text.isEmpty()) viewModel.increaseSentence() // [PM] 진행 중 숫자 상태를 empty 에 맞춰야 할까요 아니면 blank 에 맞춰야 할까요?
            } else {
                Log.d("focus_second","false")
                if(binding.etAnswerSecond.text.isEmpty()) viewModel.decreaseSentence()
            }
        }

        binding.etAnswerThird.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                Log.d("focus_third","true")
                if(binding.etAnswerThird.text.isEmpty()) viewModel.increaseSentence()
            } else {
                Log.d("focus_third","false")
                if(binding.etAnswerThird.text.isEmpty()) {
                    viewModel.decreaseSentence()
                } else {
                    Log.d("focus_third","isNotEmpty")
                }
            }
        }

//        binding.etAnswerSecond.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (s?.isNotEmpty() == true) {
//                    viewModel.increaseSentence()
//                } else {
//                    viewModel.decreaseSentence()
//                }
//            }
//
//            override fun afterTextChanged(s: Editable?) {}
//        })
//
//        binding.etAnswerThird.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (s?.isNotEmpty() == true) {
//                    viewModel.increaseSentence()
//                } else {
//                    viewModel.decreaseSentence()
//                }
//            }
//
//            override fun afterTextChanged(s: Editable?) {}
//        })
    }
}