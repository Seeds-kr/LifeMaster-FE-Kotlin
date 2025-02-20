package com.example.lifemaster.network

import com.example.lifemaster.model.TodoItem
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface NetworkService {
    @GET("/schedule/todo")
    fun getTodoItems(): Call<List<TodoItem>>

    @POST("/schedule/todo/create")
    fun registerTodoItem(
        @Body todoItem: TodoItem
    ): Call<Any>
}