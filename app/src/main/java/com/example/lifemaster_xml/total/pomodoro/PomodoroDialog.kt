package com.example.lifemaster_xml.total.pomodoro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.lifemaster_xml.data.SharedData
import com.example.lifemaster_xml.databinding.DialogPomodoroBinding

class PomodoroDialog: DialogFragment(), SendSelectedPositionInterface {
    lateinit var binding: DialogPomodoroBinding
    private var selectedPosition: Int? = null
    private val sharedViewModel by lazy {
        ViewModelProvider(requireActivity()).get(PomodoroViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogPomodoroBinding.inflate(inflater, container, false)
        binding.recyclerview.adapter = PomodoroAdapter(SharedData.todoItems, requireContext(), this) // [?] requireContext 를 전달하는게 맞나? 해당 위치에 작성하는 코드가 맞는가?
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSelect.setOnClickListener {
            // viewmodel 의 live data 값으로 세팅하기
            if(selectedPosition != null) {
                sharedViewModel.setPosition(selectedPosition!!)
                dismiss()
            } else {
                Toast.makeText(context, "할일을 선택해주세요!", Toast.LENGTH_SHORT).show() // [?] context 와 requireContext 의 차이?
            }
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun sendSelectedPosition(position: Int) {
        selectedPosition = position
    }
}