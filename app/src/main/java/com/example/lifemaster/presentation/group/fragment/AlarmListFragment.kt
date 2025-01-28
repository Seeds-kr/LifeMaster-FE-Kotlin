package com.example.lifemaster.presentation.group.fragment

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.lifemaster.databinding.FragmentAlarmListBinding

class AlarmListFragment : Fragment() {
    private lateinit var binding: FragmentAlarmListBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmListBinding.bind(view)
        setupViews()
        setupListeners()
        setupObservers()
    }

    private fun setupViews() {
        binding.recyclerview.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL)) // 구분선 넣기
    }

    private fun setupListeners() {

    }

    private fun setupObservers() {

    }
}