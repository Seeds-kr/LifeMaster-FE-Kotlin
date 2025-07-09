package com.example.lifemaster.presentation.home.todo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster.presentation.home.todo.model.TodoItem

class ToDoViewModel: ViewModel() {

    private val _todoItems: MutableLiveData<ArrayList<TodoItem>> = MutableLiveData()
    val todoItems: LiveData<ArrayList<TodoItem>> get() = _todoItems

    fun getTodoItems(todoItem: ArrayList<TodoItem>) {
        _todoItems.value = todoItem
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
        currentList[i] = changeItem
        _todoItems.value = currentList
    }
}