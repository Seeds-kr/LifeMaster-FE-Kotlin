package com.example.lifemaster.presentation.home.alarm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.presentation.home.alarm.model.AlarmRequest
import com.example.lifemaster.presentation.home.alarm.model.AlarmResponse
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.alarm.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * viewmodel의 역할: 비즈니스 로직(repository 요청)과 UI 상태 관리
 */
@HiltViewModel
class AlarmGenerateViewModel @Inject constructor(
    private val repository: AlarmRepository
): ViewModel() {

    // TODO: 알람 생성 관련 변수 저장 필요

    // 알람 시간(시/분)
    private val _alarmTime = MutableLiveData<Pair<Int, Int>>()
    val alarmTime: LiveData<Pair<Int, Int>> get() = _alarmTime

    fun setAlarmTime(alarmTime: Pair<Int, Int>) {
        _alarmTime.value = alarmTime
    }

    private val _randomMission = MutableSharedFlow<Any>(
        replay = 0, // 일회성 이벤트 처리
        extraBufferCapacity = 1 // 뷰모델의 코루틴이 UI 스레드 처리 속도에 의해 일시중지 되는 것 방지
    )
    val randomMission: SharedFlow<Any> = _randomMission

    fun setRandomMission(randomMission: Any) {
        viewModelScope.launch {
            _randomMission.emit(randomMission)
        }
    }

    // 알람 반복 요일
    private val _repeatDays = MutableLiveData<List<String>>()
    val repeatDays: LiveData<List<String>> get() = _repeatDays

    fun setRepeatDays(repeatDays: List<String>) {
        _repeatDays.value = repeatDays
    }

    // 알람 미루기 (시간, 횟수)
    private val _snoozeDuration = MutableSharedFlow<Pair<Int, Int>>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val snoozeDuration: SharedFlow<Pair<Int, Int>> = _snoozeDuration

    fun setSnoozeDuration(snoozeDuration: Pair<Int, Int>) {
        viewModelScope.launch {
            _snoozeDuration.emit(snoozeDuration)
        }
    }

    // 다시 잠들기 방지 (시간)
    private val _snoozeLockMinute = MutableSharedFlow<Int>()
    val snoozeLockMinute: SharedFlow<Int> = _snoozeLockMinute

    fun setSnoozeLockMinute(snoozeLockMinute: Int) {
        viewModelScope.launch {
            _snoozeLockMinute.emit(snoozeLockMinute)
        }
    }

    private val _alarmCreationState = MutableStateFlow<DataResource<Unit>>(DataResource.Idle)
    val alarmCreationState: StateFlow<DataResource<Unit>> = _alarmCreationState

    // 새 알람 생성
    fun createNewAlarm(alarmRequest: AlarmRequest) {
        viewModelScope.launch {
            _alarmCreationState.value = DataResource.Loading
            val result: Result<Unit> = repository.createNewAlarm(alarmRequest = alarmRequest)
            result.onSuccess {
                _alarmCreationState.value = DataResource.Success(Unit)
            }.onFailure { error ->
                _alarmCreationState.value = DataResource.Error(error)
            }
        }
    }

    private val _alarmList = MutableStateFlow<DataResource<List<AlarmResponse>>>(DataResource.Idle)
    val alarmList: StateFlow<DataResource<List<AlarmResponse>>> = _alarmList.asStateFlow()

    // 모든 알람 조회
    fun fetchAlarmList() {
        viewModelScope.launch {
            _alarmList.value = DataResource.Loading
            val result: Result<List<AlarmResponse>> = repository.fetchAlarmList()
            result.onSuccess { alarmList ->
                _alarmList.value = DataResource.Success(alarmList)
            }.onFailure { error ->
                _alarmList.value = DataResource.Error(error)
            }
        }
    }

    private val _alarmToggleState = MutableStateFlow<DataResource<Boolean>>(DataResource.Idle)
    val alarmToggleState: StateFlow<DataResource<Boolean>> = _alarmToggleState.asStateFlow()

    // 특정 알람 토글
    fun toggleAlarm(alarmId: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            _alarmToggleState.value = DataResource.Loading
            val result: Result<Boolean> = repository.toggleAlarm(alarmId = alarmId, isEnabled = isEnabled)
            result.onSuccess { isEnabled ->
                _alarmToggleState.value = DataResource.Success(isEnabled)
            }.onFailure { error ->
                _alarmToggleState.value = DataResource.Error(error)
            }
        }
    }

}