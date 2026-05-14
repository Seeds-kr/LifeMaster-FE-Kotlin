package com.example.lifemaster.presentation.total

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R

data class FaqItem(
    val category: String,
    val question: String,
    val answer: String,
)

class FaqAdapter(
    private val items: List<FaqItem>,
) : RecyclerView.Adapter<FaqAdapter.VH>() {

    private val expanded = BooleanArray(items.size) { false }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_faq, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], expanded[position]) { nowExpanded ->
            expanded[position] = nowExpanded
            notifyItemChanged(position)
        }
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionLayout: LinearLayout = itemView.findViewById(R.id.question_layout)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_category)
        private val tvQuestion: TextView = itemView.findViewById(R.id.tv_question)
        private val tvAnswer: TextView = itemView.findViewById(R.id.tv_answer)
        private val ivArrow: ImageView = itemView.findViewById(R.id.iv_arrow)

        fun bind(item: FaqItem, isExpanded: Boolean, onToggle: (Boolean) -> Unit) {
            if (item.category.isBlank()) {
                tvCategory.visibility = View.GONE
            } else {
                tvCategory.visibility = View.VISIBLE
                tvCategory.text = item.category
            }
            tvQuestion.text = item.question
            tvAnswer.text = item.answer
            tvAnswer.visibility = if (isExpanded) View.VISIBLE else View.GONE
            ivArrow.rotation = if (isExpanded) 180f else 0f
            questionLayout.setOnClickListener {
                onToggle(!isExpanded)
            }
        }
    }
}
