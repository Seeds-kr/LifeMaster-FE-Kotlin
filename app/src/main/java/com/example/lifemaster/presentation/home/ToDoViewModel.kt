package com.example.lifemaster.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster.model.TodoItem

class ToDoViewModel: ViewModel() {
    private val _todoItems: MutableLiveData<TodoItem> = MutableLiveData()
    val todoItems: LiveData<TodoItem> get() = _todoItems
    fun updateTodoItems(newItem: TodoItem) {
        _todoItems.value = newItem
    }
}