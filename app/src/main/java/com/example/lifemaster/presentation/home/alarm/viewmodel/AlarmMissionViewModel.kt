package com.example.lifemaster.presentation.home.alarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.alarm.model.MathProblemModel
import com.example.lifemaster.presentation.home.alarm.model.MathProblemResponse
import com.example.lifemaster.presentation.home.alarm.model.mapper.toPresentation
import com.example.lifemaster.presentation.home.alarm.repository.AlarmMissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmMissionViewModel @Inject constructor(private val repository: AlarmMissionRepository): ViewModel() {

    private val _mathProblemInfo = MutableStateFlow<DataResource<MathProblemModel>>(DataResource.Idle)
    val mathProblemInfo = _mathProblemInfo.asStateFlow()

    fun generateMathProblem(alarmId: Int, level: String) {
        viewModelScope.launch {
            _mathProblemInfo.value = DataResource.Loading
            val result: Result<MathProblemResponse> = repository.generateMathProblem(alarmId = alarmId, level = level)
            result.onSuccess { response ->
                _mathProblemInfo.value = DataResource.Success(response.toPresentation())
            }.onFailure { error ->
                _mathProblemInfo.value = DataResource.Error(error)
            }
        }
    }

    private val _followClickInfo = MutableStateFlow<DataResource<List<List<Int>>>>(DataResource.Idle)
    val followClickInfo = _followClickInfo.asStateFlow()

    fun generateFollowClickProblem(alarmId: Int, level: String) {
        viewModelScope.launch {
            _followClickInfo.value = DataResource.Loading
            val result: Result<List<List<Int>>> = repository.generateFollowClickProblem(alarmId = alarmId, level = level)
            result.onSuccess { response ->
                _followClickInfo.value = DataResource.Success(response)
            }.onFailure { error ->
                _followClickInfo.value = DataResource.Error(error)
            }
        }
    }
}