package com.example.lifemaster_xml.total.detox.dialog

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.databinding.DialogDetoxTimeLockBinding

class DetoxTimeLockDialog: DialogFragment(R.layout.dialog_detox_time_lock) {

    private lateinit var binding: DialogDetoxTimeLockBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxTimeLockBinding.bind(view)

        // 드롭다운 - 반복 기간
        val periodArray = resources.getStringArray(R.array.detox_time_lock_repeat_period)
        val periodAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, periodArray)
        binding.autoCompleteTextViewPeriod.setAdapter(periodAdapter)

        // 드롭다운 - 반복 요일
        val dayArray = resources.getStringArray(R.array.detox_time_lock_repeat_day)
        val dayAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, dayArray)
        binding.autoCompleteTextViewDay.setAdapter(dayAdapter)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnAdd.setOnClickListener {
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        const val TAG = "DetoxTimeLockDialog"
    }
}