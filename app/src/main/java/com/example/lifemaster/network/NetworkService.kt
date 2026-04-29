package com.example.lifemaster.network

import com.example.lifemaster.presentation.home.alarm.model.MathProblemResponse
import com.example.lifemaster.presentation.home.sleep.model.SleepResponse
import com.example.lifemaster.presentation.home.sleep.model.SleepRequest
import com.example.lifemaster.presentation.login.model.LoginInfo
import com.example.lifemaster.presentation.total.challenge.model.ChallengeListResponse
import com.example.lifemaster.presentation.total.challenge.model.ChallengeItemDto
import com.example.lifemaster.presentation.total.introspection.model.ThankRequest
import com.example.lifemaster.presentation.total.introspection.model.ThankResponse
import com.example.lifemaster.presentation.total.introspection.model.ThankCreateResponse
import com.example.lifemaster.presentation.total.introspection.model.ThankUpdateRequest
import com.example.lifemaster.presentation.total.introspection.model.DiaryRequest
import com.example.lifemaster.presentation.total.introspection.model.DiaryResponse
import com.example.lifemaster.presentation.total.introspection.model.SelfReflectionByDateResponse
import com.example.lifemaster.presentation.total.introspection.model.ThankResponse
import com.example.lifemaster.presentation.total.introspection.model.ThankCreateResponse
import com.example.lifemaster.presentation.total.introspection.model.ThankUpdateRequest
import com.example.lifemaster.presentation.total.introspection.model.DiaryRequest
import com.example.lifemaster.presentation.total.introspection.model.DiaryResponse
import com.example.lifemaster.presentation.total.introspection.model.SelfReflectionResponse
import com.example.lifemaster.presentation.login.model.NicknameCheckResponse
import com.example.lifemaster.presentation.login.model.RegisterInfo
import com.example.lifemaster.presentation.login.model.RegResponse
import com.example.lifemaster.presentation.community.model.*
import com.example.lifemaster.presentation.community.model.CommentDto
import com.example.lifemaster.presentation.community.model.NewCommentRequest
import com.example.lifemaster.presentation.community.model.NewPostRequest
import com.example.lifemaster.presentation.community.model.PollDetailsDto
import com.example.lifemaster.presentation.community.model.PollListItem
import com.example.lifemaster.presentation.community.model.PollResultDto
import com.example.lifemaster.presentation.community.model.PostDetailDto
import com.example.lifemaster.presentation.community.model.PostSummaryDto
import com.example.lifemaster.presentation.community.model.ReportRequest
import com.example.lifemaster.presentation.community.model.UpdatePostRequest
import com.example.lifemaster.presentation.community.model.VoteRequest
import com.example.lifemaster.presentation.home.alarm.model.AlarmRequest
import com.example.lifemaster.presentation.home.alarm.model.AlarmResponse
import com.example.lifemaster.presentation.home.calendar.model.CalendarEntry
import com.example.lifemaster.presentation.home.calendar.model.EventBody
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroRequest
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroResponse
import com.example.lifemaster.presentation.home.todo.model.TodoModel
import com.example.lifemaster.presentation.home.todo.model.TodoRequest
import com.example.lifemaster.presentation.home.todo.model.TodoResponse
import com.example.lifemaster.presentation.login.model.EmailRequest
import com.example.lifemaster.presentation.login.model.PasswordResetDto
import com.example.lifemaster.presentation.login.model.PasswordResponseDto
import com.example.lifemaster.presentation.group.model.GroupCreateResponse
import com.example.lifemaster.presentation.group.model.GroupGoalResponse
import com.example.lifemaster.presentation.group.model.GroupResponse
import com.example.lifemaster.presentation.group.model.GroupSleepStatsResponse
import com.example.lifemaster.presentation.group.model.GroupGoalProgressResponseItem
import com.example.lifemaster.presentation.group.model.GroupAchievementHeatmapItem
import com.example.lifemaster.presentation.group.model.GroupRankingResponse
import com.example.lifemaster.presentation.group.model.GroupChatMessage
import com.example.lifemaster.presentation.login.model.RegNickResponse
import com.example.lifemaster.presentation.total.challenge.model.ChallengeListResponse
import com.example.lifemaster.presentation.total.mypage.model.PayPalCreateOrderResponse
import com.example.lifemaster.presentation.total.detox.model.DetoxPermanentLock
import com.example.lifemaster.presentation.total.detox.model.DetoxRepeatLock
import com.example.lifemaster.presentation.total.detox.model.DetoxTimeLockRequest
import com.example.lifemaster.presentation.total.detox.model.DetoxTimeLockResponse
import com.example.lifemaster.presentation.login.model.VerifyCodeRequest
import com.example.lifemaster.presentation.total.mypage.model.MeResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
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

    // 내 정보 조회
    @GET("/users/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): Response<MeResponse>

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

    @Headers("Content-Type: application/json")
    @POST("auth/password/reset/verify-code")
    fun verifyResetCode(
        @Body body: VerifyCodeRequest
    ): Call<PasswordResponseDto>
    // 네이버 로그인: OAuth2 인증 URL 발급
    @GET("/naverLogin/authUrl")
    fun getNaverAuthUrl(): Call<String>

    // 모든 To-Do 항목 조회
    @GET("/schedule/todo")
    fun getTodoItems(
        @Header("Authorization") token: String
    ): Call<List<TodoResponse>>

    @Headers("Content-Type: application/json")
    @POST("auth/password/reset")
    fun resetPassword(
        @Body body: PasswordResetDto
    ): Call<PasswordResponseDto>

    /**
     * To-Do List API
     */
    // 새 To-Do 생성
    @POST("/schedule/todo/create")
    suspend fun addTodoItem(
        @Body request: TodoRequest
    ): TodoResponse

    // 현재 유저의 To-Do 항목 조회
    @GET("/schedule/todo/member/{memberId}")
    suspend fun getTodoItems(): List<TodoResponse>

    // To-Do 삭제
    @DELETE("/schedule/todo/{id}")
    suspend fun deleteTodoItem(
        @Path("id") id: Int
    ): Response<Unit>

    // To-Do 업데이트
    @PUT("/schedule/todo/{id}")
    suspend fun updateTodoItem(
        @Path("id") id: Int,
        @Query("date") date: String,
        @Query("title") title: String
    ): TodoResponse

    // To-Do 완료 상태 토글
    @PATCH("/schedule/todo/{id}/toggle-completed")
    suspend fun toggleTodoItem(
        @Path("id") id: Int
    ): TodoResponse

    // 특정 To-Do 조회
    @GET("/schedule/todo/{id}")
    fun getTodoItem(
        @Path("id") id: Int
    ):Call<TodoModel>

    /**
     * Pomodoro Timer API
     */
    // 새로운 포모도로 타이머 생성
    @POST("/time/pomodoro/create")
    suspend fun registerPomodoroItem(
        @Body pomodoroRequest: PomodoroRequest
    ): Response<PomodoroResponse>

    // 회원 전체 포모도로 타이머 조회
    @GET("/time/pomodoro/member/{memberId}")
    suspend fun getAllPomodoroItems(): Response<List<PomodoroResponse>>

    // 특정 할일에 대한 포모도로 타이머 조회
    @GET("time/pomodoro/member/{memberId}/{todoId}")
    suspend fun getPomodoroItemsByTodo(
        @Path("todoId") todoId: Int
    ): Response<List<PomodoroResponse>>

    // 특정 할일에 대한 포모도로 타이머 전체 삭제
    @DELETE("time/pomodoro/todo/{todoId}")
    suspend fun deletePomodoroItemsByTodo(
        @Path("todoId") todoId: Int
    ): Response<Unit>

    // 비상 탈출 문장 생성
    @GET("/time/pomodoro/escape/generate")
    suspend fun getPomodoroEscapeSentence(): Response<String>

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
    ): ChallengeListResponse

    // 내 챌린지 목록 조회
    @GET("/challenge/my")
    suspend fun getMyChallengeList(
        @Header("Authorization") token: String
    ): List<ChallengeItemDto>

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

    // 날짜 기준 자아성찰 조회
    @GET("/schedule/self-reflection")
    suspend fun getSelfReflectionByDate(
        @Header("Authorization") token: String,
        @Query("date") date: String
    ): Response<SelfReflectionByDateResponse>

    // 날짜별 자아성찰(다이어리 + 5감사) 조회
    @GET("/schedule/self-reflection")
    suspend fun getSelfReflectionByDate(
        @Header("Authorization") token: String,
        @Query("date") date: String
    ): Response<SelfReflectionResponse>

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
     * Alarm API
     */
    // 새 알람 생성
    @POST("/time/alarm")
    suspend fun createNewAlarm(
        @Body alarmRequest: AlarmRequest
    ): Response<AlarmResponse>

    // 모든 알람 조회
    @GET("/time/alarm/me")
    suspend fun fetchAlarmList(): List<AlarmResponse>

    // 특정 알람 조회
    @GET("/time/alarm/{alarmId}")
    suspend fun fetchAlarm(
        @Path("alarmId") alarmId: Int
    ): AlarmResponse

    // 특정 알람 토글 상태 변경
    @PATCH("/time/alarm/{alarmId}/toggle")
    suspend fun toggleAlarm(
        @Path("alarmId") alarmId: Int
    ): Boolean

    // 기존 알람 업데이트
    @PUT("/time/alarm/{alarmId}")
    suspend fun updateAlarm(
        @Path("alarmId") alarmId: Int,
        @Body request: AlarmRequest
    ): Response<AlarmResponse>

    // 특정 알람 삭제
    @DELETE("/time/alarm/{alarmId}")
    suspend fun deleteAlarm(
        @Path("alarmId") alarmId: Int
    )

    // 전체 알람 활성화 (비활성화된 모든 알람을 활성화합니다)
    @PATCH("/time/alarm/activate-all")
    suspend fun activateAllAlarms()

    // 전체 알람 비활성화 (활성화된 모든 알람을 비활성화합니다)
    @PATCH("time/alarm/deactivate-all")
    suspend fun deactivateAllAlarms()

    /**
     * Alarm Mission API
     */
    // 수학 문제 생성 API
    @GET("/time/alarm/mission/math-problem")
    suspend fun generateMathProblem(
        @Query("alarmId") alarmId: Int,
        @Query("level") level: String
    ): MathProblemResponse

    // 그룹 전체 목록 조회
    @GET("/group")
    suspend fun getAllGroups(
        @Header("Authorization") token: String
    ): List<GroupResponse>

    // 참여중인 그룹 조회
    @GET("/group/user/me")
    suspend fun getMyGroups(
        @Header("Authorization") token: String
    ): List<GroupResponse>

    // 그룹 생성
    @POST("/group/create")
    fun createGroup(
        @Header("Authorization") token: String,
        @Query("name") name: String,
        @Query("description") description: String?,
        @Query("icon") icon: String?,
        @Query("statistics") statistics: List<Int>?,
        @Query("password") password: String?,
        @Query("accessType") accessType: String
    ): Call<GroupCreateResponse>

    // 목표 추가
    @FormUrlEncoded
    @POST("/group/{groupId}/goal")
    suspend fun addGoalToGroup(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long,
        @Field("name") name: String,
        @Field("goalCondition") goalCondition: String,
        @Field("value") value: Int,
        @Field("duration") duration: String
    ): Response<GroupGoalResponse>

    // 그룹 초대 코드
    @GET("/group/{groupId}/invite")
    suspend fun getGroupInviteCode(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long
    ): Response<ResponseBody>

    // 그룹 상세 조회
    @GET("/group/{id}")
    suspend fun getGroupById(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<ResponseBody>

    // 그룹 가입
    @POST("/group/{groupId}/join")
    suspend fun joinGroup(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long,
        @Query("password") password: String? = null
    ): Response<ResponseBody>

    // 그룹 탈퇴
    @POST("/group/{groupId}/leave")
    suspend fun leaveGroup(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long
    ): Response<ResponseBody>

    @GET("/group/{groupId}/sleep-stats")
    suspend fun getGroupSleepStats(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long
    ): Response<GroupSleepStatsResponse>

    @GET("/group/{groupId}/goals/progress")
    suspend fun getGroupGoalsProgress(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long
    ): Response<List<GroupGoalProgressResponseItem>>

    @GET("/groups/{groupId}/chats")
    suspend fun getGroupChats(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long
    ): Response<List<GroupChatMessage>>

    // 최근 30일 달성 인원
    @GET("/groups/{groupId}/achievements/heatmap")
    suspend fun getGoalHeatmap(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long
    ): Response<List<GroupAchievementHeatmapItem>>

    // 그룹 내 랭킹
    @GET("/groups/{groupId}/stats/ranking")
    suspend fun getGroupRanking(
        @Header("Authorization") token: String,
        @Path("groupId") groupId: Long,
        @Query("scope") scope: String
    ): Response<GroupRankingResponse>

    /**
     * PayPal 결제 API
     */
    @POST("/payments/paypal/create-order")
    suspend fun createPaypalOrder(
        @Header("Authorization") token: String
    ): Response<PayPalCreateOrderResponse>

    @POST("/payments/paypal/capture/{orderId}")
    suspend fun capturePaypalOrder(
        @Header("Authorization") token: String,
        @Path("orderId") orderId: String
    ): Response<ResponseBody>

    // 5x5 클릭 그리드 생성 API
    @GET("/time/alarm/mission/follow-click")
    suspend fun generateFollowClickProblem(
        @Query("alarmId") alarmId: Int,
        @Query("level") level: String
    ): List<List<Int>>

    // 랜덤 문장 생성
    @GET("time/alarm/mission/typing")
    suspend fun generateTypingSentence(
        @Query("alarmId") alarmId: Int
    ): String
}