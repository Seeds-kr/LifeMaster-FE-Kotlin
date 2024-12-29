package com.example.lifemaster_xml.total.detox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.total.detox.model.DetoxRepeatLockItem
import com.example.lifemaster_xml.total.detox.model.DetoxTargetApp

class DetoxRepeatLockViewModel: ViewModel() {

    val allowServiceApplications = arrayListOf(
        DetoxTargetApp(R.drawable.ic_insta, "인스타그램"),
        DetoxTargetApp(R.drawable.ic_kakaotalk, "카카오톡"),
        DetoxTargetApp(R.drawable.ic_twitter, "트위터"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_ticktock, "틱톡"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_ticktock, "틱톡"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_insta, "인스타그램"),
        DetoxTargetApp(R.drawable.ic_kakaotalk, "카카오톡"),
        DetoxTargetApp(R.drawable.ic_twitter, "트위터"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_ticktock, "틱톡"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_ticktock, "틱톡"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이")
    )

    private val _allowServices : MutableLiveData<List<DetoxTargetApp>> = MutableLiveData()
    val allowServices : LiveData<List<DetoxTargetApp>> get() = _allowServices

    fun updateAllowServices(allowServices: List<DetoxTargetApp>) {
        _allowServices.value = allowServices
    }

    //////////////////////////////     --- 구분선 입니다 ---  /////////////////////////////////////

    // 디톡스 프래그먼트의 반복잠금 설정 다이얼로그
    val repeatLockTargetApplications = arrayListOf(
        DetoxTargetApp(R.drawable.ic_insta, "인스타그램"),
        DetoxTargetApp(R.drawable.ic_kakaotalk, "카카오톡"),
        DetoxTargetApp(R.drawable.ic_twitter, "트위터"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_ticktock, "틱톡"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_ticktock, "틱톡"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_insta, "인스타그램"),
        DetoxTargetApp(R.drawable.ic_kakaotalk, "카카오톡"),
        DetoxTargetApp(R.drawable.ic_twitter, "트위터"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_ticktock, "틱톡"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_ticktock, "틱톡"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이"),
        DetoxTargetApp(R.drawable.ic_line, "라인"),
        DetoxTargetApp(R.drawable.ic_netflix, "넷플릭스"),
        DetoxTargetApp(R.drawable.ic_spotify, "스포티파이")
    )

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