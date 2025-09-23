package com.example.lifemaster.presentation.community.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentCommunityPostBinding
import com.example.lifemaster.presentation.community.adapter.CommunityCommentAdapter
import com.example.lifemaster.presentation.community.model.Comment
import com.example.lifemaster.presentation.community.model.CommentDto
import com.example.lifemaster.presentation.community.model.PostDetailDto
import com.example.lifemaster.presentation.community.viewmodel.CommunityViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import android.graphics.Color

class CommunityPostFragment : Fragment(R.layout.fragment_community_post) {

    companion object { const val ARG_ITEM_ID = "arg_item_id" }

    // 지금은 “누구나 수정/삭제 허용”
    private val ALLOW_ALL_POST_ACTIONS = true

    private var _binding: FragmentCommunityPostBinding? = null
    private val binding get() = _binding!!

    private val vm: CommunityViewModel by activityViewModels()
    private var didIncreaseView = false

    private lateinit var commentAdapter: CommunityCommentAdapter

    private lateinit var postId: String
    private lateinit var authToken: String
    private var editingCommentId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.includeBackButton.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val idArg = arguments?.getString(ARG_ITEM_ID)
        val token = readAuthToken(requireContext())
        if (idArg.isNullOrBlank() || token.isNullOrBlank()) {
            toast("잘못된 접근입니다.")
            findNavController().popBackStack(); return
        }
        postId = idArg
        authToken = token

        // 댓글(수정/삭제)
        commentAdapter = CommunityCommentAdapter(
            myMemberId = null,
            myNickname = null,
            listener = object : CommunityCommentAdapter.CommentActionListener {
                override fun onEditRequest(comment: Comment, position: Int) {
                    enterEditModeUi(comment)
                }
                override fun onDeleteRequest(comment: Comment, position: Int) {
                    AlertDialog.Builder(requireContext())
                        .setMessage("댓글을 삭제할까요?")
                        .setNegativeButton("취소", null)
                        .setPositiveButton("삭제") { _, _ ->
                            vm.deleteComment(
                                token = authToken,
                                postId = postId,
                                commentId = comment.id,
                                onDone = { toast("삭제했어요") },
                                onError = ::toast
                            )
                        }
                        .show()
                }
            },
            allowAllActions = true
        )
        binding.recyclerviewComment.apply {
            adapter = commentAdapter
            isNestedScrollingEnabled = false
        }

        vm.comments.observe(viewLifecycleOwner) { list ->
            val mapped = (list ?: emptyList()).map { it.toUi() }
            commentAdapter.submitAll(mapped)
            binding.tvCommentCount.text = mapped.size.toString()
        }
        vm.fetchComments(authToken, postId, onError = ::toast)

        vm.isCommentSyncing.observe(viewLifecycleOwner) { syncing ->
            binding.btnRegister.isEnabled = !syncing
        }

        binding.btnRegister.setOnClickListener {
            val text = binding.editComment.text?.toString()?.trim().orEmpty()
            if (text.isBlank()) { toast("댓글을 입력해 주세요."); return@setOnClickListener }

            val editingId = editingCommentId
            if (editingId == null) {
                vm.addComment(
                    token = authToken, postId = postId, text = text,
                    onDone = {
                        binding.editComment.setText("")
                        hideKeyboard(binding.editComment)
                    },
                    onError = ::toast
                )
            } else {
                vm.updateComment(
                    token = authToken, postId = postId, commentId = editingId, text = text,
                    onDone = {
                        exitEditMode()
                        toast("수정 완료")
                    },
                    onError = ::toast
                )
            }
        }

        vm.postDetail.observe(viewLifecycleOwner) { detail ->
            detail?.let { bindDetail(it) }
        }
        vm.fetchPostDetail(token = authToken, id = postId, onDone = {}, onError = ::toast)

        vm.items.observe(viewLifecycleOwner) {
            vm.getById(postId)?.let { item ->
                if (binding.tvTitle.text.isNullOrBlank())
                    binding.tvTitle.text = item.title
                if (binding.tvContent.text.isNullOrBlank())
                    binding.tvContent.text = item.content
                if (binding.tvViews.text.isNullOrBlank())
                    binding.tvViews.text = (item.views ?: 0).toString()
                if (binding.tvLikeCount.text.isNullOrBlank())
                    binding.tvLikeCount.text = vm.getLikeCount(postId).toString()
                applyHeart(vm.isPostLiked(postId.toLongOrNull() ?: -1))
            }
        }

        if (!didIncreaseView) {
            binding.tvViews.text = vm.increaseViewCount(postId).toString()
            didIncreaseView = true
        }

        // 좋아요
        binding.btnLike.setOnClickListener {
            val pid = postId.toLongOrNull() ?: return@setOnClickListener toast("잘못된 게시글 ID")
            binding.btnLike.isEnabled = false
            vm.togglePostLikeRemote(
                token = authToken,
                id = pid,
                onDone = { newCount ->
                    binding.tvLikeCount.text = newCount.toString()
                    applyHeart(vm.isPostLiked(pid))
                    binding.btnLike.isEnabled = true
                },
                onError = {
                    toast(it)
                    binding.btnLike.isEnabled = true
                }
            )
        }

        // 게시글(수정/삭제)
        binding.btnMore.isVisible = true
        binding.btnMore.setOnClickListener {
            showPostMenu(
                onEdit = {
                    val b = Bundle().apply {
                        putString(CommunityWriteFragment.ARG_MODE, CommunityWriteFragment.MODE_EDIT)
                        putString(CommunityWriteFragment.ARG_ITEM_ID, postId)
                    }
                    findNavController().navigate(R.id.communityWriteFragment, b)
                },
                onDelete = {
                    vm.deletePost(
                        token = authToken,
                        id = postId,
                        onSuccess = { findNavController().popBackStack() },
                        onError   = ::toast
                    )
                }
            )
        }
    }

    private fun applyHeart(liked: Boolean) {
        binding.ivLikeIcon.setImageResource(
            if (liked) R.drawable.ic_fill_heart else R.drawable.ic_heart
        )
    }

    private fun bindDetail(detail: PostDetailDto) {
        binding.tvTitle.text   = detail.title.orEmpty()
        binding.tvContent.text = detail.content.orEmpty()

        val author = detail.nickname?.trim().orEmpty()
        val date   = detail.createdAt?.trim().orEmpty()
        binding.tvDate.text = when {
            author.isNotBlank() && date.isNotBlank() -> "$author   $date"
            date.isNotBlank()                        -> date
            author.isNotBlank()                      -> author
            else                                     -> ""
        }

        val file = detail.file?.trim()
        if (!file.isNullOrEmpty()) {
            binding.layoutFile.visibility = View.VISIBLE
            binding.tvFileName.text = file.substringAfterLast('/')
        } else {
            binding.layoutFile.visibility = View.GONE
        }

        val likeCount = detail.likeCount ?: 0
        val liked     = detail.liked == true
        binding.tvLikeCount.text = likeCount.toString()
        applyHeart(liked)

        if (ALLOW_ALL_POST_ACTIONS) binding.btnMore.isVisible = true
    }

    private fun enterEditModeUi(c: Comment) {
        editingCommentId = c.id
        binding.editComment.setText(c.content)
        binding.editComment.setSelection(binding.editComment.text?.length ?: 0)
        binding.btnRegister.text = "수정하기"
        binding.postScroll.post {
            binding.postScroll.smoothScrollTo(0, binding.commentBar.top)
            binding.editComment.requestFocus()
            showKeyboard(binding.editComment)
        }
    }

    private fun exitEditMode() {
        editingCommentId = null
        binding.editComment.setText("")
        binding.btnRegister.text = "등록하기"
        hideKeyboard(binding.editComment)
    }

    private fun showPostMenu(onEdit: () -> Unit, onDelete: () -> Unit) {
        val ctx = requireContext()
        val anchor = binding.btnMore

        val content = LayoutInflater.from(ctx).inflate(R.layout.dialog_community_menu, null)
        val btnReport = content.findViewById<TextView>(R.id.btn_report)
        val btnEdit   = content.findViewById<TextView>(R.id.btn_edit)
        val btnDelete = content.findViewById<TextView>(R.id.btn_delete)

        val popup = PopupWindow(content, WRAP_CONTENT, WRAP_CONTENT, true).apply {
            isOutsideTouchable = true
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            elevation = dp(8).toFloat()
        }

        btnReport.setOnClickListener { popup.dismiss(); toast("신고하기는 준비 중이에요.") }
        btnEdit.setOnClickListener   { popup.dismiss(); onEdit() }
        btnDelete.setOnClickListener {
            popup.dismiss()
            AlertDialog.Builder(ctx)
                .setMessage("게시글을 삭제할까요?")
                .setNegativeButton("취소", null)
                .setPositiveButton("삭제") { _, _ -> onDelete() }
                .show()
        }

        content.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val popupW = content.measuredWidth
        val xOff = anchor.width - popupW - dp(6)
        val yOff = dp(6)
        popup.showAsDropDown(anchor, xOff, yOff)
    }

    private fun readAuthToken(ctx: Context): String? {
        val raw = ctx.getSharedPreferences("auth", 0).getString("token", null).orEmpty()
        if (raw.isBlank()) return null
        return if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }

    private fun toast(msg: String?) {
        if (!msg.isNullOrBlank()) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    private fun showKeyboard(v: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        v.post { imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT) }
    }
    private fun hideKeyboard(v: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).roundToInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun CommentDto.toUi(): Comment =
        Comment(
            id        = this.commentId ?: -1L,
            memberId  = this.memberId,
            nickname  = this.nickname?.ifBlank { "익명" } ?: "익명",
            content   = this.comment.orEmpty(),
            createdAt = parseIsoToMillisFlexible(this.commentDate)
                ?: System.currentTimeMillis(),
            likeCount = 0,
            isLiked   = this.liked == true,
            isEdited  = false
        )

    private fun parseIsoToMillisFlexible(iso: String?): Long? {
        if (iso.isNullOrBlank()) return null
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )
        for (p in patterns) {
            try {
                val sdf = SimpleDateFormat(p, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(iso)?.time
            } catch (_: Throwable) {}
        }
        return null
    }
}
