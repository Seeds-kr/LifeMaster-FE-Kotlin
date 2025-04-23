package com.example.lifemaster.presentation.group.view.fragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmListBinding
import com.example.lifemaster.presentation.group.adapter.AlarmAdapter
import com.example.lifemaster.presentation.group.receiver.AlarmReceiver
import com.example.lifemaster.presentation.group.viewmodel.AlarmViewModel

class AlarmListFragment : Fragment(R.layout.fragment_alarm_list) {

    private lateinit var binding: FragmentAlarmListBinding
    private val alarmViewModel: AlarmViewModel by activityViewModels()
    private val alarmAdapter = AlarmAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmListBinding.bind(view)
        setupViews()
        setupListeners()
        setupObservers()
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
            btnAlarmTest.setOnClickListener {
                val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if(alarmManager.canScheduleExactAlarms()) {
                    val intent = Intent(requireContext(), AlarmReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    val goesOffTime = System.currentTimeMillis() + 5_000 // 현재로부터 5초 뒤
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, goesOffTime, pendingIntent)
                }
                else {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${requireContext().packageName}")
                    }
                    requireContext().startActivity(intent)
                }
            }
        }
    }

    private fun setupObservers() {
        alarmViewModel.alarmItems.observe(viewLifecycleOwner) { items ->
            if(binding.llNoAlarmItem.visibility == View.VISIBLE) { // 리팩토링하기
                binding.llNoAlarmItem.visibility = View.GONE
                binding.recyclerview.visibility = View.VISIBLE
            }
            alarmAdapter.submitList(items.toMutableList())
        }
    }
}