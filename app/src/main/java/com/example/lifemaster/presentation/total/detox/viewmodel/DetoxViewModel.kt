package com.example.lifemaster.presentation.total.detox.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.total.detox.model.DetoxPermanentLock
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp
import com.example.lifemaster.presentation.total.detox.model.DetoxTimeLockRequest
import com.example.lifemaster.presentation.total.detox.model.DetoxTimeLockResponse
import com.example.lifemaster.presentation.total.detox.repository.AppListRepository
import com.example.lifemaster.presentation.total.detox.repository.DetoxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetoxViewModel @Inject constructor(
    private val detoxRepository: DetoxRepository,
    private val appListRepository: AppListRepository
) : ViewModel() {

    // init 블록 내의 코드는 ViewModel이 생성되자마자 실행된다
    init {
        loadApplications()
        fetchTimeLockItems() // 시간 잠금 리스트 목록 조회
    }

    private val _installedApps = MutableStateFlow<List<DetoxTargetApp>>(emptyList())
    val installedApps = _installedApps.asStateFlow()

    private fun loadApplications() {
        _installedApps.value = appListRepository.fetchInstalledApps()
    }

    private val _timeLockItems = MutableStateFlow<DataResource<List<DetoxTimeLockResponse>>>(DataResource.Idle)
    val timeLockItems = _timeLockItems.asStateFlow()

    fun fetchTimeLockItems() {
        viewModelScope.launch {
            _timeLockItems.value = DataResource.Loading
            detoxRepository.fetchTimeLockItems().onSuccess {
                _timeLockItems.value = DataResource.Success(it)
            }.onFailure {
                _timeLockItems.value = DataResource.Error(it)
            }
        }
    }

    /**
     * 최종적으로 보여줄 남은 앱 리스트 (아직 잠금 상태가 안된 앱)
     * 1. 앱이 백그라운드로 가거나 화면을 벗어나서 구독자가 사라졌을 때, 즉시 연산을 멈추는 게 아니라 5초의 유예 시간을 둡니다.
     * 화면 회전이나 일시적 화면 전환 시 불필요하게 리스트를 다시 계산하는 것을 방지하는 최적화 설정입니다.
     * 2. StateFlow는 항상 초기값이 있어야 합니다. 처음에 데이터가 로드되기 전에 빈 리스트를 보여줍니다.
     * 3. List에서 contains을 쓰는 것보다 Set에서 찾는 것이 시간 복잡도가 훨씬 효율적입니다. O(n) -> O(1)
     */
    val remainingApps = combine(_installedApps, _timeLockItems) { installedApps, timeLockResource ->
        if(timeLockResource is DataResource.Success) {
            val timeLockedPackageNames = timeLockResource.data.map { it.lockedAppPackageName }.toSet() // 3
            installedApps.filter { it.appPackageName !in timeLockedPackageNames }
        } else installedApps
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // 1
        initialValue = emptyList() // 2
    )

    var allowServiceApplications = arrayListOf<DetoxTargetApp>()

    /**
     * 영구 잠금
     */
    private val _generatePermanentLockResult = MutableSharedFlow<DataResource<Unit>>()
    val generatePermanentLockResult = _generatePermanentLockResult.asSharedFlow()

    fun generatePermanentLock(request: DetoxPermanentLock) {
        viewModelScope.launch {
            _generatePermanentLockResult.emit(DataResource.Loading)
            detoxRepository.generatePermanentLock(request = request).onSuccess {
                _generatePermanentLockResult.emit(DataResource.Success(it))
            }.onFailure {
                _generatePermanentLockResult.emit(DataResource.Error(it))
            }
        }
    }

    private val _permanentLockItems = MutableStateFlow<DataResource<List<String>>>(DataResource.Idle)
    val permanentLockItems = _permanentLockItems.asStateFlow()

    fun fetchPermanentLockItems() {
        viewModelScope.launch {
            _permanentLockItems.value = DataResource.Loading
            detoxRepository.fetchPermanentLockItems().onSuccess {
                _permanentLockItems.value = DataResource.Success(it.lockedAppPackageNames)
            }.onFailure {
                _permanentLockItems.value = DataResource.Error(it)
            }
        }
    }

    /**
     * 반복 잠금
     */

    /**
     * 시간 잠금
     */
    private val _generateTimeLockResult = MutableSharedFlow<DataResource<Unit>>()
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

    private val _deleteTimeLockResult = MutableSharedFlow<DataResource<Unit>>()
    val deleteTimeLockResult = _deleteTimeLockResult.asSharedFlow()

    fun deleteTimeLockItem(id: Long) {
        viewModelScope.launch {
            _deleteTimeLockResult.emit(DataResource.Loading)
            detoxRepository.deleteTimeLockItem(id = id).onSuccess {
                _deleteTimeLockResult.emit(DataResource.Success(it))
            }.onFailure {
                _deleteTimeLockResult.emit(DataResource.Error(it))
            }
        }
    }
}