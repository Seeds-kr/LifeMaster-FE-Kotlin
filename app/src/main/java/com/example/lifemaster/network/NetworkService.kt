package com.example.lifemaster.network

import com.example.lifemaster.presentation.home.alarm.model.MathProblemResponse
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroItem
import com.example.lifemaster.presentation.home.sleep.model.SleepResponse
import com.example.lifemaster.presentation.home.sleep.model.SleepRequest
import com.example.lifemaster.presentation.login.model.LoginInfo
import com.example.lifemaster.presentation.home.todo.model.TodoItem
import com.example.lifemaster.data.remote.dto.ChallengeListResponse
import com.example.lifemaster.presentation.total.introspection.model.ThankRequest
import com.example.lifemaster.presentation.total.introspection.model.ThankResponse
import com.example.lifemaster.presentation.total.introspection.model.ThankCreateResponse
import com.example.lifemaster.presentation.total.introspection.model.DiaryRequest
import com.example.lifemaster.presentation.total.introspection.model.DiaryResponse
import com.example.lifemaster.presentation.login.model.NicknameCheckResponse
import com.example.lifemaster.presentation.login.model.RegisterInfo
import com.example.lifemaster.presentation.login.model.RegResponse
import com.example.lifemaster.presentation.community.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface NetworkService {

    // 유저 회원가입
    @POST("/user/register")
    fun registerUser(
        @Body body: RegisterInfo
    ): Call<RegResponse>

    // 닉네임 중복 확인
    @GET("/user/register/nickname")
    fun checkNickname(
        @Query("nickname") nickname: String
    ): Call<NicknameCheckResponse>

    // 유저 닉네임 등록
    @Multipart
    @POST("/user/register/nickname")
    fun registerNickname(
        @Part("regId") regId: RequestBody,
        @Part("nickName") nickName: RequestBody,
        @Part image: MultipartBody.Part? = null
    ): Call<Void>

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
    suspend fun getChallenges(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ChallengeListResponse



    // 감사일기 생성
    @POST("/schedule/self-reflection/thank")
    suspend fun createThank(
        @Header("Authorization") token: String,
        @Body request: ThankRequest
    ): Response<ThankCreateResponse>

    //감사일기 수정
    @PUT("/schedule/self-reflection/thank/{thank-id}")
    suspend fun updateThank(
        @Header("Authorization") token: String,
        @Path("thank-id") thankId: Long,
        @Body request: ThankRequest
    ): Response<Unit>

    // 감사일기 조회
    @GET("/schedule/self-reflection/thank/{thank-id}")
    suspend fun getThank(
        @Header("Authorization") token: String,
        @Path("thank-id") thankId: Long
    ): Response<ThankResponse>

    // 감사일기 삭제
    @DELETE("/schedule/self-reflection/thank/{thank-id}")
    suspend fun deleteThank(
        @Header("Authorization") token: String,
        @Path("thank-id") thankId: Long
    ): Response<Unit>

    // 다이어리 생성
    @POST("/schedule/self-reflection/diary")
    suspend fun createDiary(
        @Header("Authorization") token: String,
        @Body request: DiaryRequest
    ): Response<DiaryResponse>

    // 다이어리 수정
    @PUT("/schedule/self-reflection/diary/{diary-id}")
    suspend fun updateDiary(
        @Header("Authorization") token: String,
        @Path("diary-id") diaryId: Long,
        @Body request: DiaryRequest
    ): Response<DiaryResponse>

    // 다이어리 삭제
    @DELETE("/schedule/self-reflection/diary/{diary-id}")
    suspend fun deleteDiary(
        @Header("Authorization") token: String,
        @Path("diary-id") diaryId: Long
    ): Response<Unit>

    // 커뮤니티 게시글 전체 목록 조회
    @GET("posts")
    fun getPostsByType(
        @Header("Authorization") token: String,
        @Query("type") type: String
    ): Call<List<PostSummaryDto>>

    // 게시글 상세조회
    @GET("posts/{postId}")
    fun getPostDetail(
        @Header("Authorization") token: String,
        @Path("postId") id: String
    ): Call<PostDetailDto>

    // 게시글 생성
    @POST("posts")
    fun createPost(
        @Header("Authorization") token: String,
        @Body body: NewPostRequest
    ): Call<ResponseBody>

    // 게시글 수정
    @PATCH("posts/{postId}")
    fun updatePost(
        @Header("Authorization") token: String,
        @Path("postId") id: String,
        @Body body: UpdatePostRequest
    ): Call<ResponseBody>

    // 게시글 삭제
    @DELETE("posts/{postId}")
    fun deletePost(
        @Header("Authorization") token: String,
        @Path("postId") id: String
    ): Call<ResponseBody>

    // 게시글 좋아요
    @POST("posts/like/{postId}")
    fun togglePostLike(
        @Header("Authorization") token: String,
        @Path("postId") postId: String
    ): Call<ResponseBody>

    @GET("posts/popular")
    fun getPopularPosts(
        @Header("Authorization") token: String
    ): Call<List<PostSummaryDto>>

    // 댓글 조회
    @GET("posts/{postId}/comments")
    fun getComments(
        @Header("Authorization") token: String,
        @Path("postId") postId: String
    ): Call<List<CommentDto>>

    // 댓글 생성
    @POST("posts/{postId}/comments")
    fun createComment(
        @Header("Authorization") token: String,
        @Path("postId") postId: String,
        @Body body: NewCommentRequest
    ): Call<ResponseBody>

    // 댓글 수정
    @PATCH("posts/{postId}/comments/{commentId}")
    fun updateComment(
        @Header("Authorization") token: String,
        @Path("postId") postId: String,
        @Path("commentId") commentId: String,
        @Body body: NewCommentRequest
    ): Call<ResponseBody>

    // 댓글 삭제
    @DELETE("posts/{postId}/comments/{commentId}")
    fun deleteComment(
        @Header("Authorization") token: String,
        @Path("postId") postId: String,
        @Path("commentId") commentId: String
    ): Call<ResponseBody>

    // 개선게시판 투표
    @GET("/community/improvePost/poll/all")
    fun getPollList(): Call<List<PollListItem>>

    @GET("/community/improvePost/poll/{pollId}/details")
    fun getPollDetails(
        @Path("pollId") pollId: Long
    ): Call<PollDetailsDto>

    @GET("/community/improvePost/poll/{pollId}/results")
    fun getPollResults(
        @Path("pollId") pollId: Long
    ): Call<Map<String, PollResultDto>>

    @POST("/community/improvePost/poll/{pollId}/vote")
    fun castVote(
        @Header("Authorization") authorization: String?,
        @Path("pollId") pollId: Long,
        @Body body: VoteRequest
    ): Call<ResponseBody>
    /**
     * Sleep Management API
     */
    // 유저의 수면 기록 조회
    @GET("/sleep/{userId}")
    suspend fun getUserSleepRecord(
        @Path("userId") userId: Int
    ): List<SleepResponse>

    // 유저의 수면 기록 생성
    @POST("/sleep")
    suspend fun registerUserSleepRecord(
        @Body userRequest: SleepRequest
    ): String

    // 유저의 수면 기록 업데이트
    @PATCH("/sleep")
    suspend fun updateUserSleepRecord(
        @Body sleepRequest: SleepRequest
    ): SleepResponse

    /**
     * Alarm Mission API
     */
    // 수학 문제 생성 API
    @GET("/time/alarm/mission/math-problem")
    suspend fun generateMathProblem(
        @Query("level") level: String
    ): MathProblemResponse
}