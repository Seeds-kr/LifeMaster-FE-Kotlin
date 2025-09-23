package com.example.lifemaster.presentation.home.alarm.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.home.alarm.model.AlarmItem
import com.example.lifemaster.presentation.home.alarm.model.MathProblemResponse
import kotlinx.coroutines.launch

class AlarmViewModel(private val networkService: NetworkService): ViewModel() {

    var alarmTriggeredAt: Long? = 0L
    var alarmDismissedAt: Long? = 0L

    // dialog -> setting fragment 에서 시간 미루기 UI 업데이트
    private val _delayMinutesAndCount: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
    val delayMinutesAndCount: LiveData<Pair<Int, Int>> get() = _delayMinutesAndCount

    fun setDelayMinutesAndCount(delayMinutesAndCount: Pair<Int,Int>) {
        _delayMinutesAndCount.value = delayMinutesAndCount
    }

    // dialog -> setting fragment 에서 랜덤 미션 UI 업데이트
    private val _randomMissions = MutableLiveData<List<Any>>()
    val randomMissions: LiveData<List<Any>> = _randomMissions

    fun setRandomMissions(randomMissions: List<Any>) {
        _randomMissions.value = randomMissions
    }

    fun clearRandomMissions() {
        _randomMissions.value = emptyList()
    }

    // adapter 에 전달할 알람 아이템들
    private val _alarmItems: MutableLiveData<ArrayList<AlarmItem>> = MutableLiveData()
    val alarmItems: LiveData<ArrayList<AlarmItem>> get() = _alarmItems

    fun updateAlarmItems(newItem: AlarmItem) {
        val currentList = _alarmItems.value ?: arrayListOf()
        currentList.add(newItem)
        _alarmItems.value = currentList
    }

    /**
     * 수학 문제 랜덤 생성
     */
    private val _mathProblemInfo = MutableLiveData<MathProblemResponse>()
    val mathProblemInfo: LiveData<MathProblemResponse> get() = _mathProblemInfo

    fun generateMathProblem(level: String) {
        viewModelScope.launch {
            try {
                _mathProblemInfo.value = networkService.generateMathProblem(level)
            } catch (e: Exception) {
                Log.e("ERROR", "generateMathProblem: $e")
            }
        }
    }

}