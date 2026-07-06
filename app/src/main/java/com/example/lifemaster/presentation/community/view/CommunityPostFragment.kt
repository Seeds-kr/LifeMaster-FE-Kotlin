package com.example.lifemaster.presentation.community.view

import android.app.Dialog
import android.content.res.ColorStateList
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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
import com.example.lifemaster.presentation.home.calendar.view.CalendarFragment
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

class CommunityPostFragment : Fragment(R.layout.fragment_community_post) {

    companion object {
        const val ARG_ITEM_ID = "arg_item_id"
        private const val TAG_SHARED_CALENDAR = "tag_shared_calendar"
    }

    private var _binding: FragmentCommunityPostBinding? = null
    private val binding get() = _binding!!

    private val vm: CommunityViewModel by activityViewModels()

    private lateinit var postId: String
    private lateinit var authToken: String

    private var editingCommentId: Long? = null
    private lateinit var commentAdapter: CommunityCommentAdapter
    private var attachedCalendarOwnerId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
            findNavController().popBackStack()
            return
        }
        postId = idArg
        authToken = token

        setupCommentList()
        observeViewModel()
        setupActions()
        setupSharedCalendarHeaderControls()
    }

    private fun setupCommentList() {
        commentAdapter = CommunityCommentAdapter(
            listener = object : CommunityCommentAdapter.CommentActionListener {
                override fun onEditRequest(comment: Comment, position: Int) {
                    enterEditModeUi(comment)
                }

                override fun onDeleteRequest(comment: Comment, position: Int) {
                    showCommunityDeleteDialog(
                        title = "댓글 삭제",
                        message = "댓글을 삭제할까요?",
                        confirmText = "삭제하기"
                    ) {
                        vm.deleteComment(
                            authToken,
                            postId,
                            comment.id
                        )
                    }
                }

                override fun onToggleLike(comment: Comment, position: Int) {
                    vm.toggleCommentLike(
                        authToken,
                        postId,
                        comment.id
                    )
                }
            }
        )
        binding.recyclerviewComment.adapter = commentAdapter
        binding.recyclerviewComment.isNestedScrollingEnabled = false
    }

    private fun observeViewModel() {
        vm.comments.observe(viewLifecycleOwner) { list ->
            val mapped = (list ?: emptyList()).map { it.toUi() }
            commentAdapter.submitAll(mapped)
            binding.tvCommentCount.text = mapped.size.toString()
        }
        vm.fetchComments(authToken, postId, onError = ::toast)

        vm.isCommentSyncing.observe(viewLifecycleOwner) { syncing ->
            binding.btnRegister.isEnabled = !syncing
        }

        vm.postDetail.observe(viewLifecycleOwner) { detail ->
            detail?.let { bindDetail(it) }
        }
        vm.fetchPostDetail(authToken, postId, onDone = {}, onError = ::toast)

        vm.items.observe(viewLifecycleOwner) {
            vm.getById(postId)?.let { item ->
                if (binding.tvTitle.text.isNullOrBlank()) binding.tvTitle.text = item.title
                if (binding.tvContent.text.isNullOrBlank()) binding.tvContent.text = item.content
                if (binding.tvViews.text.isNullOrBlank()) binding.tvViews.text = item.views.toString()
                if (binding.tvLikeCount.text.isNullOrBlank()) {
                    binding.tvLikeCount.text = vm.getLikeCount(postId).toString()
                }
                applyHeart(vm.isPostLiked(postId.toLongOrNull() ?: -1))
            }
        }
    }

    private fun setupActions() {
        binding.btnRegister.setOnClickListener {
            val text = binding.editComment.text?.toString()?.trim().orEmpty()
            if (text.isBlank()) {
                toast("댓글을 입력해 주세요.")
                return@setOnClickListener
            }

            val editingId = editingCommentId
            if (editingId == null) {
                vm.addComment(
                    authToken,
                    postId,
                    text,
                    onDone = {
                        binding.editComment.setText("")
                        hideKeyboard(binding.editComment)
                    },
                    onError = ::toast
                )
            } else {
                vm.updateComment(
                    authToken,
                    postId,
                    editingId,
                    text,
                    onDone = {
                        exitEditMode()
                        toast("수정 완료")
                    },
                    onError = ::toast
                )
            }
        }

        binding.btnLike.setOnClickListener {
            val pid = postId.toLongOrNull()
            if (pid == null) {
                toast("잘못된 게시글 ID")
                return@setOnClickListener
            }

            binding.btnLike.isEnabled = false
            vm.togglePostLikeRemote(
                authToken,
                pid,
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

        binding.btnMore.isVisible = false
        binding.btnMore.setOnClickListener {
            val isMyPost = vm.postDetail.value?.isMine == true

            if (!isMyPost) {
                return@setOnClickListener
            }

            showPostMenu(
                isMine = true,
                onEdit = {
                    val b = Bundle().apply {
                        putString(CommunityWriteFragment.ARG_MODE, CommunityWriteFragment.MODE_EDIT)
                        putString(CommunityWriteFragment.ARG_ITEM_ID, postId)
                    }
                    findNavController().navigate(R.id.communityWriteFragment, b)
                },
                onDelete = {
                    vm.deletePost(
                        authToken,
                        postId,
                        onSuccess = { findNavController().popBackStack() },
                        onError = ::toast
                    )
                },
                onReport = { showReportDialogInline() }
            )
        }

        binding.btnReport.isVisible = false
        binding.btnReport.setOnClickListener {
            showReportDialogInline()
        }
    }

    private fun bindDetail(detail: PostDetailDto) {
        binding.tvTitle.text = detail.title.orEmpty()
        binding.tvContent.text = detail.content.orEmpty()

        val author = detail.nickname?.trim().orEmpty()
        val date = detail.createdAt?.trim().orEmpty()
        binding.tvDate.text = when {
            author.isNotBlank() && date.isNotBlank() -> "$author   $date"
            date.isNotBlank() -> date
            author.isNotBlank() -> author
            else -> ""
        }

        val file = detail.file?.trim()
        if (!file.isNullOrEmpty()) {
            binding.layoutFile.visibility = View.VISIBLE
            binding.tvFileName.text = file.substringAfterLast('/')
        } else {
            binding.layoutFile.visibility = View.GONE
        }

        binding.tvLikeCount.text = (detail.likeCount ?: 0).toString()
        applyHeart(detail.liked == true)
        binding.tvViews.text = (detail.viewCount ?: 0).toString()

        val isMyPost = detail.isMine == true
        binding.btnReport.isVisible = !isMyPost
        binding.btnMore.isVisible = isMyPost

        // 달력 공유 표시
        val shared = (detail.calendarShared == true)
        val ownerId = detail.memberId ?: -1L

        if (shared && ownerId > 0L) {
            binding.calendarShare.isVisible = true
            attachSharedCalendar(ownerMemberId = ownerId)
        } else {
            binding.calendarShare.isVisible = false
            detachSharedCalendarIfAny()
        }
    }

    private fun attachSharedCalendar(ownerMemberId: Long) {
        if (!isAdded) return

        if (attachedCalendarOwnerId == ownerMemberId &&
            childFragmentManager.findFragmentByTag(TAG_SHARED_CALENDAR) != null
        ) {
            // 초기값(7월/2024)으로 남아있을 수 있으니 다시 갱신
            updateSharedCalendarHeaderTitle()
            return
        }

        attachedCalendarOwnerId = ownerMemberId

        // 기존 달력이 있으면 제거 후 교체
        childFragmentManager.findFragmentByTag(TAG_SHARED_CALENDAR)?.let { prev ->
            childFragmentManager.beginTransaction().remove(prev).commitAllowingStateLoss()
        }

        val fragment = CalendarFragment().apply {
            arguments = Bundle().apply {
                putLong(CalendarFragment.ARG_TARGET_MEMBER_ID, ownerMemberId)
                putBoolean(CalendarFragment.ARG_CALENDAR_READ_ONLY, true)
            }
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.container_calendar, fragment, TAG_SHARED_CALENDAR)
            .commitAllowingStateLoss()

        childFragmentManager.executePendingTransactions()
        updateSharedCalendarHeaderTitle()
    }

    private fun detachSharedCalendarIfAny() {
        attachedCalendarOwnerId = null
        childFragmentManager.findFragmentByTag(TAG_SHARED_CALENDAR)?.let { f ->
            childFragmentManager.beginTransaction()
                .remove(f)
                .commitAllowingStateLoss()
        }
    }

    private fun setupSharedCalendarHeaderControls() {
        binding.btnPrevMonth.setOnClickListener {
            val cal = childFragmentManager.findFragmentByTag(TAG_SHARED_CALENDAR) as? CalendarFragment
            if (cal == null) return@setOnClickListener
            cal.moveMonth(-1)
            updateSharedCalendarHeaderTitle()
        }

        binding.btnNextMonth.setOnClickListener {
            val cal = childFragmentManager.findFragmentByTag(TAG_SHARED_CALENDAR) as? CalendarFragment
            if (cal == null) return@setOnClickListener
            cal.moveMonth(+1)
            updateSharedCalendarHeaderTitle()
        }
    }

    private fun updateSharedCalendarHeaderTitle() {
        val cal = childFragmentManager.findFragmentByTag(TAG_SHARED_CALENDAR) as? CalendarFragment ?: return
        val y = cal.getCurrentYear()
        val m = cal.getCurrentMonth1()

        binding.tvMonth.text = "${m}월"
        binding.tvYear.text = y.toString()
    }

    private fun showPostMenu(
        isMine: Boolean,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        onReport: () -> Unit
    ) {
        val ctx = requireContext()
        val anchor = binding.btnMore

        val content = LayoutInflater.from(ctx).inflate(R.layout.dialog_community_menu, null)
        val btnReport = content.findViewById<TextView>(R.id.btn_report)
        val btnEdit = content.findViewById<TextView>(R.id.btn_edit)
        val btnDelete = content.findViewById<TextView>(R.id.btn_delete)

        val realIsMine = (vm.postDetail.value?.isMine == true) || isMine

        if (realIsMine) {
            btnReport.visibility = View.GONE
            btnEdit.visibility = View.VISIBLE
            btnDelete.visibility = View.VISIBLE
        } else {
            btnReport.visibility = View.VISIBLE
            btnEdit.visibility = View.GONE
            btnDelete.visibility = View.GONE
        }

        val popup = PopupWindow(content, WRAP_CONTENT, WRAP_CONTENT, true).apply {
            isOutsideTouchable = true
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            elevation = dp(8).toFloat()
        }

        btnReport.setOnClickListener {
            popup.dismiss()
            onReport()
        }
        btnEdit.setOnClickListener {
            popup.dismiss()
            onEdit()
        }
        btnDelete.setOnClickListener {
            popup.dismiss()
            showCommunityDeleteDialog(
                title = "게시글 삭제",
                message = "게시글을 삭제할까요?",
                confirmText = "삭제하기"
            ) {
                onDelete()
            }
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

    private fun showCommunityDeleteDialog(
        title: String,
        message: String,
        confirmText: String = "삭제하기",
        onConfirm: () -> Unit
    ) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_group_delete, null, false)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvDesc = dialogView.findViewById<TextView>(R.id.tvDesc)
        val tvPwLabel = dialogView.findViewById<TextView>(R.id.tvPwLabel)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val tvWrong = dialogView.findViewById<TextView>(R.id.tvPwLabel_wrong)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val btnDelete = dialogView.findViewById<TextView>(R.id.btnDelete)

        tvTitle.text = title
        tvDesc.text = message

        tvPwLabel.visibility = View.GONE
        etPassword.visibility = View.GONE
        tvWrong.visibility = View.GONE

        btnDelete.text = confirmText
        btnDelete.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EA9F95"))

        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogView)
            setCancelable(true)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }

        dialog.show()
    }

    private fun showReportDialogInline() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_group_delete, null, false)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvDesc = dialogView.findViewById<TextView>(R.id.tvDesc)
        val tvPwLabel = dialogView.findViewById<TextView>(R.id.tvPwLabel)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val tvWrong = dialogView.findViewById<TextView>(R.id.tvPwLabel_wrong)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)
        val btnDelete = dialogView.findViewById<TextView>(R.id.btnDelete)

        tvTitle.text = "게시글 신고"
        tvDesc.text = "이 게시글을 신고할까요?"

        tvPwLabel.visibility = View.VISIBLE
        tvPwLabel.text = "신고 사유를 입력해주세요"

        etPassword.visibility = View.VISIBLE
        etPassword.hint = "신고 사유 입력"
        etPassword.setText("")
        etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        etPassword.maxLines = 3

        tvWrong.visibility = View.GONE

        btnDelete.text = "신고하기"
        btnDelete.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EA9F95"))

        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogView)
            setCancelable(true)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            val pid = postId.toLongOrNull()

            if (pid == null) {
                tvWrong.text = "잘못된 게시글 ID입니다."
                tvWrong.visibility = View.VISIBLE
                return@setOnClickListener
            }

            binding.btnReport.isEnabled = false

            vm.reportPost(
                token = authToken,
                postId = pid,
                reason = etPassword.text?.toString()?.trim().orEmpty(),
                onSuccess = {
                    dialog.dismiss()
                    toast("신고가 접수되었습니다.")
                    binding.btnReport.isEnabled = true
                },
                onError = {
                    tvWrong.text = it
                    tvWrong.visibility = View.VISIBLE
                    binding.btnReport.isEnabled = true
                }
            )
        }

        dialog.show()
    }

    private fun applyHeart(liked: Boolean) {
        binding.ivLikeIcon.setImageResource(
            if (liked) R.drawable.ic_fill_heart else R.drawable.ic_heart
        )
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

    private fun readAuthToken(ctx: Context): String? {
        val raw = ctx.getSharedPreferences("auth", 0)
            .getString("token", null)
            .orEmpty()
        if (raw.isBlank()) return null
        return if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }

    private fun toast(msg: String?) {
        if (!msg.isNullOrBlank()) {
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showKeyboard(v: View) {
        val imm = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        v.post { imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT) }
    }

    private fun hideKeyboard(v: View) {
        val imm = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).roundToInt()

    override fun onDestroyView() {
        super.onDestroyView()
        detachSharedCalendarIfAny()
        _binding = null
    }

    private fun CommentDto.toUi(): Comment =
        Comment(
            id = this.commentId ?: -1L,
            memberId = this.memberId,
            nickname = this.nickname?.ifBlank { "익명" } ?: "익명",
            content = this.comment.orEmpty(),
            createdAt = parseIsoToMillisFlexible(this.commentDate) ?: System.currentTimeMillis(),
            likeCount = this.likeCount ?: 0,
            isLiked = this.liked == true,
            isEdited = false,
            isMine = this.isMine == true
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
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                return sdf.parse(iso)?.time
            } catch (_: Throwable) {}
        }
        return null
    }
}