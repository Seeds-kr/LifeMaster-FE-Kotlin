package com.example.lifemaster.presentation.group.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
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

    private var currentInviteCode: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val groupName = arguments?.getString("groupName").orEmpty()
        val groupDesc = arguments?.getString("groupDesc").orEmpty()
        val rawInviteCode = arguments?.getString("inviteCode").orEmpty()
        val goalLines = arguments?.getStringArrayList("goalLines") ?: arrayListOf()

        val groupId = arguments?.getLong("groupId")
            ?: rawInviteCode.substringBefore(":").toLongOrNull()
            ?: -1L

        val tvName = view.findViewById<TextView>(R.id.tv_group_name)
        val tvDesc = view.findViewById<TextView>(R.id.tv_group_desc)
        val tvInvite = view.findViewById<TextView>(R.id.tv_invite_code)

        currentInviteCode = cleanInviteCode(rawInviteCode)

        tvName.text = groupName
        tvDesc.text = if (groupDesc.isBlank()) " " else groupDesc
        tvInvite.text = if (currentInviteCode.isBlank()) "-" else currentInviteCode

        bindGoals(view, goalLines)

        if (groupId > 0L) {
            fetchInviteCode(groupId, tvInvite)
        }

        view.findViewById<ImageButton>(R.id.btn_copy).setOnClickListener {
            if (currentInviteCode.isBlank()) {
                Toast.makeText(requireContext(), "초대코드가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("inviteCode", currentInviteCode))
            Toast.makeText(requireContext(), "초대코드가 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<ImageButton>(R.id.btn_etc).setOnClickListener {
            shareInviteCode()
        }

        view.findViewById<ImageButton>(R.id.btn_instagram).setOnClickListener {
            shareInviteCode()
        }

        view.findViewById<ImageButton>(R.id.btn_x).setOnClickListener {
            shareInviteCode()
        }

        view.findViewById<TextView>(R.id.btn_back_group).setOnClickListener {
            findNavController().popBackStack(R.id.groupFragment, false)
        }

        view.findViewById<View?>(R.id.includeBackButton)?.setOnClickListener {
            findNavController().popBackStack()
        }
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

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}