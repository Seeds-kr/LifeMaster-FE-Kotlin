package com.example.lifemaster.presentation.total.detox.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster.R
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.total.detox.model.DetoxRepeatLock
import com.example.lifemaster.presentation.total.detox.model.DetoxRepeatLockResponseItem
import com.example.lifemaster.presentation.total.detox.model.DetoxRepeatLockItem
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetoxRepeatLockViewModel @Inject constructor(
    private val networkService: NetworkService,
    @ApplicationContext private val context: Context
) : ViewModel() {

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

    fun generateRepeatLock(request: DetoxRepeatLock) {
        viewModelScope.launch {
            try {
                val response = networkService.generateRepeatLock(request)

                if (response.isSuccessful) {
                    fetchRepeatLockItems()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchRepeatLockItems() {
        viewModelScope.launch {
            try {
                val response = networkService.fetchRepeatLockItems()

                if (response.isSuccessful) {
                    val items = response.body()?.lockedApps.orEmpty().map { item ->
                        item.toRepeatLockItem()
                    }

                    _repeatLockApp.value = ArrayList(items)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteRepeatLockItem(id: Long) {
        viewModelScope.launch {
            try {
                val response = networkService.deleteRepeatLockItem(id)

                if (response.isSuccessful) {
                    fetchRepeatLockItems()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 반복 잠금 아이템 시간(사용 시간)
    private val _useTime: MutableLiveData<Pair<Int,Int>> = MutableLiveData()
    val useTime: LiveData<Pair<Int,Int>> get() = _useTime

    fun setUseTime(useTime: Pair<Int,Int>) {
        _useTime.value = useTime
    }

    // 반복 잠금 아이템 시간(잠금 시간)
    private val _lockTime: MutableLiveData<Pair<Int,Int>> = MutableLiveData()
    val lockTime: LiveData<Pair<Int,Int>> get() = _lockTime

    fun setLockTime(useTime: Pair<Int,Int>) {
        _lockTime.value = useTime
    }

    // 반복 잠금 아이템 시간(최대 사용 시간)
    private val _maxUseTime: MutableLiveData<Pair<Int,Int>> = MutableLiveData()
    val maxUseTime: LiveData<Pair<Int,Int>> get() = _maxUseTime

    fun setMaxUseTime(useTime: Pair<Int,Int>) {
        _maxUseTime.value = useTime
    }

    //////////////////////////////////테스트 용//////////////////////////////

    // 반복 잠금 아이템 시간(사용 시간)
    private val _tempUseTime: MutableLiveData<Int> = MutableLiveData()
    val tempUseTime: LiveData<Int> get() = _tempUseTime

    fun setTempUseTime(tempUseTime: Int) {
        _tempUseTime.value = tempUseTime
    }

    // 반복 잠금 아이템 시간(잠금 시간)
    private val _tempLockTime: MutableLiveData<Int> = MutableLiveData()
    val tempLockTime: LiveData<Int> get() = _tempLockTime

    fun setTempLockTime(tempUseTime: Int) {
        _tempLockTime.value = tempUseTime
    }

    // 반복 잠금 아이템 시간(최대 사용 시간)
    private val _tempMaxUseTime: MutableLiveData<Int> = MutableLiveData()
    val tempMaxUseTime: LiveData<Int> get() = _tempMaxUseTime

    fun setTempMaxUseTime(tempUseTime: Int) {
        _tempMaxUseTime.value = tempUseTime
    }

    private fun DetoxRepeatLockResponseItem.toRepeatLockItem(): DetoxRepeatLockItem {
        val appInfo = getAppInfo(lockedApp)

        return DetoxRepeatLockItem(
            id = id,
            appIcon = appInfo.first,
            appName = appInfo.second,
            appPackageName = lockedApp,
            useTime = sessionUsageLimit,
            lockTime = lockDuration,
            maxTime = dailyMaxUsageLimit,
            accumulatedTime = 0L,
            isMaxTimeLimitSet = dailyMaxUsageLimit != 0
        )
    }

    private fun getAppInfo(packageName: String): Pair<android.graphics.drawable.Drawable, String> {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val icon = packageManager.getApplicationIcon(appInfo)
            val name = packageManager.getApplicationLabel(appInfo).toString()
            Pair(icon, name)
        } catch (e: PackageManager.NameNotFoundException) {
            val defaultIcon = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)!!
            Pair(defaultIcon, packageName)
        }
    }
}