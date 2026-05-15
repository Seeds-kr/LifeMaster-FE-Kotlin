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

        if (coupon.couponStatus == "USE") {
            holder.tvStatus.text = "사용 중"
            holder.tvStatus.setTextColor(Color.parseColor("#00796B")) // 사용 가능할 때와 같은 색상 혹은 강조색
        } else {
            holder.tvStatus.text = "사용 가능"
            holder.tvStatus.setTextColor(Color.parseColor("#888888")) // 사용 전을 오히려 연하게 표시하거나 기호에 맞게 조정
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, CouponUseActivity::class.java).apply {
                putExtra("coupon", coupon)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = coupons.size
}
