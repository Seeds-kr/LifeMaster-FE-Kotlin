package com.example.lifemaster.network

import com.example.lifemaster.model.TodoItem
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NetworkService {
    @GET("/schedule/todo")
    fun getTodoItems(): Call<List<TodoItem>>

    @POST("/schedule/todo/create")
    fun registerTodoItem(
        @Query("date") date: String,
        @Query("title") title: String
    ): Call<TodoItem>

    @DELETE("/schedule/todo/{id}")
    fun deleteTodoItem(
        @Path("id") id: Int
    ):Call<Any>
}