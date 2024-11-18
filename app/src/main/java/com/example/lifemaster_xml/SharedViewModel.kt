package com.example.lifemaster_xml

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel(application: Application): AndroidViewModel(application) {
    private val _isEmergencyEscapeSuccess = MutableLiveData<Boolean>(false)
    val isEmergencyEscapeSuccess: LiveData<Boolean> = _isEmergencyEscapeSuccess

    fun onEscapeSuccess() {
        _isEmergencyEscapeSuccess.value = true
    }

    fun onEscapeReset() {
        _isEmergencyEscapeSuccess.value = false
    }
}