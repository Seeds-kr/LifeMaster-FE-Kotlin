package com.example.lifemaster.presentation.home.todo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

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
//        currentList[i] = currentList[i].copy(date = changeItem.date, title = changeItem.title) // 수정 관점에서 사용
        currentList[i] = changeItem
        _todoItems.value = currentList
    }
}