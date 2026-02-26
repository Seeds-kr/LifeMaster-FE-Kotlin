package com.example.lifemaster.presentation.total.detox.repository

import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.total.detox.model.DetoxTimeLockRequest
import com.example.lifemaster.presentation.total.detox.model.DetoxTimeLockResponse
import javax.inject.Inject

class DetoxRepository @Inject constructor(private val networkService: NetworkService){
    suspend fun generateTimeLock(request: DetoxTimeLockRequest): Result<DetoxTimeLockResponse> = try {
        val response = networkService.generateTimeLock(request = request)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
