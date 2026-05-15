package com.example.lifemaster.presentation.home.alarm.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.home.alarm.model.AlarmModel
import com.example.lifemaster.presentation.home.alarm.model.MathProblemResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val networkService: NetworkService
): ViewModel() {

    var alarmTriggeredAt: Long? = 0L
    var alarmDismissedAt: Long? = 0L

    // dialog -> setting fragment 에서 시간 미루기 UI 업데이트
    private val _delayMinutesAndCount: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
    val delayMinutesAndCount: LiveData<Pair<Int, Int>> get() = _delayMinutesAndCount

    fun setDelayMinutesAndCount(delayMinutesAndCount: Pair<Int,Int>) {
        _delayMinutesAndCount.value = delayMinutesAndCount
    }

    // adapter 에 전달할 알람 아이템들
    private val _alarmItems: MutableLiveData<ArrayList<AlarmModel>> = MutableLiveData()
    val alarmItems: LiveData<ArrayList<AlarmModel>> get() = _alarmItems

    fun updateAlarmItems(newItem: AlarmModel) {
        val currentList = _alarmItems.value ?: arrayListOf()
        currentList.add(newItem)
        _alarmItems.value = currentList
    }

}