package com.example.lifemaster_xml.total.detox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.total.detox.model.DetoxTargetApp

class DetoxViewModel: ViewModel() {

    val tempAppLogos = arrayListOf(
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

}