package com.example.lifemaster.presentation.authentication.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentLoginEmailBinding

class LoginEmailFragment: Fragment(R.layout.fragment_login_email) {

    lateinit var binding: FragmentLoginEmailBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginEmailBinding.bind(view)
    }

}