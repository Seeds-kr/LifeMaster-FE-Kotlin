package com.example.lifemaster.presentation.total.challenge.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lifemaster.R
import com.example.lifemaster.databinding.ItemChallengeBinding
import com.example.lifemaster.presentation.total.challenge.model.ChallengeItem

class ChallengeAdapter : ListAdapter<ChallengeItem, ChallengeAdapter.ChallengeViewHolder>(ChallengeDiffCallback()) {

    var onItemClickListener: ((ChallengeItem) -> Unit)? = null
    var onJoinButtonClickListener: ((ChallengeItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val binding = ItemChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChallengeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = getItem(position)
        if (challenge != null) {
            holder.bind(challenge)
        }
    }

    inner class ChallengeViewHolder(private val binding: ItemChallengeBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    getItem(bindingAdapterPosition)?.let { challenge ->
                        onItemClickListener?.invoke(challenge)
                    }
                }
            }

            binding.btnJoinChallenge.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    getItem(bindingAdapterPosition)?.let { challenge ->
                        onJoinButtonClickListener?.invoke(challenge)
                    }
                }
            }
        }

        fun bind(challenge: ChallengeItem) {
            binding.tvChallengeTitle.text = challenge.challName
            binding.tvParticipantCount.text = "${challenge.challJoinCnt}명 참여"
            binding.tvChallengeDescription.text = challenge.challTitle // challTitle에 API의 challDesc가 매핑되어 있음
            
            // 날짜 표시 (createdAt 활용)
            if (!challenge.createdAt.isNullOrBlank()) {
                // "2024-07-20T..." -> "2024.07.20 ~" 형태로 변환 (간단히 앞부분만 사용)
                val datePart = challenge.createdAt.split("T").firstOrNull() ?: ""
                binding.tvChallengeDate.text = if (datePart.isNotBlank()) "$datePart ~" else ""
            }

            if (challenge.isJoined) {
                binding.btnJoinChallenge.text = "참여중"
                binding.btnJoinChallenge.isEnabled = false
            } else {
                binding.btnJoinChallenge.text = "참여하기"
                binding.btnJoinChallenge.isEnabled = true
            }

            Glide.with(binding.ivChallengeBanner.context)
                .load(challenge.challImg)
                .placeholder(R.drawable.bg_circle_default) // 기본 로딩 이미지
                .into(binding.ivChallengeBanner)
        }
    }

    // DiffUtil: RecyclerView의 성능을 최적화하기 위해 사용됨
    // PagingDataAdapter: 내부적으로 DiffUtil을 사용하여 아이템 변경을 효율적으로 처리
    private class ChallengeDiffCallback : DiffUtil.ItemCallback<ChallengeItem>() {
        // 두 아이템이 동일한 객체인지 ID로 비교
        override fun areItemsTheSame(oldItem: ChallengeItem, newItem: ChallengeItem): Boolean {
            return oldItem.challId == newItem.challId
        }

        // 두 아이템의 내용이 동일한지 모든 속성 비교
        override fun areContentsTheSame(oldItem: ChallengeItem, newItem: ChallengeItem): Boolean {
            return oldItem == newItem
        }
    }
}
