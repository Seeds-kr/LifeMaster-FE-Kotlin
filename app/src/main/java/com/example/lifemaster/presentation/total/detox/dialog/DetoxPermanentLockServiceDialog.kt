package com.example.lifemaster.presentation.total.detox.dialog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogDetoxTargetAppBinding
import com.example.lifemaster.presentation.home.alarm.model.DataResource
import com.example.lifemaster.presentation.total.detox.adapter.DetoxPermanentLockServiceSettingAdapter
import com.example.lifemaster.presentation.total.detox.model.DetoxPermanentLock
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxRepeatLockViewModel
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxViewModel
import kotlinx.coroutines.launch

class DetoxPermanentLockServiceDialog(
    private val permanentLockedPackageNames: Set<String>? = null
): RoundedDialogFragment(R.layout.dialog_detox_target_app) {
    private lateinit var binding: DialogDetoxTargetAppBinding
    private val repeatLockViewModel: DetoxRepeatLockViewModel by activityViewModels()
    private val viewModel: DetoxViewModel by activityViewModels()
    private val adapter by lazy {
        DetoxPermanentLockServiceSettingAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogDetoxTargetAppBinding.bind(view)
        setupViews()
        setupListeners()
        initObservers()
    }

    private fun setupViews() = with(binding) {
        tvDetoxTargetServiceTitle.text = "차단할 서비스 설정"
        tvDetoxTargetServiceSubTitle.text = "영구적으로 차단하고 싶은 서비스를 설정하세요."
        rvDetoxTargetService.layoutManager = GridLayoutManager(context, 5)
        rvDetoxTargetService.adapter = adapter
        permanentLockedPackageNames?.let { adapter.selectedPackages = it.toMutableSet() }
        adapter.submitList(viewModel.installedApps.value) // TODO: 반복 잠금, 시간 잠금 설정된 앱은 필터링해서 제거하기
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnApply.setOnClickListener {
            val selectedPackages = adapter.getSelectedPackageNames()

            viewModel.savePermanentLock(
                request = DetoxPermanentLock(
                    lockedAppPackageNames = selectedPackages
                )
            )

            val intent = Intent("com.example.lifemaster.BROADCAST_RECEIVER")
            intent.putStringArrayListExtra(
                "PERMANENT_BLOCK_SERVICE_APPLICATIONS",
                ArrayList(selectedPackages)
            )
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.generatePermanentLockResult.collect { dataResource ->
                        when(dataResource) {
                            is DataResource.Error -> {}
                            DataResource.Idle -> {}
                            DataResource.Loading -> {}
                            is DataResource.Success<Unit> -> {
                                Toast.makeText(context, "영구 차단할 서비스가 생성되었습니다.", Toast.LENGTH_SHORT).show()
                                viewModel.fetchPermanentLockItems()
                                dismiss()
                            }
                        }
                    }
                }
                launch {
                    viewModel.updatePermanentLockResult.collect { dataResource ->
                        when (dataResource) {
                            is DataResource.Error -> {}
                            DataResource.Idle -> {}
                            DataResource.Loading -> {}
                            is DataResource.Success<Unit> -> {
                                Toast.makeText(context, "영구 차단할 서비스가 변경되었습니다.", Toast.LENGTH_SHORT)
                                    .show()
                                viewModel.fetchPermanentLockItems()
                                dismiss()
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "DetoxPermanentLockServiceDialog"
    }
}