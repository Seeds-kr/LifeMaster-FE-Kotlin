package com.example.lifemaster.presentation.home.alarm.view.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmListBinding
import com.example.lifemaster.presentation.home.alarm.adapter.AlarmAdapter
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModel
import com.example.lifemaster.presentation.home.sleep.viewmodel.SleepViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate

@AndroidEntryPoint
class AlarmListFragment : Fragment(R.layout.fragment_alarm_list) {

    private lateinit var binding: FragmentAlarmListBinding
    private val alarmViewModel: AlarmViewModel by activityViewModels()
    private val alarmAdapter = AlarmAdapter()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmListBinding.bind(view)
        val origin = arguments?.getString("origin")
        if(origin == "alarm_random_mission") {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation).isVisible = true
            saveLocalAlarmInfo()
        }
        setupViews()
        setupListeners()
        setupObservers()
    }

    private fun saveLocalAlarmInfo() {

        val triggerAt = alarmViewModel.alarmTriggeredAt // 밀리초(long)
        val triggeredDate = triggerAt?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
        }

        val dismissedAt = alarmViewModel.alarmDismissedAt // 밀리초(long)
        val dismissedDate = dismissedAt?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
        }

        val formatted = "%02d:%02d ~ %02d:%02d".format(
            triggeredDate?.hour,
            triggeredDate?.minute,
            dismissedDate?.hour,
            dismissedDate?.minute
        )

        // shared preference (internal)
        val userAlarmPrefs = requireContext().getSharedPreferences("user_alarm_info", Context.MODE_PRIVATE)
        userAlarmPrefs.edit().putString(LocalDate.now().toString(), formatted).apply()
    }

    private fun setupViews() {
        with(binding) {
            recyclerview.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL)) // 구분선 넣기
            recyclerview.adapter = alarmAdapter
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setupListeners() {
        with(binding) {
            tvAddAlarmItem.setOnClickListener {
                findNavController().navigate(R.id.action_alarmListFragment_to_alarmSettingFragment)
            }
        }
    }

    private fun setupObservers() {
        alarmViewModel.alarmItems.observe(viewLifecycleOwner) { items ->
            if (binding.llNoAlarmItem.visibility == View.VISIBLE) { // 리팩토링하기
                binding.llNoAlarmItem.visibility = View.GONE
                binding.recyclerview.visibility = View.VISIBLE
            }
            alarmAdapter.submitList(items)
        }
    }
}