package com.example.lifemaster.presentation.total.mypage.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lifemaster.R
import com.example.lifemaster.presentation.total.mypage.model.CouponResponse

class CouponAdapter(private val coupons: List<CouponResponse>) :
    RecyclerView.Adapter<CouponAdapter.CouponViewHolder>() {

    class CouponViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCode: TextView = view.findViewById(R.id.tvCouponCode)
        val tvType: TextView = view.findViewById(R.id.tvCouponType)
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
    }

    override fun getItemCount() = coupons.size
}
