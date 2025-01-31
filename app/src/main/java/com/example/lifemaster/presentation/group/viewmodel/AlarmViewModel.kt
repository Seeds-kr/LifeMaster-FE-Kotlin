package com.example.lifemaster.presentation.group.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster.presentation.group.model.AlarmItem
import com.example.lifemaster.presentation.total.detox.model.DetoxRepeatLockItem

class AlarmViewModel: ViewModel() {

    // dialog -> setting fragment 에서 시간 미루기 관련 값 UI 업데이트
    private val _delayMinutesAndCount: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
    val delayMinutesAndCount: LiveData<Pair<Int, Int>> get() = _delayMinutesAndCount

    fun setDelayMinutesAndCount(delayMinutesAndCount: Pair<Int,Int>) {
        _delayMinutesAndCount.value = delayMinutesAndCount
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