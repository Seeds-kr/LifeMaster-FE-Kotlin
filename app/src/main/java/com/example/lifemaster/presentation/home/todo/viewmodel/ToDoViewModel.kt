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

//    private val _todoItems: MutableLiveData<ArrayList<TodoModel>> = MutableLiveData()
//    val todoItems: LiveData<ArrayList<TodoModel>> get() = _todoItems
//
//    fun getTodoItems(todoModel: ArrayList<TodoModel>) {
//        _todoItems.value = todoModel
//    }
//
//    fun addTodoItems(newItem: TodoModel) {
//        val currentList = _todoItems.value ?: arrayListOf()
//        currentList.add(newItem)
//        _todoItems.value = currentList
//    }
//
//    fun deleteTodoItems(deleteItem: TodoModel) {
//        val currentList = _todoItems.value ?: arrayListOf()
//        currentList.remove(deleteItem)
//        _todoItems.value = currentList
//    }


//    fun changeTodoItems(changeItem: TodoModel) {
//        val currentList = _todoItems.value ?: arrayListOf()
//        val i = currentList.indexOfFirst { it.id == changeItem.id }
//        currentList[i] = changeItem
//        _todoItems.value = currentList
//    }
}