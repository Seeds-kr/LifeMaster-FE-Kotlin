package com.example.lifemaster.presentation.total.mypage.view

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.lifemaster.R

class ProfileEditActivity : AppCompatActivity() {

    private var alertDialog: AlertDialog? = null
    private lateinit var backgroundDimmer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        val btnCancel = findViewById<Button>(R.id.btnCancel)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val cvBasic = findViewById<CardView>(R.id.cvBasic)
        val cvPro = findViewById<CardView>(R.id.cvPro)
        val cvUltra = findViewById<CardView>(R.id.cvUltra)
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        backgroundDimmer = View(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(resources.getColor(R.color.black_dim, theme))
            visibility = View.GONE
        }
        rootView.addView(backgroundDimmer)

        btnCancel.setOnClickListener {
            showExitDialog()
        }

        btnSave.setOnClickListener {
            Toast.makeText(this, "저장하기 버튼 클릭", Toast.LENGTH_SHORT).show()
        }

        cvBasic.setOnClickListener {
            handleSubscriptionClick("Basic")
        }

        cvPro.setOnClickListener {
            handleSubscriptionClick("Pro")
        }

        cvUltra.setOnClickListener {
            handleSubscriptionClick("Ultra")
        }
    }

    private fun showExitDialog() {
        val builder = AlertDialog.Builder(this)
        val customLayout = layoutInflater.inflate(R.layout.dialog_confirm_exit, null)
        builder.setView(customLayout)
        builder.setCancelable(false)

        alertDialog = builder.create()
        alertDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnCancelDialog = customLayout.findViewById<Button>(R.id.btnCancelDialog)
        val btnExitDialog = customLayout.findViewById<Button>(R.id.btnExitDialog)

        btnCancelDialog.setOnClickListener {
            alertDialog?.dismiss()
            backgroundDimmer.visibility = View.GONE
        }

        btnExitDialog.setOnClickListener {
            alertDialog?.dismiss()
            backgroundDimmer.visibility = View.GONE
            finish()
        }

        backgroundDimmer.visibility = View.VISIBLE
        alertDialog?.show()
    }

    private fun handleSubscriptionClick(planName: String) {
        Toast.makeText(this, "$planName 플랜 선택", Toast.LENGTH_SHORT).show()
    }
}