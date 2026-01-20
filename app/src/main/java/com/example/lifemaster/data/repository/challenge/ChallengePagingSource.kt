package com.example.lifemaster.data.repository.challenge

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.lifemaster.domain.model.ChallengeItem
import com.example.lifemaster.network.NetworkService
import retrofit2.HttpException
import java.io.IOException

/**
 * 챌린지 목록 데이터를 서버에서 페이지 단위로 로드하는 PagingSource 구현체.
 */
class ChallengePagingSource(
    private val apiService: NetworkService
) : PagingSource<Int, ChallengeItem>() {

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

            // API 응답(DTO)을 앱에서 사용할 모델(Domain Model)로 변환합니다.
            val challenges: List<ChallengeItem> = response.content.map { dto ->
                ChallengeItem(
                    challId = dto.challId,
                    challName = dto.challName,
                    challTitle = dto.challDesc,  // API의 challDesc를 UI의 challTitle로 사용
                    challImg = dto.challImg,
                    challJoinCnt = dto.challCnt // API의 challCnt를 UI의 challJoinCnt로 사용
                )
            }

            // 다음 페이지 키 계산: API 응답의 last 필드를 사용하여 마지막 페이지 여부를 정확히 판단합니다.
            val nextKey = if (response.last) null else currentPage + 1

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
