package com.example.lifemaster.presentation.home.pomodoro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroButtonStatus
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroModel
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroRequest
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroTimeType
import com.example.lifemaster.presentation.home.pomodoro.model.toPresentation
import com.example.lifemaster.presentation.home.pomodoro.repository.PomodoroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(private val repository: PomodoroRepository): ViewModel() {

    var pomodoroStatus: PomodoroButtonStatus = PomodoroButtonStatus.TODO
    var pomodoroTimeType: PomodoroTimeType = PomodoroTimeType.NONE

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

    private val _deletePomodoroItemsResult = MutableSharedFlow<DataResource<Int>>()
    val deletePomodoroItemsResult = _deletePomodoroItemsResult

    fun deletePomodoroItemsByTodo(todoId: Int) {
        viewModelScope.launch {
            _deletePomodoroItemsResult.emit(DataResource.Loading)
            val result = repository.deletePomodoroItemsByTodo(todoId = todoId)
            result.onSuccess { todoId ->
                _deletePomodoroItemsResult.emit(DataResource.Success(todoId))
            }.onFailure { error ->
                _deletePomodoroItemsResult.emit(DataResource.Error(error))
            }
        }
    }

    private val _escapeSentences = MutableStateFlow<DataResource<List<String>>>(DataResource.Idle)
    val escapeSentences = _escapeSentences.asStateFlow()

    fun getPomodoroEscapeSentence(count: Int) {
        viewModelScope.launch {
            _escapeSentences.value = DataResource.Loading
            try {
                val deferredResults: List<Deferred<Result<String>>> = (1..count).map {
                    async { repository.getPomodoroEscapeSentence() }
                }
                val results: List<Result<String>> = deferredResults.awaitAll()
                val sentences = mutableListOf<String>()
                for(result in results) {
                    result.onSuccess { originalSentence ->
                        val convertedSentence = originalSentence.substringAfter("Type this phrase to escape: ")
                        sentences.add(convertedSentence)
                    }.onFailure { throw it }
                }
                _escapeSentences.value = DataResource.Success(sentences)
            } catch (e: Exception) {
                _escapeSentences.value = DataResource.Error(e)
            }
        }
    }

    private val _writtenSentenceCount = MutableStateFlow(0)
    val writtenSentenceCount = _writtenSentenceCount.asStateFlow()

    fun increaseSentenceCount() {
        _writtenSentenceCount.value += 1
    }

    fun decreaseSentenceCount() {
        _writtenSentenceCount.value -= 1
    }

    fun clearData() {
        _writtenSentenceCount.value = 0
    }
}