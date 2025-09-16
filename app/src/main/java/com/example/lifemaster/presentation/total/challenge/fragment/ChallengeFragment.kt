package com.example.lifemaster.presentation.challenge

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.databinding.FragmentChallengeBinding
import com.example.lifemaster.databinding.ItemChallengeBinding
import com.example.lifemaster.presentation.total.challenge.model.ChallengeItem
import com.example.lifemaster.presentation.challenge.viewmodel.ChallengeViewModel

data class MyChallenge(
    val imageRes: Int,
    val isCompleted: Boolean,
    val completionTime: String? = null
)

class ChallengeFragment : Fragment() {

    private var _binding: FragmentChallengeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChallengeViewModel by viewModels()
    private lateinit var challengeAdapter: ChallengeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMyChallenges()
        setupRecyclerView()
        observeViewModel()
        viewModel.loadChallenges()
    }

    private fun observeViewModel() {
        viewModel.challengeData.observe(viewLifecycleOwner) { challengeResponse ->
            challengeResponse?.content?.let { challengeList ->
                challengeAdapter.submitList(challengeList)
                Log.d("ChallengeFragment", "챌린지 목록 UI 업데이트: ${challengeList.size}개")
            }
        }
    }

    private fun setupMyChallenges() {
        val myChallenge1 = MyChallenge(
            imageRes = R.drawable.ic_cold_shower,
            isCompleted = true,
            completionTime = "9:12am"
        )
        val myChallenge2 = MyChallenge(
            imageRes = R.drawable.ic_stretching,
            isCompleted = false
        )

        setupMyChallengeView(binding.myChallenge1.root, myChallenge1)
        setupMyChallengeView(binding.myChallenge2.root, myChallenge2)
    }

    private fun setupMyChallengeView(challengeView: View, challengeData: MyChallenge) {
        val imageView = challengeView.findViewById<ImageView>(R.id.iv_challenge_image)
        val checkmark = challengeView.findViewById<ImageView>(R.id.iv_checkmark)
        val timeTextView = challengeView.findViewById<TextView>(R.id.tv_completion_time)

        imageView.setImageResource(challengeData.imageRes)

        if (challengeData.isCompleted) {
            checkmark.visibility = View.VISIBLE
            timeTextView.visibility = View.VISIBLE
            timeTextView.text = challengeData.completionTime

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val blurEffect = RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
                imageView.setRenderEffect(blurEffect)
            }
        } else {
            checkmark.visibility = View.GONE
            timeTextView.visibility = View.GONE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                imageView.setRenderEffect(null)
            }
        }
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter(emptyList())
        binding.rvChallenges.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = challengeAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ChallengeAdapter(private var items: List<ChallengeItem>) : RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder>() {

    fun submitList(newItems: List<ChallengeItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val binding = ItemChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChallengeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ChallengeViewHolder(private val binding: ItemChallengeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(challenge: ChallengeItem) {
            binding.tvChallengeTitle.text = challenge.challName
            binding.tvParticipantCount.text = "${challenge.challCnt}명 참여 중"
            binding.tvChallengeDescription.text = challenge.challDesc
            binding.tvChallengeDate.text = challenge.createdAt

            //val isParticipating = false // 임시 값
            /*if (challenge.isParticipating) {
                binding.btnJoinChallenge.text = "참여중"
                binding.btnJoinChallenge.setBackgroundResource(R.drawable.btn_background_participating)
                binding.btnJoinChallenge.setTextColor(ContextCompat.getColor(itemView.context, R.color.challenge_blue))
            } else {
                binding.btnJoinChallenge.text = "참여하기"
                binding.btnJoinChallenge.setBackgroundResource(R.drawable.btn_background_join)
                binding.btnJoinChallenge.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
            }*/

            val imageResId = itemView.context.resources.getIdentifier(challenge.challImg, "drawable", itemView.context.packageName)
            binding.ivChallengeBanner.setImageResource(imageResId)
        }
    }
}

