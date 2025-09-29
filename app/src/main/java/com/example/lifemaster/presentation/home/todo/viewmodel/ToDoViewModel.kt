package com.example.lifemaster.presentation.home.todo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.home.HomeFragment
import com.example.lifemaster.presentation.home.todo.model.TodoModel
import kotlinx.coroutines.launch

class ToDoViewModel(private val networkService: NetworkService): ViewModel() {

    private val _todoItems: MutableLiveData<ArrayList<TodoItem>> = MutableLiveData()
    val todoItems: LiveData<ArrayList<TodoItem>> get() = _todoItems

    fun getTodoItems(todoItem: ArrayList<TodoItem>) {
        _todoItems.value = todoItem
    }

    private val _remoteTodoItems: MutableLiveData<List<TodoModel>> = MutableLiveData()
    val remoteTodoItems: LiveData<List<TodoModel>> get() = _remoteTodoItems

    fun getRemoteTodoItems(token: String) {
        viewModelScope.launch {
            try {
                _remoteTodoItems.value = networkService.getTodoItems(token = token)
            } catch (e: Exception) {
                Log.e(HomeFragment.TAG_TODO, e.message ?: "")
            }
        }
    }

    fun addTodoItems(newItem: TodoModel) {
        val currentList = _todoItems.value ?: arrayListOf()
        currentList.add(newItem)
        _todoItems.value = currentList
    }

    fun deleteTodoItems(deleteItem: TodoItem) {
        val currentList = _todoItems.value ?: arrayListOf()
        currentList.remove(deleteItem)
        _todoItems.value = currentList
    }

    fun changeTodoItems(changeItem: TodoItem) {
        val currentList = _todoItems.value ?: arrayListOf()
        val i = currentList.indexOfFirst { it.id == changeItem.id }
        currentList[i] = changeItem
        _todoItems.value = currentList
    }
}