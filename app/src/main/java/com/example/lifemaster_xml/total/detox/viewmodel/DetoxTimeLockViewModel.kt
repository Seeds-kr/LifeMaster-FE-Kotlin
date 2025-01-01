package com.example.lifemaster_xml.total.detox.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.total.detox.model.DetoxTargetApp
import com.example.lifemaster_xml.total.detox.model.DetoxTimeLockItem

class DetoxTimeLockViewModel: ViewModel() {

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

    private val _timeLockItems: MutableLiveData<ArrayList<DetoxTimeLockItem>> = MutableLiveData()
    val timeLockItems: LiveData<ArrayList<DetoxTimeLockItem>> get() = _timeLockItems

    fun addTimeLockItems(timeLockItem: DetoxTimeLockItem) {
        val currentList = _timeLockItems.value ?: arrayListOf()
        currentList.add(timeLockItem)
        _timeLockItems.value = currentList
    }
}