package com.example.lifemaster.presentation.home.pomodoro.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroButtonStatus
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroModel
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroRequest
import com.example.lifemaster.presentation.home.pomodoro.model.toPresentation
import com.example.lifemaster.presentation.home.pomodoro.repository.PomodoroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(private val repository: PomodoroRepository): ViewModel() {

    var currentStatus: PomodoroButtonStatus = PomodoroButtonStatus.TODO

    private val _newPomodoroItem = MutableSharedFlow<DataResource<PomodoroModel>>()
    val newPomodoroItem = _newPomodoroItem.asSharedFlow()

    fun registerPomodoroItem(pomodoroRequest: PomodoroRequest) {
        viewModelScope.launch {
            _newPomodoroItem.emit(DataResource.Loading)
            val result = repository.registerPomodoroItem(pomodoroRequest = pomodoroRequest)
            result.onSuccess { response ->
                _newPomodoroItem.emit(DataResource.Success(response.toPresentation()))
            }.onFailure { error ->
                _newPomodoroItem.emit(DataResource.Error(error))
            }
        }
    }

    private val _allPomodoroItems = MutableStateFlow<DataResource<List<PomodoroModel>>>(DataResource.Idle)
    val allPomodoroItems = _allPomodoroItems.asStateFlow()

    fun getPomodoroAllItems() {
        viewModelScope.launch {
            _allPomodoroItems.value = DataResource.Loading
            val result = repository.getPomodoroAllItems()
            result.onSuccess { response ->
                _allPomodoroItems.value = DataResource.Success(response.map { it.toPresentation() })
            }.onFailure { error ->
                _allPomodoroItems.value = DataResource.Error(error)
            }
        }
    }

    private val _pomodoroItemsByTodo = MutableStateFlow<DataResource<List<PomodoroModel>>>(DataResource.Idle)
    val pomodoroItemsByTodo = _pomodoroItemsByTodo.asStateFlow()

    fun getPomodoroItemsByTodo(todoId: Int) {
        viewModelScope.launch {
            _pomodoroItemsByTodo.value = DataResource.Loading
            val result = repository.getPomodoroItemsByTodo(todoId = todoId)
            result.onSuccess { response ->
                _pomodoroItemsByTodo.value = DataResource.Success(response.map { it.toPresentation() })
            }.onFailure { error ->
                _pomodoroItemsByTodo.value = DataResource.Error(error)
            }
        }
    }

    private val _selectedPosition = MutableLiveData<Int>() // 직접 세팅할 때 쓰는 값 → 외부에서는 함수로 접근
    val selectedPosition: LiveData<Int> = _selectedPosition // observing 할 때 쓰는 값

    fun setPosition(position: Int) {
        _selectedPosition.value = position
    }

    private val _buttonCount = MutableLiveData<Int>()
    val buttonCount: LiveData<Int> = _buttonCount

    var btnCnt = 0

    fun clickButton() {
        _buttonCount.value = ++btnCnt
    }

    fun resetButtonCount() {
        btnCnt = 0
        _buttonCount.value = btnCnt
    }
}