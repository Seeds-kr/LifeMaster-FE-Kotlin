package com.example.lifemaster.presentation.total.challenge.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lifemaster.databinding.ItemChallengeBinding
import com.example.lifemaster.presentation.total.challenge.model.ChallengeItem

class ChallengeAdapter : PagingDataAdapter<ChallengeItem, ChallengeAdapter.ChallengeViewHolder>(ChallengeDiffCallback()) {

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
                getItem(bindingAdapterPosition)?.let { challenge ->
                    onItemClickListener?.invoke(challenge)
                }
            }

            binding.btnJoinChallenge.setOnClickListener {
                getItem(bindingAdapterPosition)?.let { challenge ->
                    onJoinButtonClickListener?.invoke(challenge)
                }
            }
        }

        fun bind(challenge: ChallengeItem) {
            binding.tvChallengeTitle.text = challenge.challName
            binding.tvParticipantCount.text = "${challenge.challJoinCnt}명 참여중"

            Glide.with(binding.ivChallengeBanner.context)
                .load(challenge.challImg) // 이미지 URL
                //.placeholder(R.drawable.loading_placeholder) // 로딩 중에 보여줄 이미지
                //.error(R.drawable.error_placeholder)         // 에러 시 보여줄 이미지
                .into(binding.ivChallengeBanner) // 이미지를 표시할 ImageView
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
