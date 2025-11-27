package com.example.lifemaster.presentation.total.challenge.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.animation.with
import androidx.compose.ui.semantics.text
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // 2. Glide 라이브러리를 import 합니다.
import com.example.lifemaster.databinding.ItemChallengeBinding
import com.example.lifemaster.domain.model.ChallengeItem

class ChallengeAdapter : PagingDataAdapter<ChallengeItem, ChallengeAdapter.ChallengeViewHolder>(ChallengeDiffCallback()) {

    // 아이템 전체 클릭 리스너
    var onItemClickListener: ((ChallengeItem) -> Unit)? = null
    // '참여하기' 버튼 클릭 리스너
    var onJoinButtonClickListener: ((ChallengeItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val binding = ItemChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChallengeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        // getItem()은 PagingDataAdapter에서 제공하는 함수로, 해당 위치의 아이템을 가져옵니다.
        val challenge = getItem(position)
        // Paging 데이터가 로드되는 동안 position에 아이템이 null일 수 있으므로, null 체크를 해줍니다.
        if (challenge != null) {
            holder.bind(challenge)
        }
    }

    inner class ChallengeViewHolder(private val binding: ItemChallengeBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            // 클릭 리스너를 init 블록에서 설정하여 재사용 효율을 높입니다.
            itemView.setOnClickListener {
                // bindingAdapterPosition을 통해 현재 아이템의 위치를 안전하게 가져옵니다.
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

        fun bind(challenge: ChallengeItem) {
            binding.tvChallengeName.text = challenge.challName
            binding.tvChallengeTitle.text = challenge.challTitle
            binding.tvJoinCount.text = "${challenge.challJoinCnt}명 참여중"

            // 4. Glide를 사용하여 서버에서 받은 이미지 URL을 ImageView에 로드합니다.
            Glide.with(binding.ivChallengeImage.context)
                .load(challenge.challImg) // 이미지 URL
                //.placeholder(R.drawable.loading_placeholder) // 로딩 중에 보여줄 이미지
                //.error(R.drawable.error_placeholder)         // 에러 시 보여줄 이미지
                .into(binding.ivChallengeImage) // 이미지를 표시할 ImageView
        }
    }

    // DiffUtil은 RecyclerView의 성능을 최적화하기 위해 사용됩니다.
    // PagingDataAdapter는 내부적으로 DiffUtil을 사용하여 아이템 변경을 효율적으로 처리합니다.
    private class ChallengeDiffCallback : DiffUtil.ItemCallback<ChallengeItem>() {
        // 두 아이템이 동일한 객체인지 ID로 비교합니다.
        override fun areItemsTheSame(oldItem: ChallengeItem, newItem: ChallengeItem): Boolean {
            return oldItem.challId == newItem.challId
        }

        // 두 아이템의 내용이 동일한지 모든 속성을 비교합니다.
        override fun areContentsTheSame(oldItem: ChallengeItem, newItem: ChallengeItem): Boolean {
            return oldItem == newItem
        }
    }
}
