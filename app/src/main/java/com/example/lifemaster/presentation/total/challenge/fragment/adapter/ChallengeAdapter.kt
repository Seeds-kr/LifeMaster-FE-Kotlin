package com.example.lifemaster.presentation.total.challenge.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.databinding.ItemChallengeBinding
import com.example.lifemaster.presentation.challenge.Challenge

class ChallengeAdapter(private val challengeList: List<Challenge>) : RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder>() {
    var onJoinButtonClickListener: ((Challenge) -> Unit)? = null

    var onItemClickListener: ((Challenge) -> Unit)? = null

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
        fun bind(challenge: Challenge) {
            binding.tvChallengeTitle.text = challenge.title
            binding.tvChallengeDescription.text = challenge.description

            binding.btnJoinChallenge.setOnClickListener {
                onJoinButtonClickListener?.invoke(challenge)
            }

            itemView.setOnClickListener {
                onItemClickListener?.invoke(challenge)
            }
        }
    }
}