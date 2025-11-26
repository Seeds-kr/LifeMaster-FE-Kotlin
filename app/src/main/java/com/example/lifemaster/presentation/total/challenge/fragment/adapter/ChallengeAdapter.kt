package com.example.lifemaster.presentation.total.challenge.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.semantics.text
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemChallengeBinding // 💡 실제 생성된 바인딩 클래스를 import 합니다.
import com.example.lifemaster.domain.model.ChallengeItem

class ChallengeAdapter : PagingDataAdapter<ChallengeItem, ChallengeAdapter.ChallengeViewHolder>(ChallengeDiffCallback()) {

    var onItemClickListener: ((ChallengeItem) -> Unit)? = null
    var onJoinButtonClickListener: ((ChallengeItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        // 💡 실제 ItemChallengeBinding 클래스를 사용하여 뷰 홀더를 생성합니다.
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
        fun bind(challenge: ChallengeItem) {
            binding.tvChallengeName.text = challenge.challName
            binding.tvChallengeTitle.text = challenge.challTitle
            binding.tvJoinCount.text = "${challenge.challJoinCnt}명 참여중"

            // Glide나 Coil 같은 이미지 로딩 라이브러리를 사용하여 이미지 URL을 로드하세요.
            // 예: Glide.with(binding.ivChallengeImage.context).load(challenge.challImg).into(binding.ivChallengeImage)

            itemView.setOnClickListener {
                getItem(bindingAdapterPosition)?.let { challenge ->
                    onItemClickListener?.invoke(challenge)
                }
            }

            binding.btnJoin.setOnClickListener {
                getItem(bindingAdapterPosition)?.let { challenge ->
                    onJoinButtonClickListener?.invoke(challenge)
                }
            }
        }
    }

    private class ChallengeDiffCallback : DiffUtil.ItemCallback<ChallengeItem>() {
        override fun areItemsTheSame(oldItem: ChallengeItem, newItem: ChallengeItem): Boolean {
            return oldItem.challId == newItem.challId
        }

        override fun areContentsTheSame(oldItem: ChallengeItem, newItem: ChallengeItem): Boolean {
            return oldItem == newItem
        }
    }
}
