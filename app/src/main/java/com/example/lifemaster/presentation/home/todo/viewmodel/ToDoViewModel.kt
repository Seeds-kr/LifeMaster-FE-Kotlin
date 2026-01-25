package com.example.lifemaster.presentation.home.todo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.todo.model.TodoModel
import com.example.lifemaster.presentation.home.todo.model.TodoRequest
import com.example.lifemaster.presentation.home.todo.model.TodoResponse
import com.example.lifemaster.presentation.home.todo.model.toPresentation
import com.example.lifemaster.presentation.home.todo.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ToDoViewModel @Inject constructor(private val repository: TodoRepository) : ViewModel() {

    private val _newItem = MutableSharedFlow<DataResource<TodoModel>>()
    val newItem = _newItem.asSharedFlow()

    fun addTodoItem(request: TodoRequest) {
        viewModelScope.launch {
            _newItem.emit(DataResource.Loading)
            val result: Result<TodoResponse> = repository.addTodoItem(request = request)
            result.onSuccess { todoItem ->
                _newItem.emit(DataResource.Success(todoItem.toPresentation()))
            }.onFailure { error ->
                _newItem.emit(DataResource.Error(error))
            }
        }
    }

    private val _currentItems = MutableStateFlow<DataResource<List<TodoModel>>>(DataResource.Idle)
    val currentItems = _currentItems.asStateFlow()

    fun getTodoItems() {
        viewModelScope.launch {
            _currentItems.value = DataResource.Loading
            val result: Result<List<TodoResponse>> = repository.getTodoItems()
            result.onSuccess { remoteItems ->
                val todoItems = remoteItems.map { it.toPresentation() }
                _currentItems.value = DataResource.Success(todoItems)
            }.onFailure { error ->
                _currentItems.value = DataResource.Error(error)
            }
        }
    }

    private val _deletionState = MutableSharedFlow<DataResource<Int>>()
    val deletionState = _deletionState.asSharedFlow()

    fun deleteTodoItem(deleteId: Int) {
        viewModelScope.launch {
            _deletionState.emit(DataResource.Loading)
            val result: Result<Int> = repository.deleteTodoItem(deleteId = deleteId)
            result.onSuccess { deleteId ->
                _deletionState.emit(DataResource.Success(deleteId))
            }.onFailure { error ->
                _deletionState.emit(DataResource.Error(error))
            }
        }
    }

    private val _updateItem = MutableSharedFlow<DataResource<TodoModel>>()
    val updateItem = _updateItem.asSharedFlow()

    fun updateItem(id: Int, date: String, title: String) {
        viewModelScope.launch {
            _updateItem.emit(DataResource.Loading)
            val result: Result<TodoResponse> = repository.updateItem(id = id, date = date, title = title)
            result.onSuccess { updateItem ->
                _updateItem.emit(DataResource.Success(updateItem.toPresentation()))
            }.onFailure { error ->
                _updateItem.emit(DataResource.Error(error))
            }
        }
    }

    private val _toggleItem = MutableSharedFlow<DataResource<TodoModel>>()
    val toggleItem = _toggleItem.asSharedFlow()

    fun toggleItem(id: Int) {
        viewModelScope.launch {
            _toggleItem.emit(DataResource.Loading)
            val result = repository.toggleItem(id = id)
            result.onSuccess { item ->
                _toggleItem.emit(DataResource.Success(item.toPresentation()))
            }.onFailure { error ->
                _toggleItem.emit(DataResource.Error(error))
            }
        }
    }

}