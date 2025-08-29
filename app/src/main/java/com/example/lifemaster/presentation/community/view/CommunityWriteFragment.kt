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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.presentation.community.model.CommunityItem
import com.example.lifemaster.presentation.community.viewmodel.CommunityViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommunityWriteFragment : Fragment() {

    companion object {
        const val ARG_MODE = "arg_mode"
        const val ARG_ITEM_ID = "arg_item_id"
        const val MODE_CREATE = "create"
        const val MODE_EDIT = "edit"
    }

    private var isSelectedState = false
    private val vm: CommunityViewModel by activityViewModels()

    private var selectedFileUri: Uri? = null
    private var selectedFileName: String? = null

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_community_write, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val ivCheck   = view.findViewById<ImageView>(R.id.iv_calendar_share)
        val btnReg    = view.findViewById<LinearLayout>(R.id.btn_register)
        val tvReg     = (btnReg.findViewById<TextView?>(R.id.tv_register_label)
            ?: btnReg.getChildAt(0) as? TextView)
        val etTitle   = view.findViewById<EditText>(R.id.et_title)
        val etContent = view.findViewById<EditText>(R.id.et_content)
        val btnFile   = view.findViewById<LinearLayout>(R.id.btn_file_upload)
        val ivFileClr = view.findViewById<ImageView>(R.id.iv_file_clear)

        val mode = arguments?.getString(ARG_MODE) ?: MODE_CREATE
        val editItemId = arguments?.getString(ARG_ITEM_ID)

        tvReg?.text = if (mode == MODE_EDIT) "수정하기" else "등록하기"

        if (mode == MODE_EDIT && editItemId != null) {
            vm.getById(editItemId)?.let { item ->
                etTitle.setText(item.title)
                etContent.setText(item.content)

                isSelectedState = item.shareCalendar
                ivCheck.setImageResource(
                    if (isSelectedState) R.drawable.ic_check_circle_selected
                    else R.drawable.ic_check_circle_unselected
                )

                item.fileUri?.let { saved ->
                    selectedFileUri = Uri.parse(saved)
                    selectedFileName = getDisplayName(selectedFileUri!!) ?: "첨부됨"
                }
                applyFileUi()
            }
        } else {
            applyFileUi()
        }

        ivCheck.setOnClickListener {
            isSelectedState = !isSelectedState
            ivCheck.setImageResource(
                if (isSelectedState) R.drawable.ic_check_circle_selected
                else R.drawable.ic_check_circle_unselected
            )
        }

        btnFile.setOnClickListener {
            if (selectedFileUri == null) {
                pickOneDocument.launch(arrayOf("*/*"))
            }
        }

        ivFileClr.setOnClickListener { clearFile() }

        btnReg.setOnClickListener {
            val title = etTitle.text?.toString()?.trim().orEmpty()
            val content = etContent.text?.toString()?.trim().orEmpty()

            when {
                title.isEmpty() && content.isEmpty() -> { toast("제목과 내용을 모두 입력해주세요."); return@setOnClickListener }
                title.isEmpty() -> { toast("제목을 입력해주세요."); return@setOnClickListener }
                content.isEmpty() -> { toast("내용을 입력해주세요."); return@setOnClickListener }
            }

            val fileUriStr = selectedFileUri?.toString()

            if (mode == MODE_EDIT && editItemId != null) {
                vm.updateItem(
                    id = editItemId,
                    title = title,
                    content = content,
                    shareCalendar = isSelectedState,
                    fileUri = fileUriStr
                )
                findNavController().popBackStack()
            } else {
                val dateText = SimpleDateFormat("M월 d일", Locale.KOREA).format(Date())
                val item = CommunityItem(
                    title = title,
                    content = content,
                    author = "ME",
                    views = 0,
                    likes = 0,
                    dateText = dateText,
                    imageResId = null,
                    fileUri = fileUriStr,
                    shareCalendar = isSelectedState
                )
                vm.addItemAtTop(item)
                val bundle = Bundle().apply {
                    putString(CommunityPostFragment.ARG_ITEM_ID, item.id)
                    putBoolean(CommunityPostFragment.ARG_SHARE_CALENDAR, isSelectedState)
                }
                findNavController().navigate(
                    R.id.action_communityWriteFragment_to_communityPostFragment,
                    bundle
                )
            }
        }
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
        } finally {
            cursor?.close()
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}