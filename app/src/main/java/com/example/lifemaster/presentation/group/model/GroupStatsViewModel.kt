package com.example.lifemaster.presentation.group.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.group.model.GroupGoalProgressResponseItem
import com.example.lifemaster.presentation.group.model.GroupSleepStatsResponse
import com.example.lifemaster.presentation.total.mypage.model.MeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

data class GroupStatsUiState(
    val sleepDurations: List<Int> = emptyList(),
    val sleepMessage: String = "",
    val pomodoroMy: List<Int> = emptyList(),
    val pomodoroGroup: List<Int> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class GroupStatsViewModel @Inject constructor(
    private val networkService: NetworkService
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupStatsUiState())
    val uiState: StateFlow<GroupStatsUiState> = _uiState

    fun load(token: String, groupId: Long) {
        viewModelScope.launch {
            try {
                val meDeferred = async { networkService.getMe(token) }
                val sleepDeferred = async { networkService.getGroupSleepStats(token, groupId) }
                val goalsDeferred = async { networkService.getGroupGoalsProgress(token, groupId) }

                val meResp: Response<MeResponse> = meDeferred.await()
                val sleepResp: Response<GroupSleepStatsResponse> = sleepDeferred.await()
                val goalsResp: Response<List<GroupGoalProgressResponseItem>> = goalsDeferred.await()

                val myEmail = meResp.body()?.email
                val sleepDurations = sleepResp.body()?.userSleepDurations.orEmpty()

                val goals = goalsResp.body().orEmpty()
                val pomodoro = goals.firstOrNull { it.goalName.contains("뽀모도로") }

                val myProgress = pomodoro?.userProgress
                    ?.firstOrNull { it.userEmail == myEmail }
                    ?.progress ?: 0

                val groupAvg = pomodoro?.userProgress
                    ?.mapNotNull { it.progress }
                    ?.average()
                    ?.toInt() ?: 0

                _uiState.value = GroupStatsUiState(
                    sleepDurations = sleepDurations,
                    sleepMessage = buildSleepMessage(sleepDurations),
                    pomodoroMy = listOf(myProgress),
                    pomodoroGroup = listOf(groupAvg),
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = GroupStatsUiState(
                    error = e.message ?: "unknown error"
                )
            }
        }
    }

    private fun buildSleepMessage(list: List<Int>): String {
        if (list.isEmpty()) return ""
        val avg = list.average().toInt()
        return "최근 평균 수면: ${avg}분"
    }
}