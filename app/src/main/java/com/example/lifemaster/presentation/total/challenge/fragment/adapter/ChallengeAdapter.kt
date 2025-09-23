package com.example.lifemaster.presentation.total.challenge.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lifemaster.R
import com.example.lifemaster.databinding.ItemChallengeBinding
//import com.example.lifemaster.network.ChallengeItem // 네트워크 모델 import

/*class ChallengeAdapter : ListAdapter<ChallengeItem, ChallengeAdapter.ChallengeViewHolder>(ChallengeDiffCallback()) {

    // 클릭 리스너 프로퍼티 (변수) 두 개를 정의합니다.
    var onItemClickListener: ((ChallengeItem) -> Unit)? = null
    var onJoinButtonClickListener: ((ChallengeItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val binding = ItemChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChallengeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChallengeViewHolder(private val binding: ItemChallengeBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(challenge: ChallengeItem) {
            binding.tvChallengeTitle.text = challenge.challName
            binding.tvChallengeDescription.text = challenge.challDesc

            Glide.with(itemView.context)
                .load(challenge.challImg)
                .placeholder(R.drawable.placeholder_image) // 임시 이미지를 drawable에 추가해주세요
                .error(R.drawable.error_image) // 임시 이미지를 drawable에 추가해주세요
                .into(binding.ivChallengeBanner)

            // 아이템 전체 클릭 리스너 설정
            itemView.setOnClickListener {
                onItemClickListener?.invoke(getItem(adapterPosition))
            }

            // 참여하기 버튼 클릭 리스너 설정
            binding.btnJoinChallenge.setOnClickListener {
                onJoinButtonClickListener?.invoke(getItem(adapterPosition))
            }
        }
    }

    class ChallengeDiffCallback : DiffUtil.ItemCallback<ChallengeItem>() {
        override fun areItemsTheSame(oldItem: ChallengeItem, newItem: ChallengeItem): Boolean {
            return oldItem.challId == newItem.challId
        }
        override fun areContentsTheSame(oldItem: ChallengeItem, newItem: ChallengeItem): Boolean {
            return oldItem == newItem
        }
    }
}*/