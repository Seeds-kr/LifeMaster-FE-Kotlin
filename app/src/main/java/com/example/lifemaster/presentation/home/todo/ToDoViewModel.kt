package com.example.lifemaster.presentation.home.todo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster.model.CalendarItem
import com.example.lifemaster.model.PomodoroTimer
import com.example.lifemaster.model.TodoItem

class ToDoViewModel: ViewModel() {

    val dummyData = arrayListOf(
        TodoItem(0, "20250318", "밥먹기", false, PomodoroTimer.NONE, CalendarItem(0, "20250318", "TUESDAY", listOf())),
        TodoItem(1, "20250317", "운동하기", false, PomodoroTimer.NONE, CalendarItem(1, "20250317", "MONDAY", listOf())),
        TodoItem(2, "20250316", "카페가기", false, PomodoroTimer.NONE, CalendarItem(2, "20250316", "SUNDAY", listOf()))
    )

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
//        currentList[i] = currentList[i].copy(date = changeItem.date, title = changeItem.title) // 수정 관점에서 사용
        currentList[i] = changeItem
        _todoItems.value = currentList
    }
}