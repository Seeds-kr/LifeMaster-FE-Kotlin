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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.lifemaster.presentation.home.alarm.ItemClickListener
import com.example.lifemaster.presentation.home.alarm.model.AlarmModel
import com.example.lifemaster.presentation.home.alarm.model.AlarmResponse
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.home.alarm.model.mapper.toPresentation
import com.example.lifemaster.presentation.home.alarm.util.cancelAlarm
import com.example.lifemaster.presentation.home.alarm.util.scheduleAlarm
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.lifemaster.network.NetworkService

@AndroidEntryPoint
class AlarmListFragment : Fragment(R.layout.fragment_alarm_list), ItemClickListener {

    private lateinit var binding: FragmentAlarmListBinding
    @Inject lateinit var networkService: NetworkService
    private val alarmViewModel: AlarmViewModel by activityViewModels(
        factoryProducer = { AlarmViewModelFactory(networkService) }
    )
    private val alarmGenerateViewModel: AlarmGenerateViewModel by activityViewModels()

    private var alarmId: Int? = null
    private var alarmStatus: Boolean? = null

    private val alarmAdapter = AlarmAdapter(this)

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
        ivAlarmItemAdd.setOnClickListener {
            findNavController().navigate(R.id.action_alarmListFragment_to_alarmCreateFragment)
        }
        ivAlarmItemOption.setOnClickListener {
            val popup = PopupMenu(requireContext(), ivAlarmItemOption)
            popup.menuInflater.inflate(R.menu.alarm_option_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.alarm_activate -> {
                        alarmGenerateViewModel.activateAllAlarms()
                        true
                    }
                    R.id.alarm_deactivate -> {
                        alarmGenerateViewModel.deactivateAllAlarms()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun initObservers() = with(binding) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    alarmGenerateViewModel.alarmList.collect { resource ->
                        when (resource) {
                            is DataResource.Loading -> { }
                            is DataResource.Idle -> { }
                            is DataResource.Success -> {
                                val alarmList = resource.data
                                if (alarmList.isNotEmpty()) {
                                    llNoAlarmItem.isVisible = false
                                    alarmRecyclerview.isVisible = true
                                    alarmAdapter.submitList(alarmList.toMutableList())
                                }
                            }
                            is DataResource.Error -> {
                                Toast.makeText(context, "알람 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                launch {
                    alarmGenerateViewModel.alarmToggleState.collect { resource ->
                        when(resource) {
                            is DataResource.Success -> {
                                val isEnabled = resource.data
                                if(isEnabled) {
                                    Toast.makeText(context, "알람이 켜졌습니다.", Toast.LENGTH_SHORT).show()
                                    // 알람 매니저 등록
                                    alarmId?.let { id ->
                                        alarmAdapter.currentList.find { it.id == id }?.let { alarm ->
                                            scheduleAlarm(requireContext(), alarm)
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "알람이 꺼졌습니다.", Toast.LENGTH_SHORT).show()
                                    // 알람 매니저 해제
                                    alarmId?.let { id ->
                                        cancelAlarm(requireContext(), id)
                                    }
                                }
                            }
                            is DataResource.Error -> { }
                            DataResource.Idle -> { }
                            DataResource.Loading -> { }
                        }
                    }
                }
                launch {
                    alarmGenerateViewModel.alarmDeleteState.collect { resource ->
                        when(resource) {
                            is DataResource.Error -> {
                                Toast.makeText(context, "알람을 삭제하지 못했습니다.", Toast.LENGTH_SHORT).show()
                                Log.e("TEST", "alarm delete error", resource.throwable)
                            }
                            DataResource.Idle -> {}
                            DataResource.Loading -> {}
                            is DataResource.Success -> {
                                val deleteAlarmId = resource.data
                                cancelAlarm(requireContext(), deleteAlarmId)
                                val updatedList = alarmAdapter.currentList.filter { it.id != deleteAlarmId }
                                alarmAdapter.submitList(updatedList.toList())
                                Toast.makeText(context, "알람을 삭제했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                launch {
                    alarmGenerateViewModel.alarmActivateState.collect { resource ->
                        when(resource) {
                            is DataResource.Error -> {}
                            DataResource.Idle -> {}
                            DataResource.Loading -> {}
                            is DataResource.Success -> {
                                val newAlarmList = alarmAdapter.currentList.map { it.copy(switchOnOff = true) }
                                alarmAdapter.submitList(newAlarmList)
                                // 모든 알람 스케줄링
                                newAlarmList.forEach { scheduleAlarm(requireContext(), it) }
                                Toast.makeText(context, "전체 알람을 켰습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                launch {
                    alarmGenerateViewModel.alarmDeactivateState.collect { resource ->
                        when(resource) {
                            is DataResource.Error -> {}
                            DataResource.Idle -> {}
                            DataResource.Loading -> {}
                            is DataResource.Success -> {
                                val newAlarmList = alarmAdapter.currentList.map { it.copy(switchOnOff = false) }
                                alarmAdapter.submitList(newAlarmList)
                                // 모든 알람 취소
                                newAlarmList.forEach { cancelAlarm(requireContext(), it.id) }
                                Toast.makeText(context, "전체 알람을 껐습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onItemClick(alarmId: Int) {
        val action = AlarmListFragmentDirections.actionAlarmListFragmentToAlarmEditFragment(alarmId = alarmId)
        findNavController().navigate(action)
    }

    override fun onItemLongClick(alarmId: Int) {
        AlertDialog.Builder(requireContext()).setTitle("알람 삭제").setMessage("알람을 삭제하시겠습니까?").setPositiveButton("확인") { _, _ ->
            alarmGenerateViewModel.deleteAlarm(alarmId = alarmId)
        }.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }.show()
    }

    override fun onSwitchToggle(alarmId: Int, alarmStatus: Boolean) {
        this.alarmId = alarmId
        this.alarmStatus = alarmStatus
        alarmGenerateViewModel.toggleAlarm(alarmId = alarmId)
    }

    companion object {
        const val ALARM = "ALARM"
    }
}