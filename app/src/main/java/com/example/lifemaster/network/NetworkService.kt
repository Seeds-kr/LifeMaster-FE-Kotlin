package com.example.lifemaster.network

import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroItem
import com.example.lifemaster.presentation.login.model.LoginInfo
import com.example.lifemaster.presentation.home.todo.model.TodoItem
import com.example.lifemaster.presentation.total.challenge.model.ChallengeResponse
import com.example.lifemaster.presentation.total.introspection.model.ThankRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
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

    // 모든 To-Do 항목 조회
    @GET("/schedule/todo")
    fun getTodoItems(
        @Header("Authorization") token: String
    ): Call<List<TodoItem>>

    // 새 To-Do 생성
    @POST("/schedule/todo/create")
    fun registerTodoItem(
        @Header("Authorization") token: String,
        @Body todoItem: TodoItem
    ): Call<TodoItem>

    // To-Do 삭제
    @DELETE("/schedule/todo/{id}")
    fun deleteTodoItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ):Call<Any>

    // 특정 To-Do 조회
    @GET("/schedule/todo/{id}")
    fun getTodoItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ):Call<TodoItem>

    // To-Do 업데이트
    @PUT("/schedule/todo/{id}")
    fun updateTodoItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Query("date") date: String,
        @Query("title") title: String
    ):Call<TodoItem>

    // To-Do 완료 상태 토글
    @PATCH("/schedule/todo/{id}/toggle-completed")
    fun toggleTodoItem(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ):Call<TodoItem>

    // 새로운 포모도로 타이머 생성
    @POST("/time/pomodoro/create")
    fun registerPomodoroTimer(
        @Header("Authorization") token: String,
        @Body pomodoroItem: PomodoroItem
    ):Call<Any>

    // 모든 포모도로 타이머 조회
    @GET("/time/pomodoro")
    fun getPomodoroItems(
        @Header("Authorization") token: String
    ):Call<List<PomodoroItem>>

    // 비상 탈출 문장 생성
    @GET("/time/pomodoro/escape/generate")
    fun getEscapeSentence(
        @Header("Authorization") token: String
    ):Call<String>

    // 챌린지 목록 조회
    @GET("/challenge")
    fun getChallenges(
        @Query("page") page: Int
    ): Call<ChallengeResponse>

    // 감사일기 생성
    @POST("/schedule/self-reflection/thank")
    suspend fun createThank(
        @Header("Authorization") token: String,
        @Body request: ThankRequest
    ): Response<Unit>

    //감사일기 수정
    @PUT("schedule/self-reflection/thank/{thank-id}")
    suspend fun updateThank(
        @Header("Authorization") token: String,
        @Path("thank-id") thankId: Long,
        @Body request: ThankRequest
    ): Response<Unit>



}