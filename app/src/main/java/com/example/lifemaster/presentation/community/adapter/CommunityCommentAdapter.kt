package com.example.lifemaster.presentation.community.adapter

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
import kotlin.math.roundToInt

class CommunityCommentAdapter(
    // 나중에 권한 줄때 (지금은 무시)
    private val myMemberId: Long? = null,
    private val myNickname: String? = null,

    private val items: MutableList<Comment> = mutableListOf(),
    private val listener: CommentActionListener? = null,

    private val allowAllActions: Boolean = true
) : RecyclerView.Adapter<CommunityCommentAdapter.VH>() {

    companion object { private const val PAYLOAD_TIME = "payload_time" }

    interface CommentActionListener {
        fun onEditRequest(comment: Comment, position: Int)
        fun onDeleteRequest(comment: Comment, position: Int)
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
        h.btnLike?.setImageResource(if (item.isLiked) R.drawable.ic_fill_heart else R.drawable.ic_heart)
        h.tvEdited?.visibility = if (item.isEdited) View.VISIBLE else View.GONE

        h.btnLike?.setOnClickListener {
            val pos = h.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            val cur = items[pos]
            val newLiked = !cur.isLiked
            val newCount = if (newLiked) cur.likeCount + 1 else (cur.likeCount - 1).coerceAtLeast(0)
            items[pos] = cur.copy(likeCount = newCount, isLiked = newLiked)
            notifyItemChanged(pos)
        }

        h.itemView.setOnLongClickListener { anchor ->
            val pos = h.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnLongClickListener true
            val itemNow = items[pos]

            if (allowAllActions || isMine(itemNow)) {
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
                    val p = h.adapterPosition
                    if (p != RecyclerView.NO_POSITION) listener?.onEditRequest(items[p], p)
                    popup.dismiss()
                }
                content.findViewById<TextView>(R.id.btn_comment_delete).setOnClickListener {
                    val p = h.adapterPosition
                    if (p != RecyclerView.NO_POSITION) listener?.onDeleteRequest(items[p], p)
                    popup.dismiss()
                }

                content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                val dp = ctx.resources.displayMetrics.density
                val xOff = anchor.width - content.measuredWidth - (6 * dp).roundToInt()
                val yOff = (4 * dp).roundToInt()
                popup.showAsDropDown(anchor, xOff, yOff)
            }
            true
        }
    }

    // 나중에 권한 줄때 (지금은 미사용)
    private fun isMine(c: Comment): Boolean {
        val mine = myMemberId
        return when {
            mine != null && c.memberId != null -> c.memberId == mine
            !myNickname.isNullOrBlank()        -> c.nickname == myNickname
            else                               -> false
        }
    }

    override fun onBindViewHolder(h: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_TIME)) {
            h.tvTime?.text = toRelativeTime(items[position].createdAt)
            return
        }
        super.onBindViewHolder(h, position, payloads)
    }

    override fun getItemCount(): Int = items.size

    fun submitAll(newItems: List<Comment>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun add(comment: Comment) {
        items.add(comment)
        notifyItemInserted(items.lastIndex)
    }

    private fun toRelativeTime(timeMillis: Long): String {
        val diff = System.currentTimeMillis() - timeMillis
        val min = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hr  = TimeUnit.MILLISECONDS.toHours(diff)
        val day = TimeUnit.MILLISECONDS.toDays(diff)
        val years = (day / 365)
        return when {
            min < 1   -> "방금 전"
            min < 60  -> "${min}분전"
            hr  < 24  -> "${hr}시간전"
            day < 365 -> "${day}일전"
            else      -> "${years}년전"
        }
    }
}