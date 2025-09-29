package com.example.lifemaster.presentation.home.todo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lifemaster.network.NetworkService

class ToDoViewModelFactory(private val networkService: NetworkService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ToDoViewModel(networkService) as T
    }
}