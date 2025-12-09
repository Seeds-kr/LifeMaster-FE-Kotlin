package com.example.lifemaster.presentation.home.alarm.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmListBinding
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.home.alarm.adapter.AlarmAdapter
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmGenerateViewModel
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModel
import com.example.lifemaster.presentation.home.alarm.viewmodel.AlarmViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.lifemaster.presentation.home.alarm.model.AlarmModel
import com.example.lifemaster.presentation.home.alarm.model.AlarmResponse
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.alarm.model.mapper.toPresentation
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmListFragment : Fragment(R.layout.fragment_alarm_list) {

    private lateinit var binding: FragmentAlarmListBinding
    private val alarmViewModel: AlarmViewModel by activityViewModels(
        factoryProducer = { AlarmViewModelFactory(RetrofitInstance.networkService) }
    )
    private val alarmGenerateViewModel: AlarmGenerateViewModel by activityViewModels()

    private lateinit var toggleAlarm: AlarmModel

    private val alarmAdapter = AlarmAdapter { item ->
        toggleAlarm = item
        alarmGenerateViewModel.toggleAlarm(alarmId = item.id, isEnabled = item.switchOnOff)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmListBinding.bind(view)
        // 수면이랑 연관된 코드
        val origin = arguments?.getString("origin")
        if (origin == "alarm_random_mission") {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation).isVisible =
                true
            saveLocalAlarmInfo()
        }
        initViews()
        initListeners()
        initObservers()
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
        val userAlarmPrefs =
            requireContext().getSharedPreferences("user_alarm_info", Context.MODE_PRIVATE)
        userAlarmPrefs.edit().putString(LocalDate.now().toString(), formatted).apply()
    }

    private fun initViews() = with(binding) {
        alarmGenerateViewModel.fetchAlarmList()
        alarmRecyclerview.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayout.VERTICAL
            )
        )
        alarmRecyclerview.adapter = alarmAdapter
    }

    private fun initListeners() = with(binding) {
        tvAddAlarmItem.setOnClickListener {
            findNavController().navigate(R.id.action_alarmListFragment_to_alarmSettingFragment)
        }
    }

    private fun initObservers() = with(binding) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                alarmGenerateViewModel.alarmList.collect { resource ->
                    when (resource) {
                        is DataResource.Loading -> { }
                        is DataResource.Idle -> { }
                        is DataResource.Success -> {
                            val alarmResponse: List<AlarmResponse> = resource.data
                            if (alarmResponse.isNotEmpty()) {
                                llNoAlarmItem.isVisible = false
                                alarmRecyclerview.isVisible = true
                                alarmAdapter.submitList(alarmResponse.map { it.toPresentation() })
                            }
                        }
                        is DataResource.Error -> {
                            Toast.makeText(context, "알람 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                            Log.e(ALARM, "alarmList fetch error", resource.throwable)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                alarmGenerateViewModel.alarmToggleState.collect { resource ->
                    when(resource) {
                        is DataResource.Success -> {
                            val isEnabled = resource.data
                            if(isEnabled) {
                                Toast.makeText(context, "알람이 켜졌습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "알람이 꺼졌습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        is DataResource.Error -> {
                            // TODO: 테스트 확인 필요 → 네트워크 끈 상태에서 알람 스위치 바꿔보기 (예상 동작: 토스트 메세지 뜨면서 스위치 안바뀌어야함)
                            Toast.makeText(context, "네트워크가 불안정합니다.", Toast.LENGTH_SHORT).show()
                            val currentList = alarmAdapter.currentList.toMutableList()
                            val index = currentList.indexOfFirst { it.id == toggleAlarm.id }
                            val oldItem = currentList[index]
                            val rollbackItem = oldItem.copy(switchOnOff = !toggleAlarm.switchOnOff)
                            currentList[index] = rollbackItem
                            alarmAdapter.submitList(currentList.toList())
                        }
                        DataResource.Idle -> TODO()
                        DataResource.Loading -> TODO()
                    }
                }
            }
        }
    }

    companion object {
        const val ALARM = "ALARM"
    }
}