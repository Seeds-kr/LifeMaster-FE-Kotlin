package com.example.lifemaster.presentation.home.alarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lifemaster.network.NetworkService

class AlarmViewModelFactory(
    private val networkService: NetworkService
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AlarmViewModel(networkService) as T
    }
}