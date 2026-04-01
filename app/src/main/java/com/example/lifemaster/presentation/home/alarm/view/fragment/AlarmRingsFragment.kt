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
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModel
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class AlarmRingsFragment : Fragment(R.layout.fragment_alarm_ring) {

    lateinit var binding: FragmentAlarmRingBinding

    @Inject lateinit var networkService: NetworkService

    private val alarmViewModel: AlarmViewModel by activityViewModels(
        factoryProducer = { AlarmViewModelFactory(networkService) }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmRingBinding.bind(view)
        alarmViewModel.alarmTriggeredAt = arguments?.getLong("time") // 알람이 울린 시간 (알람이 울린 시간과 핸드폰 화면에 들어온 시간은 다르다)
        initViews()
        initListeners()
    }

    private fun initViews() = with(binding) {
        val localDateTime = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime()
        val hour = localDateTime.hour
        val minute = localDateTime.minute
        val timeType = when(hour) {
            in 6..11 -> "Morning"
            in 12..18 -> "Afternoon"
            in 19..23 -> "Night"
            else -> "Dawn"
        }
        tvTimeType.text = "Good\n${timeType},\nit's"
        tvCurrentTime.text = "$hour:$minute"
        tvDayOrNight.text = if(hour in 0..11) "AM" else "PM"
    }

    private fun initListeners() = with(binding) {
        btnRandomMission.setOnClickListener {
            findNavController().navigate(R.id.action_alarmRingsFragment_to_alarmRandomMissionTapFragment)
        }

        // 뒤로가기 버튼(백버튼) 막기
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() { Toast.makeText(context, "뒤로갈 수 없습니다!", Toast.LENGTH_SHORT).show() }
            }
        )
    }
}