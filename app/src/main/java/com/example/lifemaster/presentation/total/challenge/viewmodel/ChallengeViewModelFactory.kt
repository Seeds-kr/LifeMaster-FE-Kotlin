package com.example.lifemaster.presentation.total.challenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lifemaster.network.NetworkService

/**
 * ChallengeViewModel에 의존성을 주입하기 위한 Factory
 * DI(의존성 주입) 패턴을 사용하여 ViewModel을 생성합니다.
 */
class ChallengeViewModelFactory(
    private val apiService: NetworkService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChallengeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChallengeViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

