package com.example.lifemaster.presentation.total.detox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp
import com.example.lifemaster.presentation.total.detox.model.DetoxTimeLockItem
import com.example.lifemaster.presentation.total.detox.model.DetoxTimeLockRequest
import com.example.lifemaster.presentation.total.detox.model.DetoxTimeLockResponse
import com.example.lifemaster.presentation.total.detox.repository.AppListRepository
import com.example.lifemaster.presentation.total.detox.repository.DetoxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetoxTimeLockViewModel @Inject constructor(
    private val detoxRepository: DetoxRepository,
    private val appListRepository: AppListRepository
) : ViewModel() {

    init {
        loadApplications() // init 블록 내의 코드는 ViewModel이 생성되자마자 실행된다
    }

    private val _repeatLockTargetApplications = MutableStateFlow<List<DetoxTargetApp>>(emptyList())
    val repeatLockTargetApplications: StateFlow<List<DetoxTargetApp>> = _repeatLockTargetApplications

    private fun loadApplications() {
        _repeatLockTargetApplications.value = appListRepository.fetchInstalledApps()
    }

    var allowServiceApplications = arrayListOf<DetoxTargetApp>()

    private val _timeLockItems: MutableLiveData<ArrayList<DetoxTimeLockItem>> = MutableLiveData()
    val timeLockItems: LiveData<ArrayList<DetoxTimeLockItem>> get() = _timeLockItems

    fun addTimeLockItems(timeLockItem: DetoxTimeLockItem) {
        val currentList = _timeLockItems.value ?: arrayListOf()
        currentList.add(timeLockItem)
        _timeLockItems.value = currentList
    }

    private val _generateTimeLockResult = MutableSharedFlow<DataResource<DetoxTimeLockResponse>>()
    val generateTimeLockResult = _generateTimeLockResult.asSharedFlow()

    fun generateTimeLock(request: DetoxTimeLockRequest) {
        viewModelScope.launch {
            detoxRepository.generateTimeLock(request = request).onSuccess {
                _generateTimeLockResult.emit(DataResource.Success(it))
            }.onFailure {
                _generateTimeLockResult.emit(DataResource.Error(it))
            }
        }
    }
}