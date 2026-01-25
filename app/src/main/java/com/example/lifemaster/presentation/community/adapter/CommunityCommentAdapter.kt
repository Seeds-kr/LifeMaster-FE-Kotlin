package com.example.lifemaster.presentation.community.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.presentation.community.model.Comment
import java.util.concurrent.TimeUnit

class CommunityCommentAdapter(
    private val items: MutableList<Comment> = mutableListOf(),
    private val listener: CommentActionListener? = null
) : RecyclerView.Adapter<CommunityCommentAdapter.VH>() {

    interface CommentActionListener {
        fun onEditRequest(comment: Comment, position: Int)
        fun onDeleteRequest(comment: Comment, position: Int)
        fun onToggleLike(comment: Comment, position: Int)
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvNickname: TextView? = v.findViewById(R.id.tv_comment_nickname)
        val tvTime: TextView?     = v.findViewById(R.id.tv_comment_time)
        val tvEdited: TextView?   = v.findViewById(R.id.tv_comment_edited)
        val tvContent: TextView?  = v.findViewById(R.id.tv_comment)
        val tvLike: TextView?     = v.findViewById(R.id.tv_comment_like)
        val btnLike: ImageButton? = v.findViewById(R.id.btn_like)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community_comment, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = items[position]

        h.tvContent?.text = item.content
        h.tvTime?.text = toRelativeTime(item.createdAt)
        h.tvNickname?.text = item.nickname
        h.tvLike?.text = item.likeCount.toString()
        h.btnLike?.setImageResource(
            if (item.isLiked) R.drawable.ic_fill_heart else R.drawable.ic_heart
        )
        h.tvEdited?.visibility = if (item.isEdited) View.VISIBLE else View.GONE

        // 좋아요
        h.btnLike?.setOnClickListener {
            val pos = h.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

            listener?.onToggleLike(items[pos], pos)

            val cur = items[pos]
            val liked = !cur.isLiked
            val count = if (liked) cur.likeCount + 1 else (cur.likeCount - 1).coerceAtLeast(0)

            items[pos] = cur.copy(isLiked = liked, likeCount = count)
            notifyItemChanged(pos)
        }

        // 내 댓글(isMine)만 편집/삭제 메뉴 노출
        h.itemView.setOnLongClickListener { anchor ->
            val pos = h.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnLongClickListener true

            val cur = items[pos]
            if (!cur.isMine) return@setOnLongClickListener true

            showOwnerMenu(anchor) { action ->
                val p = h.adapterPosition
                if (p == RecyclerView.NO_POSITION) return@showOwnerMenu

                when (action) {
                    MenuAction.EDIT -> listener?.onEditRequest(items[p], p)
                    MenuAction.DELETE -> listener?.onDeleteRequest(items[p], p)
                }
            }
            true
        }
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitAll(newItems: List<Comment>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun add(comment: Comment) {
        items.add(comment)
        notifyItemInserted(items.lastIndex)
    }

    private enum class MenuAction { EDIT, DELETE }

    private fun showOwnerMenu(anchor: View, onAction: (MenuAction) -> Unit) {
        val ctx = anchor.context
        val content = LayoutInflater.from(ctx)
            .inflate(R.layout.dialog_community_comment_menu, null)

        val popup = PopupWindow(
            content,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            setBackgroundDrawable(android.graphics.Color.TRANSPARENT.toDrawable())
            elevation = 16f
        }

        content.findViewById<TextView>(R.id.btn_comment_edit).setOnClickListener {
            onAction(MenuAction.EDIT)
            popup.dismiss()
        }

        content.findViewById<TextView>(R.id.btn_comment_delete).setOnClickListener {
            onAction(MenuAction.DELETE)
            popup.dismiss()
        }

        content.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )

        val xOff = anchor.width - content.measuredWidth
        popup.showAsDropDown(anchor, xOff, 0)
    }

    private fun toRelativeTime(timeMillis: Long): String {
        val diff = System.currentTimeMillis() - timeMillis
        val min = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hr  = TimeUnit.MILLISECONDS.toHours(diff)
        val day = TimeUnit.MILLISECONDS.toDays(diff)
        val years = day / 365

        return when {
            min < 1   -> "방금 전"
            min < 60  -> "${min}분 전"
            hr  < 24  -> "${hr}시간 전"
            day < 365 -> "${day}일 전"
            else      -> "${years}년 전"
        }
    }
}