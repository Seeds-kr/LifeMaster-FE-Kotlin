package com.example.lifemaster.presentation.home.sleep.viewmodel

import androidx.lifecycle.ViewModel
import java.time.Duration

class SleepViewModel: ViewModel() {
    var sleepDuration: Duration = Duration.ZERO // 총 수면 시간
    var wakeTime: Long = 0L // 다음날 일어난 시각(단위: 밀리초)
    var sleepTime: Long = 0L // 전날 잠든 시각(단위: 밀리초)
    var shouldAddOneMinute: Boolean = false
}