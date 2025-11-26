package com.example.lifemaster.presentation.total.challenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.lifemaster.data.repository.challenge.ChallengeRepository
import com.example.lifemaster.domain.model.ChallengeItem
import com.example.lifemaster.network.RetrofitInstance
import kotlinx.coroutines.flow.Flow

/**
 * 챌린지 목록 데이터를 관리하고 UI에 노출하는 ViewModel.
 */
// ⭐ Hilt/Koin 같은 DI(의존성 주입)를 사용하면 아래 코드가 훨씬 더 간결해집니다.
class ChallengeViewModel : ViewModel() {

    private val repository: ChallengeRepository = ChallengeRepository(RetrofitInstance.networkService)

    /**
     * UI(Fragment 또는 Activity)에서 관찰할 챌린지 목록 PagingData Flow입니다.
     * .cachedIn(viewModelScope)를 통해 화면 회전 등에도 데이터를 안전하게 유지합니다.
     */
    val challenges: Flow<PagingData<ChallengeItem>> = repository.getChallengePagingData()
        .cachedIn(viewModelScope)
}
