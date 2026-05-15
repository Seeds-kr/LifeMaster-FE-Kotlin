package com.example.lifemaster.presentation.group.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.presentation.group.model.GroupChatMessage
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class GroupChatAdapter(
    private val myUserId: Long
) : ListAdapter<GroupChatMessage, RecyclerView.ViewHolder>(diff) {

    companion object {
        private const val TYPE_ME = 1
        private const val TYPE_OTHER = 2

        private val diff = object : DiffUtil.ItemCallback<GroupChatMessage>() {
            override fun areItemsTheSame(
                oldItem: GroupChatMessage,
                newItem: GroupChatMessage
            ): Boolean {
                return if (oldItem.id != null && newItem.id != null) {
                    oldItem.id == newItem.id
                } else {
                    oldItem.groupId == newItem.groupId &&
                            oldItem.senderId == newItem.senderId &&
                            oldItem.content == newItem.content &&
                            oldItem.timestamp == newItem.timestamp
                }
            }

            override fun areContentsTheSame(
                oldItem: GroupChatMessage,
                newItem: GroupChatMessage
            ): Boolean = oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderId == myUserId) TYPE_ME else TYPE_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ME) {
            MeViewHolder(inflater.inflate(R.layout.item_chat_me, parent, false))
        } else {
            OtherViewHolder(inflater.inflate(R.layout.item_chat_other, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is MeViewHolder -> holder.bind(item)
            is OtherViewHolder -> holder.bind(item)
        }
    }

    class MeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val tvReadStatus: TextView? = itemView.findViewById(R.id.tv_read_status)

        fun bind(item: GroupChatMessage) {
            tvMessage.text = item.content
            tvTime.text = formatChatTime(item.timestamp)
            tvReadStatus?.visibility = View.GONE
            tvReadStatus?.text = ""
        }
    }

    class OtherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSender: TextView = itemView.findViewById(R.id.tv_sender_name)
        private val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)

        fun bind(item: GroupChatMessage) {
            tvSender.text = item.senderName?.takeIf { it.isNotBlank() } ?: "그룹원 1"
            tvMessage.text = item.content
            tvTime.text = formatChatTime(item.timestamp)
        }
    }
}

private fun formatChatTime(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return ""

    val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREA)
    val seoulZone = ZoneId.of("Asia/Seoul")

    return runCatching {
        OffsetDateTime.parse(timestamp)
            .atZoneSameInstant(seoulZone)
            .format(formatter)
    }.recoverCatching {
        Instant.parse(timestamp)
            .atZone(seoulZone)
            .format(formatter)
    }.recoverCatching {
        LocalDateTime.parse(timestamp)
            .atOffset(ZoneOffset.UTC)
            .atZoneSameInstant(seoulZone)
            .format(formatter)
    }.getOrElse {
        ""
    }
}