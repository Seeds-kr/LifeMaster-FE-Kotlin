package com.example.lifemaster.presentation.home.alarm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster.presentation.home.alarm.model.AlarmItem

class AlarmViewModel: ViewModel() {

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

}