package com.example.lifemaster.presentation.community.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.community.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt

class PollViewModel : ViewModel() {

    data class PollUi(
        val pollId: Long,
        val title: String,
        val isExpired: Boolean,
        val options: List<PollOption>,
        val totalVotes: Int,
        val myVotedOptionId: Int?
    )

    private val _ui = MutableLiveData<PollUi?>()
    val ui: LiveData<PollUi?> = _ui

    fun fetchActivePoll(token: String? = null, onError: (String) -> Unit = {}) {
        RetrofitInstance.networkService.getPollList()
            .enqueue(object : Callback<List<PollListItem>> {
                override fun onResponse(
                    call: Call<List<PollListItem>>,
                    res: Response<List<PollListItem>>
                ) {
                    val list = res.body().orEmpty()
                    val active = list.firstOrNull { it.status.contains("진행") } ?: list.firstOrNull()
                    if (active == null) { _ui.value = null; return }
                    fetchPollDetails(active.pollId, onError)
                }
                override fun onFailure(call: Call<List<PollListItem>>, t: Throwable) {
                    onError("네트워크 오류(목록): ${t.message ?: ""}")
                }
            })
    }

    fun fetchPollDetails(pollId: Long, onError: (String) -> Unit = {}) {
        RetrofitInstance.networkService.getPollDetails(pollId)
            .enqueue(object : Callback<PollDetailsDto> {
                override fun onResponse(
                    call: Call<PollDetailsDto>,
                    res: Response<PollDetailsDto>
                ) {
                    val d = res.body() ?: return
                    val normalized = d.options.mapIndexed { idx, o ->
                        o.copy(optionId = o.optionId ?: (idx + 1))
                    }
                    val total = normalized.sumOf { it.votes ?: 0 }
                    _ui.value = PollUi(
                        pollId = pollId,
                        title = d.title,
                        isExpired = d.isExpired,
                        options = normalized,
                        totalVotes = total,
                        myVotedOptionId = null
                    )
                    fetchPollResults(pollId)
                }
                override fun onFailure(call: Call<PollDetailsDto>, t: Throwable) {
                    onError("네트워크 오류(상세): ${t.message ?: ""}")
                }
            })
    }

    fun fetchPollResults(
        pollId: Long,
        then: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        RetrofitInstance.networkService.getPollResults(pollId)
            .enqueue(object : Callback<Map<String, PollResultDto>> {
                override fun onResponse(
                    call: Call<Map<String, PollResultDto>>,
                    res: Response<Map<String, PollResultDto>>
                ) {
                    val map = res.body().orEmpty()
                    val now = _ui.value ?: return then()
                    if (now.pollId != pollId) return then()

                    val updated = now.options.map { opt ->
                        val keyById = (opt.optionId ?: 0).toString()
                        val result = map[keyById]
                            ?: opt.content.toIntOrNull()?.let { map[it.toString()] }
                        if (result != null) {
                            opt.copy(votes = result.votes, votePercentage = result.percentage)
                        } else opt
                    }

                    val totalVotes = updated.sumOf { it.votes ?: 0 }
                    val finalized = updated.map { o ->
                        if (o.votePercentage == null) {
                            val pct = if (totalVotes > 0)
                                ((o.votes ?: 0) * 100f / totalVotes).roundToInt() else 0
                            o.copy(votePercentage = pct)
                        } else o
                    }

                    _ui.value = now.copy(options = finalized, totalVotes = totalVotes)
                    then()
                }
                override fun onFailure(call: Call<Map<String, PollResultDto>>, t: Throwable) {
                    onError("네트워크 오류(결과): ${t.message ?: ""}")
                    then()
                }
            })
    }

    fun castVote(
        token: String?,
        pollId: Long,
        optionIndex1Based: Int,
        userId: String,
        onDone: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val bearer = token?.takeIf { it.isNotBlank() }?.let { "Bearer $it" }

        fun request(optionIdToSend: Int, retryZeroBase: Boolean) {
            RetrofitInstance.networkService
                .castVote(bearer, pollId, VoteRequest(optionId = optionIdToSend, userId = userId))
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, res: Response<ResponseBody>) {
                        if (res.isSuccessful) {
                            val now = _ui.value
                            if (now != null && now.pollId == pollId) {
                                _ui.value = now.copy(myVotedOptionId = optionIdToSend)
                            }
                            fetchPollResults(pollId) { onDone() }
                            return
                        }

                        when (res.code()) {
                            401, 403 -> onError("인증 필요: 로그인 또는 토큰 확인")
                            409 -> {
                                fetchPollResults(pollId) {
                                    val now = _ui.value
                                    _ui.value = now?.copy(myVotedOptionId = now?.myVotedOptionId ?: optionIdToSend)
                                    onDone()
                                }
                            }
                            in 400..499 -> {
                                if (retryZeroBase) {
                                    val zeroBase = optionIndex1Based - 1
                                    if (zeroBase >= 0) {
                                        request(zeroBase, retryZeroBase = false)
                                    } else onError("투표 실패(${res.code()})")
                                } else onError("투표 실패(${res.code()})")
                            }
                            else -> onError("투표 실패(${res.code()})")
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        onError("네트워크 오류(투표): ${t.message ?: ""}")
                    }
                })
        }

        request(optionIndex1Based, retryZeroBase = true)
    }
}