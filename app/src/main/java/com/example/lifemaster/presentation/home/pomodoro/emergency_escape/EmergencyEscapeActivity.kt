package com.example.lifemaster.presentation.home.pomodoro.emergency_escape

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.lifemaster.R
import com.example.lifemaster.databinding.ActivityEmergencyEscapeBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.data.SharedData
import com.google.android.material.internal.TextWatcherAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmergencyEscapeActivity : AppCompatActivity() {

    private var userToken: String? = null
    lateinit var binding: ActivityEmergencyEscapeBinding
    private val emergencyEscapeViewModel by viewModels<EmergencyEscapeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ttest(Escape)", "onCreate")
        binding = ActivityEmergencyEscapeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val (sentenceList, answerList) = initViews()
        initObservers(sentenceList, answerList)
        initTextWatcher()
        initListeners(answerList)
    }

    private fun initViews(): Pair<List<TextView>, List<EditText>> = with(binding) {

        tvMinutesAndSeconds.text = intent.getStringExtra("remainMinutesAndSeconds")
        tvDecisecond.text = intent.getStringExtra("remainDeciSeconds")

        val questionList = listOf(tvQuestionFirst, tvQuestionSecond, tvQuestionThird)
        val answerList = listOf(etAnswerFirst, etAnswerSecond, etAnswerThird)

        val sharedPreference = getSharedPreferences("USER_TABLE", MODE_PRIVATE)
        userToken = sharedPreference.getString("token", "null")

        questionList.forEach { question ->
            RetrofitInstance.networkService.getEscapeSentence(token = "Bearer $userToken")
                .enqueue(object : Callback<String> {
                    override fun onResponse(
                        call: Call<String?>,
                        response: Response<String?>
                    ) {
                        if (response.isSuccessful) {
                            question.text =
                                response.body()?.substringAfter("Type this phrase to escape:")
                                    ?.trim()
                        }
                    }

                    override fun onFailure(call: Call<String?>, t: Throwable) {
                        TODO("Not yet implemented")
                    }

                })
        }

        return Pair(questionList, answerList)
    }

    private fun initObservers(
        sentenceList: List<TextView>,
        answerList: List<EditText>
    ) {
        emergencyEscapeViewModel.buttonCount.observe(this) { buttonCount ->
            when (buttonCount) {
                1, 2, 3, 4 -> {
                    // 2페이지, 3페이지, 4페이지, 5페이지
                    sentenceList.forEach { sentence ->
                        RetrofitInstance.networkService.getEscapeSentence(token = "Bearer $userToken")
                            .enqueue(object : Callback<String> {
                                override fun onResponse(
                                    call: Call<String?>,
                                    response: Response<String?>
                                ) {
                                    if (response.isSuccessful) {
                                        sentence.text =
                                            response.body()?.substringAfter("Type this phrase to escape:")?.trim()
                                    }
                                }

                                override fun onFailure(call: Call<String?>, t: Throwable) {
                                    TODO("Not yet implemented")
                                }
                            })
                    }
                    answerList.forEach {
                        it.text.clear()
                    }
                }

                5 -> {
                    // 마지막 페이지에서 클릭한 경우
                    // 뽀모도로 화면으로 돌아오기
//                    SharedData.pomodoroStatus = PomodoroStatus.ESCAPE_SUCCESS
//                    finish()
                    Toast.makeText(this@EmergencyEscapeActivity, "마지막 페이지입니다!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        emergencyEscapeViewModel.writtenSentence.observe(this) { sentence ->
            binding.btnNextPage.text = "${sentence}/15 진행 중"
        }
    }

    private fun initListeners(answerList: List<EditText>) = with(binding) {
        btnNextPage.setOnClickListener {
            answerList.forEach { if (it.isFocused) it.clearFocus() }
            emergencyEscapeViewModel.clickButton()
//            if (etAnswerFirst.text.toString() == tvQuestionFirst.text.toString()
//                && etAnswerSecond.text.toString() == tvQuestionSecond.text.toString()
//                && etAnswerThird.text.toString() == tvQuestionThird.text.toString()
//            ) {
//                answerList.forEach {
//                    if (it.isFocused) it.clearFocus()
//                }
//                emergencyEscapeViewModel.clickButton()
//            } else if (etAnswerFirst.text.isBlank() || etAnswerSecond.text.isBlank() || etAnswerThird.text.isBlank()) {
//                Toast.makeText(this@EmergencyEscapeActivity, "아직 입력하지 않은 문장이 있습니다!", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this@EmergencyEscapeActivity, "문장을 정확하게 입력해주세요!", Toast.LENGTH_SHORT).show()
//            }
        }

        ivBackToPomodoro.setOnClickListener {
            val dialog = EmergencyEscapeDialog()
            dialog.isCancelable = false
            dialog.show(supportFragmentManager, EmergencyEscapeDialog.TAG)
        }
    }

    private fun initTextWatcher() = with(binding) {

        etAnswerFirst.addTextChangedListener(
            @SuppressLint("RestrictedApi")
            object : TextWatcherAdapter() {
                override fun afterTextChanged(s: Editable) {
                    checkUserInputSentence(s, 1)
                }
            }
        )

        etAnswerSecond.addTextChangedListener(
            @SuppressLint("RestrictedApi")
            object : TextWatcherAdapter() {
                override fun afterTextChanged(s: Editable) {
                    checkUserInputSentence(s, 2)
                }
            }
        )

        etAnswerThird.addTextChangedListener(
            @SuppressLint("RestrictedApi")
            object : TextWatcherAdapter() {
                override fun afterTextChanged(s: Editable) {
                    checkUserInputSentence(s, 3)
                }
            }
        )

        etAnswerFirst.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (etAnswerFirst.text.isEmpty()) emergencyEscapeViewModel.increaseSentence()
            } else {
                if (etAnswerFirst.text.isEmpty()) emergencyEscapeViewModel.decreaseSentence()
            }
        }

        etAnswerSecond.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (etAnswerSecond.text.isEmpty()) emergencyEscapeViewModel.increaseSentence() // [PM] 진행 중 숫자 상태를 empty 에 맞춰야 할까요 아니면 blank 에 맞춰야 할까요?
            } else {
                if (etAnswerSecond.text.isEmpty()) emergencyEscapeViewModel.decreaseSentence()
            }
        }

        etAnswerThird.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (etAnswerThird.text.isEmpty()) emergencyEscapeViewModel.increaseSentence()
            } else {
                if (etAnswerThird.text.isEmpty()) emergencyEscapeViewModel.decreaseSentence()
            }
        }
    }

    private fun checkUserInputSentence(s: Editable, etPosition: Int) = with(binding) {
        when (etPosition) {
            1 -> {
                // 1번째 edittext
                val size = s.length
                if (s.contentEquals(tvQuestionFirst.text.toString().take(size))
                ) {
                    cvSentenceFirst.strokeWidth =
                        resources.getDimensionPixelSize(R.dimen.text_watcher_success)
                    etAnswerFirst.setTextColor(getColor(R.color.edit_text_gray))
                } else {
                    cvSentenceFirst.strokeColor = getColor(R.color.red_100)
                    cvSentenceFirst.strokeWidth =
                        resources.getDimensionPixelSize(R.dimen.text_watcher_error)
                    etAnswerFirst.setTextColor(getColor(R.color.red_100))
                }
            }

            2 -> {
                // 2번째 edittext
                val size = s.length
                if (s.contentEquals(tvQuestionSecond.text.toString().take(size))
                ) {
                    cvSentenceSecond.strokeWidth =
                        resources.getDimensionPixelSize(R.dimen.text_watcher_success)
                    etAnswerSecond.setTextColor(getColor(R.color.edit_text_gray))
                } else {
                    cvSentenceSecond.strokeColor = getColor(R.color.red_100)
                    cvSentenceSecond.strokeWidth =
                        resources.getDimensionPixelSize(R.dimen.text_watcher_error)
                    etAnswerSecond.setTextColor(getColor(R.color.red_100))
                }
            }

            3 -> {
                // 3번째 edittext
                val size = s.length
                if (s.contentEquals(tvQuestionThird.text.toString().take(size))
                ) {
                    cvSentenceThird.strokeWidth =
                        resources.getDimensionPixelSize(R.dimen.text_watcher_success)
                    etAnswerThird.setTextColor(getColor(R.color.edit_text_gray))
                } else {
                    cvSentenceThird.strokeColor = getColor(R.color.red_100)
                    cvSentenceThird.strokeWidth =
                        resources.getDimensionPixelSize(R.dimen.text_watcher_error)
                    etAnswerThird.setTextColor(getColor(R.color.red_100))
                }
            }
        }
    }

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