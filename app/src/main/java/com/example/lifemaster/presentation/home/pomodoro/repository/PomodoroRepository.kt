package com.example.lifemaster.presentation.home.pomodoro.repository

import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroRequest
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroResponse
import javax.inject.Inject

class PomodoroRepository @Inject constructor(private val networkService: NetworkService) {
    suspend fun registerPomodoroItem(pomodoroRequest: PomodoroRequest): Result<PomodoroResponse> = try {
        val response = networkService.registerPomodoroItem(pomodoroRequest = pomodoroRequest)
        if(response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Error Code: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPomodoroAllItems(): Result<List<PomodoroResponse>> = try {
        val response = networkService.getAllPomodoroItems()
        if(response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Error Code: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPomodoroItemsByTodo(todoId: Int): Result<List<PomodoroResponse>> = try {
        val response = networkService.getPomodoroItemsByTodo(todoId = todoId)
        if(response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception("Error Code: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deletePomodoroItemsByTodo(todoId: Int): Result<Int> = try {
        val response = networkService.deletePomodoroItemsByTodo(todoId = todoId)
        if(response.isSuccessful) {
            Result.success(todoId)
        } else {
            Result.failure(Exception("Error Code: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}