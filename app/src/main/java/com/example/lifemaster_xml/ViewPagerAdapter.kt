package com.example.lifemaster_xml

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.lifemaster_xml.community.CommunityFragment
import com.example.lifemaster_xml.group.GroupFragment
import com.example.lifemaster_xml.home.HomeFragment
import com.example.lifemaster_xml.total.TotalFragment

class ViewPagerAdapter(activity: FragmentActivity): FragmentStateAdapter(activity) {
    private val fragments = listOf(HomeFragment(), GroupFragment(), CommunityFragment(), TotalFragment())

    // 프래그먼트 항목 개수 반환
    override fun getItemCount(): Int = fragments.size

    // 항목을 구성하는 프래그먼트 객체 반환
    override fun createFragment(position: Int): Fragment = fragments[position]
}