package com.example.lifemaster.presentation.total.detox.dialog

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogDetoxTimeLockBinding
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp
import com.example.lifemaster.presentation.total.detox.model.DetoxTimeLockItem
import com.example.lifemaster.presentation.total.detox.model.DetoxTimeLockRequest
import com.example.lifemaster.presentation.total.detox.model.TimeLockRepeatDay
import com.example.lifemaster.presentation.total.detox.model.TimeLockRepeatPeriod
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxTimeLockViewModel
import kotlinx.coroutines.launch

class DetoxTimeLockDialog: DialogFragment(R.layout.dialog_detox_time_lock) {

    private lateinit var binding: DialogDetoxTimeLockBinding
    private val viewModel: DetoxTimeLockViewModel by activityViewModels()
    private var selectedPeriod: TimeLockRepeatPeriod ?= null
    private var selectedDay: TimeLockRepeatDay ?= null

    private var selectedApp: DetoxTargetApp? = null

    private var startHour: Int? = null // 24H
    private var startMinutes: Int? = null // 0~60

    private var endHour: Int? = null // 24
    private var endMinutes: Int? = null // 0~60

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxTimeLockBinding.bind(view)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() = with(binding) {
        // 드롭다운 - 반복 기간
        val periodArray = resources.getStringArray(R.array.detox_time_lock_repeat_period)
        val periodAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, periodArray)
        binding.autoCompleteTextViewPeriod.setAdapter(periodAdapter)
        binding.autoCompleteTextViewPeriod.setOnItemClickListener { parent, view, position, id ->
            selectedPeriod = TimeLockRepeatPeriod.WEEKLY // TODO: 서버 및 UI에서 매주 외에 다른 필드 추가시 수정하기
        }

        // 드롭다운 - 반복 요일
        val dayArray = resources.getStringArray(R.array.detox_time_lock_repeat_day)
        val dayAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, dayArray)
        binding.autoCompleteTextViewDay.setAdapter(dayAdapter)
        binding.autoCompleteTextViewDay.setOnItemClickListener { parent, view, position, id ->
            selectedDay = TimeLockRepeatDay.entries[position]
        }
    }

    private fun initListeners() = with(binding) {

        tvDetoxTimeLockSelectTargetApp.setOnClickListener {
            root.alpha = 0.0f
            val dialog = DetoxTimeLockTargetDialog(selectedApp).apply { isCancelable = false }
            dialog.show(childFragmentManager, DetoxTimeLockTargetDialog.TAG)
        }

        childFragmentManager.setFragmentResultListener(DetoxTimeLockTargetDialog.REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
            root.alpha = 1.0f
            if(bundle.containsKey(DetoxTimeLockTargetDialog.BUNDLE_KEY)) {
                selectedApp = bundle.getParcelable(DetoxTimeLockTargetDialog.BUNDLE_KEY, DetoxTargetApp::class.java)
                tvDetoxTimeLockSelectTargetApp.isVisible = false
                tvTargetAppName.text = selectedApp?.appName
                ivSelectTargetApp.setImageDrawable(selectedApp?.appIcon)
                tvTargetAppName.isVisible = true
                ivSelectTargetApp.isVisible = true
            }
        }

        // 시작 시간
        binding.btnStartHour.setOnClickListener {
            showTimePickerDialogAndSetStartTime()
        }
        binding.btnStartMinutes.setOnClickListener {
            showTimePickerDialogAndSetStartTime()
        }

        // 종료 시간
        binding.btnEndHour.setOnClickListener {
            showTimePickerDialogAndSetEndTime()
        }
        binding.btnEndMinutes.setOnClickListener {
            showTimePickerDialogAndSetEndTime()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnAdd.setOnClickListener {
            if(selectedApp == null || selectedPeriod == null || selectedDay == null || startHour == null || startMinutes == null || endHour == null || endMinutes == null) {
                Toast.makeText(context, "정보를 입력해주세요!", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.generateTimeLock(request = DetoxTimeLockRequest(
                    cycle = selectedPeriod!!,
                    day = selectedDay!!,
                    startTime = String.format("%02d:%02d:%02d", startHour, startMinutes, 0),
                    endTime = String.format("%02d:%02d:%02d", endHour, endMinutes, 0),
                    active = true,
                    lockedApps = selectedApp!!.appPackageName
                ))
            }
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.generateTimeLockResult.collect { resource ->
                    when(resource) {
                        is DataResource.Error -> {}
                        DataResource.Idle -> {}
                        DataResource.Loading -> {}
                        is DataResource.Success -> {
                            dismiss()
                            val response = resource.data // TODO: 어떻게 쓸건지?
                        }
                    }
                }
            }
        }
    }

    private fun showTimePickerDialogAndSetStartTime() {
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hour, minutes ->
                binding.btnStartHour.text = if(hour in 0..12) hour.toString() else (hour-12).toString()
                binding.btnStartMinutes.text = minutes.toString()
                binding.btnStartDayPart.text = if(hour in 0..11) "AM" else "PM"
                this.startHour = hour
                this.startMinutes = minutes
            },
            10,
            20,
            false
        )
        timePickerDialog.show()
    }

    private fun showTimePickerDialogAndSetEndTime() {
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hour, minutes ->
                binding.btnEndHour.text = if(hour in 0..12) hour.toString() else (hour-12).toString()
                binding.btnEndMinutes.text = minutes.toString()
                binding.btnEndDayPart.text = if(hour in 0..11) "AM" else "PM"
                this.endHour = hour
                this.endMinutes = minutes
            },
            10,
            20,
            false
        )
        timePickerDialog.show()
    }

    companion object {
        const val TAG = "DetoxTimeLockDialog"
    }
}