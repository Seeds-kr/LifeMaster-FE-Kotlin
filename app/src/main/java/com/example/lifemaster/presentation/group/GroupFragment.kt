package com.example.lifemaster.presentation.group

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R

class GroupFragment : Fragment(R.layout.fragment_group) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 전체 그룹 보기 버튼
        val allGroupsBtn = view.findViewById<View>(R.id.btn_all_groups_list)

        // 전체 그룹 보기로 이동
        allGroupsBtn?.setOnClickListener {
            findNavController().navigate(R.id.action_groupFragment_to_groupListFragment)
        }
    }
}