package com.example.lifemaster.presentation.home.alarm.repository

import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.home.alarm.model.AlarmRequest
import com.example.lifemaster.presentation.home.alarm.model.AlarmResponse
import com.example.lifemaster.presentation.home.alarm.model.AlarmToggleRequest
import javax.inject.Inject

/**
 * repository 역할: 네트워크 호출
 */

class AlarmRepository @Inject constructor(private val networkService: NetworkService) {

    suspend fun createNewAlarm(alarmRequest: AlarmRequest): Result<Unit> = try {
        networkService.createNewAlarm(alarmRequest = alarmRequest)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun fetchAlarmList(): Result<List<AlarmResponse>> = try {
        val response: List<AlarmResponse> = networkService.fetchAlarmList()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun toggleAlarm(alarmId: Int, isEnabled: Boolean): Result<Boolean> = try {
        networkService.toggleAlarm(alarmId = alarmId, request = AlarmToggleRequest(isEnabled = isEnabled))
        Result.success(isEnabled)
    } catch (e: Exception) {
        Result.failure(e)
    }
}