package com.example.lifemaster.presentation.total.challenge.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemChallengeBinding
import com.example.lifemaster.presentation.total.challenge.model.ChallengeItem

class ChallengeAdapter(private val challengeList: List<ChallengeItem>) : RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder>() {
    var onJoinButtonClickListener: ((ChallengeItem) -> Unit)? = null
    var onItemClickListener: ((ChallengeItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val binding = ItemChallengeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChallengeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val challenge = challengeList[position]
        holder.bind(challenge)
    }

    override fun getItemCount() = challengeList.size

    inner class ChallengeViewHolder(private val binding: ItemChallengeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(challenge: ChallengeItem) {
            binding.tvChallengeTitle.text = challenge.challName
            binding.tvChallengeDescription.text = challenge.challDesc

            binding.btnJoinChallenge.setOnClickListener {
                onJoinButtonClickListener?.invoke(challenge)
            }

            itemView.setOnClickListener {
                onItemClickListener?.invoke(challenge)
            }
        }
    }
}