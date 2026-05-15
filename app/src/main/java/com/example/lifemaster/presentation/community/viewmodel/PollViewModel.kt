package com.example.lifemaster.presentation.community.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.community.model.PollDetailsDto
import com.example.lifemaster.presentation.community.model.PollListItem
import com.example.lifemaster.presentation.community.model.PollOption
import com.example.lifemaster.presentation.community.model.PollResultDto
import com.example.lifemaster.presentation.community.model.VoteRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class PollViewModel @Inject constructor(
    private val networkService: NetworkService
) : ViewModel() {

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

    private fun bearer(token: String) = "Bearer $token"

    fun fetchActivePoll(token: String, onError: (String) -> Unit = {}) {
        val auth = bearer(token)

        networkService.getPollList(auth)
            .enqueue(object : Callback<List<PollListItem>> {
                override fun onResponse(
                    call: Call<List<PollListItem>>,
                    res: Response<List<PollListItem>>
                ) {
                    if (!res.isSuccessful) {
                        onError("투표 목록 오류(${res.code()})")
                        _ui.value = null
                        return
                    }

                    val list = res.body().orEmpty()
                    val active = list.firstOrNull { it.status.contains("진행") }
                        ?: list.firstOrNull()

                    if (active == null) {
                        _ui.value = null
                        return
                    }

                    fetchPollDetails(token, active.pollId, onError)
                }

                override fun onFailure(call: Call<List<PollListItem>>, t: Throwable) {
                    onError("네트워크 오류(목록): ${t.message ?: ""}")
                }
            })
    }

    fun fetchPollDetails(token: String, pollId: Long, onError: (String) -> Unit = {}) {
        val auth = bearer(token)

        networkService.getPollDetails(auth, pollId)
            .enqueue(object : Callback<PollDetailsDto> {
                override fun onResponse(
                    call: Call<PollDetailsDto>,
                    res: Response<PollDetailsDto>
                ) {
                    if (!res.isSuccessful) {
                        onError("투표 상세 오류(${res.code()})")
                        return
                    }

                    val d = res.body() ?: return

                    val normalized = d.options.mapIndexed { idx, o ->
                        o.copy(optionId = o.optionId ?: (idx + 1))
                    }

                    val total = d.totalVotes ?: normalized.sumOf { it.votes ?: 0 }

                    _ui.value = PollUi(
                        pollId = pollId,
                        title = d.title,
                        isExpired = d.isExpired,
                        options = normalized,
                        totalVotes = total,
                        myVotedOptionId = d.myVotedOptionId
                    )

                    fetchPollResults(token, pollId)
                }

                override fun onFailure(call: Call<PollDetailsDto>, t: Throwable) {
                    onError("네트워크 오류(상세): ${t.message ?: ""}")
                }
            })
    }

    /**
     * 결과(표 수/퍼센트)만 따로 조회해서 UI 갱신
     */
    fun fetchPollResults(
        token: String,
        pollId: Long,
        then: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val auth = bearer(token)

        networkService.getPollResults(auth, pollId)
            .enqueue(object : Callback<Map<String, PollResultDto>> {
                override fun onResponse(
                    call: Call<Map<String, PollResultDto>>,
                    res: Response<Map<String, PollResultDto>>
                ) {
                    if (!res.isSuccessful) {
                        onError("투표 결과 오류(${res.code()})")
                        then()
                        return
                    }

                    val map = res.body().orEmpty()
                    val now = _ui.value ?: return then()
                    if (now.pollId != pollId) return then()

                    val updated = now.options.map { opt ->
                        val keyById = (opt.optionId ?: 0).toString()
                        val result = map[keyById]
                            ?: opt.content.toIntOrNull()?.let { map[it.toString()] }
                        if (result != null) {
                            opt.copy(
                                votes = result.votes,
                                votePercentage = result.percentage
                            )
                        } else opt
                    }

                    val totalVotes = updated.sumOf { it.votes ?: 0 }
                    val finalized = updated.map { o ->
                        if (o.votePercentage == null) {
                            val pct = if (totalVotes > 0)
                                ((o.votes ?: 0) * 100f / totalVotes).roundToInt()
                            else 0
                            o.copy(votePercentage = pct)
                        } else o
                    }

                    _ui.value = now.copy(
                        options = finalized,
                        totalVotes = totalVotes
                    )
                    then()
                }

                override fun onFailure(call: Call<Map<String, PollResultDto>>, t: Throwable) {
                    onError("네트워크 오류(결과): ${t.message ?: ""}")
                    then()
                }
            })
    }

    fun castVote(
        token: String,
        pollId: Long,
        optionId: Int,
        userId: String,
        onDone: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val bearerToken = bearer(token)

        networkService
            .castVote(bearerToken, pollId, VoteRequest(optionId = optionId, userId = userId))
            .enqueue(object : Callback<ResponseBody> {

                override fun onResponse(call: Call<ResponseBody>, res: Response<ResponseBody>) {
                    if (res.isSuccessful) {
                        val now = _ui.value
                        if (now != null && now.pollId == pollId) {
                            _ui.value = now.copy(myVotedOptionId = optionId)
                        }
                        fetchPollResults(token, pollId) { onDone() }
                        return
                    }

                    val err = try {
                        res.errorBody()?.string()
                    } catch (_: Throwable) {
                        null
                    }
                    val msg = err ?: ""

                    // 이미 투표한 사용자 케이스 (409 또는 400 + 메시지)
                    val alreadyVoted =
                        res.code() == 409 ||
                                (res.code() == 400 && msg.contains("이미 투표한"))

                    if (alreadyVoted) {
                        // 이미 투표한 경우에도 결과는 보여줘야 하므로 결과 다시 조회
                        fetchPollResults(token, pollId) {
                            val now = _ui.value
                            _ui.value = now?.copy(
                                myVotedOptionId = now.myVotedOptionId ?: optionId
                            )
                            onDone()
                        }
                        return
                    }

                    when (res.code()) {
                        401, 403 -> onError("인증 필요: 로그인/토큰 확인. ${msg}".trim())
                        else -> onError("투표 실패(${res.code()}) ${msg}".trim())
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onError("네트워크 오류(투표): ${t.message ?: ""}")
                }
            })
    }
}