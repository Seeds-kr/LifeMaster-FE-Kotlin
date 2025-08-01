package com.example.lifemaster.presentation.home.sleep.viewmodel

import androidx.lifecycle.ViewModel

class SleepViewModel: ViewModel() {
    var wakeTime: String = "" // 다음날 일어난 시각(HH:mm) ex) 07:44
    var sleepTime: String = "" // 전날 잠든 시각(HH:mm) ex) 01:11
    var sleepDurationHour: Int = 0 // 하루 단위 몇시간 잤는가
    var sleepDurationMinutes: Int = 0 // 하루 단위 몇분 잤는가
}