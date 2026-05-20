package com.example.lifemaster.presentation.total.detox.dialog

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogDetoxTargetAppBinding
import com.example.lifemaster.presentation.total.detox.adapter.DetoxTimeLockTargetAppAdapter
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxViewModel

class DetoxTimeLockTargetDialog(
    selectedApp: DetoxTargetApp? = null
): RoundedDialogFragment(R.layout.dialog_detox_target_app) {

    private lateinit var binding: DialogDetoxTargetAppBinding
    private val viewModel: DetoxViewModel by activityViewModels()
    private val adapter: DetoxTimeLockTargetAppAdapter by lazy { DetoxTimeLockTargetAppAdapter { app ->
        this.selectedApp = app
    } }

    private var selectedApp: DetoxTargetApp? = null

    init {
        this.selectedApp = selectedApp
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxTargetAppBinding.bind(view)
        setupViews()
        setupListeners()
    }

    private fun setupViews() = with(binding) {
        tvDetoxTargetServiceTitle.text = "시간 잠금 대상 어플"
        tvDetoxTargetServiceSubTitle.text = "시간 잠금을 설정할 어플을 선택해주세요"
        rvDetoxTargetService.layoutManager = GridLayoutManager(context, 5)
        rvDetoxTargetService.adapter = adapter
        adapter.submitList(viewModel.installedApps.value)
        if(selectedApp != null) {
            val currentPosition = adapter.currentList.indexOf(selectedApp)
            adapter.setCurrentPosition(currentPosition)
        }
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
            parentFragmentManager.setFragmentResult(REQUEST_KEY, Bundle())
        }
        binding.btnApply.setOnClickListener {
            selectedApp?.let {
                dismiss()
                parentFragmentManager.setFragmentResult(REQUEST_KEY, Bundle().apply {
                    putParcelable(BUNDLE_KEY, it)
                })
            } ?: Toast.makeText(context, "앱을 선택해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val TAG = "DetoxRepeatLockTargetAppDialog"
        const val REQUEST_KEY = "TargetDialogResult"
        const val BUNDLE_KEY = "BundleKey"
    }
}