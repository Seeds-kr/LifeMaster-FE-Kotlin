package com.example.lifemaster.presentation.total.detox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DetoxCommonViewModel: ViewModel() {

    // 실제 사용된 앱의 누적 총 시간
    private val _totalAccumulatedAppUsageTimes: MutableLiveData<Long> = MutableLiveData(0L)
    val totalAccumulatedAppUsageTimes: LiveData<Long> get() = _totalAccumulatedAppUsageTimes

    fun updateTotalAccumulatedAppUsageTimes(newTime: Long) {
        _totalAccumulatedAppUsageTimes.value = newTime
    }

    // 해당 앱이 포그라운드 상태일때의 앱 사용 시간
    private val _tempElapsedForegroundTime: MutableLiveData<Long> = MutableLiveData(0L)
    val tempElapsedForegroundTime: LiveData<Long> get() = _tempElapsedForegroundTime

    fun updateTempElapsedForegroundTime(newTime: Long) {
        _tempElapsedForegroundTime.value = newTime
    }
}