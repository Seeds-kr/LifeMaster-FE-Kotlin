package com.example.lifemaster.network

import com.example.lifemaster.presentation.home.alarm.model.MathProblemResponse
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroItem
import com.example.lifemaster.presentation.home.sleep.model.SleepResponse
import com.example.lifemaster.presentation.home.sleep.model.SleepRequest
import com.example.lifemaster.presentation.home.calendar.model.CalendarEntry
import com.example.lifemaster.presentation.home.calendar.model.EventBody
import com.example.lifemaster.presentation.login.model.LoginInfo
import com.example.lifemaster.presentation.home.todo.model.TodoItem
import com.example.lifemaster.presentation.total.challenge.model.ChallengeListResponse
import com.example.lifemaster.presentation.total.introspection.model.ThankRequest
import com.example.lifemaster.presentation.total.introspection.model.ThankResponse
import com.example.lifemaster.presentation.total.introspection.model.ThankCreateResponse
import com.example.lifemaster.presentation.total.introspection.model.ThankUpdateRequest
import com.example.lifemaster.presentation.total.introspection.model.DiaryRequest
import com.example.lifemaster.presentation.total.introspection.model.DiaryResponse
import com.example.lifemaster.presentation.login.model.NicknameCheckResponse
import com.example.lifemaster.presentation.login.model.RegisterInfo
import com.example.lifemaster.presentation.login.model.RegResponse
import com.example.lifemaster.presentation.login.model.EmailRequest
import com.example.lifemaster.presentation.login.model.PasswordResetDto
import com.example.lifemaster.presentation.login.model.PasswordResponseDto
import com.example.lifemaster.presentation.community.model.*
import com.example.lifemaster.presentation.login.model.RegNickResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.Headers
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
    ): Call<RegNickResponse>

    // 유저 로그인 API
    @POST("/user/login")
    fun enterUserLogin(
        @Body loginInfo: LoginInfo
    ): Call<String>

    // 비밀번호 재설정
    @Headers("Content-Type: application/json")
    @POST("auth/password/reset/confirm-email")
    fun requestResetEmail(
        @Body body: EmailRequest
    ): Call<PasswordResponseDto>

    @GET("auth/password/reset/verify")
    fun verifyResetToken(
        @Query("token") token: String
    ): Call<PasswordResponseDto>

    @Headers("Content-Type: application/json")
    @POST("auth/password/reset")
    fun resetPassword(
        @Body body: PasswordResetDto
    ): Call<PasswordResponseDto>

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
    ): Response<ChallengeListResponse>

    // 챌린지 참여
    @POST("/challenge/{challId}/join")
    suspend fun joinChallenge(
        @Header("Authorization") token: String,
        @Path("challId") challId: Long
    ): Response<String>

    // 챌린지 참여 취소
    @DELETE("/challenge/{challId}/leave")
    suspend fun leaveChallenge(
        @Header("Authorization") token: String,
        @Path("challId") challId: Long
    ): Response<String>

    // 챌린지 상세 조회
    @GET("/challenge/{challId}")
    suspend fun getChallengeDetail(
        @Header("Authorization") token: String,
        @Path("challId") challId: Long
    ): Response<com.example.lifemaster.presentation.total.challenge.model.ChallengeItemDto>

    // 챌린지 검색
    @GET("/challenge/search")
    suspend fun searchChallenges(
        @Header("Authorization") token: String,
        @Query("name") name: String,
        @Query("page") page: Int = 0
    ): com.example.lifemaster.presentation.total.challenge.model.ChallengeListResponse

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
        @Body request: ThankUpdateRequest
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

    @GET("/calendar")
    suspend fun getCalendarAll(
        @Header("Authorization") token: String? = null
    ): List<CalendarEntry>

    @GET("/calendar/{date}")
    suspend fun getCalendarByDate(
        @Path("date") date: String,
        @Header("Authorization") token: String? = null
    ): CalendarEntry

    @GET("/calendar/month/{date}")
    suspend fun getCalendarByMonth(
        @Path("date") yyyymm: String,
        @Header("Authorization") token: String? = null
    ): List<CalendarEntry>

    @POST("/calendar/create")
    suspend fun createDay(
        @Query("date") date: String,
        @Header("Authorization") token: String? = null
    ): CalendarEntry

    @POST("/calendar/{date}/add")
    suspend fun addEvent(
        @Path("date") date: String,
        @Body body: EventBody,
        @Header("Authorization") token: String? = null
    ): CalendarEntry

    @POST("/calendar/create/{date}/events")
    suspend fun createEvents(
        @Path("date") date: String,
        @Body events: List<String>,
        @Header("Authorization") token: String? = null
    ): CalendarEntry

    @DELETE("/calendar/{date}")
    suspend fun deleteAllEventsOnDate(
        @Path("date") date: String,
        @Header("Authorization") token: String? = null
    ): String

    @HTTP(method = "DELETE", path = "/calendar/{date}/event", hasBody = true)
    suspend fun deleteSpecificEvent(
        @Path("date") date: String,
        @Body body: EventBody,
        @Header("Authorization") token: String? = null
    ): String


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

    // 특정 멤버 전체 캘린더 조회
    @GET("/calendar/member/{memberId}")
    suspend fun getCalendarAllByMember(
        @Path("memberId") memberId: Long,
        @Header("Authorization") token: String? = null
    ): List<CalendarEntry>

    // 특정 멤버 월별 캘린더 조회
    @GET("/calendar/member/{memberId}/month/{yyyymm}")
    suspend fun getCalendarByMemberMonth(
        @Path("memberId") memberId: Long,
        @Path("yyyymm") yyyymm: String,
        @Header("Authorization") token: String? = null
    ): List<CalendarEntry>

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

    // 신고하기
    @POST("reports")
    fun reportPost(
        @Header("Authorization") token: String,
        @Body body: ReportRequest
    ): Call<ResponseBody>

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

    // 댓글 좋아요 토글 (postId 필요 없음)
    @POST("comments/like/{commentId}")
    fun toggleCommentLike(
        @Header("Authorization") token: String,
        @Path("commentId") commentId: String
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
    fun getPollList(
        @Header("Authorization") token: String
    ): Call<List<PollListItem>>

    @GET("/community/improvePost/poll/{pollId}/details")
    fun getPollDetails(
        @Header("Authorization") token: String,
        @Path("pollId") pollId: Long
    ): Call<PollDetailsDto>

    @GET("/community/improvePost/poll/{pollId}/results")
    fun getPollResults(
        @Header("Authorization") token: String,
        @Path("pollId") pollId: Long
    ): Call<Map<String, PollResultDto>>

    @POST("/community/improvePost/poll/{pollId}/vote")
    fun castVote(
        @Header("Authorization") authorization: String,
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