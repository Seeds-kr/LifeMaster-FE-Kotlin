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

class CommunityViewModel : ViewModel() {

    private val _items = MutableLiveData<List<CommunityItem>>(emptyList())
    val items: LiveData<List<CommunityItem>> = _items

    private val _bestItems = MutableLiveData<List<CommunityItem>>(emptyList())
    val bestItems: LiveData<List<CommunityItem>> = _bestItems
    private var lastBestIds: List<String> = emptyList()

    private val _postDetail = MutableLiveData<PostDetailDto?>()
    val postDetail: LiveData<PostDetailDto?> = _postDetail

    private val likedPosts = mutableSetOf<String>()
    private val likeCounts = mutableMapOf<String, Int>()
    private val viewCounts = mutableMapOf<String, Int>()

    private fun mutateItem(id: String, transform: (CommunityItem) -> CommunityItem) {
        val cur = _items.value ?: return
        val idx = cur.indexOfFirst { it.id == id }
        if (idx < 0) return
        val next = cur.toMutableList()
        next[idx] = transform(next[idx])
        _items.postValue(next)
    }

    private fun updateLikeStateEverywhere(id: String, liked: Boolean, count: Int) {
        if (liked) likedPosts.add(id) else likedPosts.remove(id)
        likeCounts[id] = count.coerceAtLeast(0)
        _postDetail.value = _postDetail.value?.copy(liked = liked, likeCount = count.coerceAtLeast(0))
        mutateItem(id) { it.copy(likes = count.coerceAtLeast(0)) }
    }

    fun isPostLiked(id: Long): Boolean = likedPosts.contains(id.toString())
    fun getLikeCount(id: String): Int = likeCounts[id] ?: 0

    fun increaseViewCount(postId: String): Int {
        val next = (viewCounts[postId] ?: 0) + 1
        viewCounts[postId] = next
        mutateItem(postId) { it.copy(views = next) }
        return next
    }

    fun fetchPostsByType(token: String, type: String, onError: (String) -> Unit = {}) {
        RetrofitInstance.networkService
            .getPostsByType(bear(token), type)
            .enqueue(object : Callback<List<PostSummaryDto>> {
                override fun onResponse(call: Call<List<PostSummaryDto>>, res: Response<List<PostSummaryDto>>) {
                    if (!res.isSuccessful) { onError("목록 조회 실패 (${res.code()})"); return }
                    val list = (res.body() ?: emptyList()).mapIndexed { idx, dto ->
                        dto.toCommunityItem(idStr = dto.id?.toString() ?: "tmp_$idx")
                    }
                    _items.postValue(list)
                    if (lastBestIds.isNotEmpty()) updateBestFromIds()
                }
                override fun onFailure(call: Call<List<PostSummaryDto>>, t: Throwable) {
                    onError("네트워크 오류(목록): ${t.localizedMessage}")
                }
            })
    }
    fun fetchFreePosts(token: String, onError: (String) -> Unit = {}) =
        fetchPostsByType(token, "FREE", onError)

    fun fetchPopularPosts(token: String, onError: (String) -> Unit = {}) {
        RetrofitInstance.networkService
            .getPopularPosts(bear(token))
            .enqueue(object : Callback<List<PostSummaryDto>> {
                override fun onResponse(call: Call<List<PostSummaryDto>>, res: Response<List<PostSummaryDto>>) {
                    if (!res.isSuccessful) {
                        onError("인기글 조회 실패 (${res.code()})")
                        _bestItems.postValue(emptyList())
                        return
                    }
                    val list = (res.body() ?: emptyList()).mapIndexed { idx, dto ->
                        dto.toCommunityItem(idStr = dto.id?.toString() ?: "best_$idx")
                    }
                    _bestItems.postValue(list)
                }
                override fun onFailure(call: Call<List<PostSummaryDto>>, t: Throwable) {
                    onError("네트워크 오류(인기글): ${t.localizedMessage}")
                    _bestItems.postValue(emptyList())
                }
            })
    }
    private fun updateBestFromIds() {
        val cur = _items.value.orEmpty()
        _bestItems.postValue(cur.filter { it.id in lastBestIds })
    }

    fun getById(id: String): CommunityItem? =
        _items.value?.firstOrNull { it.id == id }

    fun fetchPostDetail(
        token: String,
        id: String,
        onDone: (PostDetailDto) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        RetrofitInstance.networkService
            .getPostDetail(bear(token), id)
            .enqueue(object : Callback<PostDetailDto> {
                override fun onResponse(call: Call<PostDetailDto>, res: Response<PostDetailDto>) {
                    val body = res.body()
                    if (!res.isSuccessful || body == null) {
                        onError("상세 조회 실패 (${res.code()})"); return
                    }
                    val likedNow  = likedPosts.contains(id) || (body.liked == true)
                    val countNow  = likeCounts[id] ?: (body.likeCount ?: 0)
                    val merged = body.copy(liked = likedNow, likeCount = countNow)
                    _postDetail.postValue(merged)
                    updateLikeStateEverywhere(id, likedNow, countNow)
                    onDone(merged)
                }
                override fun onFailure(call: Call<PostDetailDto>, t: Throwable) {
                    onError("네트워크 오류(상세): ${t.localizedMessage}")
                }
            })
    }

    fun togglePostLikeRemote(
        token: String,
        id: Long,
        onDone: (Int) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val key = id.toString()
        val beforeLiked = likedPosts.contains(key)
        val beforeCnt   = likeCounts[key] ?: _postDetail.value?.likeCount ?: 0

        val nowLiked = !beforeLiked
        val nowCnt   = (if (nowLiked) beforeCnt + 1 else beforeCnt - 1).coerceAtLeast(0)
        updateLikeStateEverywhere(key, nowLiked, nowCnt)
        onDone(nowCnt)

        RetrofitInstance.networkService
            .togglePostLike(bear(token), key)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, res: Response<ResponseBody>) {
                    if (res.isSuccessful) return
                    updateLikeStateEverywhere(key, beforeLiked, beforeCnt)
                    onDone(beforeCnt)
                    onError("좋아요 실패 (${res.code()})")
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    updateLikeStateEverywhere(key, beforeLiked, beforeCnt)
                    onDone(beforeCnt)
                    onError("네트워크 오류(좋아요): ${t.localizedMessage}")
                }
            })
    }

    private val _comments = MutableLiveData<List<CommentDto>>(emptyList())
    val comments: LiveData<List<CommentDto>> = _comments

    private val _isCommentSyncing = MutableLiveData(false)
    val isCommentSyncing: LiveData<Boolean> = _isCommentSyncing

    fun fetchComments(token: String, postId: String, onError: (String) -> Unit = {}) {
        RetrofitInstance.networkService
            .getComments(bear(token), postId)
            .enqueue(object : Callback<List<CommentDto>> {
                override fun onResponse(call: Call<List<CommentDto>>, res: Response<List<CommentDto>>) {
                    if (res.isSuccessful) _comments.postValue(res.body() ?: emptyList())
                    else onError("댓글 조회 실패 (${res.code()})")
                }
                override fun onFailure(call: Call<List<CommentDto>>, t: Throwable) {
                    onError("네트워크 오류(댓글): ${t.localizedMessage}")
                }
            })
    }

    fun addComment(token: String, postId: String, text: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        _isCommentSyncing.postValue(true)
        RetrofitInstance.networkService
            .createComment(bear(token), postId, NewCommentRequest(text))
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, res: Response<ResponseBody>) {
                    _isCommentSyncing.postValue(false)
                    if (res.isSuccessful) { onDone(); fetchComments(token, postId, onError) }
                    else onError("댓글 등록 실패 (${res.code()})")
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _isCommentSyncing.postValue(false)
                    onError("네트워크 오류(댓글 등록): ${t.localizedMessage}")
                }
            })
    }

    fun updateComment(token: String, postId: String, commentId: Long, text: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        _isCommentSyncing.postValue(true)
        RetrofitInstance.networkService
            .updateComment(bear(token), postId, commentId.toString(), NewCommentRequest(text))
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, res: Response<ResponseBody>) {
                    _isCommentSyncing.postValue(false)
                    if (res.isSuccessful) { onDone(); fetchComments(token, postId, onError) }
                    else onError("댓글 수정 실패 (${res.code()})")
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _isCommentSyncing.postValue(false)
                    onError("네트워크 오류(댓글 수정): ${t.localizedMessage}")
                }
            })
    }

    fun deleteComment(token: String, postId: String, commentId: Long, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        _isCommentSyncing.postValue(true)
        RetrofitInstance.networkService
            .deleteComment(bear(token), postId, commentId.toString())
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, res: Response<ResponseBody>) {
                    _isCommentSyncing.postValue(false)
                    if (res.isSuccessful) { onDone(); fetchComments(token, postId, onError) }
                    else onError("댓글 삭제 실패 (${res.code()})")
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _isCommentSyncing.postValue(false)
                    onError("네트워크 오류(댓글 삭제): ${t.localizedMessage}")
                }
            })
    }

    fun createPost(token: String, title: String, content: String, file: String?, type: String = "FREE", onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val body = NewPostRequest(title = title, content = content, file = file, type = type)
        RetrofitInstance.networkService
            .createPost(bear(token), body)
            .enqueue(simpleCallback("게시글 등록", onSuccess, onError))
    }

    fun updatePost(token: String, id: String, title: String, content: String, file: String?, type: String = "FREE", onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val body = UpdatePostRequest(title = title, content = content, file = file, type = type)
        RetrofitInstance.networkService
            .updatePost(bear(token), id, body)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, res: Response<ResponseBody>) {
                    if (!res.isSuccessful) {
                        val err = try { res.errorBody()?.string() } catch (_: Throwable) { null }
                        onError("게시글 수정 실패 (${res.code()})${err?.let { "\n$it" } ?: ""}")
                        return
                    }
                    _items.value?.toMutableList()?.let { cur ->
                        val i = cur.indexOfFirst { it.id == id }
                        if (i >= 0) {
                            cur[i] = cur[i].copy(title = title, content = content, fileUri = file, type = type)
                            _items.postValue(cur)
                        }
                    }
                    _postDetail.value = _postDetail.value?.copy(title = title, content = content, file = file, type = type)
                    onSuccess()
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onError("네트워크 오류(수정): ${t.localizedMessage}")
                }
            })
    }

    fun deletePost(token: String, id: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        RetrofitInstance.networkService
            .deletePost(bear(token), id)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, res: Response<ResponseBody>) {
                    if (!res.isSuccessful) { onError("게시글 삭제 실패 (${res.code()})"); return }
                    _items.value?.toMutableList()?.let { cur ->
                        val i = cur.indexOfFirst { it.id == id }
                        if (i >= 0) { cur.removeAt(i); _items.postValue(cur) }
                    }
                    onSuccess()
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onError("네트워크 오류(삭제): ${t.localizedMessage}")
                }
            })
    }

    private fun bear(token: String): String =
        if (token.startsWith("Bearer ")) token else "Bearer $token"

    private fun simpleCallback(
        action: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, res: Response<ResponseBody>) {
            if (res.isSuccessful) onSuccess() else {
                val err = try { res.errorBody()?.string() } catch (_: Throwable) { null }
                onError("$action 실패 (${res.code()})${err?.let { "\n$it" } ?: ""}")
            }
        }
        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            onError("네트워크 오류($action): ${t.localizedMessage}")
        }
    }
}
