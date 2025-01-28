package com.example.lifemaster.presentation.group

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentAlarmBinding

class AlarmFragment : Fragment(R.layout.fragment_alarm) {

    lateinit var binding: FragmentAlarmBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlarmBinding.bind(view)
    }
}