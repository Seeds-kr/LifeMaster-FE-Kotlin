package com.example.lifemaster_xml.total.detox.dialog

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.databinding.DialogDetoxTimeLockBinding
import com.example.lifemaster_xml.total.detox.model.DetoxTimeLockItem
import com.example.lifemaster_xml.total.detox.viewmodel.DetoxTimeLockViewModel

class DetoxTimeLockDialog: DialogFragment(R.layout.dialog_detox_time_lock) {

    private lateinit var binding: DialogDetoxTimeLockBinding
    private val viewModel: DetoxTimeLockViewModel by activityViewModels()
    private var selectedPeriod: String? = null
    private var selectedDay: String ?= null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxTimeLockBinding.bind(view)

        // 드롭다운 - 반복 기간
        val periodArray = resources.getStringArray(R.array.detox_time_lock_repeat_period)
        val periodAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, periodArray)
        binding.autoCompleteTextViewPeriod.setAdapter(periodAdapter)
        binding.autoCompleteTextViewPeriod.setOnItemClickListener { parent, view, position, id ->
            selectedPeriod = parent.getItemAtPosition(position).toString()
        }

        // 드롭다운 - 반복 요일
        val dayArray = resources.getStringArray(R.array.detox_time_lock_repeat_day)
        val dayAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, dayArray)
        binding.autoCompleteTextViewDay.setAdapter(dayAdapter)
        binding.autoCompleteTextViewDay.setOnItemClickListener { parent, view, position, id ->
            selectedDay = parent.getItemAtPosition(position).toString()
        }
        setupListeners()
    }

    private fun setupListeners() {

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
            if(selectedPeriod == null || selectedDay == null) {
                Toast.makeText(context, "기간 또는 요일 설정을 해주세요!", Toast.LENGTH_SHORT).show()
            } else {

                val newItemId = viewModel.timeLockItems.value?.size ?: 0
                val startHour = if(binding.btnStartHour.text.length == 1) "0${binding.btnStartHour.text}" else binding.btnStartHour.text
                val startMinutes = if(binding.btnStartMinutes.text.length == 1) "0${binding.btnStartMinutes.text}" else binding.btnStartMinutes.text
                val endHour = if(binding.btnEndHour.text.length == 1) "0${binding.btnEndHour.text}" else binding.btnEndHour.text
                val endMinutes = if(binding.btnEndMinutes.text.length == 1) "0${binding.btnEndMinutes.text}" else binding.btnEndMinutes.text

                val newItem = DetoxTimeLockItem(
                    itemId = newItemId,
                    weekType = selectedPeriod!!,
                    day = selectedDay!!,
                    startHour = startHour.toString(),
                    startMinutes = startMinutes.toString(),
                    startType = binding.btnStartDayPart.text.toString(),
                    endHour = endHour.toString(),
                    endMinutes = endMinutes.toString(),
                    endType = binding.btnEndDayPart.text.toString()
                )
                viewModel.addTimeLockItems(newItem)
                dismiss()
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