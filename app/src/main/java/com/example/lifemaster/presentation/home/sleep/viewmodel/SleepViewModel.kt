package com.example.lifemaster.presentation.home.sleep.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.home.alarm.view.fragment.Event
import com.example.lifemaster.presentation.home.sleep.model.Result
import com.example.lifemaster.presentation.home.sleep.model.SleepResponse
import com.example.lifemaster.presentation.home.sleep.model.UserRequest
import kotlinx.coroutines.launch

class SleepViewModel(private val networkService: NetworkService): ViewModel() {

    var isMeasured: Boolean = false // 수면 시간이 제대로 측정되었는지 유무

    var sleepTime: String? = null // 금일 기준 전날 잠든 시각(HH:mm) ex) 01:11
    var rawSleepTime: Long? = null
    var wakeTime: String? = null // 금일 일어난 시각(HH:mm) ex) 07:44

    var sleepDurationHour: Int? = null // 금일 몇시간 잤는가
    var sleepDurationMinutes: Int? = null // 금일 몇분 잤는가

    private val _isUserSleepRecordGenerated = MutableLiveData<Event<Boolean>>()
    val isUserSleepRecordGenerated: LiveData<Event<Boolean>> get() = _isUserSleepRecordGenerated

    private val _userSleepRecordList = MutableLiveData<Result<List<SleepResponse>>>()
    val userSleepRecordList: LiveData<Result<List<SleepResponse>>> get() = _userSleepRecordList

    private val _userSleepUpdatedRecord = MutableLiveData<Result<SleepResponse>>()
    val userSleepUpdatedRecord: LiveData<Result<SleepResponse>> get() = _userSleepUpdatedRecord

    // 유저의 수면 기록 조회
    fun getUserSleepInfo(userId: Int) {
        viewModelScope.launch {
            _userSleepRecordList.value = Result.Loading
            try {
                val response = networkService.getUserSleepRecord(userId)
                _userSleepRecordList.value = Result.Success(response)
            } catch (e: Exception) {
                _userSleepRecordList.value = Result.Error(e)
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

    // 유저의 수면 기록 업데이트
    fun updateUserSleepInfo(userRequest: UserRequest) {
        viewModelScope.launch {
            try {
                val response = networkService.updateUserSleepRecord(userRequest)
                _userSleepUpdatedRecord.value = Result.Success(response)
            } catch (e: Exception) {
                _userSleepUpdatedRecord.value = Result.Error(e)
            }
        }
    }
}