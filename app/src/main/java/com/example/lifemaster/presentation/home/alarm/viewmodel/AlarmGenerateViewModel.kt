package com.example.lifemaster.presentation.home.alarm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.presentation.home.alarm.model.AlarmRequest
import com.example.lifemaster.presentation.home.alarm.model.AlarmResponse
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionLevel
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionType
import com.example.lifemaster.presentation.home.alarm.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
) : ViewModel() {

    // 랜덤 미션
    private val _randomMission = MutableStateFlow<Map<RandomMissionType, RandomMissionLevel?>?>(null)
    val randomMission = _randomMission.asStateFlow()

    fun setRandomMission(randomMission: Map<RandomMissionType, RandomMissionLevel?>) {
        _randomMission.value = randomMission
    }

    fun resetRandomMission() {
        _randomMission.value = null
    }

    // 알람 미루기 (시간, 횟수)
    private val _snoozeDuration = MutableStateFlow<Pair<Int, Int>>(Pair(10, 2))
    val snoozeDuration = _snoozeDuration.asStateFlow()

    fun setSnoozeDuration(snoozeDuration: Pair<Int, Int>) {
        _snoozeDuration.value = snoozeDuration
    }

    fun resetSnoozeDuration() {
        _snoozeDuration.value = Pair(10, 2)
    }

    // 다시 잠들기 방지 (시간)
    private val _snoozeAntiMinute = MutableStateFlow<Int>(2)
    val snoozeLockMinute = _snoozeAntiMinute.asStateFlow()

    fun setSnoozeLockMinute(snoozeLockMinute: Int) {
        _snoozeAntiMinute.value = snoozeLockMinute
    }

    fun resetSnoozeAntiMinute() {
        _snoozeAntiMinute.value = 2
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

    private val _alarm = MutableStateFlow<DataResource<AlarmResponse>>(DataResource.Idle)
    val alarm: StateFlow<DataResource<AlarmResponse>> get() = _alarm

    // 특정 알람 조회
    fun fetchAlarm(alarmId: Int) {
        viewModelScope.launch {
            _alarm.value = DataResource.Loading
            val result = repository.fetchAlarm(alarmId = alarmId)
            result.onSuccess { alarm ->
                _alarm.value = DataResource.Success(alarm)
            }.onFailure { error ->
                _alarm.value = DataResource.Error(error)
            }
        }
    }

    private val _alarmToggleState = MutableStateFlow<DataResource<Boolean>>(DataResource.Idle)
    val alarmToggleState: StateFlow<DataResource<Boolean>> = _alarmToggleState.asStateFlow()

    // 특정 알람 토글
    fun toggleAlarm(alarmId: Int) {
        viewModelScope.launch {
            _alarmToggleState.value = DataResource.Loading
            val result: Result<Boolean> = repository.toggleAlarm(alarmId = alarmId)
            result.onSuccess { isEnabled ->
                _alarmToggleState.value = DataResource.Success(isEnabled)
            }.onFailure { error ->
                _alarmToggleState.value = DataResource.Error(error)
            }
        }
    }

    private val _alarmUpdateState = MutableStateFlow<DataResource<Unit>>(DataResource.Idle)
    val alarmUpdateState = _alarmUpdateState.asStateFlow()

    // 특정 알람 상태 업데이트
    fun updateAlarm(alarmId: Int, request: AlarmRequest) {
        viewModelScope.launch {
            _alarmUpdateState.value = DataResource.Loading
            val result: Result<Unit> = repository.updateAlarm(alarmId = alarmId, request = request)
            result.onSuccess {
                _alarmUpdateState.value = DataResource.Success(Unit)
            }.onFailure { error ->
                _alarmUpdateState.value = DataResource.Error(error)
            }
        }
    }

    private val _alarmDeleteState = MutableStateFlow<DataResource<Int>>(DataResource.Idle)
    val alarmDeleteState = _alarmDeleteState.asStateFlow()

    // 특정 알람 삭제
    fun deleteAlarm(alarmId: Int) {
        viewModelScope.launch {
            _alarmDeleteState.value = DataResource.Loading
            val result: Result<Int> = repository.deleteAlarm(alarmId = alarmId)
            result.onSuccess { alarmId ->
                _alarmDeleteState.value = DataResource.Success(alarmId)
            }.onFailure { error ->
                _alarmDeleteState.value = DataResource.Error(error)
            }
        }
    }

}