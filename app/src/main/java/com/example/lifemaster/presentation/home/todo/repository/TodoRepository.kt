package com.example.lifemaster.presentation.home.todo.repository

import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.home.todo.model.TodoRequest
import com.example.lifemaster.presentation.home.todo.model.TodoResponse
import javax.inject.Inject

class TodoRepository @Inject constructor(private val networkService: NetworkService) {

    // 새 할일 생성
    suspend fun addTodoItem(request: TodoRequest): Result<TodoResponse> = try {
        val response = networkService.addTodoItem(request = request)
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

//    suspend fun getRemoteTodoItems() = try {
//        networkService.getTodoItems()
//    } catch (e: Exception) {
//        Result.failure(e)
//    }
}