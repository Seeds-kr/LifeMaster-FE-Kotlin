package com.example.lifemaster.presentation.total.detox.dialog

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.compose.material3.rememberTopAppBarState
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogDetoxTargetAppBinding
import com.example.lifemaster.presentation.total.detox.adapter.DetoxRepeatLockTargetAppAdapter
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxRepeatLockViewModel
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxViewModel

class DetoxRepeatLockTargetDialog: DialogFragment(R.layout.dialog_detox_target_app) {

    private lateinit var binding: DialogDetoxTargetAppBinding
    private val viewModel: DetoxViewModel by activityViewModels()
    private val adapter by lazy {
        DetoxRepeatLockTargetAppAdapter { item ->
            targetApp = item
        }
    }
    private var targetApp: DetoxTargetApp? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxTargetAppBinding.bind(view)
        setupViews()
        setupListeners()
    }

    private fun setupViews() = with(binding) {
        tvDetoxTargetServiceTitle.text = "반복 잠금 대상 어플"
        tvDetoxTargetServiceSubTitle.text = "반복 잠금을 설정할 어플을 선택해주세요"
        binding.rvDetoxTargetService.layoutManager = GridLayoutManager(context, 5)
        binding.rvDetoxTargetService.adapter = adapter
        adapter.submitList(viewModel.installedApps.value) // TODO: 영구 잠금, 시간 잠금 제외 필터링한 변수 전달하기
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
            showParentDialog(targetApp = null)
        }
        binding.btnApply.setOnClickListener {
            targetApp?.let {
                dismiss()
                showParentDialog(targetApp = it)
            } ?: Toast.makeText(context, "반복 잠금할 앱을 선택해주세요", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showParentDialog(targetApp: DetoxTargetApp? = null) {
        val dialog = DetoxRepeatLockSettingDialog(targetApp)
        dialog.isCancelable = false
        dialog.show(parentFragmentManager, DetoxRepeatLockSettingDialog.TAG)
    }

    companion object {
        const val TAG = "DetoxRepeatLockTargetAppDialog"
    }
}