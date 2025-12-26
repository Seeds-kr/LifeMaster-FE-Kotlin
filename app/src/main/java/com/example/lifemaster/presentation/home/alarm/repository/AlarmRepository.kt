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

    // 새 알람 생성
    suspend fun createNewAlarm(alarmRequest: AlarmRequest): Result<String> = try {
        networkService.createNewAlarm(alarmRequest = alarmRequest)
        Result.success(alarmRequest.alarmTime)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // 모든 알람 조회
    suspend fun fetchAlarmList(): Result<List<AlarmResponse>> = try {
        val response: List<AlarmResponse> = networkService.fetchAlarmList()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // 특정 알람 조회
    suspend fun fetchAlarm(alarmId: Int): Result<AlarmResponse> = try {
        val response = networkService.fetchAlarm(alarmId = alarmId)
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // 특정 알람 토글 상태 변경
    suspend fun toggleAlarm(alarmId: Int): Result<Boolean> = try {
        val response: Boolean = networkService.toggleAlarm(alarmId = alarmId)
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // 특정 알람 상태 업데이트
    suspend fun updateAlarm(alarmId: Int, request: AlarmRequest): Result<String> = try {
        networkService.updateAlarm(alarmId = alarmId, request = request)
        Result.success(request.alarmTime)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // 특정 알람 삭제
    suspend fun deleteAlarm(alarmId: Int): Result<Int> = try {
        networkService.deleteAlarm(alarmId = alarmId)
        Result.success(alarmId)
    } catch (e: Exception) {
        Result.failure(e)
    }
}