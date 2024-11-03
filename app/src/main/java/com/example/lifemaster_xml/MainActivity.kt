package com.example.lifemaster_xml

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import com.example.lifemaster_xml.data.Datas
import com.example.lifemaster_xml.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        codeCacheDir.setReadOnly()

        binding.viewpager.adapter = ViewPagerAdapter(this@MainActivity)
        // tabLayout 과 viewpager 연동
        TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->
            tab.text = Datas.tabLayoutTexts[position]
            tab.icon = ResourcesCompat.getDrawable(resources, Datas.tabLayoutIcons[position], null)
        }.attach()
    }
}