package com.example.lifemaster.presentation.total.detox.repository

import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.total.detox.model.DetoxPermanentLock
import com.example.lifemaster.presentation.total.detox.model.DetoxPermanentLockResponse
import com.example.lifemaster.presentation.total.detox.model.DetoxTimeLockRequest
import com.example.lifemaster.presentation.total.detox.model.DetoxTimeLockResponse
import javax.inject.Inject

class DetoxRepository @Inject constructor(private val networkService: NetworkService){

    suspend fun generatePermanentLock(request: DetoxPermanentLock): Result<Unit> = try {
        val response = networkService.generatePermanentLock(request = request)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun fetchPermanentLockItems(): Result<DetoxPermanentLock> = try {
        val response = networkService.fetchPermanentLockItems()
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun generateTimeLock(request: DetoxTimeLockRequest): Result<Unit> = try {
        val response = networkService.generateTimeLock(request = request)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun fetchTimeLockItems(): Result<List<DetoxTimeLockResponse>> = try {
        val response = networkService.fetchTimeLockItems()
        if(response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteTimeLockItem(id: Long): Result<Unit> = try {
        val response = networkService.deleteTimeLockItem(id = id)
        if(response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(response.message()))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}