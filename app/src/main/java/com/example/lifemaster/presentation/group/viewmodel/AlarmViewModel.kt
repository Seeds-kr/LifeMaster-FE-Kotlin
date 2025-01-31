package com.example.lifemaster.presentation.group.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AlarmViewModel: ViewModel() {
    private val _delayMinutesAndCount: MutableLiveData<Pair<Int, Int>> = MutableLiveData()
    val delayMinutesAndCount: LiveData<Pair<Int, Int>> get() = _delayMinutesAndCount

    fun setDelayMinutesAndCount(delayMinutesAndCount: Pair<Int,Int>) {
        _delayMinutesAndCount.value = delayMinutesAndCount
    }
}