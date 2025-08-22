package com.example.lifemaster.presentation.home.sleep.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.home.sleep.model.SleepResponse
import com.example.lifemaster.presentation.home.sleep.model.UserRequest
import kotlinx.coroutines.launch

class SleepViewModel(private val networkService: NetworkService): ViewModel() {

    var wakeTime: String = "" // 금일 일어난 시각(HH:mm) ex) 07:44
    var sleepTime: String = "" // 금일 기준 전날 잠든 시각(HH:mm) ex) 01:11
    var sleepDurationHour: Int = 0 // 금일 몇시간 잤는가
    var sleepDurationMinutes: Int = 0 // 금일 몇분 잤는가

    private val _userSleepResponseList = MutableLiveData<List<SleepResponse>>()
    val userSleepResponseList: LiveData<List<SleepResponse>> get() = _userSleepResponseList

    fun loadUserSleepInfo(userRequest: UserRequest) {
        viewModelScope.launch {
            try {
                _userSleepResponseList.value = networkService.getUserSleepRecord(userRequest)
            } catch (e: Exception) {
                Log.e("ERROR", ""+ e.message)
            }
        }
    }
}