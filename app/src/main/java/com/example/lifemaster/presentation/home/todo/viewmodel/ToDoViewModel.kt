package com.example.lifemaster.presentation.home.todo.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.home.HomeFragment
import com.example.lifemaster.presentation.home.todo.model.TodoModel
import kotlinx.coroutines.launch

class ToDoViewModel(private val networkService: NetworkService): ViewModel() {

    private val _todoItems: MutableLiveData<ArrayList<TodoModel>> = MutableLiveData()
    val todoItems: LiveData<ArrayList<TodoModel>> get() = _todoItems

    fun getTodoItems(todoModel: ArrayList<TodoModel>) {
        _todoItems.value = todoModel
    }

    private val _remoteTodoItems: MutableLiveData<List<TodoModel>> = MutableLiveData()
    val remoteTodoItems: LiveData<List<TodoModel>> get() = _remoteTodoItems

    fun getRemoteTodoItems(token: String) {
        viewModelScope.launch {
            try {
                _remoteTodoItems.value = networkService.getTodoItems(token = token)
            } catch (e: Exception) {
                Log.e(HomeFragment.TAG_TODO, "GET: ${e.message}")
            }
        }
    }

    fun addTodoItems(newItem: TodoModel) {
        val currentList = _todoItems.value ?: arrayListOf()
        currentList.add(newItem)
        _todoItems.value = currentList
    }

    fun deleteTodoItems(deleteItem: TodoModel) {
        val currentList = _todoItems.value ?: arrayListOf()
        currentList.remove(deleteItem)
        _todoItems.value = currentList
    }

    private val _isDeleteSuccess: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDeleteSuccess: LiveData<Boolean> get() = _isDeleteSuccess

    fun deleteRemoteTodoItems(token: String, id: Int) {
        viewModelScope.launch {
            try {
                networkService.deleteTodoItem(token = token, id = id)
                _isDeleteSuccess.value = true
            } catch (e: Exception) {
                Log.e(HomeFragment.TAG_TODO, "DELETE: ${e.message}")
            }
        }
    }

    fun changeTodoItems(changeItem: TodoModel) {
        val currentList = _todoItems.value ?: arrayListOf()
        val i = currentList.indexOfFirst { it.id == changeItem.id }
        currentList[i] = changeItem
        _todoItems.value = currentList
    }
}