package com.example.lifemaster.presentation.group.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R

class GroupCreateSuccessFragment : Fragment(R.layout.fragment_group_create_success) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val groupName = arguments?.getString("groupName").orEmpty()
        val groupDesc = arguments?.getString("groupDesc").orEmpty()
        val inviteCode = arguments?.getString("inviteCode").orEmpty()
        val goalLines = arguments?.getStringArrayList("goalLines") ?: arrayListOf()

        val tvName = view.findViewById<TextView>(R.id.tv_group_name)
        val tvDesc = view.findViewById<TextView>(R.id.tv_group_desc)
        val tvInvite = view.findViewById<TextView>(R.id.tv_invite_code)

        val tvGoalNum1 = view.findViewById<TextView>(R.id.tv_goal_num)
        val tvGoal1 = view.findViewById<TextView>(R.id.tv_goal)
        val tvGoalNum2 = view.findViewById<TextView>(R.id.tv_goal_num2)
        val tvGoal2 = view.findViewById<TextView>(R.id.tv_goal2)

        tvName.text = groupName
        tvDesc.text = if (groupDesc.isBlank()) " " else groupDesc
        tvInvite.text = if (inviteCode.isBlank()) "-" else inviteCode

        if (goalLines.isNotEmpty()) {
            tvGoalNum1.text = "최소목표 1"
            tvGoal1.text = goalLines.getOrNull(0).orEmpty()
        } else {
            tvGoalNum1.text = "최소목표 1"
            tvGoal1.text = "-"
        }

        if (goalLines.size >= 2) {
            tvGoalNum2.visibility = View.VISIBLE
            tvGoal2.visibility = View.VISIBLE
            tvGoalNum2.text = "최소목표 2"
            tvGoal2.text = goalLines.getOrNull(1).orEmpty()
        } else {
            tvGoalNum2.visibility = View.GONE
            tvGoal2.visibility = View.GONE
        }

        view.findViewById<ImageButton>(R.id.btn_copy).setOnClickListener {
            if (inviteCode.isBlank()) {
                Toast.makeText(requireContext(), "초대코드가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("inviteCode", inviteCode))
            Toast.makeText(requireContext(), "초대코드가 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<ImageButton>(R.id.btn_etc).setOnClickListener {
            shareText("[LifeMaster] 그룹 초대코드: $inviteCode")
        }

        view.findViewById<ImageButton>(R.id.btn_instagram).setOnClickListener {
            shareText("[LifeMaster] 그룹 초대코드: $inviteCode")
        }

        view.findViewById<ImageButton>(R.id.btn_x).setOnClickListener {
            shareText("[LifeMaster] 그룹 초대코드: $inviteCode")
        }

        view.findViewById<TextView>(R.id.btn_back_group).setOnClickListener {
            findNavController().popBackStack(R.id.groupFragment, false)
        }

        view.findViewById<View?>(R.id.includeBackButton)?.setOnClickListener {
            findNavController().popBackStack()
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
}