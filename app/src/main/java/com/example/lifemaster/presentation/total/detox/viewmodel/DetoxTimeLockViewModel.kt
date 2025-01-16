package com.example.lifemaster.presentation.total.detox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp
import com.example.lifemaster.presentation.total.detox.model.DetoxTimeLockItem

class DetoxTimeLockViewModel: ViewModel() {

    var allowServiceApplications = arrayListOf<DetoxTargetApp>()

    private val _allowServices : MutableLiveData<List<DetoxTargetApp>> = MutableLiveData()
    val allowServices : LiveData<List<DetoxTargetApp>> get() = _allowServices

    fun updateAllowServices(allowServices: List<DetoxTargetApp>) {
        _allowServices.value = allowServices
    }

    private val _timeLockItems: MutableLiveData<ArrayList<DetoxTimeLockItem>> = MutableLiveData()
    val timeLockItems: LiveData<ArrayList<DetoxTimeLockItem>> get() = _timeLockItems

    fun addTimeLockItems(timeLockItem: DetoxTimeLockItem) {
        val currentList = _timeLockItems.value ?: arrayListOf()
        currentList.add(timeLockItem)
        _timeLockItems.value = currentList
    }
}