package com.example.lifemaster.presentation.community.view

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.lifemaster.R
import com.example.lifemaster.presentation.community.viewmodel.PollViewModel

class CommunityImproveFragment : Fragment(R.layout.fragment_community_improve) {

    private val vm: PollViewModel by viewModels()

    private var tvQuestion: TextView? = null
    private var tvParticipantsNumber: TextView? = null

    // 투표 전
    private var preVoteGroup: View? = null
    private var option1: View? = null
    private var option2: View? = null
    private var option3: View? = null
    private var option1Text: TextView? = null
    private var option2Text: TextView? = null
    private var option3Text: TextView? = null

    // 투표 후
    private var postVoteGroup: View? = null
    private var result1Track: View? = null
    private var result2Track: View? = null
    private var result3Track: View? = null
    private var result1Fill: View? = null
    private var result2Fill: View? = null
    private var result3Fill: View? = null
    private var result1FillImg: ImageView? = null
    private var result2FillImg: ImageView? = null
    private var result3FillImg: ImageView? = null
    private var result1Title: TextView? = null
    private var result2Title: TextView? = null
    private var result3Title: TextView? = null
    private var result1Percent: TextView? = null
    private var result2Percent: TextView? = null
    private var result3Percent: TextView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.tv_free_board)?.setOnClickListener {
            findNavController().navigate(R.id.communityFragment)
        }
        view.findViewById<View>(R.id.tab_free_container)?.setOnClickListener {
            findNavController().navigate(R.id.communityFragment)
        }

        tvQuestion = view.findViewById(R.id.tvPollQuestion)
        tvParticipantsNumber = view.findViewById(R.id.pollParticipantsnumber)

        // 투표 전
        preVoteGroup = view.findViewById(R.id.preVoteContainer)
        option1 = view.findViewById(R.id.option1)
        option2 = view.findViewById(R.id.option2)
        option3 = view.findViewById(R.id.option3)
        option1Text = view.findViewById(R.id.option1Text)
        option2Text = view.findViewById(R.id.option2Text)
        option3Text = view.findViewById(R.id.option3Text)

        // 투표 후
        postVoteGroup = view.findViewById(R.id.postVoteContainer)
        result1Track = view.findViewById(R.id.result1Track)
        result2Track = view.findViewById(R.id.result2Track)
        result3Track = view.findViewById(R.id.result3Track)
        result1Fill = view.findViewById(R.id.result1Fill)
        result2Fill = view.findViewById(R.id.result2Fill)
        result3Fill = view.findViewById(R.id.result3Fill)
        result1FillImg = view.findViewById(R.id.result1FillImg)
        result2FillImg = view.findViewById(R.id.result2FillImg)
        result3FillImg = view.findViewById(R.id.result3FillImg)
        result1Title = view.findViewById(R.id.result1Title)
        result2Title = view.findViewById(R.id.result2Title)
        result3Title = view.findViewById(R.id.result3Title)
        result1Percent = view.findViewById(R.id.result1Percent)
        result2Percent = view.findViewById(R.id.result2Percent)
        result3Percent = view.findViewById(R.id.result3Percent)

        vm.fetchActivePoll(readAuthToken()) { toast(it) }

        vm.ui.observe(viewLifecycleOwner) { ui ->
            if (ui == null) { showPreVote(false); return@observe }

            tvQuestion?.text = ui.title
            option1Text?.text = ui.options.getOrNull(0)?.content.orEmpty()
            option2Text?.text = ui.options.getOrNull(1)?.content.orEmpty()
            option3Text?.text = ui.options.getOrNull(2)?.content.orEmpty()

            tvParticipantsNumber?.text = String.format("%,d", ui.totalVotes)

            if (ui.myVotedOptionId != null || ui.isExpired) showResults(ui) else showPreVote(true)

            option1?.setOnClickListener { cast(ui.pollId, 1, ui.isExpired) }
            option2?.setOnClickListener { cast(ui.pollId, 2, ui.isExpired) }
            option3?.setOnClickListener { cast(ui.pollId, 3, ui.isExpired) }
        }
    }

    private fun cast(pollId: Long, optionIndex1Based: Int, isExpired: Boolean) {
        if (isExpired) { toast("만료된 투표입니다."); return }
        val token  = readAuthToken() ?: run { toast("로그인이 필요합니다."); return }
        val userId = readUserId() ?: run { toast("회원 정보가 필요합니다."); return }

        setOptionsEnabled(false)
        vm.castVote(
            token = token,
            pollId = pollId,
            optionIndex1Based = optionIndex1Based,
            userId = userId,
            onDone = { setOptionsEnabled(false) },
            onError = { msg -> toast(msg); setOptionsEnabled(true) }
        )
    }

    private fun showPreVote(show: Boolean) {
        preVoteGroup?.isVisible = show
        postVoteGroup?.isVisible = !show
        setOptionsEnabled(show)
    }

    private fun showResults(ui: PollViewModel.PollUi) {
        showPreVote(false)

        val o1 = ui.options.getOrNull(0)
        val o2 = ui.options.getOrNull(1)
        val o3 = ui.options.getOrNull(2)

        result1Title?.text = o1?.content.orEmpty()
        result2Title?.text = o2?.content.orEmpty()
        result3Title?.text = o3?.content.orEmpty()

        result1Percent?.text = "${o1?.votePercentage ?: 0}%"
        result2Percent?.text = "${o2?.votePercentage ?: 0}%"
        result3Percent?.text = "${o3?.votePercentage ?: 0}%"

        setFillWidth(result1Track, result1Fill, o1?.votePercentage ?: 0)
        setFillWidth(result2Track, result2Fill, o2?.votePercentage ?: 0)
        setFillWidth(result3Track, result3Fill, o3?.votePercentage ?: 0)

        val my = ui.myVotedOptionId
        result1FillImg?.setImageResource(
            if (my == (o1?.optionId ?: 1)) R.drawable.bg_community_poll_progress_pink
            else R.drawable.bg_community_poll_progress_gray
        )
        result2FillImg?.setImageResource(
            if (my == (o2?.optionId ?: 2)) R.drawable.bg_community_poll_progress_pink
            else R.drawable.bg_community_poll_progress_gray
        )
        result3FillImg?.setImageResource(
            if (my == (o3?.optionId ?: 3)) R.drawable.bg_community_poll_progress_pink
            else R.drawable.bg_community_poll_progress_gray
        )
    }

    private fun setFillWidth(track: View?, fill: View?, percent: Int) {
        if (track == null || fill == null) return
        val p = percent.coerceIn(0, 100)
        track.post {
            val target = (track.width * (p / 100f)).toInt()
            val lp = fill.layoutParams
            lp.width = target
            fill.layoutParams = lp
        }
    }

    private fun setOptionsEnabled(enabled: Boolean) {
        option1?.isEnabled = enabled
        option2?.isEnabled = enabled
        option3?.isEnabled = enabled
    }

    private fun readAuthToken(): String? =
        requireContext().getSharedPreferences("auth", 0).getString("token", null)

    private fun readUserId(): String? {
        val sp = requireContext().getSharedPreferences("auth", 0)
        return sp.getString("userId", null)
            ?: sp.getString("id", null)
            ?: sp.getString("memberId", null)
    }

    private fun toast(msg: String?) {
        if (!msg.isNullOrBlank())
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}