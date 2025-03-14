package com.example.lifemaster.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster.model.TodoItem

class ToDoViewModel: ViewModel() {

    private val _todoItems: MutableLiveData<ArrayList<TodoItem>> = MutableLiveData()
    val todoItems: LiveData<ArrayList<TodoItem>> get() = _todoItems

    fun updateTodoItems(items: ArrayList<TodoItem>) {
        _todoItems.value = items
    }

    fun addTodoItems(newItem: TodoItem) {
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
        currentList[i] = currentList[i].copy(date = changeItem.date, title = changeItem.title) // currentList[i] = changeItem 보다 더 안전한 방식. 전체를 바꾸는 것보단 일부만 바꾸는 것이 더 안전하다.
        _todoItems.value = currentList
    }
}