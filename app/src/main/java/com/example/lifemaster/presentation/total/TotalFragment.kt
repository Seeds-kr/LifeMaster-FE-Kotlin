package com.example.lifemaster.presentation.total

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.lifemaster.SubscriptionHelper
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentTotalBinding
import com.example.lifemaster.network.NetworkService
import com.example.lifemaster.network.TokenManager
import com.example.lifemaster.presentation.total.mypage.model.MeResponse
import com.example.lifemaster.presentation.total.mypage.view.LegalDocumentActivity
import com.example.lifemaster.presentation.total.mypage.view.MyPageActivity
import com.example.lifemaster.presentation.total.mypage.view.RefundPolicyActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class TotalFragment : Fragment(R.layout.fragment_total) {

    private lateinit var binding: FragmentTotalBinding

    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var networkService: NetworkService

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTotalBinding.bind(view)
        initListeners()
    }

    override fun onResume() {
        super.onResume()
        refreshProfile()
        bindServiceRows()
        refreshMeFromServer()
    }

    private fun refreshProfile() {
        if (!isAdded) return
        val prefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        val nick = prefs.getString("nickname", null)?.trim()?.takeIf { it.isNotBlank() && it != "null" }
            ?: prefs.getString("nickName", null)?.trim()?.takeIf { it.isNotBlank() && it != "null" }
            ?: run {
                val email = prefs.getString("email", null)?.trim()
                    ?: prefs.getString("loginEmail", null)?.trim()
                if (!email.isNullOrBlank() && email.contains("@") && !email.contains("example@test.com")) {
                    email.substringBefore("@")
                } else null
            }
            ?: getString(R.string.mypage_nickname_sample)

        binding.tvUserName.text = nick

        val url = prefs.getString("profileImageUrl", null)?.trim().orEmpty()
        if (url.isBlank() || url == "null") {
            Glide.with(this).clear(binding.ivProfile)
            binding.ivProfile.setImageDrawable(null)
            binding.ivProfilePlaceholder.visibility = View.VISIBLE
        } else {
            binding.ivProfilePlaceholder.visibility = View.GONE
            Glide.with(this)
                .load(url)
                .circleCrop()
                .into(binding.ivProfile)
        }
    }

    private fun refreshMeFromServer() {
        val bearer = tokenManager.getBearerToken() ?: return
        lifecycleScope.launch {
            val me = withContext(Dispatchers.IO) {
                runCatching { networkService.getMe(bearer) }.getOrNull()
            }?.takeIf { it.isSuccessful }?.body() ?: return@launch

            SubscriptionHelper.saveAuthUserFromMe(requireContext(), me)
            withContext(Dispatchers.Main) {
                refreshProfile()
            }
        }
    }

    private fun bindServiceRows() {
        binding.llServiceItems.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val order = TotalServicesConfig.loadOrder(requireContext())

        for (key in order) {
            val content = rowContentForServiceKey(key) ?: continue
            val row = inflater.inflate(R.layout.item_total_service_row, binding.llServiceItems, false)
            val hex = TotalServicesConfig.homeEditTintHexForKey(key)
            row.findViewById<ImageView>(R.id.ivServiceIcon).apply {
                setImageResource(R.drawable.ic_logo_star)
                imageTintList = ColorStateList.valueOf(hex.toColorInt())
            }
            row.findViewById<TextView>(R.id.tvServiceTitle).text = content.title
            row.setOnClickListener { content.onClick() }
            binding.llServiceItems.addView(row)
        }
    }

    private data class ServiceRowContent(val title: String, val onClick: () -> Unit)

    private fun rowContentForServiceKey(key: String): ServiceRowContent? {
        return when (key) {
            TotalServicesConfig.KEY_ALARM -> ServiceRowContent(getString(R.string.total_menu_alarm)) {
                findNavController().navigate(R.id.action_totalFragment_to_alarmListFragment)
            }
            TotalServicesConfig.KEY_SLEEP -> ServiceRowContent(getString(R.string.sleep)) {
                SubscriptionHelper.checkPremiumAndRun(requireContext()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.sleep_feature_in_development),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            TotalServicesConfig.KEY_CHALLENGE -> ServiceRowContent(getString(R.string.challenge)) {
                findNavController().navigate(R.id.action_totalFragment_to_challengeFragment)
            }
            TotalServicesConfig.KEY_INTROSPECTION_DIARY -> ServiceRowContent(getString(R.string.total_menu_diary)) {
                goIntrospection("TODAY")
            }
            TotalServicesConfig.KEY_INTROSPECTION_THANKS -> ServiceRowContent(getString(R.string.total_menu_thanks)) {
                goIntrospection("THANKS")
            }
            TotalServicesConfig.KEY_GROUP -> ServiceRowContent(getString(R.string.group)) {
                findNavController().navigate(R.id.action_totalFragment_to_groupFragment)
            }
            TotalServicesConfig.KEY_COMMUNITY -> ServiceRowContent(getString(R.string.total_menu_community)) {
                findNavController().navigate(R.id.action_totalFragment_to_communityFragment)
            }
            else -> null
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
            SubscriptionHelper.checkPremiumAndRun(requireContext()) {
                startActivity(Intent(requireContext(), TotalServicesEditActivity::class.java))
            }
        }

        binding.btnFaq.setOnClickListener {
            startActivity(Intent(requireContext(), FaqActivity::class.java))
        }

        binding.btnCustomerSupport.setOnClickListener {
            Toast.makeText(requireContext(), R.string.total_page_preparing, Toast.LENGTH_SHORT).show()
        }

        binding.tvTermsOfService.setOnClickListener {
            startActivity(
                LegalDocumentActivity.createIntent(
                    requireContext(),
                    R.string.total_terms,
                    R.string.terms_of_service_content,
                )
            )
        }

        binding.tvPrivacyPolicy.setOnClickListener {
            startActivity(
                LegalDocumentActivity.createIntent(
                    requireContext(),
                    R.string.total_privacy,
                    R.string.privacy_policy_content,
                )
            )
        }

        binding.tvRefundPolicy.setOnClickListener {
            startActivity(Intent(requireContext(), RefundPolicyActivity::class.java))
        }
    }
}
