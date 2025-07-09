package com.example.lifemaster.presentation.total

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentTotalBinding

class TotalFragment : Fragment(R.layout.fragment_total) {

    private lateinit var binding: FragmentTotalBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTotalBinding.bind(view)
        initListeners()
    }

    private fun initListeners() {
        binding.cvDetox.setOnClickListener {
            findNavController().navigate(R.id.action_totalFragment_to_detoxFragment)
        }
    }
}