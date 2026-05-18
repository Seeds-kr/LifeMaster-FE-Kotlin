package com.example.lifemaster.presentation.total.mypage.adapter

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.presentation.total.mypage.model.CouponResponse
import com.example.lifemaster.presentation.total.mypage.view.CouponUseActivity

class CouponAdapter(private val coupons: List<CouponResponse>) :
    RecyclerView.Adapter<CouponAdapter.CouponViewHolder>() {

    class CouponViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCode: TextView = view.findViewById(R.id.tvCouponCode)
        val tvType: TextView = view.findViewById(R.id.tvCouponType)
        val tvStatus: TextView = view.findViewById(R.id.tvCouponStatus)
        val tvDate: TextView = view.findViewById(R.id.tvCouponDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_registered_coupon, parent, false)
        return CouponViewHolder(view)
    }

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        val coupon = coupons[position]
        holder.tvCode.text = coupon.couponCode
        holder.tvType.text = coupon.couponType

        // 서버에서 내려온 쿠폰 상태값 확인 (대소문자 무시 및 USED 포함)
        val status = coupon.couponStatus.trim().uppercase()
        if (status == "USE" || status == "USED") {
            holder.tvStatus.text = "사용 완료"
            holder.tvStatus.setTextColor(Color.parseColor("#00796B"))
            
            val dateStr = coupon.updatedAt?.take(10)?.replace("-", ".") ?: ""
            if (dateStr.isNotBlank()) {
                holder.tvDate.text = "$dateStr 사용"
                holder.tvDate.visibility = View.VISIBLE
            } else {
                holder.tvDate.visibility = View.GONE
            }
            
            holder.itemView.setOnClickListener(null)
            holder.itemView.isClickable = false
        } else {
            holder.tvStatus.text = "사용 가능"
            holder.tvStatus.setTextColor(Color.parseColor("#888888"))
            holder.tvDate.visibility = View.GONE
            
            holder.itemView.isClickable = true
            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, CouponUseActivity::class.java).apply {
                    putExtra("coupon", coupon)
                }
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = coupons.size
}
