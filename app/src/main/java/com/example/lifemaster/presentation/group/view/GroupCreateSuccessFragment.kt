package com.example.lifemaster.presentation.group.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.network.TokenProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupCreateSuccessFragment : Fragment(R.layout.fragment_group_create_success) {

    companion object {
        private const val ACCESS_TYPE_PUBLIC = "PUBLIC"
        private const val ACCESS_TYPE_PASSWORD = "PASSWORD"
        private const val ACCESS_TYPE_PRIVATE = "PRIVATE"
    }

    private var currentInviteCode: String = ""
    private var isPublicGroup: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val groupName = arguments?.getString("groupName").orEmpty()
        val groupDesc = arguments?.getString("groupDesc").orEmpty()
        val rawInviteCode = arguments?.getString("inviteCode").orEmpty()
        val groupAccessType = arguments?.getString("groupAccessType").orEmpty()
        val goalLines = arguments?.getStringArrayList("goalLines") ?: arrayListOf()

        isPublicGroup = groupAccessType.equals(ACCESS_TYPE_PUBLIC, ignoreCase = true)

        val groupId = arguments?.getLong("groupId")
            ?: rawInviteCode.substringBefore(":").toLongOrNull()
            ?: -1L

        val tvName = view.findViewById<TextView>(R.id.tv_group_name)
        val tvDesc = view.findViewById<TextView>(R.id.tv_group_desc)
        val tvInvite = view.findViewById<TextView>(R.id.tv_invite_code)

        val btnCopy = view.findViewById<ImageButton>(R.id.btn_copy)
        val btnEtc = view.findViewById<ImageButton>(R.id.btn_etc)
        val btnInstagram = view.findViewById<ImageButton>(R.id.btn_instagram)
        val btnX = view.findViewById<ImageButton>(R.id.btn_x)

        currentInviteCode = if (isPublicGroup) "" else cleanInviteCode(rawInviteCode)

        tvName.text = groupName
        tvDesc.text = if (groupDesc.isBlank()) " " else groupDesc
        tvInvite.text = if (currentInviteCode.isBlank()) "-" else currentInviteCode

        bindAccessTypeText(view, groupAccessType)
        bindGoals(view, goalLines)
        bindInviteArea(
            root = view,
            tvInvite = tvInvite,
            btnCopy = btnCopy,
            btnEtc = btnEtc,
            btnInstagram = btnInstagram,
            btnX = btnX
        )

        if (!isPublicGroup && groupId > 0L) {
            fetchInviteCode(groupId, tvInvite)
        }

        btnCopy.setOnClickListener {
            if (isPublicGroup) return@setOnClickListener

            if (currentInviteCode.isBlank()) {
                Toast.makeText(requireContext(), "초대코드가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("inviteCode", currentInviteCode))
            Toast.makeText(requireContext(), "초대코드가 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }

        btnEtc.setOnClickListener {
            if (isPublicGroup) return@setOnClickListener
            shareInviteCode()
        }

        btnInstagram.setOnClickListener {
            if (isPublicGroup) return@setOnClickListener
            shareInviteCode()
        }

        btnX.setOnClickListener {
            if (isPublicGroup) return@setOnClickListener
            shareInviteCode()
        }

        view.findViewById<TextView>(R.id.btn_back_group).setOnClickListener {
            findNavController().popBackStack(R.id.groupFragment, false)
        }

        view.findViewById<View?>(R.id.includeBackButton)?.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun bindAccessTypeText(root: View, groupAccessType: String) {
        val accessText = when (groupAccessType.uppercase()) {
            ACCESS_TYPE_PUBLIC -> "공개그룹"
            ACCESS_TYPE_PASSWORD, ACCESS_TYPE_PRIVATE -> "비공개그룹"
            else -> "비공개그룹"
        }

        val tvAccessType = findTextViewByText(root, "비공개그룹")
            ?: findTextViewByText(root, "공개그룹")

        tvAccessType?.text = accessText
    }

    private fun bindInviteArea(
        root: View,
        tvInvite: TextView,
        btnCopy: ImageButton,
        btnEtc: ImageButton,
        btnInstagram: ImageButton,
        btnX: ImageButton
    ) {
        if (!isPublicGroup) {
            tvInvite.visibility = View.VISIBLE
            btnCopy.visibility = View.VISIBLE
            btnEtc.visibility = View.VISIBLE
            btnInstagram.visibility = View.VISIBLE
            btnX.visibility = View.VISIBLE
            return
        }

        tvInvite.visibility = View.GONE
        btnCopy.visibility = View.GONE
        btnEtc.visibility = View.GONE
        btnInstagram.visibility = View.GONE
        btnX.visibility = View.GONE

        (tvInvite.parent as? View)?.visibility = View.GONE
        (btnEtc.parent as? View)?.visibility = View.GONE

        findTextViewByText(root, "초대코드")?.visibility = View.GONE
        findTextViewByText(root, "친구 초대하기")?.visibility = View.GONE
    }

    private fun fetchInviteCode(groupId: Long, tvInvite: TextView) {
        val token = TokenProvider.getBearerToken(requireContext())

        if (token.isNullOrBlank()) {
            return
        }

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.networkService.getGroupInviteCode(token, groupId)
                }

                if (response.isSuccessful) {
                    val inviteCode = response.body()?.string().orEmpty().trim()

                    if (inviteCode.isNotBlank()) {
                        currentInviteCode = cleanInviteCode(inviteCode)
                        tvInvite.text = currentInviteCode
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun cleanInviteCode(rawCode: String): String {
        val trimmed = rawCode.trim()
        if (trimmed.isBlank()) return ""

        return if (trimmed.contains(":")) {
            trimmed.substringAfter(":").trim()
        } else {
            trimmed
        }
    }

    private fun shareInviteCode() {
        if (isPublicGroup) return
        if (currentInviteCode.isBlank()) {
            Toast.makeText(requireContext(), "초대코드가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        shareText("[LifeMaster] 그룹 초대코드: $currentInviteCode")
    }

    private fun bindGoals(view: View, goalLines: List<String>) {
        val layoutGoals = view.findViewById<LinearLayout>(R.id.layout_goals)
        layoutGoals.removeAllViews()

        val goals = if (goalLines.isEmpty()) listOf("-") else goalLines

        goals.forEachIndexed { index, goalText ->
            val row = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (index > 0) topMargin = 12.dp
                }
                orientation = LinearLayout.HORIZONTAL
            }

            val tvGoalNum = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = "최소목표 ${index + 1}"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                textSize = 12f
            }

            val tvGoal = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = goalText
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                textSize = 12f
            }

            row.addView(tvGoalNum)
            row.addView(tvGoal)
            layoutGoals.addView(row)
        }
    }

    private fun shareText(text: String) {
        if (text.isBlank()) return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "공유"))
    }

    private fun findTextViewByText(root: View, targetText: String): TextView? {
        if (root is TextView && root.text?.toString() == targetText) {
            return root
        }

        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                val result = findTextViewByText(root.getChildAt(i), targetText)
                if (result != null) return result
            }
        }

        return null
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}