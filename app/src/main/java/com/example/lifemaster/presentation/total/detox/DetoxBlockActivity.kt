package com.example.lifemaster.presentation.total.detox

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.example.lifemaster.databinding.ActivityDetoxBlockBinding
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DetoxBlockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetoxBlockBinding
    private var countDownTimer: CountDownTimer? = null

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetoxBlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val blockType = intent.getStringExtra("blockType") ?: "TIME"

        when (blockType) {
            "TIME" -> bindTimeBlockUi()
            "REPEAT" -> bindRepeatBlockUi()
        }

        binding.btnStartPomodoro.setOnClickListener {
            val escapeIntent = Intent(this, DetoxEscapeActivity::class.java).apply {
                putExtra("blockType", blockType)
                putExtra("blockedPackageName", intent.getStringExtra("blockedPackageName"))

                if (blockType == "REPEAT") {
                    putExtra("repeatLockId", intent.getLongExtra("repeatLockId", -1L))
                    putExtra(
                        "currentLockThresholdMinutes",
                        intent.getIntExtra("currentLockThresholdMinutes", 0)
                    )
                    putExtra(
                        "isLastLockSection",
                        intent.getBooleanExtra("isLastLockSection", false)
                    )
                    putExtra(
                        "isDailyLimitLock",
                        intent.getBooleanExtra("isDailyLimitLock", false)
                    )
                }
            }

            startActivity(escapeIntent)
        }
    }

    private fun bindTimeBlockUi() = with(binding) {
        val startTime = intent.getStringExtra("startTime")
        val endTime = intent.getStringExtra("endTime")

        btnStartPomodoro.text = "비상탈출"

        if (startTime.isNullOrBlank() || endTime.isNullOrBlank()) {
            tvAccumulatedTimeTitle.text = "시간 잠금 중"
            tvAccumulatedTime.text = "-"
            tvTimerTitle.text = "다음 사용 가능 시간까지"
            tvMinutesAndSeconds.text = "--:--:--"
            return
        }

        tvAccumulatedTimeTitle.text = "시간 잠금 중"
        tvAccumulatedTime.text = "$startTime ~ $endTime"
        tvTimerTitle.text = "다음 사용 가능 시간까지"
        tvMinutesAndSeconds.text = "00:00:00"

        startTimeCountdown(startTime, endTime)
    }

    private fun bindRepeatBlockUi() = with(binding) {
        val todayUsedMinutes = intent.getIntExtra("todayUsedMinutes", 0)
        val remainingUnlockMinutes = intent.getIntExtra("remainingUnlockMinutes", 0)
        val exceededDailyLimit = intent.getBooleanExtra("exceededDailyLimit", false)
        val escapeAvailable = intent.getBooleanExtra("escapeAvailable", true)

        btnStartPomodoro.text = "비상탈출"

        tvAccumulatedTimeTitle.text = "오늘 누적 사용 시간"
        tvAccumulatedTime.text = formatMinutesToHourMinute(todayUsedMinutes)

        if (exceededDailyLimit) {
            tvTimerTitle.text = "오늘 사용 가능 시간을 모두 사용했어요"
            btnStartPomodoro.isEnabled = escapeAvailable
        } else {
            tvTimerTitle.text = "다음 사용 가능 시간까지"
            btnStartPomodoro.isEnabled = escapeAvailable
        }

        val remainingMillis = remainingUnlockMinutes * 60L * 1000L

        if (remainingMillis <= 0L) {
            tvMinutesAndSeconds.text = "00:00:00"
            return
        }

        startRepeatCountdown(remainingMillis)
    }

    private fun startTimeCountdown(startTime: String, endTime: String) {
        val now = LocalDateTime.now()

        val startLocalTime = LocalTime.parse(startTime, timeFormatter)
        val endLocalTime = LocalTime.parse(endTime, timeFormatter)

        val startDateTime = LocalDateTime.of(LocalDate.now(), startLocalTime)
        var endDateTime = LocalDateTime.of(LocalDate.now(), endLocalTime)

        if (endDateTime.isBefore(startDateTime)) {
            endDateTime = endDateTime.plusDays(1)
        }

        val remainingMillis = java.time.Duration.between(now, endDateTime).toMillis()

        if (remainingMillis <= 0L) {
            binding.tvMinutesAndSeconds.text = "00:00:00"
            finish()
            return
        }

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(remainingMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvMinutesAndSeconds.text = formatMillis(millisUntilFinished)
            }

            override fun onFinish() {
                binding.tvMinutesAndSeconds.text = "00:00:00"
                finish()
            }
        }.start()
    }

    private fun startRepeatCountdown(remainingMillis: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(remainingMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvMinutesAndSeconds.text = formatMillis(millisUntilFinished)
            }

            override fun onFinish() {
                binding.tvMinutesAndSeconds.text = "00:00:00"
                finish()
            }
        }.start()
    }

    private fun formatMillis(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun formatMinutesToHourMinute(minutes: Int): String {
        val hours = minutes / 60
        val remainMinutes = minutes % 60

        return if (hours > 0) {
            "${hours}시간 ${remainMinutes}분"
        } else {
            "${remainMinutes}분"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}