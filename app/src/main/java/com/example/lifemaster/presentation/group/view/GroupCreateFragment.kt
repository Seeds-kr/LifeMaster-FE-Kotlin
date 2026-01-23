package com.example.lifemaster.presentation.group.view

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.lifemaster.R

class GroupCreateFragment : Fragment(R.layout.fragment_group_create) {

    // true = 공개, false = 비공개
    private var isPublicGroup: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val segmentRoot = view.findViewById<ConstraintLayout>(R.id.segment_root)
        val guidelineHalf = view.findViewById<View>(R.id.guideline_half)
        val selectedPill = view.findViewById<View>(R.id.view_selected_pill)

        val btnPublic = view.findViewById<TextView>(R.id.btn_public)
        val btnPrivate = view.findViewById<TextView>(R.id.btn_private)

        applySegmentUi(
            segmentRoot = segmentRoot,
            selectedPill = selectedPill,
            guidelineHalfId = guidelineHalf.id,
            btnPublic = btnPublic,
            btnPrivate = btnPrivate,
            isPublic = isPublicGroup
        )

        btnPublic.setOnClickListener {
            if (!isPublicGroup) {
                isPublicGroup = true
                applySegmentUi(segmentRoot, selectedPill, guidelineHalf.id, btnPublic, btnPrivate, true)
            }
        }

        btnPrivate.setOnClickListener {
            if (isPublicGroup) {
                isPublicGroup = false
                applySegmentUi(segmentRoot, selectedPill, guidelineHalf.id, btnPublic, btnPrivate, false)
            }
        }

        // TODO: btn_goal_add, btn_done 로직 연결
    }

    private fun applySegmentUi(
        segmentRoot: ConstraintLayout,
        selectedPill: View,
        guidelineHalfId: Int,
        btnPublic: TextView,
        btnPrivate: TextView,
        isPublic: Boolean
    ) {
        val set = ConstraintSet()
        set.clone(segmentRoot)

        set.clear(selectedPill.id, ConstraintSet.START)
        set.clear(selectedPill.id, ConstraintSet.END)

        if (isPublic) {
            set.connect(selectedPill.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            set.connect(selectedPill.id, ConstraintSet.END, guidelineHalfId, ConstraintSet.START)
        } else {
            set.connect(selectedPill.id, ConstraintSet.START, guidelineHalfId, ConstraintSet.START)
            set.connect(selectedPill.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        }

        set.applyTo(segmentRoot)

        val white = ContextCompat.getColor(requireContext(), R.color.white)
        val black30 = ContextCompat.getColor(requireContext(), R.color.black_30)

        if (isPublic) {
            btnPublic.setTextColor(white)
            btnPrivate.setTextColor(black30)
        } else {
            btnPublic.setTextColor(black30)
            btnPrivate.setTextColor(white)
        }
    }
}