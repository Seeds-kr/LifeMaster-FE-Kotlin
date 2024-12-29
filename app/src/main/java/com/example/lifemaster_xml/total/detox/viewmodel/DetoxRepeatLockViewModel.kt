package com.example.lifemaster_xml.total.detox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.total.detox.model.DetoxTargetApp

class DetoxRepeatLockViewModel: ViewModel() {

    val allowServiceApplications = arrayListOf(
        DetoxTargetApp(R.drawable.ic_insta),
        DetoxTargetApp(R.drawable.ic_kakaotalk),
        DetoxTargetApp(R.drawable.ic_twitter),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_ticktock),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_ticktock),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_insta),
        DetoxTargetApp(R.drawable.ic_kakaotalk),
        DetoxTargetApp(R.drawable.ic_twitter),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_ticktock),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_ticktock),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify)
    )

    private val _allowServices : MutableLiveData<List<DetoxTargetApp>> = MutableLiveData()
    val allowServices : LiveData<List<DetoxTargetApp>> get() = _allowServices

    fun updateAllowServices(allowServices: List<DetoxTargetApp>) {
        _allowServices.value = allowServices
    }

    //////////////////////////////     --- 구분선 입니다 ---  /////////////////////////////////////

    val repeatLockTargetApplications = arrayListOf(
        DetoxTargetApp(R.drawable.ic_insta),
        DetoxTargetApp(R.drawable.ic_kakaotalk),
        DetoxTargetApp(R.drawable.ic_twitter),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_ticktock),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_ticktock),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_insta),
        DetoxTargetApp(R.drawable.ic_kakaotalk),
        DetoxTargetApp(R.drawable.ic_twitter),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_ticktock),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_ticktock),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_spotify),
        DetoxTargetApp(R.drawable.ic_line),
        DetoxTargetApp(R.drawable.ic_netflix),
        DetoxTargetApp(R.drawable.ic_spotify)
    )

    private val _repeatLockTargetApp: MutableLiveData<DetoxTargetApp> = MutableLiveData()
    val repeatLockTargetApp: LiveData<DetoxTargetApp> get() = _repeatLockTargetApp

    fun updateRepeatLockTargetApp(repeatLockTargetApp: DetoxTargetApp) {
        _repeatLockTargetApp.value = repeatLockTargetApp
    }

}