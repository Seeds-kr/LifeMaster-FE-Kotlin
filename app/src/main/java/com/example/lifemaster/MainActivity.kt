package com.example.lifemaster

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.lifemaster.community.CommunityFragment
import com.example.lifemaster.data.SharedData
import com.example.lifemaster.databinding.ActivityMainBinding
import com.example.lifemaster.group.GroupFragment
import com.example.lifemaster.home.HomeFragment
import com.example.lifemaster.total.detox.fragment.DetoxFragment
import com.example.lifemaster.total.detox.model.DetoxTargetApp
import com.example.lifemaster.total.detox.viewmodel.DetoxRepeatLockViewModel
import com.example.lifemaster.total.detox.viewmodel.DetoxTimeLockViewModel

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val detoxRepeatLockViewModel: DetoxRepeatLockViewModel by viewModels()
    private val detoxTimeLockViewModel: DetoxTimeLockViewModel by viewModels()

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

        fetchApplications()

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

    private fun fetchApplications() {

        val applicationList = arrayListOf<DetoxTargetApp>()
        val totalApps = packageManager.getInstalledApplications(0) // manifest 에 <queries> 에 intent 필터로 따로 추가

        val requiredApps = totalApps.filter { app ->
            val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystemApp = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            !isSystemApp && !isUpdatedSystemApp
        }

//        val requiredApps2 = totalApps.filter {
//            it.flags and ApplicationInfo.FLAG_SYSTEM == 0
//        }

        Log.d("app(total)", ""+totalApps.size) // 542
        Log.d("app(required)", ""+requiredApps.size) // 153

        for(app in requiredApps) {
            val appName = app.loadLabel(packageManager).toString()
            val appIcon = app.loadUnbadgedIcon(packageManager)
            applicationList.add(DetoxTargetApp(appIcon, appName))
        }

        detoxRepeatLockViewModel.blockServiceApplications = applicationList
        detoxRepeatLockViewModel.repeatLockTargetApplications = applicationList
        detoxTimeLockViewModel.allowServiceApplications = applicationList
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