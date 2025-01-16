package com.example.lifemaster.presentation.group

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentGroupBinding

class GroupFragment : Fragment(R.layout.fragment_group) {

    lateinit var binding: FragmentGroupBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGroupBinding.bind(view)
    }
}