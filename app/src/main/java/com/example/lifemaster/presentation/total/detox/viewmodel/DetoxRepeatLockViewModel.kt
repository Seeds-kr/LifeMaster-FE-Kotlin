package com.example.lifemaster.presentation.total.detox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster.presentation.total.detox.model.DetoxRepeatLockItem
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp

class DetoxRepeatLockViewModel: ViewModel() {

    var blockServiceApplications = arrayListOf<DetoxTargetApp>()

    private val _blockServices : MutableLiveData<List<DetoxTargetApp>> = MutableLiveData()
    val blockServices : LiveData<List<DetoxTargetApp>> get() = _blockServices

    fun updateBlockServices(blockServices: List<DetoxTargetApp>) {
        _blockServices.value = blockServices
    }

    //////////////////////////////     --- 구분선 입니다 ---  /////////////////////////////////////

    // 디톡스 프래그먼트의 반복잠금 설정 다이얼로그
    var repeatLockTargetApplications = arrayListOf<DetoxTargetApp>()

    private val _repeatLockTargetApp: MutableLiveData<DetoxTargetApp> = MutableLiveData()
    val repeatLockTargetApp: LiveData<DetoxTargetApp> get() = _repeatLockTargetApp

    fun updateRepeatLockTargetApp(repeatLockTargetApp: DetoxTargetApp) {
        _repeatLockTargetApp.value = repeatLockTargetApp
    }


    // 디톡스 프래그먼트의 반복 잠금 리스트
    private val _repeatLockApp: MutableLiveData<ArrayList<DetoxRepeatLockItem>> = MutableLiveData()
    val repeatLockApp: LiveData<ArrayList<DetoxRepeatLockItem>> get() = _repeatLockApp

    fun addRepeatLockApp(repeatLockApp: DetoxRepeatLockItem) {
        val currentList = _repeatLockApp.value ?: arrayListOf()
        currentList.add(repeatLockApp)
        _repeatLockApp.value = currentList
    }

}