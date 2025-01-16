package com.example.lifemaster

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.lifemaster.community.CommunityFragment
import com.example.lifemaster.data.SharedData
import com.example.lifemaster.databinding.ActivityMainBinding
import com.example.lifemaster.group.GroupFragment
import com.example.lifemaster.home.HomeFragment
import com.example.lifemaster.total.detox.fragment.DetoxFragment
import com.example.lifemaster.total.detox.model.DetoxTargetApp
import com.example.lifemaster.total.detox.viewmodel.DetoxCommonViewModel
import com.example.lifemaster.total.detox.viewmodel.DetoxRepeatLockViewModel
import com.example.lifemaster.total.detox.viewmodel.DetoxTimeLockViewModel
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val detoxCommonViewModel: DetoxCommonViewModel by viewModels()
    private val detoxRepeatLockViewModel: DetoxRepeatLockViewModel by viewModels()
    private val detoxTimeLockViewModel: DetoxTimeLockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        codeCacheDir.setReadOnly()

        if(savedInstanceState == null) {
            binding.navigation.selectedItemId = R.id.action_total
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, DetoxFragment()).commit()
        }

        fetchApplications()

        // 차단할 앱 서비스 기능을 위한 접근성 권한 관련 코드
        val isAccessibilityPermitted = checkAccessibilityPermissions()
        if(!isAccessibilityPermitted) {
            AlertDialog.Builder(this).apply {
                setTitle("접근성 권한 허용 필요")
                setMessage("앱을 사용하기 위해 접근성 권한이 필요합니다.")
                setPositiveButton("허용") { _, _ ->
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
                setCancelable(false)
                create().show()
            }
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

    private fun fetchApplications() {

        val applicationList = arrayListOf<DetoxTargetApp>()
        val totalApps = packageManager.getInstalledApplications(0) // manifest 에 <queries> 에 intent 필터로 따로 추가

        val requiredApps = totalApps.filter { app ->
            val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystemApp = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            !isSystemApp && !isUpdatedSystemApp
        }

        val usageStatsMap = getDailyUsageStats(this)

        for(app in requiredApps) {
            val appName = app.loadLabel(packageManager).toString()
            val appIcon = app.loadUnbadgedIcon(packageManager)
            val appPackageName = app.packageName
            val accumulatedTime = usageStatsMap[app.packageName] ?: 0L
            detoxCommonViewModel.totalAccumulatedAppUsageTimes += accumulatedTime
            applicationList.add(DetoxTargetApp(appIcon, appName, appPackageName, accumulatedTime))
        }

        detoxRepeatLockViewModel.blockServiceApplications = ArrayList(applicationList)
        detoxRepeatLockViewModel.repeatLockTargetApplications = ArrayList(applicationList)
        detoxTimeLockViewModel.allowServiceApplications = ArrayList(applicationList)
    }

    private fun getDailyUsageStats(context: Context): Map<String, Long> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        return usageStats.associate { it.packageName to it.totalTimeInForeground }
    }

    // 접근성 권한을 확인하는 메서드
    private fun checkAccessibilityPermissions(): Boolean {
        val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)

        for(serviceInfo in enabledServices) {
            if(serviceInfo.resolveInfo.serviceInfo.packageName == application.packageName) return true
        }

        return false
    }

    // 앱 사용 시간에 대한 설정 화면 이동
    private fun requestPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
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