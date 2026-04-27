package com.example.lifemaster.presentation.home.pomodoro.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lifemaster.databinding.FragmentPomodoroEscapeBinding
import com.example.lifemaster.network.NetworkService
import com.google.android.material.internal.TextWatcherAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class EmergencyEscapeActivity : AppCompatActivity() {

    @Inject
    lateinit var networkService: NetworkService

    lateinit var binding: FragmentPomodoroEscapeBinding
    private var currentPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ttest(Escape)", "onCreate")
        binding = FragmentPomodoroEscapeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val (sentenceList, answerList) = initViews()
        initObservers(sentenceList, answerList)
        initTextWatcher(answerList)
        initListeners(sentenceList, answerList)
    }

    private fun initViews(): Pair<List<TextView>, List<EditText>> = with(binding) {

        tvPomodoroEscapeTypingDate.text =
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"))
        tvPomodoroEscapeTypingTime.text = intent.getStringExtra("remainMinutesAndSeconds") ?: "00:00"
        tvPomodoroEscapeTypingAmPm.text = ""

        val questionList = listOf(
            tvPomodoroEscapeQuestionFirst,
            tvPomodoroEscapeQuestionSecond,
            tvPomodoroEscapeQuestionThird
        )
        val answerList = listOf(
            etPomodoroEscapeAnswerFirst,
            etPomodoroEscapeAnswerSecond,
            etPomodoroEscapeAnswerThird
        )

        loadSentences(questionList)

        return Pair(questionList, answerList)
    }

    private fun initObservers(
        sentenceList: List<TextView>,
        answerList: List<EditText>
    ) {
        updateProgress(answerList)
    }

    private fun initListeners(sentenceList: List<TextView>, answerList: List<EditText>) = with(binding) {
        btnPomodoroEscapeTypingNextPage.setOnClickListener {
            answerList.forEach { if (it.isFocused) it.clearFocus() }
            if (currentPage >= 5) {
                Toast.makeText(this@EmergencyEscapeActivity, "마지막 페이지입니다!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            currentPage += 1
            loadSentences(sentenceList)
            answerList.forEach { it.text.clear() }
            updateProgress(answerList)
//            if (etPomodoroEscapeAnswerFirst.text.toString() == tvPomodoroEscapeQuestionFirst.text.toString()
//                && etPomodoroEscapeAnswerSecond.text.toString() == tvPomodoroEscapeQuestionSecond.text.toString()
//                && etPomodoroEscapeAnswerThird.text.toString() == tvPomodoroEscapeQuestionThird.text.toString()
//            ) {
//                answerList.forEach {
//                    if (it.isFocused) it.clearFocus()
//                }
//            } else if (etAnswerFirst.text.isBlank() || etAnswerSecond.text.isBlank() || etAnswerThird.text.isBlank()) {
//                Toast.makeText(this@EmergencyEscapeActivity, "아직 입력하지 않은 문장이 있습니다!", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this@EmergencyEscapeActivity, "문장을 정확하게 입력해주세요!", Toast.LENGTH_SHORT).show()
//            }
        }
    }

    private fun initTextWatcher(answerList: List<EditText>) {
        answerList.forEach { answer ->
            answer.addTextChangedListener(object : TextWatcherAdapter() {
                override fun afterTextChanged(s: Editable) {
                    updateProgress(answerList)
                }
            })
        }
    }

    private fun loadSentences(sentenceList: List<TextView>) {
        sentenceList.forEach { sentence ->
            lifecycleScope.launch {
                runCatching { networkService.getPomodoroEscapeSentence() }
                    .onSuccess { response ->
                        if (response.isSuccessful) {
                            sentence.text = response.body()
                                ?.substringAfter("Type this phrase to escape:")
                                ?.trim()
                                .orEmpty()
                        }
                    }
                    .onFailure {
                        Toast.makeText(
                            this@EmergencyEscapeActivity,
                            "문장을 불러오지 못했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun updateProgress(answerList: List<EditText>) {
        val completed = ((currentPage - 1) * 3 + answerList.count { it.text.isNotBlank() })
            .coerceAtMost(15)
        binding.btnPomodoroEscapeTypingNextPage.text = "${completed}/15 진행 중"
    }
}
