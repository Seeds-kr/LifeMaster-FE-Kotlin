package com.example.lifemaster.presentation.tutorial

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.DialogTutorialBinding

/**
 * 할일(홈)/챌린지 화면 첫 진입 시 보여주는 전체 화면 튜토리얼 이미지 다이얼로그.
 */
class TutorialDialogFragment : DialogFragment(R.layout.dialog_tutorial) {

    private var _binding: DialogTutorialBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            window?.setBackgroundDrawable(Color.WHITE.toDrawable())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = DialogTutorialBinding.bind(view)

        val imageRes = requireArguments().getInt(ARG_IMAGE_RES)
        binding.ivTutorial.setImageResource(imageRes)

        binding.btnTutorialClose.setOnClickListener { dismiss() }
        binding.btnTutorialConfirm.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "TutorialDialogFragment"
        private const val ARG_IMAGE_RES = "arg_image_res"

        fun newInstance(imageRes: Int): TutorialDialogFragment =
            TutorialDialogFragment().apply {
                arguments = bundleOf(ARG_IMAGE_RES to imageRes)
            }
    }
}
