package com.example.lifemaster.presentation.home.alarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.alarm.model.MathProblemModel
import com.example.lifemaster.presentation.home.alarm.model.MathProblemResponse
import com.example.lifemaster.presentation.home.alarm.model.mapper.toPresentation
import com.example.lifemaster.presentation.home.alarm.repository.AlarmMissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

    private val _typingSentenceInfo = MutableStateFlow<DataResource<List<String>>>(DataResource.Idle)
    val typingSentenceInfo = _typingSentenceInfo.asStateFlow()

    fun generateTypingSentence(count: Int, alarmId: Int) {
        viewModelScope.launch {
            _typingSentenceInfo.value = DataResource.Loading
            try {
                // 1. 동시에 여러 요청을 보냄
                val deferredResults: List<Deferred<Result<String>>> = (1..count).map {
                    async { repository.generateTypingSentence(alarmId = alarmId) }
                }
                // 2. 모든 결과가 올 때까지 기다림
                val results: List<Result<String>> = deferredResults.awaitAll()
                // 3. 결과 분석 및 리스트 추출
                val sentences = mutableListOf<String>()
                for(result in results) {
                    result.onSuccess { sentences.add(it) }.onFailure { throw it }
                }
                _typingSentenceInfo.value = DataResource.Success(sentences)
            } catch (e: Exception) {
                _typingSentenceInfo.value = DataResource.Error(e)
            }
        }
    }

    private val _writtenSentenceCount = MutableStateFlow<Int>(0)
    val writtenSentenceCount = _writtenSentenceCount.asStateFlow()

    fun increaseSentenceCount() {
        _writtenSentenceCount.value += 1
    }

    fun decreaseSentenceCount() {
        _writtenSentenceCount.value -= 1
    }

    fun clearData() {
        _writtenSentenceCount.value = 0
    }
}