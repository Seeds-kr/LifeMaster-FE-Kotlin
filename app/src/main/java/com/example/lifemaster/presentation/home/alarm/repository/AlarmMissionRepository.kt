package com.example.lifemaster.presentation.home.alarm.repository

import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.home.alarm.model.MathProblemResponse
import javax.inject.Inject

class AlarmMissionRepository @Inject constructor(private val networkService: NetworkService) {

    suspend fun generateMathProblem(alarmId: Int, level: String): Result<MathProblemResponse> = try {
        val response = networkService.generateMathProblem(alarmId = alarmId, level = level)
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun generateFollowClickProblem(alarmId: Int, level: String): Result<List<List<Int>>> = try {
        val response = networkService.generateFollowClickProblem(alarmId = alarmId, level = level)
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun generateTypingSentence(alarmId: Int): Result<String> = try {
        val response = networkService.generateTypingSentence(alarmId = alarmId)
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

}