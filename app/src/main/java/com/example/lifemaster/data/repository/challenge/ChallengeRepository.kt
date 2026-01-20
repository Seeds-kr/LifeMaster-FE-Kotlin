package com.example.lifemaster.data.repository.challenge

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.lifemaster.domain.model.ChallengeItem
import com.example.lifemaster.network.NetworkService
import kotlinx.coroutines.flow.Flow

class ChallengeRepository(private val apiService: NetworkService) {

    fun getChallengePagingData(): Flow<PagingData<ChallengeItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { ChallengePagingSource(apiService) }
        ).flow
    }
}
