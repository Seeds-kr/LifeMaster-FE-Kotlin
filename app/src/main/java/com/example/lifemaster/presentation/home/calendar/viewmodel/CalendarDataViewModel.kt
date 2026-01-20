package com.example.lifemaster.presentation.home.calendar.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.presentation.home.calendar.model.CalendarRepository
import kotlinx.coroutines.launch

class CalendarDataViewModel(
    private val repo: CalendarRepository
) : ViewModel() {

    private val _monthEvents = MutableLiveData<Map<String, List<String>>>()
    val monthEvents: LiveData<Map<String, List<String>>> = _monthEvents

    fun loadMonth(year: Int, month1: Int, memberId: Long? = null) {
        viewModelScope.launch {
            runCatching {
                if (memberId != null && memberId > 0L) {
                    repo.getMemberMonth(memberId, year, month1)
                } else {
                    repo.getMonth(year, month1)
                }
            }.onSuccess { list ->
                _monthEvents.value = list.associate { it.date to it.events }
            }.onFailure {
                _monthEvents.value = emptyMap()
            }
        }
    }

    fun loadMonths(months: List<Pair<Int, Int>>, memberId: Long? = null) {
        viewModelScope.launch {
            val acc = linkedMapOf<String, List<String>>()
            for ((y, m) in months.distinct()) {
                runCatching {
                    if (memberId != null && memberId > 0L) {
                        repo.getMemberMonth(memberId, y, m)
                    } else {
                        repo.getMonth(y, m)
                    }
                }.onSuccess { list ->
                    list.forEach { acc[it.date] = it.events }
                }
            }

            _monthEvents.value = acc
        }
    }
}