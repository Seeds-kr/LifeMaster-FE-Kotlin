package com.example.lifemaster.presentation.community.view

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.presentation.community.model.PostDetailDto
import com.example.lifemaster.presentation.community.viewmodel.CommunityViewModel

class CommunityWriteFragment : Fragment() {

    companion object {
        const val ARG_MODE = "arg_mode"
        const val ARG_ITEM_ID = "arg_item_id"
        const val MODE_CREATE = "create"
        const val MODE_EDIT = "edit"
        private const val STATE_CALENDAR_SHARE = "state_calendar_share"
        const val ARG_POST_TYPE = "arg_post_type"
    }

    private val vm: CommunityViewModel by activityViewModels()

    private var selectedFileUri: Uri? = null
    private var selectedFileName: String? = null
    private var isCalendarShareChecked: Boolean = false

    private val pickOneDocument =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            requireContext().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            selectedFileUri = uri
            selectedFileName = getDisplayName(uri) ?: "첨부됨"
            applyFileUi()
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_community_write, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rowCalendar = view.findViewById<LinearLayout>(R.id.row_calendar_share)
        val ivCalendar  = view.findViewById<ImageView>(R.id.iv_calendar_share)

        val btnReg    = view.findViewById<LinearLayout>(R.id.btn_register)
        val tvReg     = btnReg.findViewById<TextView>(R.id.tv_register_label)
            ?: btnReg.getChildAt(0) as TextView
        val etTitle   = view.findViewById<EditText>(R.id.et_title)
        val etContent = view.findViewById<EditText>(R.id.et_content)
        val btnFile   = view.findViewById<LinearLayout>(R.id.btn_file_upload)
        val ivFileClr = view.findViewById<ImageView>(R.id.iv_file_clear)

        isCalendarShareChecked = savedInstanceState?.getBoolean(STATE_CALENDAR_SHARE) ?: false
        applyCalendarShareUi(ivCalendar)
        applyFileUi()

        val mode = arguments?.getString(ARG_MODE) ?: MODE_CREATE
        val editId = arguments?.getString(ARG_ITEM_ID)
        val postType = arguments?.getString(ARG_POST_TYPE) ?: "FREE"

        tvReg.text = if (mode == MODE_EDIT) "수정하기" else "등록하기"

        if (mode == MODE_EDIT && !editId.isNullOrBlank()) {
            vm.getById(editId)?.let { item ->
                if (etTitle.text.isNullOrBlank()) etTitle.setText(item.title)
                if (etContent.text.isNullOrBlank()) etContent.setText(item.content)
                item.fileUri?.let {
                    selectedFileUri = it.toUri()
                    selectedFileName = it.substringAfterLast('/')
                    applyFileUi()
                }
            }

            readAuthToken()?.let { auth ->
                vm.fetchPostDetail(
                    token = auth,
                    id = editId,
                    onDone = { detail ->
                        bindForEdit(detail, etTitle, etContent)
                        isCalendarShareChecked = detail.calendarShared ?: false
                        applyCalendarShareUi(ivCalendar)
                    },
                    onError = ::toast
                )
            }
        }

        btnFile.setOnClickListener {
            if (selectedFileUri == null) pickOneDocument.launch(arrayOf("*/*"))
        }
        ivFileClr.setOnClickListener { clearFile() }

        val toggle: (View) -> Unit = {
            isCalendarShareChecked = !isCalendarShareChecked
            applyCalendarShareUi(ivCalendar)
        }
        rowCalendar.setOnClickListener(toggle)
        ivCalendar.setOnClickListener(toggle)

        btnReg.setOnClickListener {
            val title = etTitle.text?.toString()?.trim().orEmpty()
            val content = etContent.text?.toString()?.trim().orEmpty()

            when {
                title.isEmpty() && content.isEmpty() -> { toast("제목과 내용을 모두 입력해주세요."); return@setOnClickListener }
                title.isEmpty() -> { toast("제목을 입력해주세요."); return@setOnClickListener }
                content.isEmpty() -> { toast("내용을 입력해주세요."); return@setOnClickListener }
            }

            val auth = readAuthToken() ?: return@setOnClickListener

            if (mode == MODE_EDIT && !editId.isNullOrBlank()) {
                vm.updatePost(
                    token = auth,
                    id = editId,
                    title = title,
                    content = content,
                    file = selectedFileUri?.toString(),
                    type = postType,
                    calendarShared = isCalendarShareChecked,
                    onSuccess = {
                        findNavController().previousBackStackEntry?.savedStateHandle?.set("refresh_post", editId)
                        findNavController().popBackStack()
                    },
                    onError = ::toast
                )
            } else {
                vm.createPost(
                    token = auth,
                    title = title,
                    content = content,
                    file = selectedFileUri?.toString(),
                    type = postType,
                    calendarShared = isCalendarShareChecked,
                    onSuccess = {
                        findNavController().previousBackStackEntry?.savedStateHandle?.set("refresh_posts", true)
                        findNavController().popBackStack()
                    },
                    onError = ::toast
                )
            }
        }
    }

    private fun bindForEdit(detail: PostDetailDto, etTitle: EditText, etContent: EditText) {
        etTitle.setText(detail.title.orEmpty())
        etContent.setText(detail.content.orEmpty())
        etContent.post { etContent.setSelection(etContent.text?.length ?: 0) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_CALENDAR_SHARE, isCalendarShareChecked)
    }

    private fun applyCalendarShareUi(iv: ImageView) {
        iv.setImageResource(
            if (isCalendarShareChecked) R.drawable.ic_check_circle_selected
            else R.drawable.ic_check_circle_unselected
        )
    }

    private fun applyFileUi() {
        val tv = view?.findViewById<TextView>(R.id.tv_file_label)
        val ivClear = view?.findViewById<ImageView>(R.id.iv_file_clear)
        val ivIcon = view?.findViewById<ImageView>(R.id.iv_file_icon)

        if (selectedFileUri == null) {
            tv?.text = "파일 첨부하기"
            ivIcon?.visibility = View.VISIBLE
            ivClear?.visibility = View.GONE
            ivIcon?.setImageResource(R.drawable.ic_file_upload)
        } else {
            tv?.text = selectedFileName ?: "첨부됨"
            ivIcon?.visibility = View.GONE
            ivClear?.visibility = View.VISIBLE
        }
    }

    private fun clearFile() {
        selectedFileUri = null
        selectedFileName = null
        applyFileUi()
    }

    private fun getDisplayName(uri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            val idx = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: -1
            if (cursor != null && cursor.moveToFirst() && idx >= 0) cursor.getString(idx) else null
        } finally { cursor?.close() }
    }

    private fun readAuthToken(): String? {
        val raw = requireContext().getSharedPreferences("auth", 0).getString("token", null).orEmpty()
        if (raw.isBlank()) { toast("로그인 후 작성할 수 있어요."); return null }
        return if (raw.startsWith("Bearer ")) raw else "Bearer $raw"
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}