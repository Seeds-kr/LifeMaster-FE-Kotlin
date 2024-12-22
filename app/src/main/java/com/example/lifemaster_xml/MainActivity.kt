package com.example.lifemaster_xml

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.lifemaster_xml.community.CommunityFragment
import com.example.lifemaster_xml.data.SharedData
import com.example.lifemaster_xml.databinding.ActivityMainBinding
import com.example.lifemaster_xml.group.GroupFragment
import com.example.lifemaster_xml.home.HomeFragment
import com.example.lifemaster_xml.total.detox.fragment.DetoxFragment

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Activity_Main", "onCreate")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        codeCacheDir.setReadOnly()
        if(savedInstanceState == null) {
            binding.navigation.selectedItemId = R.id.action_home
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, HomeFragment()).commit()
        }

        // 할일 목록 리스트 불러오기
        val sharedPreferences = getSharedPreferences("todo_items", Context.MODE_PRIVATE)
        SharedData.todoItems.clear() // [?] todoItems 에 있는 값은 어떤 생명주기에서 사라지는걸까?
        SharedData.todoItems.addAll(sharedPreferences.all.values as Collection<String>)

        // [!] setOnClickListener 가 아니라 setOnItemSelectedListener 이기 때문에, 하단 개별 뷰를 누르지 않더라도 selectedItemId 를 해당 뷰로 바꿔주면 동작한다.
        binding.navigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.action_home -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, HomeFragment()).commit()
                    true
                }
                R.id.action_group -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, GroupFragment()).commit()
                    true
                }
                R.id.action_community -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, CommunityFragment()).commit()
                    true
                }
                R.id.action_total -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, DetoxFragment()).commit()
                    true
                }
                else -> false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("Activity_Main", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Activity_Main", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("Activity_Main", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Activity_Main", "onStop")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("Activity_Main", "onRestart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Activity_Main", "onDestroy")
    }
}