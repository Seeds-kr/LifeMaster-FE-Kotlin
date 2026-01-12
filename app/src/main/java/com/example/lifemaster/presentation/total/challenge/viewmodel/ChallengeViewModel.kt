package com.example.lifemaster.presentation.total.challenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.lifemaster.data.repository.challenge.ChallengeRepository
import com.example.lifemaster.domain.model.ChallengeItem
import com.example.lifemaster.network.RetrofitInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 챌린지 목록 데이터를 관리하고 UI에 노출하는 ViewModel.
 */
// ⭐ Hilt/Koin 같은 DI(의존성 주입)를 사용하면 아래 코드가 훨씬 더 간결해집니다.
class ChallengeViewModel : ViewModel() {

    private val repository: ChallengeRepository = ChallengeRepository(RetrofitInstance.networkService)
    private val apiService = RetrofitInstance.networkService

    /**
     * UI(Fragment 또는 Activity)에서 관찰할 챌린지 목록 PagingData Flow입니다.
     * .cachedIn(viewModelScope)를 통해 화면 회전 등에도 데이터를 안전하게 유지합니다.
     */
    val challenges: Flow<PagingData<ChallengeItem>> = repository.getChallengePagingData()
        .cachedIn(viewModelScope)

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
}
