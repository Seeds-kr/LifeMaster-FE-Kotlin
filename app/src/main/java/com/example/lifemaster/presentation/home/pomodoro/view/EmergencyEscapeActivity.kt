package com.example.lifemaster.presentation.home.pomodoro.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lifemaster.R
import com.example.lifemaster.databinding.ActivityEmergencyEscapeBinding
import com.example.lifemaster.network.NetworkService
import com.google.android.material.internal.TextWatcherAdapter
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@AndroidEntryPoint
class EmergencyEscapeActivity : AppCompatActivity() {

    @Inject
    lateinit var networkService: NetworkService

    private var userToken: String? = null
    lateinit var binding: ActivityEmergencyEscapeBinding
    private var currentPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ttest(Escape)", "onCreate")
        binding = ActivityEmergencyEscapeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val (sentenceList, answerList) = initViews()
        initObservers(sentenceList, answerList)
        initTextWatcher(answerList)
        initListeners(sentenceList, answerList)
    }

    private fun initViews(): Pair<List<TextView>, List<EditText>> = with(binding) {

        tvMinutesAndSeconds.text = intent.getStringExtra("remainMinutesAndSeconds")
        tvDecisecond.text = intent.getStringExtra("remainDeciSeconds")

        val questionList = listOf(tvQuestionFirst, tvQuestionSecond, tvQuestionThird)
        val answerList = listOf(etAnswerFirst, etAnswerSecond, etAnswerThird)

        val sharedPreference = getSharedPreferences("USER_TABLE", MODE_PRIVATE)
        userToken = sharedPreference.getString("token", "null")

        questionList.forEach { question ->
            networkService.getEscapeSentence(token = "Bearer $userToken")
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
        updateProgress(answerList)
    }

    private fun initListeners(sentenceList: List<TextView>, answerList: List<EditText>) = with(binding) {
        btnNextPage.setOnClickListener {
            answerList.forEach { if (it.isFocused) it.clearFocus() }
            if (currentPage >= 5) {
                Toast.makeText(this@EmergencyEscapeActivity, "마지막 페이지입니다!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            currentPage += 1
            loadSentences(sentenceList)
            answerList.forEach { it.text.clear() }
            updateProgress(answerList)
//            if (etAnswerFirst.text.toString() == tvQuestionFirst.text.toString()
//                && etAnswerSecond.text.toString() == tvQuestionSecond.text.toString()
//                && etAnswerThird.text.toString() == tvQuestionThird.text.toString()
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
            networkService.getEscapeSentence(token = "Bearer $userToken")
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
    }

    private fun updateProgress(answerList: List<EditText>) {
        val completed = ((currentPage - 1) * 3 + answerList.count { it.text.isNotBlank() })
            .coerceAtMost(15)
        binding.btnNextPage.text = "${completed}/15 진행 중"
    }
}
