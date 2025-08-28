package com.example.lifemaster.presentation.home.sleep.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.home.alarm.view.fragment.Event
import com.example.lifemaster.presentation.home.sleep.model.SleepResponse
import com.example.lifemaster.presentation.home.sleep.model.UserRequest
import kotlinx.coroutines.launch

class SleepViewModel(private val networkService: NetworkService): ViewModel() {

    var wakeTime: String = "" // 금일 일어난 시각(HH:mm) ex) 07:44
    var sleepTime: String = "" // 금일 기준 전날 잠든 시각(HH:mm) ex) 01:11
    var rawSleepTime: Long = 0L

    var sleepDurationHour: Int = 0 // 금일 몇시간 잤는가
    var sleepDurationMinutes: Int = 0 // 금일 몇분 잤는가

    private val _userSleepRecordList = MutableLiveData<List<SleepResponse>>()
    val userSleepRecordList: LiveData<List<SleepResponse>> get() = _userSleepRecordList

    private val _isUserSleepRecordGenerated = MutableLiveData<Event<Boolean>>()
    val isUserSleepRecordGenerated: LiveData<Event<Boolean>> get() = _isUserSleepRecordGenerated

    // 유저의 수면 기록 조회
    fun getUserSleepInfo(userId: Int) {
        viewModelScope.launch {
            try {
                _userSleepRecordList.value = networkService.getUserSleepRecord(userId)
            } catch (e: Exception) {
                Log.e("ERROR", "getUserSleepInfo: $e")
            }
        }
    }

    // 유저의 수면 기록 생성
    fun registerUserSleepInfo(userRequest: UserRequest) {
        viewModelScope.launch {
            try {
                networkService.registerUserSleepRecord(userRequest)
                _isUserSleepRecordGenerated.value = Event(true)
            } catch (e: Exception) {
                _isUserSleepRecordGenerated.value = Event(false)
                Log.e("ERROR", "registerUserSleepInfo: $e")
            }
        }
    }
}