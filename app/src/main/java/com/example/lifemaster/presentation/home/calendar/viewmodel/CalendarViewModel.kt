package com.example.lifemaster.presentation.home.calendar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate

enum class CalendarMode { MONTH, WEEK, DAY }

class CalendarViewModel : ViewModel() {
    // 현재 달력 표시 모드
    private val _mode = MutableLiveData(CalendarMode.MONTH)
    val mode: LiveData<CalendarMode> = _mode

    // 사용자가 선택한 날짜
    private val _selectedDate = MutableLiveData(LocalDate.now())
    val selectedDate: LiveData<LocalDate> = _selectedDate

    // 자아성찰(5감사/일기) 기록이 있는 날짜들
    private val _introspectionDates = MutableLiveData<Set<LocalDate>>(emptySet())
    val introspectionDates: LiveData<Set<LocalDate>> = _introspectionDates

    fun setMode(m: CalendarMode) { if (_mode.value != m) _mode.value = m }
    fun selectDate(date: LocalDate) { _selectedDate.value = date }

    fun addIntrospectionDate(date: LocalDate) {
        val current = _introspectionDates.value ?: emptySet()
        _introspectionDates.value = current + date
    }

    fun setIntrospectionDates(dates: Set<LocalDate>) {
        _introspectionDates.value = dates
    }
}