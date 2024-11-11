package com.example.lifemaster_xml.total.pomodoro

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PomodoroViewModel: ViewModel() {
    // livedata
    private val _selectedPosition = MutableLiveData<Int>() // 직접 세팅할 때 쓰는 값 → 외부에서는 함수로 접근
    val selectedPosition: LiveData<Int> = _selectedPosition // observing 할 때 쓰는 값

    fun setPosition(position: Int) {
        _selectedPosition.value = position
        Log.d("viewmodel", _selectedPosition.value.toString())
    }

    // 버튼 옵져빙
    private val _buttonCount = MutableLiveData<Int>()
    val buttonCount: LiveData<Int> = _buttonCount
    private var btnCnt = 0

    fun clickButton() {
        _buttonCount.value = ++btnCnt
    }
}