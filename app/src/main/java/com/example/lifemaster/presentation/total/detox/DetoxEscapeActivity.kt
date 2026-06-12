package com.example.lifemaster.presentation.total.detox

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.lifemaster.R
import com.example.lifemaster.databinding.ActivityDetoxEscapeBinding
import com.example.lifemaster.network.NetworkService
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class DetoxEscapeActivity : AppCompatActivity() {

    @Inject
    lateinit var networkService: NetworkService

    private lateinit var binding: ActivityDetoxEscapeBinding
    private var currentPage = 1

    private val blockType by lazy {
        intent.getStringExtra("blockType") ?: "TIME"
    }

    private val questionList by lazy {
        listOf(
            binding.tvPomodoroEscapeQuestionFirst,
            binding.tvPomodoroEscapeQuestionSecond,
            binding.tvPomodoroEscapeQuestionThird
        )
    }

    private val answerList by lazy {
        listOf(
            binding.etPomodoroEscapeAnswerFirst,
            binding.etPomodoroEscapeAnswerSecond,
            binding.etPomodoroEscapeAnswerThird
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetoxEscapeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        loadSentences()
        initTextWatcher()
        initListeners()
        updateProgress()
    }

    private fun initViews() = with(binding) {
        val localDateTime = LocalDateTime.now()

        tvPomodoroEscapeTypingDate.text =
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"))

        tvPomodoroEscapeTypingTime.text =
            String.format("%02d:%02d", localDateTime.hour, localDateTime.minute)

        tvPomodoroEscapeTypingAmPm.text =
            if (localDateTime.hour in 0..11) "am" else "pm"
    }

    private fun initListeners() = with(binding) {
        btnPomodoroEscapeTypingNextPage.setOnClickListener {
            val isCorrect =
                etPomodoroEscapeAnswerFirst.text.toString() == tvPomodoroEscapeQuestionFirst.text.toString() &&
                        etPomodoroEscapeAnswerSecond.text.toString() == tvPomodoroEscapeQuestionSecond.text.toString() &&
                        etPomodoroEscapeAnswerThird.text.toString() == tvPomodoroEscapeQuestionThird.text.toString()

            val hasBlank =
                etPomodoroEscapeAnswerFirst.text.isBlank() ||
                        etPomodoroEscapeAnswerSecond.text.isBlank() ||
                        etPomodoroEscapeAnswerThird.text.isBlank()

            when {
                isCorrect -> {
                    if (currentPage < TOTAL_PAGE_COUNT) {
                        currentPage += 1
                        answerList.forEach { it.text.clear() }
                        resetAnswerCardStyles()
                        loadSentences()
                        updateProgress()
                    } else {
                        verifyPhrase(tvPomodoroEscapeQuestionThird.text.toString())
                    }
                }

                hasBlank -> {
                    Toast.makeText(
                        this@DetoxEscapeActivity,
                        "아직 입력하지 않은 문장이 있습니다!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    Toast.makeText(
                        this@DetoxEscapeActivity,
                        "문장을 정확하게 입력해주세요!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun initTextWatcher() {
        answerList.forEachIndexed { position, answerEditText ->
            answerEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) = Unit

                override fun afterTextChanged(currentEditText: Editable) {
                    val size = currentEditText.length
                    val parentCardView = answerEditText.parent as MaterialCardView
                    val expectedText = questionList[position].text.toString().take(size)

                    if (currentEditText.isEmpty()) {
                        parentCardView.strokeWidth = 0
                        answerEditText.setTextColor(
                            getColor(this@DetoxEscapeActivity, R.color.black_800)
                        )
                    } else if (currentEditText.contentEquals(expectedText)) {
                        parentCardView.strokeWidth = 0
                        answerEditText.setTextColor(
                            getColor(this@DetoxEscapeActivity, R.color.black_800)
                        )
                    } else {
                        parentCardView.strokeWidth =
                            resources.getDimensionPixelSize(R.dimen.text_watcher_error)

                        parentCardView.strokeColor =
                            getColor(this@DetoxEscapeActivity, R.color.red_100)

                        answerEditText.setTextColor(
                            getColor(this@DetoxEscapeActivity, R.color.red_100)
                        )
                    }

                    updateProgress()
                }
            })
        }
    }

    private fun loadSentences() {
        lifecycleScope.launch {
            questionList.forEach { sentence ->
                runCatching { networkService.generateDetoxEscapePhrase() }
                    .onSuccess { response ->
                        if (response.isSuccessful) {
                            sentence.text = response.body()
                                ?.substringAfter("Type this phrase to escape:")
                                ?.trim()
                                .orEmpty()
                        } else {
                            Toast.makeText(
                                this@DetoxEscapeActivity,
                                "문장을 불러오지 못했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .onFailure {
                        Toast.makeText(
                            this@DetoxEscapeActivity,
                            "문장을 불러오지 못했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun verifyPhrase(input: String) {
        lifecycleScope.launch {
            runCatching { networkService.verifyDetoxEscapePhrase(input) }
                .onSuccess { response ->
                    if (response.isSuccessful) {
                        when (blockType) {
                            "TIME" -> clearTimeBlockService()
                            "REPEAT" -> escapeRepeatLock()
                        }

                        Toast.makeText(
                            this@DetoxEscapeActivity,
                            "비상탈출이 완료되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()

                        finishAffinity()
                    } else {
                        Toast.makeText(
                            this@DetoxEscapeActivity,
                            "문장 검증에 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .onFailure {
                    Toast.makeText(
                        this@DetoxEscapeActivity,
                        "오류가 발생했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun escapeRepeatLock() {
        val repeatLockId = intent.getLongExtra("repeatLockId", -1L)
        val currentLockThresholdMinutes =
            intent.getIntExtra("currentLockThresholdMinutes", 0)
        val isDailyLimitLock =
            intent.getBooleanExtra("isDailyLimitLock", false)

        if (repeatLockId == -1L || currentLockThresholdMinutes == 0) return

        DetoxRepeatLockLocalManager.escapeRepeatLock(
            context = this,
            id = repeatLockId,
            currentLockThresholdMinutes = currentLockThresholdMinutes,
            isDailyLimitLock = isDailyLimitLock
        )
    }

    private fun clearTimeBlockService() {
        val intent = Intent("com.example.lifemaster.BROADCAST_RECEIVER").apply {
            putStringArrayListExtra("TIME_BLOCK_SERVICE_APPLICATIONS", arrayListOf())
            putStringArrayListExtra("TIME_BLOCK_SERVICE_INFOS", arrayListOf())
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun resetAnswerCardStyles() {
        answerList.forEach { answerEditText ->
            val parentCardView = answerEditText.parent as MaterialCardView
            parentCardView.strokeWidth = 0
            answerEditText.setTextColor(getColor(this, R.color.black_800))
        }
    }

    private fun updateProgress() {
        val completed = ((currentPage - 1) * 3 + answerList.count { it.text.isNotBlank() })
            .coerceAtMost(TOTAL_SENTENCE_COUNT)

        binding.btnPomodoroEscapeTypingNextPage.text =
            "${completed}/${TOTAL_SENTENCE_COUNT} 진행 중"
    }

    companion object {
        private const val TOTAL_SENTENCE_COUNT = 15
        private const val TOTAL_PAGE_COUNT = 5
    }
}