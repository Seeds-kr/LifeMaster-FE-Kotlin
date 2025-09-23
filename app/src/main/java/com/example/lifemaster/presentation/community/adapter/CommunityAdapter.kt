package com.example.lifemaster.presentation.community.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.databinding.ItemCommunityBoardPreviewBinding
import com.example.lifemaster.presentation.community.model.CommunityItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommunityAdapter(
    private val onClick: (CommunityItem) -> Unit
) : RecyclerView.Adapter<CommunityAdapter.VH>() {

    private val data = mutableListOf<CommunityItem>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: List<CommunityItem>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(
        private val b: ItemCommunityBoardPreviewBinding
    ) : RecyclerView.ViewHolder(b.root) {

        fun bind(item: CommunityItem) = with(b) {
            tvTitle.text = item.title
            tvAuthor.text = item.author.ifBlank { "익명" }
            tvViews.text = item.views.toString()
            tvLikes.text = item.likes.toString()
            tvDate.text = formatKoreanDate(item.createdAt)
            ivImage.setImageResource(R.drawable.ic_community_image)

            root.setOnClickListener { onClick(item) }
        }

        private fun formatKoreanDate(epochMillis: Long): String {
            if (epochMillis <= 0L) return ""
            val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
            return sdf.format(Date(epochMillis))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCommunityBoardPreviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}