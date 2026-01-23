package com.example.lifemaster.presentation.login.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.lifemaster.databinding.ActivityLoginBinding
import android.content.Intent
import androidx.navigation.fragment.NavHostFragment
import com.example.lifemaster.R

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handlePasswordResetDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePasswordResetDeepLink(intent)
    }

    private fun handlePasswordResetDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        val token = data.getQueryParameter("token") ?: return

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_view) as? NavHostFragment
                ?: return
        val navController = navHostFragment.navController

        if (navController.currentDestination?.id == R.id.resetPasswordFragment) return

        val args = Bundle().apply { putString("token", token) }
        navController.navigate(R.id.resetPasswordFragment, args)
    }
}