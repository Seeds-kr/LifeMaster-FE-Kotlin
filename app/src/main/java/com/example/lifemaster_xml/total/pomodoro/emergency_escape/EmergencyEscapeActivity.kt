package com.example.lifemaster_xml.total.pomodoro.emergency_escape

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.data.Datas
import com.example.lifemaster_xml.databinding.ActivityEmergencyEscapeBinding

class EmergencyEscapeActivity : AppCompatActivity() {
    lateinit var binding: ActivityEmergencyEscapeBinding
    private val viewModel by viewModels<EmergencyEscapeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmergencyEscapeBinding.inflate(layoutInflater)

        val tvSentenceFirst = binding.tvSentenceFirst
        val tvSentenceSecond = binding.tvSentenceSecond
        val tvSentenceThird = binding.tvSentenceThird
        val sentenceList = listOf(tvSentenceFirst, tvSentenceSecond, tvSentenceThird)

        sentenceList.forEachIndexed { idx, sentence ->
            sentence.text = Datas.pomodoroEmergecyEscapeList[idx]
        }

        viewModel.buttonCount.observe(this) { buttonCount ->
            when(buttonCount) {
                1 -> {
                    // 2페이지
                    sentenceList.forEachIndexed { idx, sentence ->
                        sentence.text = Datas.pomodoroEmergecyEscapeList[idx+3] // [?] 좋지 않은 코드
                    }
                }
                2 -> {
                    // 3페이지
                    sentenceList.forEachIndexed { idx, sentence ->
                        sentence.text = Datas.pomodoroEmergecyEscapeList[idx+6]
                    }
                }
                3 -> {
                    // 4페이지
                    sentenceList.forEachIndexed { idx, sentence ->
                        sentence.text = Datas.pomodoroEmergecyEscapeList[idx+9]
                    }
                }
                4 -> {
                    // 5페이지
                    sentenceList.forEachIndexed { idx, sentence ->
                        sentence.text = Datas.pomodoroEmergecyEscapeList[idx+12]
                    }
                }
                else -> {
                    // 원래 화면으로 돌아오기?
                    finish()
                }
            }
        }

        setContentView(binding.root)

        // regex pattern, text watcher 기능 사용
        binding.btnNextPage.setOnClickListener {
            viewModel.clickButton()
        }
    }
}