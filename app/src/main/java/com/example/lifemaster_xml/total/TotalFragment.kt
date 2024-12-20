package com.example.lifemaster_xml.total

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.lifemaster_xml.R
import com.example.lifemaster_xml.databinding.FragmentTotalBinding

class TotalFragment : Fragment(R.layout.fragment_total) {

    lateinit var binding: FragmentTotalBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTotalBinding.bind(view)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnAddRepeatLockApp.setOnClickListener {
            val dialog = DetoxRepeatLockDialog()
            dialog.isCancelable = false
            dialog.show(activity?.supportFragmentManager!!, DetoxRepeatLockDialog.TAG)
        }
    }
}