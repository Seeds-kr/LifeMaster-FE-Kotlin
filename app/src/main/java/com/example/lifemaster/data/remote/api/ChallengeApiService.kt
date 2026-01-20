import com.example.lifemaster.data.remote.dto.ChallengeListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ChallengeApiService {

    @GET("challenge")
    suspend fun getChallengeList(
        @Query("page") page: Int,
        @Query("size") size: Int = 20 // 페이지 크기는 20으로 고정하거나 필요시 파라미터로 받기
    ): ChallengeListResponse
}
