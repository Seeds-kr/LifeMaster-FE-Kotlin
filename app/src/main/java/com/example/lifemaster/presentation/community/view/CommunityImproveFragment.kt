package com.example.lifemaster.presentation.community.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.presentation.community.adapter.CommunityAdapter
import com.example.lifemaster.presentation.community.model.CommunityItem
import com.example.lifemaster.presentation.community.viewmodel.CommunityViewModel
import com.example.lifemaster.presentation.community.viewmodel.PollViewModel

class CommunityImproveFragment : Fragment(R.layout.fragment_community_improve) {

    private val pollVm: PollViewModel by viewModels()
    private val communityVm: CommunityViewModel by activityViewModels()
    private var rvImprove: RecyclerView? = null
    private lateinit var improveAdapter: CommunityAdapter

    // 투표 영역
    private var tvQuestion: TextView? = null
    private var tvParticipantsNumber: TextView? = null

    private var preVoteGroup: View? = null
    private var option1: View? = null
    private var option2: View? = null
    private var option3: View? = null
    private var option1Text: TextView? = null
    private var option2Text: TextView? = null
    private var option3Text: TextView? = null

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

        // 개선게시판/자유게시판 전환
        view.findViewById<View>(R.id.tv_free_board)?.setOnClickListener {
            findNavController().navigate(R.id.communityFragment)
        }
        view.findViewById<View>(R.id.tab_free_container)?.setOnClickListener {
            findNavController().navigate(R.id.communityFragment)
        }

        rvImprove = view.findViewById(R.id.recyclerview_improve)
        improveAdapter = CommunityAdapter { onClickItem(it) }

        rvImprove?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = improveAdapter
            isNestedScrollingEnabled = false
        }

        communityVm.items.observe(viewLifecycleOwner) { list ->
            improveAdapter.submitList(list)
        }

        tvQuestion = view.findViewById(R.id.tvPollQuestion)
        tvParticipantsNumber = view.findViewById(R.id.pollParticipantsnumber)

        preVoteGroup = view.findViewById(R.id.preVoteContainer)
        option1 = view.findViewById(R.id.option1)
        option2 = view.findViewById(R.id.option2)
        option3 = view.findViewById(R.id.option3)
        option1Text = view.findViewById(R.id.option1Text)
        option2Text = view.findViewById(R.id.option2Text)
        option3Text = view.findViewById(R.id.option3Text)

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

        val token = readAuthToken()
        if (!token.isNullOrBlank()) {
            // 개선 게시판 목록
            communityVm.fetchPostsByType(token, "IMPROVEMENT") { toast(it) }
            // 투표 정보
            pollVm.fetchActivePoll(token) { toast(it) }
        }

        pollVm.ui.observe(viewLifecycleOwner) { ui ->
            if (ui == null) {
                showPreVote(false)
                return@observe
            }

            tvQuestion?.text = ui.title

            option1Text?.text = ui.options.getOrNull(0)?.content.orEmpty()
            option2Text?.text = ui.options.getOrNull(1)?.content.orEmpty()
            option3Text?.text = ui.options.getOrNull(2)?.content.orEmpty()
            tvParticipantsNumber?.text = String.format("%,d", ui.totalVotes)

            if (ui.myVotedOptionId != null || ui.isExpired) {
                showResults(ui)
            } else {
                showPreVote(true)
            }

            val o1Id = ui.options.getOrNull(0)?.optionId
            val o2Id = ui.options.getOrNull(1)?.optionId
            val o3Id = ui.options.getOrNull(2)?.optionId

            option1?.setOnClickListener {
                o1Id?.let { id -> cast(ui.pollId, id, ui.isExpired) } ?: toast("옵션 ID 없음")
            }
            option2?.setOnClickListener {
                o2Id?.let { id -> cast(ui.pollId, id, ui.isExpired) } ?: toast("옵션 ID 없음")
            }
            option3?.setOnClickListener {
                o3Id?.let { id -> cast(ui.pollId, id, ui.isExpired) } ?: toast("옵션 ID 없음")
            }
        }
    }

    private fun onClickItem(item: CommunityItem) {
        val b = Bundle().apply {
            putString(CommunityPostFragment.ARG_ITEM_ID, item.id)
        }
        findNavController().navigate(R.id.communityPostFragment, b)
    }

    private fun cast(pollId: Long, optionId: Int, isExpired: Boolean) {
        if (isExpired) {
            toast("만료된 투표입니다.")
            return
        }

        val token = readAuthToken() ?: run {
            toast("로그인이 필요합니다.")
            return
        }

        val userId = readUserId() ?: run {
            toast("회원 이메일 정보를 찾을 수 없어요.")
            return
        }

        setOptionsEnabled(false)

        pollVm.castVote(
            token = token,
            pollId = pollId,
            optionId = optionId,
            userId = userId,
            onDone = { setOptionsEnabled(false) },
            onError = { msg ->
                toast(msg)
                setOptionsEnabled(true)
            }
        )
    }

    private fun showPreVote(show: Boolean) {
        preVoteGroup?.isVisible = show
        postVoteGroup?.isVisible = !show
        setOptionsEnabled(show)
    }

    @SuppressLint("SetTextI18n")
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
        val p1Default = result1Percent?.currentTextColor
        val p2Default = result2Percent?.currentTextColor
        val p3Default = result3Percent?.currentTextColor
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.poll_percent_selected)

        result1Percent?.setTextColor(
            if (my == (o1?.optionId ?: -1)) selectedColor else (p1Default ?: selectedColor)
        )
        result2Percent?.setTextColor(
            if (my == (o2?.optionId ?: -1)) selectedColor else (p2Default ?: selectedColor)
        )
        result3Percent?.setTextColor(
            if (my == (o3?.optionId ?: -1)) selectedColor else (p3Default ?: selectedColor)
        )

        result1FillImg?.setImageResource(
            if (my == (o1?.optionId ?: -1)) R.drawable.bg_community_poll_progress_pink
            else R.drawable.bg_community_poll_progress_gray
        )
        result2FillImg?.setImageResource(
            if (my == (o2?.optionId ?: -1)) R.drawable.bg_community_poll_progress_pink
            else R.drawable.bg_community_poll_progress_gray
        )
        result3FillImg?.setImageResource(
            if (my == (o3?.optionId ?: -1)) R.drawable.bg_community_poll_progress_pink
            else R.drawable.bg_community_poll_progress_gray
        )
    }

    private fun setFillWidth(track: View?, fill: View?, percent: Int) {
        if (track == null || fill == null) return
        val p = percent.coerceIn(0, 100)
        track.post {
            val target = (track.width * (p / 100f)).toInt()
            val lp = fill.layoutParams
            val start = lp.width
            ValueAnimator.ofInt(start, target).apply {
                duration = 300
                addUpdateListener { anim ->
                    lp.width = anim.animatedValue as Int
                    fill.layoutParams = lp
                }
            }.start()
        }
    }

    private fun setOptionsEnabled(enabled: Boolean) {
        option1?.isEnabled = enabled
        option2?.isEnabled = enabled
        option3?.isEnabled = enabled
    }

    private fun readAuthToken(): String? =
        requireContext().getSharedPreferences("auth", 0)
            .getString("token", null)

    /**
     * userId: 로그인할 때 SharedPreferences("auth") 에 저장해둔 이메일 사용.
     *  - "userId" 키 먼저 찾고, 없으면 "email" 키 확인.
     */
    private fun readUserId(): String? {
        val sp = requireContext().getSharedPreferences("auth", 0)

        sp.getString("userId", null)?.let { if (it.isNotBlank()) return it }
        sp.getString("email", null)?.let { if (it.isNotBlank()) return it }

        return null
    }

    private fun toast(msg: String?) {
        if (!msg.isNullOrBlank())
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}