package com.example.lifemaster.network

import com.example.lifemaster.model.PomodoroItem
import com.example.lifemaster.model.TodoItem
import com.example.lifemaster.presentation.authentication.model.LoginInfo
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NetworkService {

    // 유저 로그인 API
    @POST("/user/login")
    fun enterUserLogin(
        @Body loginInfo: LoginInfo
    ): Call<String>

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 모든 To-Do 항목 조회
    @GET("/schedule/todo")
    fun getTodoItems(): Call<List<TodoItem>>

    // 새 To-Do 생성
    @POST("/schedule/todo/create")
    fun registerTodoItem(
        @Query("date") date: String,
        @Query("title") title: String
    ): Call<TodoItem>

    // To-Do 삭제
    @DELETE("/schedule/todo/{id}")
    fun deleteTodoItem(
        @Path("id") id: Int
    ):Call<Any>

    // 특정 To-Do 조회
    @GET("/schedule/todo/{id}")
    fun getTodoItem(
        @Path("id") id: Int
    ):Call<TodoItem>

    // To-Do 업데이트
    @PUT("/schedule/todo/{id}")
    fun updateTodoItem(
        @Query("id") id: Int,
        @Query("date") date: String,
        @Query("title") title: String
    ):Call<TodoItem>

    // To-Do 완료 상태 토글
    @PATCH("/schedule/todo/{id}/toggle-completed")
    fun toggleTodoItem(
        @Path("id") id: Int
    ):Call<TodoItem>
}