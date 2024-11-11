package com.example.lifemaster_xml.total.pomodoro.emergency_escape

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EmergencyEscapeViewModel : ViewModel() {
    private val _buttonCount = MutableLiveData<Int>()
    val buttonCount: LiveData<Int> = _buttonCount
    private var btnCount = 0

    fun clickButton() {
        _buttonCount.value = ++btnCount
    }
}