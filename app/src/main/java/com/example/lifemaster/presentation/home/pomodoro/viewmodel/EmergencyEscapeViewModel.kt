package com.example.lifemaster.presentation.home.pomodoro.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EmergencyEscapeViewModel : ViewModel() {
    private val _buttonCount = MutableLiveData<Int>()
    val buttonCount: LiveData<Int> = _buttonCount
    var btnCount = 0

    fun clickButton() {
        _buttonCount.value = ++btnCount
    }

    private val _writtenSentence = MutableLiveData<Int>()
    val writtenSentence: LiveData<Int> = _writtenSentence
    private var sentence = 0

    fun increaseSentence() {
        _writtenSentence.value = ++sentence
    }

    fun decreaseSentence() {
        _writtenSentence.value = --sentence
    }
}