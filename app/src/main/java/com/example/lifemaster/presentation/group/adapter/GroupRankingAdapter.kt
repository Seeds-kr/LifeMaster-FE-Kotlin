package com.example.lifemaster.presentation.group.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lifemaster.R
import com.example.lifemaster.presentation.group.model.GroupRankingItem

class GroupRankingAdapter : RecyclerView.Adapter<GroupRankingAdapter.RankingViewHolder>() {

    private val items = mutableListOf<GroupRankingItem>()
    private var myRank: Int? = null

    fun submitList(newItems: List<GroupRankingItem>, myRank: Int?) {
        items.clear()
        items.addAll(newItems)
        this.myRank = myRank
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_ranking_row, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        holder.bind(items[position], myRank)
    }

    override fun getItemCount(): Int = items.size

    class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val highlightBg: View = itemView.findViewById(R.id.view_highlight_bg)
        private val tvRank: TextView = itemView.findViewById(R.id.tv_rank)
        private val ivProfile: ImageView = itemView.findViewById(R.id.iv_profile)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvCount: TextView = itemView.findViewById(R.id.tv_count)

        fun bind(item: GroupRankingItem, myRank: Int?) {
            tvRank.text = item.rank.toString()
            tvName.text = item.nickname?.takeIf { it.isNotBlank() } ?: "이름 없음"
            tvCount.text = item.achieveCount.toString()

            val isMine = myRank != null && item.rank == myRank
            highlightBg.visibility = if (isMine) View.VISIBLE else View.INVISIBLE

            if (!item.profileImage.isNullOrBlank()) {
                Glide.with(itemView)
                    .load(item.profileImage)
                    .placeholder(R.drawable.bg_circle)
                    .error(R.drawable.bg_circle)
                    .circleCrop()
                    .into(ivProfile)
            } else {
                ivProfile.setImageResource(R.drawable.bg_circle)
            }
        }
    }
}