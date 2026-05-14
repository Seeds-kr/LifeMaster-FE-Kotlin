package com.example.lifemaster.presentation.total

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentTotalBinding
import com.example.lifemaster.presentation.home.HomeConfig
import com.example.lifemaster.presentation.home.edit.view.HomeEditActivity
import com.example.lifemaster.presentation.total.mypage.view.MyPageActivity
import com.example.lifemaster.presentation.total.mypage.view.RefundPolicyActivity
import java.time.LocalDate

class TotalFragment : Fragment(R.layout.fragment_total) {

    private lateinit var binding: FragmentTotalBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTotalBinding.bind(view)
        bindServiceRows()
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        refreshProfile()
    }

    private fun refreshProfile() {
        val prefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nick = prefs.getString("nickname", null)
            ?.takeIf { it.isNotBlank() }
            ?: getString(R.string.mypage_nickname_sample)
        binding.tvUserName.text = nick
    }

    private fun bindServiceRows() {
        binding.llServiceItems.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        fun addRow(
            title: String,
            homeEditTintKey: String?,
            onClick: () -> Unit
        ) {
            val row = inflater.inflate(R.layout.item_total_service_row, binding.llServiceItems, false)
            val hex = homeEditTintKey?.let { HomeConfig.HOME_EDIT_ICON_TINT_BY_NAME[it] } ?: "#333333"
            row.findViewById<ImageView>(R.id.ivServiceIcon).apply {
                setImageResource(R.drawable.ic_logo_star)
                imageTintList = ColorStateList.valueOf(hex.toColorInt())
            }
            row.findViewById<TextView>(R.id.tvServiceTitle).text = title
            row.setOnClickListener { onClick() }
            binding.llServiceItems.addView(row)
        }

        addRow(getString(R.string.total_menu_alarm), "알람") {
            findNavController().navigate(R.id.action_totalFragment_to_alarmListFragment)
        }
        addRow(getString(R.string.sleep), "수면") {
            findNavController().navigate(R.id.action_totalFragment_to_sleepPlaylistDetailFragment)
        }
        addRow(getString(R.string.challenge), "챌린지") {
            findNavController().navigate(R.id.action_totalFragment_to_challengeFragment)
        }
        addRow(getString(R.string.total_menu_diary), "자아성찰 바로가기") {
            goIntrospection("TODAY")
        }
        addRow(getString(R.string.total_menu_thanks), "자아성찰 바로가기") {
            goIntrospection("THANKS")
        }

        val d = resources.displayMetrics.density
        binding.llServiceItems.addView(
            View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (1 * d).toInt()
                ).apply {
                    topMargin = (8 * d).toInt()
                    bottomMargin = (8 * d).toInt()
                }
                setBackgroundColor(0xFFEEEEEE.toInt())
            }
        )

        addRow(getString(R.string.group), "그룹 바로가기") {
            findNavController().navigate(R.id.action_totalFragment_to_groupFragment)
        }
        addRow(getString(R.string.total_menu_community), null) {
            findNavController().navigate(R.id.action_totalFragment_to_communityFragment)
        }
    }

    private fun goIntrospection(startTab: String) {
        val args = bundleOf(
            "startTab" to startTab,
            "selectedDate" to LocalDate.now().toString()
        )
        findNavController().navigate(R.id.action_totalFragment_to_introspectionFragment, args)
    }

    private fun initListeners() {
        binding.btnMyPage.setOnClickListener {
            startActivity(Intent(requireContext(), MyPageActivity::class.java))
        }

        binding.tvEditHome.setOnClickListener {
            startActivity(Intent(requireContext(), HomeEditActivity::class.java))
        }

        binding.btnFaq.setOnClickListener {
            Toast.makeText(requireContext(), R.string.total_page_preparing, Toast.LENGTH_SHORT).show()
        }

        binding.btnCustomerSupport.setOnClickListener {
            Toast.makeText(requireContext(), R.string.total_page_preparing, Toast.LENGTH_SHORT).show()
        }

        binding.tvTermsOfService.setOnClickListener {
            Toast.makeText(requireContext(), R.string.total_page_preparing, Toast.LENGTH_SHORT).show()
        }

        binding.tvPrivacyPolicy.setOnClickListener {
            Toast.makeText(requireContext(), R.string.total_page_preparing, Toast.LENGTH_SHORT).show()
        }

        binding.tvRefundPolicy.setOnClickListener {
            startActivity(Intent(requireContext(), RefundPolicyActivity::class.java))
        }
    }
}
