package com.example.lifemaster.presentation.home.alarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.presentation.home.alarm.model.AlarmModel
import com.example.lifemaster.presentation.home.alarm.model.AlarmRequest
import com.example.lifemaster.presentation.home.alarm.model.AlarmResponse
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionLevel
import com.example.lifemaster.presentation.home.alarm.model.RandomMissionType
import com.example.lifemaster.presentation.home.alarm.model.mapper.toPresentation
import com.example.lifemaster.presentation.home.alarm.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val _snoozeDuration = MutableStateFlow(Pair(10, 2))
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

    fun resetAlarmData() {
        resetRandomMission()
        resetSnoozeDuration()
        resetSnoozeAntiMinute()
    }

    private val _alarmCreationResult = MutableSharedFlow<DataResource<AlarmModel>>()
    val alarmCreationState = _alarmCreationResult.asSharedFlow()

    // 새 알람 생성
    fun createNewAlarm(alarmRequest: AlarmRequest) {
        viewModelScope.launch {
            _alarmCreationResult.emit(DataResource.Loading)
            val result: Result<AlarmModel> = repository.createNewAlarm(alarmRequest = alarmRequest)
            result.onSuccess { alarm ->
                _alarmCreationResult.emit(DataResource.Success(alarm))
            }.onFailure { error ->
                _alarmCreationResult.emit(DataResource.Error(error))
            }
        }
    }

    private val _alarmList = MutableStateFlow<DataResource<List<AlarmModel>>>(DataResource.Idle)
    val alarmList = _alarmList.asStateFlow()

    // 모든 알람 조회
    fun fetchAlarmList() {
        viewModelScope.launch {
            _alarmList.value = DataResource.Loading
            val result: Result<List<AlarmResponse>> = repository.fetchAlarmList()
            result.onSuccess { alarmList ->
                _alarmList.value = DataResource.Success(alarmList.map { it.toPresentation() })
            }.onFailure { error ->
                _alarmList.value = DataResource.Error(error)
            }
        }
    }

    private val _selectedAlarm = MutableStateFlow<DataResource<AlarmModel>>(DataResource.Idle)
    val selectedAlarm = _selectedAlarm.asStateFlow()

    fun fetchAlarm(alarmId: Int) {
        viewModelScope.launch {
            _selectedAlarm.value = DataResource.Loading
            val result = repository.fetchAlarm(alarmId = alarmId)
            result.onSuccess { alarm ->
                _selectedAlarm.value = DataResource.Success(alarm.toPresentation())
            }.onFailure { error ->
                _selectedAlarm.value = DataResource.Error(error)
            }
        }
    }

    private val _alarmToggleState = MutableSharedFlow<DataResource<Boolean>>()
    val alarmToggleState = _alarmToggleState.asSharedFlow()

    // 특정 알람 토글
    fun toggleAlarm(alarmId: Int) {
        viewModelScope.launch {
            _alarmToggleState.emit(DataResource.Loading)
            val result: Result<Boolean> = repository.toggleAlarm(alarmId = alarmId)
            result.onSuccess { isEnabled ->
                _alarmToggleState.emit( DataResource.Success(isEnabled))
            }.onFailure { error ->
                _alarmToggleState.emit( DataResource.Error(error))
            }
        }
    }

    private val _alarmUpdateState = MutableSharedFlow<DataResource<AlarmModel>>()
    val alarmUpdateState = _alarmUpdateState.asSharedFlow()

    // 특정 알람 상태 업데이트
    fun updateAlarm(alarmId: Int, request: AlarmRequest) {
        viewModelScope.launch {
            _alarmUpdateState.emit(DataResource.Loading)
            val result: Result<AlarmModel> = repository.updateAlarm(alarmId = alarmId, request = request)
            result.onSuccess { alarmItem ->
                _alarmUpdateState.emit( DataResource.Success(alarmItem))
            }.onFailure { error ->
                _alarmUpdateState.emit( DataResource.Error(error))
            }
        }
    }

    private val _alarmDeleteState = MutableSharedFlow<DataResource<Int>>()
    val alarmDeleteState = _alarmDeleteState.asSharedFlow()

    // 특정 알람 삭제
    fun deleteAlarm(alarmId: Int) {
        viewModelScope.launch {
            _alarmDeleteState.emit( DataResource.Loading)
            val result: Result<Int> = repository.deleteAlarm(alarmId = alarmId)
            result.onSuccess { alarmId ->
                _alarmDeleteState.emit( DataResource.Success(alarmId))
            }.onFailure { error ->
                _alarmDeleteState.emit( DataResource.Error(error))
            }
        }
    }

    private val _alarmActivateState = MutableSharedFlow<DataResource<Unit>>()
    val alarmActivateState = _alarmActivateState.asSharedFlow()

    // 전체 알람 활성화
    fun activateAllAlarms() {
        viewModelScope.launch {
            _alarmActivateState.emit( DataResource.Loading)
            val result: Result<Unit> = repository.activateAllAlarms()
            result.onSuccess {
                _alarmActivateState.emit( DataResource.Success(Unit))
            }.onFailure { error ->
                _alarmActivateState.emit( DataResource.Error(error))
            }
        }
    }

    private val _alarmDeactivateState = MutableSharedFlow<DataResource<Unit>>()
    val alarmDeactivateState = _alarmDeactivateState.asSharedFlow()

    // 전체 알람 비활성화
    fun deactivateAllAlarms() {
        viewModelScope.launch {
            _alarmDeactivateState.emit( DataResource.Loading)
            val result: Result<Unit> = repository.deactivateAllAlarms()
            result.onSuccess {
                _alarmDeactivateState.emit( DataResource.Success(Unit))
            }.onFailure { error ->
                _alarmDeactivateState.emit(DataResource.Error(error))
            }
        }
    }

}