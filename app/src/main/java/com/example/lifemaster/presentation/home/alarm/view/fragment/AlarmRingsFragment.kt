package com.example.lifemaster.presentation.home.alarm.view.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmRingBinding
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.getValue

class AlarmRingsFragment : Fragment(R.layout.fragment_alarm_ring) {

    lateinit var binding: FragmentAlarmRingBinding
    private val alarmViewModel: AlarmViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmRingBinding.bind(view)
        alarmViewModel.alarmTriggeredAt = arguments?.getLong("time") // 알람이 울린 시간
        initViews()
        initListeners()
    }

    private fun initViews() = with(binding) {
        val currentTimeMillis = System.currentTimeMillis()
        val date = Date(currentTimeMillis)
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val result = formatter.format(date)
        val hour = result.substringBefore(":").toInt()
        val timeType = when(hour) {
            in 6..11 -> "Morning"
            in 12..18 -> "Afternoon"
            in 19..23 -> "Night"
            else -> "Dawn"
        }
        tvTimeType.text = "Good\n${timeType},\nit's"
    }

    private fun initListeners() = with(binding) {
        btnRandomMission.setOnClickListener {
            findNavController().navigate(R.id.action_alarmRingsFragment_to_alarmRandomMissionTapFragment)
        }

        // 뒤로가기 버튼(백버튼) 막기
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() { Toast.makeText(context, "허튼 수작 부리지 마세요!", Toast.LENGTH_SHORT).show() }
            }
        )
    }
}