package com.example.lifemaster.presentation.community.view

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentCommunityPostBinding
import com.example.lifemaster.presentation.community.adapter.CommunityCommentAdapter
import com.example.lifemaster.presentation.community.model.Comment
import com.example.lifemaster.presentation.community.viewmodel.CommunityViewModel
import com.example.lifemaster.presentation.home.calendar.view.CalendarFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CommunityPostFragment : Fragment() {

    private var _binding: FragmentCommunityPostBinding? = null
    private val binding get() = _binding!!
    private val vm: CommunityViewModel by activityViewModels()

    companion object {
        const val ARG_ITEM_ID = "arg_item_id"
        const val ARG_SHARE_CALENDAR = "arg_share_calendar"
    }

    private var currentItemId: String? = null
    private lateinit var commentAdapter: CommunityCommentAdapter
    private var timeJob: Job? = null

    private var editingPos: Int? = null

    private fun myNickname(): String {
        val sp = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sp.getString("nickname", "ME") ?: "ME"
    }

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

        currentItemId = arguments?.getString(ARG_ITEM_ID)

        vm.getById(currentItemId)?.let { item ->
            binding.tvTitle.text = item.title
            binding.tvContent.text = item.content
            binding.tvLikeCount.text = item.likes.toString()
            val liked = vm.isPostLiked(item.id)
            binding.ivLikeIcon.setImageResource(
                if (liked) R.drawable.ic_fill_heart else R.drawable.ic_heart
            )

            if (!item.fileUri.isNullOrEmpty()) {
                binding.layoutFile.visibility = View.VISIBLE
                val uri = Uri.parse(item.fileUri)
                binding.tvFileName.text = getDisplayName(uri) ?: "첨부된 파일"
                binding.layoutFile.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "*/*")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try { startActivity(intent) }
                    catch (_: Exception) {
                        Toast.makeText(requireContext(), "파일을 열 수 있는 앱이 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else binding.layoutFile.visibility = View.GONE
        }

        setupCalendarArea()

        commentAdapter = CommunityCommentAdapter(
            myNickname = myNickname(),
            items = mutableListOf(),
            listener = object : CommunityCommentAdapter.CommentActionListener {
                override fun onEditRequest(comment: Comment, position: Int) {
                    editingPos = position
                    binding.editComment.setText(comment.content)
                    binding.editComment.setSelection(binding.editComment.text?.length ?: 0)
                    binding.btnRegister.text = "수정하기"
                    binding.editComment.requestFocus()
                }
                override fun onDeleteRequest(comment: Comment, position: Int) {
                    showDeleteConfirm(position)
                }
            }
        )
        binding.recyclerviewComment.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerviewComment.adapter = commentAdapter

        binding.btnRegister.setOnClickListener {
            val text = binding.editComment.text?.toString()?.trim().orEmpty()
            if (text.isEmpty()) return@setOnClickListener

            val pos = editingPos
            if (pos != null) {
                commentAdapter.updateContentAt(pos, text) // isEdited = true
                editingPos = null
                binding.btnRegister.text = "등록하기"
                binding.editComment.setText("")
                binding.postScroll.post { binding.postScroll.smoothScrollTo(0, binding.postScroll.bottom) }
            } else {
                commentAdapter.add(Comment(nickname = myNickname(), content = text))
                binding.editComment.setText("")
                binding.tvCommentCount.text = commentAdapter.itemCount.toString()
                commentAdapter.tickTime()
                binding.postScroll.post { binding.postScroll.smoothScrollTo(0, binding.postScroll.bottom) }
            }
        }

        binding.btnLike.setOnClickListener {
            val id = currentItemId ?: return@setOnClickListener
            val newCount = vm.togglePostLike(id)
            binding.tvLikeCount.text = newCount.toString()
            val liked = vm.isPostLiked(id)
            binding.ivLikeIcon.setImageResource(
                if (liked) R.drawable.ic_fill_heart else R.drawable.ic_heart
            )
        }

        vm.items.observe(viewLifecycleOwner) {
            val id = currentItemId ?: return@observe
            vm.getById(id)?.let { item ->
                binding.tvLikeCount.text = item.likes.toString()
                val liked = vm.isPostLiked(id)
                binding.ivLikeIcon.setImageResource(
                    if (liked) R.drawable.ic_fill_heart else R.drawable.ic_heart
                )
                renderCalendarVisibility(item.shareCalendar)
            }
        }

        binding.btnMore.setOnClickListener { anchor -> showMoreMenu(anchor) }
    }

    private fun setupCalendarArea() {
        val id = currentItemId
        val share = vm.getById(id)?.shareCalendar == true

        if (!share) {
            binding.calendarShare.visibility = View.GONE
            return
        }
        binding.calendarShare.visibility = View.VISIBLE

        val calFrag = (childFragmentManager.findFragmentById(binding.containerCalendar.id)
                as? CalendarFragment) ?: CalendarFragment().also {
            childFragmentManager.beginTransaction()
                .replace(binding.containerCalendar.id, it)
                .commit()
            childFragmentManager.executePendingTransactions()
        }

        renderCalendarHeader(calFrag.getCurrentYear(), calFrag.getCurrentMonth1())

        calFrag.setOnMonthChangedListener { y, m1 ->
            renderCalendarHeader(y, m1)
        }

        binding.btnPrevMonth.setOnClickListener { calFrag.moveMonth(-1) }
        binding.btnNextMonth.setOnClickListener { calFrag.moveMonth(1) }
    }

    private fun renderCalendarVisibility(visible: Boolean) {
        binding.calendarShare.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun renderCalendarHeader(year: Int, month1: Int) {
        binding.tvMonth.text = "${month1}월"
        binding.tvYear.text = year.toString()
    }

    override fun onStart() {
        super.onStart()
        startTimeTicker()
    }

    override fun onStop() {
        super.onStop()
        timeJob?.cancel()
        timeJob = null
    }

    private fun startTimeTicker() {
        timeJob?.cancel()
        timeJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    delay(60_000)
                    commentAdapter.tickTime()
                }
            }
        }
    }

    private fun showDeleteConfirm(position: Int) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setMessage("댓글을 삭제할까요?")
            .setPositiveButton("삭제") { d, _ ->
                commentAdapter.removeAt(position)
                binding.tvCommentCount.text = commentAdapter.itemCount.toString()
                if (editingPos == position) {
                    editingPos = null
                    binding.btnRegister.text = "등록하기"
                    binding.editComment.setText("")
                }
                d.dismiss()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showMoreMenu(anchor: View) {
        val content = LayoutInflater.from(anchor.context)
            .inflate(R.layout.dialog_community_menu, null)
        val popup = PopupWindow(
            content,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            elevation = 16f
        }

        content.findViewById<TextView>(R.id.btn_report)?.setOnClickListener { popup.dismiss() }
        content.findViewById<TextView>(R.id.btn_edit)?.setOnClickListener {
            popup.dismiss()
            currentItemId?.let { id ->
                val args = Bundle().apply {
                    putString(CommunityWriteFragment.ARG_MODE, CommunityWriteFragment.MODE_EDIT)
                    putString(CommunityWriteFragment.ARG_ITEM_ID, id)
                }
                findNavController().navigate(R.id.communityWriteFragment, args)
            }
        }
        content.findViewById<TextView>(R.id.btn_delete)?.setOnClickListener {
            popup.dismiss()
            currentItemId?.let { id ->
                vm.deleteById(id)
                findNavController().popBackStack(R.id.communityFragment, false)
            }
        }

        content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val xOff = anchor.width - content.measuredWidth - 6
        val yOff = 4
        popup.showAsDropDown(anchor, xOff, yOff)
    }

    private fun getDisplayName(uri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: -1
            if (cursor != null && cursor.moveToFirst() && nameIndex >= 0) {
                cursor.getString(nameIndex)
            } else null
        } finally {
            cursor?.close()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}