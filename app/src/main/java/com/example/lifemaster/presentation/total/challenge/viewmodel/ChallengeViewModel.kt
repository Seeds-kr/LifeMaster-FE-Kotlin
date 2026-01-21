package com.example.lifemaster.presentation.total.challenge.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.example.lifemaster.presentation.total.challenge.model.ChallengeItemDto
import com.example.lifemaster.presentation.total.challenge.model.ChallengeItem
import com.example.lifemaster.network.NetworkService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.lifemaster.network.RetrofitInstance
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

// DI(의존성 주입) 패턴을 사용하여 의존성을 생성자로 주입받음
class ChallengeViewModel(
    private val apiService: NetworkService
) : ViewModel() {

    /**
     * 챌린지 목록 데이터를 서버에서 페이지 단위로 로드하는 PagingSource 구현체.
     */
    private inner class ChallengePagingSource : PagingSource<Int, ChallengeItem>() {
        private val STARTING_PAGE_INDEX = 0

        override fun getRefreshKey(state: PagingState<Int, ChallengeItem>): Int? {
            return state.anchorPosition?.let { anchorPosition ->
                state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                    ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
            }
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChallengeItem> {
            val currentPage = params.key ?: STARTING_PAGE_INDEX

            return try {
                val response = apiService.getChallenges(
                    page = currentPage,
                    size = params.loadSize
                )

                if (!response.isSuccessful || response.body() == null) {
                    return LoadResult.Error(HttpException(response))
                }

                val responseBody = response.body()!!

                // API 응답(DTO)을 앱에서 사용할 모델(Domain Model)로 변환합니다.
                val challenges: List<ChallengeItem> = responseBody.content.map { dto ->
                    ChallengeItem(
                        challId = dto.challId,
                        challName = dto.challName,
                        challTitle = dto.challDesc,  // API의 challDesc를 UI의 challTitle로 사용
                        challImg = dto.challImg,
                        challJoinCnt = dto.challCnt // API의 challCnt를 UI의 challJoinCnt로 사용
                    )
                }

                // 다음 페이지 키 계산: API 응답의 last 필드를 사용하여 마지막 페이지 여부를 정확히 판단합니다.
                val nextKey = if (responseBody.last) null else currentPage + 1

                LoadResult.Page(
                    data = challenges,
                    prevKey = if (currentPage == STARTING_PAGE_INDEX) null else currentPage - 1,
                    nextKey = nextKey
                )
            } catch (e: IOException) {
                // 네트워크 연결 문제 (IO 예외) 처리
                LoadResult.Error(e)
            } catch (e: HttpException) {
                // HTTP 오류 (4xx, 5xx 등) 처리
                LoadResult.Error(e)
            }
        }
    }

    /**
     * UI(Fragment 또는 Activity)에서 관찰할 챌린지 목록 PagingData Flow
     * .cachedIn(viewModelScope)를 통해 화면 회전 등에도 데이터를 안전하게 유지
     */
    val challenges: Flow<PagingData<ChallengeItem>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { ChallengePagingSource() }
    ).flow.cachedIn(viewModelScope)

    // 검색 결과를 저장하는 StateFlow (PagingData로 변환)
    private val _searchResults = MutableStateFlow<PagingData<ChallengeItem>>(PagingData.empty())
    val searchResults = _searchResults.asStateFlow()

    // 검색 모드 여부
    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode = _isSearchMode.asStateFlow()
    private val _originalChallengeData = MutableLiveData<List<ChallengeItem>>()
    private val _sortedChallengeList = MutableLiveData<List<ChallengeItem>>()
    val sortedChallengeList: LiveData<List<ChallengeItem>> = _sortedChallengeList

    /**
     * 챌린지 참여 API를 호출하는 함수
     * @param token Authorization 토큰 (Bearer 포함)
     * @param challId 참여할 챌린지 ID
     * @param onSuccess 성공 시 호출될 콜백
     * @param onError 실패 시 호출될 콜백 (에러 메시지 전달)
     */
    fun joinChallenge(
        token: String,
        challId: Long,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val response = apiService.joinChallenge(token, challId)
                if (response.isSuccessful) {
                    val message = response.body() ?: "챌린지 참여 완료!"
                    onSuccess(message)
                } else {
                    onError("챌린지 참여 실패 (${response.code()})")
                }
            } catch (e: Exception) {
                onError("네트워크 오류: ${e.localizedMessage}")
            }
        }
    }

    /**
     * 챌린지 참여 취소 API를 호출하는 함수
     * @param token Authorization 토큰 (Bearer 포함)
     * @param challId 참여 취소할 챌린지 ID
     * @param onSuccess 성공 시 호출될 콜백
     * @param onError 실패 시 호출될 콜백 (에러 메시지 전달)
     */
    fun leaveChallenge(
        token: String,
        challId: Long,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val response = apiService.leaveChallenge(token, challId)
                if (response.isSuccessful) {
                    val message = response.body() ?: "챌린지 참여 취소 완료!"
                    onSuccess(message)
                } else {
                    onError("챌린지 참여 취소 실패 (${response.code()})")
                }
            } catch (e: Exception) {
                onError("네트워크 오류: ${e.localizedMessage}")
            }
        }
    }

    suspend fun loadChallenges() {
        Log.d("API_CALL", "챌린지 API 호출 시작")
        try {
            val response = RetrofitInstance.networkService.getChallenges(page = 0, size = 10)

            if (response.isSuccessful) {
                response.body()?.content?.let { dtoList ->
                    // DTO를 ChallengeItem으로 변환
                    val challengeList: List<ChallengeItem> = dtoList.map { dto ->
                        ChallengeItem(
                            challId = dto.challId,
                            challName = dto.challName,
                            challTitle = dto.challDesc,
                            challImg = dto.challImg,
                            challJoinCnt = dto.challCnt,
                            createdAt = dto.createdAt
                        )
                    }
                    _originalChallengeData.value = challengeList
                    sortChallenges("latest")
                    Log.d("ViewModel", "챌린지 로딩 성공: ${challengeList.size}개")
                }
            } else {
                Log.e("ViewModel", "서버 응답 에러: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("ViewModel", "통신 실패: ${e.message}")
        }
    }





    /**
     * 챌린지 상세 정보 조회 API를 호출하는 함수
     * @param token Authorization 토큰 (Bearer 포함)
     * @param challId 조회할 챌린지 ID
     * @param onSuccess 성공 시 호출될 콜백 (ChallengeItemDto 전달)
     * @param onError 실패 시 호출될 콜백 (에러 메시지 전달)
     */
    fun getChallengeDetail(
        token: String,
        challId: Long,
        onSuccess: (ChallengeItemDto) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val response = apiService.getChallengeDetail(token, challId)
                if (response.isSuccessful) {
                    val challengeDetail = response.body()
                    if (challengeDetail != null) {
                        onSuccess(challengeDetail)
                    } else {
                        onError("챌린지 정보를 불러올 수 없습니다.")
                    }
                } else {
                    onError("챌린지 조회 실패 (${response.code()})")
                }
            } catch (e: Exception) {
                onError("네트워크 오류: ${e.localizedMessage}")
            }
        }
    }

    /**
     * 챌린지 검색 API를 호출하는 함수
     * @param token Authorization 토큰 (Bearer 포함)
     * @param searchQuery 검색어
     * @param page 페이지 번호 (기본값: 0)
     * @param onSuccess 성공 시 호출될 콜백
     * @param onError 실패 시 호출될 콜백 (에러 메시지 전달)
     */
    fun searchChallenges(
        token: String,
        searchQuery: String,
        page: Int = 0,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val response = apiService.searchChallenges(token, searchQuery, page)
                // API 응답(DTO)을 앱에서 사용할 모델(Domain Model)로 변환
                val challenges: List<ChallengeItem> = response.content.map { dto ->
                    ChallengeItem(
                        challId = dto.challId,
                        challName = dto.challName,
                        challTitle = dto.challDesc,
                        challImg = dto.challImg,
                        challJoinCnt = dto.challCnt,
                        createdAt = dto.createdAt
                    )
                }
                // 검색 결과를 PagingData로 변환
                _searchResults.value = PagingData.from(challenges)
                _isSearchMode.value = true
                onSuccess()
            } catch (e: Exception) {
                onError("네트워크 오류: ${e.localizedMessage}")
            }
        }
    }

    /**
     * 검색 모드를 해제하고 일반 목록으로 돌아감
     */
    fun clearSearch() {
        _isSearchMode.value = false
        _searchResults.value = PagingData.empty()
    }

    fun sortChallenges(criteria: String) {
        val currentList = _originalChallengeData.value ?: return

        val sortedList = when (criteria) {
            "latest" -> currentList.sortedWith(compareByDescending { it.createdAt ?: "" })
            "oldest" -> currentList.sortedWith(compareBy { it.createdAt ?: "" })
            "popularity" -> currentList.sortedWith(compareByDescending { it.challJoinCnt })
            "name" -> currentList.sortedWith(compareBy { it.challName })
            else -> currentList
        }

        _sortedChallengeList.value = sortedList
        Log.d("ViewModel", "챌린지 정렬 완료: 기준=$criteria")
    }
}