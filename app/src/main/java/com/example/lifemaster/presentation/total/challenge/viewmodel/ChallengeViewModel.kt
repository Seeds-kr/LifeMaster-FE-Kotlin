package com.example.lifemaster.presentation.total.challenge.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.presentation.total.challenge.model.ChallengeItemDto
import com.example.lifemaster.presentation.total.challenge.model.ChallengeItem
import com.example.lifemaster.network.NetworkService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val apiService: NetworkService
) : ViewModel() {

    private val _myParticipatingIds = MutableStateFlow<Set<Long>>(emptySet())
    val myParticipatingIds: StateFlow<Set<Long>> = _myParticipatingIds.asStateFlow()

    private val _myParticipatingChallenges = MutableStateFlow<List<ChallengeItem>>(emptyList())
    val myParticipatingChallenges: StateFlow<List<ChallengeItem>> = _myParticipatingChallenges.asStateFlow()

    private val _catalogDtos = MutableStateFlow<List<ChallengeItemDto>>(emptyList())

    fun isUserParticipating(challId: Long): Boolean = challId in _myParticipatingIds.value

    suspend fun refreshMyParticipatingChallenges(token: String) {
        runCatching {
            apiService.getMyChallengeList(token)
        }.onSuccess { list ->
            _myParticipatingIds.value = list.map { it.challId }.toSet()
            _myParticipatingChallenges.value = list.map { mapDtoToItem(it) }
            rebuildCatalogFromCache()
        }.onFailure { e ->
            Log.e("ChallengeViewModel", "내 챌린지 목록 로드 실패", e)
        }
    }

    private fun mapDtoToItem(dto: ChallengeItemDto): ChallengeItem {
        val ids = _myParticipatingIds.value
        val joined = dto.challMe == true || dto.challId in ids
        return ChallengeItem(
            challId = dto.challId,
            challName = dto.challName,
            challTitle = dto.challDesc,
            challImg = dto.challImg,
            challJoinCnt = dto.challCnt,
            createdAt = dto.createdAt,
            isJoined = joined
        )
    }

    private fun rebuildCatalogFromCache() {
        val dtos = _catalogDtos.value
        if (dtos.isNotEmpty()) {
            _originalChallengeData.value = dtos.map { mapDtoToItem(it) }
            publishChallengeList()
        }
    }

    private val _originalChallengeData = MutableStateFlow<List<ChallengeItem>>(emptyList())
    private val _sortedChallengeList = MutableStateFlow<List<ChallengeItem>>(emptyList())
    val sortedChallengeList: StateFlow<List<ChallengeItem>> = _sortedChallengeList.asStateFlow()

    /** 서버 순서 유지: 생성일 내림차순만 적용 */
    private fun publishChallengeList() {
        val currentList = _originalChallengeData.value
        if (currentList.isEmpty()) {
            _sortedChallengeList.value = emptyList()
            return
        }
        val ordered = currentList.sortedWith(compareByDescending { it.createdAt ?: "" })
        _sortedChallengeList.value = ordered
    }

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
                    _myParticipatingIds.update { it + challId }
                    rebuildCatalogFromCache()
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
                    _myParticipatingIds.update { it - challId }
                    rebuildCatalogFromCache()
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

    fun loadChallenges(token: String) {
        Log.d("ChallengeViewModel", "챌린지 API 호출 시작 (Token: ${token.take(15)}...)")
        viewModelScope.launch {
            try {
                // 먼저 내 참여 목록을 확실히 갱신
                refreshMyParticipatingChallenges(token)

                val response = apiService.getChallenges(token, page = 0, size = 20)

                if (response.isSuccessful) {
                    val body = response.body()
                    val dtoList = body?.content ?: emptyList()
                    Log.d("ChallengeViewModel", "API 호출 성공: 총 ${dtoList.size}개의 챌린지 수신")
                    
                    _catalogDtos.value = dtoList
                    val challengeList = dtoList.map { mapDtoToItem(it) }
                    _originalChallengeData.value = challengeList
                    publishChallengeList()
                    
                    if (challengeList.isEmpty()) {
                        Log.w("ChallengeViewModel", "수신된 챌린지 목록이 비어있습니다. (Response body: $body)")
                    }
                } else {
                    Log.e("ChallengeViewModel", "서버 응답 에러: ${response.code()} ${response.message()}")
                    _sortedChallengeList.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "통신 중 예외 발생: ${e.message}", e)
                _sortedChallengeList.value = emptyList()
            }
        }
    }

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
}
