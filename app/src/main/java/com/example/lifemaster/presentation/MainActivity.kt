package com.example.lifemaster.presentation

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.lifemaster.R
import com.example.lifemaster.databinding.ActivityMainBinding
import com.example.lifemaster.presentation.home.pomodoro.model.PomodoroItem
import com.example.lifemaster.network.RetrofitInstance
import com.example.lifemaster.presentation.home.sleep.viewmodel.SleepViewModel
import com.example.lifemaster.presentation.home.sleep.viewmodel.SleepViewModelFactory
import com.example.lifemaster.presentation.home.todo.viewmodel.ToDoViewModel
import com.example.lifemaster.presentation.home.todo.model.TodoItem
import com.example.lifemaster.presentation.total.detox.model.DetoxTargetApp
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxCommonViewModel
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxRepeatLockViewModel
import com.example.lifemaster.presentation.total.detox.viewmodel.DetoxTimeLockViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.getValue

class MainActivity : AppCompatActivity() {

    // View 관련 변수
    private lateinit var binding: ActivityMainBinding
    private lateinit var totalApps: MutableList<ApplicationInfo>
    private lateinit var requiredApps: List<ApplicationInfo>
    private var foregroundStartTime: Long = 0L
    private var userToken: String? = ""

    // ViewModel 변수
    private val detoxCommonViewModel: DetoxCommonViewModel by viewModels()
    private val detoxRepeatLockViewModel: DetoxRepeatLockViewModel by viewModels()
    private val detoxTimeLockViewModel: DetoxTimeLockViewModel by viewModels()
    private val toDoViewModel: ToDoViewModel by viewModels()

    // 실시간 UI 변경을 위한 변수
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateRunnable: Runnable

    // 수면 관련 변수
    private var lastUsageTimeBeforeSleep: Long = 0L // 마지막 사용 시간 = 핸드폰 화면을 끈 시간
    private var firstUsageTimeAfterWake: Long? = null // 핸드폰을 처음 킨 시간 (잠금 해제x)
    private val sleepViewModel: SleepViewModel by viewModels {
        SleepViewModelFactory(RetrofitInstance.networkService)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        codeCacheDir.setReadOnly()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val targetFragment = intent.getStringExtra("destination")
        if (targetFragment == "alarm") {
            val time = intent.getLongExtra("time", 0L) // 알람이 울린 시간
            val navController =
                (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment).navController
            navController.navigate(
                R.id.alarmRingsFragment,
                bundleOf("time" to time)
            )
            binding.bottomNavigation.isVisible = false
        }

//        userToken = intent.getStringExtra("user_token")
//        val sharedPreferences = getSharedPreferences("USER_TABLE", MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//        editor.putString("token", userToken)
//        editor.commit()

        updateRunnable = object : Runnable {
            override fun run() {
                val elapsedForegroundTime =
                    SystemClock.elapsedRealtime() - foregroundStartTime // 포그라운드로 전환 이후 누적된 시간
                detoxCommonViewModel.updateTempElapsedForegroundTime(elapsedForegroundTime)
                handler.postDelayed(this, 1000L)
            }
        }

        totalApps = packageManager.getInstalledApplications(0)
        requiredApps = totalApps.filter { app ->
            val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystemApp = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            !isSystemApp && !isUpdatedSystemApp
        }

        if (savedInstanceState == null) binding.bottomNavigation.selectedItemId = R.id.action_home

        fetchApplications()

        setupListeners()

        requestUsageAccessPermission(this) // 사용 용도: 디톡스, 수면시간 측정

        getUserSleepInfo()

//        requestAccessibilityPermission(this)
    }

    // 사용자의 전날 수면 정보를 가져오는 함수
    private fun getUserSleepInfo() {
        // 사용자가 잠든 시간 추적하기
        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager // 1. USAGE_STATS_SERVICE란? UsageStatsManager란?
        val sleepCalendar = Calendar.getInstance().apply {
            // 오늘 날짜
            set(Calendar.HOUR_OF_DAY, 4)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val sleepTrackingEndTime = sleepCalendar.timeInMillis // 04:00

        sleepCalendar.add(Calendar.HOUR_OF_DAY, -8)
        val sleepTrackingStartTime = sleepCalendar.timeInMillis // 20:00

        val sleepEvent = UsageEvents.Event() // 2.

        val sleepUsageEvents = usageStatsManager.queryEvents( // 3.
            sleepTrackingStartTime,
            sleepTrackingEndTime
        )

        while (sleepUsageEvents.hasNextEvent()) {
            sleepUsageEvents.getNextEvent(sleepEvent)
            if (sleepEvent.eventType == UsageEvents.Event.SCREEN_NON_INTERACTIVE) lastUsageTimeBeforeSleep = sleepEvent.timeStamp
        }

        Log.e("SLEEP(NIGHT)", "잠든 시간: ${Date(lastUsageTimeBeforeSleep)}")

        // 사용자가 일어난 시간 추적하기 (화면을 킨 시점)
        val wakeUpCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 5)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val wakeTrackingStartTime = wakeUpCalendar.timeInMillis // 오전 5시

        wakeUpCalendar.add(Calendar.HOUR_OF_DAY, 5) // 오전 10시
        val wakeTrackingEndTime = wakeUpCalendar.timeInMillis

        val wakeEvent = UsageEvents.Event()
        val wakeUsageEvents = usageStatsManager.queryEvents(
            wakeTrackingStartTime,
            wakeTrackingEndTime
        ) // 금일 오전 5시 ~ 금일 오전 10시 사이의 이벤트 조회

        while (wakeUsageEvents.hasNextEvent()) {
            wakeUsageEvents.getNextEvent(wakeEvent)
            if (wakeEvent.eventType == UsageEvents.Event.SCREEN_INTERACTIVE && firstUsageTimeAfterWake == null) {
                firstUsageTimeAfterWake = wakeEvent.timeStamp // 기상 후 처음 핸드폰을 킨 시간 추적
                Log.e("SLEEP(MORNING)", "일어난 시간: ${Date(firstUsageTimeAfterWake ?: 0L)}")
            }
        }

        // FIXME: 오전 12:07 시점 앱 다운 + 로그 값 lastUsageTimeBeforeSleep: 1753973258389, firstUsageTimeAfterWake: null
        // FIXME: 원인 → 자정 이후로 금일 오전 5시 이후부터의 데이터는 존재하지 않기에 null 발생 (= 자정 이후 ~ 오전 5시 이전 앱 들어가면 튕김)

        val sharedPreference = getSharedPreferences("user_sleep_info", MODE_PRIVATE)

        if(lastUsageTimeBeforeSleep == 0L || firstUsageTimeAfterWake == null) {
            // 수면 기록 측정이 제대로 안된 경우
            sharedPreference.edit().putString(LocalDate.now().toString(), "null").apply()
            sleepViewModel.isMeasured = false
        } else {
            // 수면 기록이 측정이 제대로 된 경우
            Log.e("VALUE", "잠든 시간: ${Instant.ofEpochMilli(lastUsageTimeBeforeSleep)}, 일어난 시간: ${Instant.ofEpochMilli(firstUsageTimeAfterWake!!)}")

            sleepViewModel.isMeasured = true

            val shortTimeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            sleepViewModel.sleepTime = shortTimeFormatter.format(lastUsageTimeBeforeSleep) // 01:11
            sleepViewModel.rawSleepTime = lastUsageTimeBeforeSleep
            sleepViewModel.wakeTime = shortTimeFormatter.format(firstUsageTimeAfterWake) // 07:44
            sleepViewModel.rawWakeTime = firstUsageTimeAfterWake

            val longTimeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            val sleepTime = longTimeFormatter.format(lastUsageTimeBeforeSleep) // 01:11:58
            val wakeTime = longTimeFormatter.format(firstUsageTimeAfterWake) // 07:44:56

            val sleepTimeSeconds = sleepTime.split(":")[2].toInt() // ["01", "11", "58"] → "58" → 58
            val wakeTimeSeconds = wakeTime.split(":")[2].toInt() // ["07", "44", "56"] → "56" → 56

            val duration = Duration.ofMillis(firstUsageTimeAfterWake!! - lastUsageTimeBeforeSleep)

            sleepViewModel.sleepDurationHour = duration.toHours().toInt()
            sleepViewModel.sleepDurationMinutes =
                if (sleepTimeSeconds > wakeTimeSeconds) (duration.toMinutes() % 60 + 1).toInt() else (duration.toMinutes() % 60).toInt()

            // 로컬에 수면 정보 저장
            sharedPreference.edit().putString(
                LocalDate.now().toString(),
                "${sleepViewModel.sleepDurationHour}시간 ${sleepViewModel.sleepDurationMinutes}분"
            ).apply()
        }
    }

    override fun onResume() {
        super.onResume()
        foregroundStartTime = SystemClock.elapsedRealtime() // 앱이 포그라운드로 전환된 시간 기록
        updateUsageStats()
        handler.post(updateRunnable)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // 포모도로 → 할일 업데이트
        val updateItem = intent.getParcelableExtra("pomodoro", TodoItem::class.java)
        val todoItemTitle = intent.getStringExtra("todoItemTitle")
        if (todoItemTitle != null) {
            RetrofitInstance.networkService.getPomodoroItems(token = "Bearer $userToken")
                .enqueue(object : Callback<List<PomodoroItem>> {
                    override fun onResponse(
                        call: Call<List<PomodoroItem>?>,
                        response: Response<List<PomodoroItem>?>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.let { pomodoroList ->
                                val pomodoro = pomodoroList.filter { it.taskName == todoItemTitle }
                                val pomodoro25Count = pomodoro.count { it.focusTime == 20 }
                                val pomodoro50Count = pomodoro.count { it.focusTime == 40 }
                                val todoItem =
                                    toDoViewModel.todoItems.value?.find { it.title == todoItemTitle }
                                todoItem?.let {
                                    it.timer25Number = pomodoro25Count
                                    it.timer50Number = pomodoro50Count
                                    toDoViewModel.changeTodoItems(it)
                                }
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<List<PomodoroItem>?>,
                        t: Throwable
                    ) {
                        TODO("Not yet implemented")
                    }

                })
        }
        updateItem?.let { toDoViewModel.changeTodoItems(it) }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }

    // 실제 디바이스에 설치된 어플리케이션을 가져오는 함수
    private fun fetchApplications() {

        val applicationList = arrayListOf<DetoxTargetApp>()
        val usageStatsMap = getDailyUsageStats(this)

        for (app in requiredApps) {
            val appName = app.loadLabel(packageManager).toString()
            val appIcon = app.loadUnbadgedIcon(packageManager)
            val appPackageName = app.packageName
            val accumulatedTime =
                usageStatsMap[app.packageName] ?: 0L // 누적 사용 시간은 실시간으로 변동되지 않음 (리팩토링 필요)
            applicationList.add(DetoxTargetApp(appIcon, appName, appPackageName, accumulatedTime))
        }

        detoxRepeatLockViewModel.blockServiceApplications = ArrayList(applicationList)
        detoxRepeatLockViewModel.repeatLockTargetApplications = ArrayList(applicationList)
        detoxTimeLockViewModel.allowServiceApplications = ArrayList(applicationList)
    }

    // 앱 사용 시간을 실시간으로 업데이트하는 함수 (1초마다 실행됨)
    private fun updateUsageStats() {

        val usageStatsMap = getDailyUsageStats(this)

        var totalUsageTime = 0L

        for (app in requiredApps) {
            val accumulatedUsageTime = usageStatsMap[app.packageName] ?: 0L
//            if(accumulatedUsageTime>0) {
//                Log.d("debugging", "${app.loadLabel(packageManager)}: ${convertLongFormat(accumulatedUsageTime)}")
//            }
            totalUsageTime += accumulatedUsageTime
        }

        detoxCommonViewModel.updateTotalAccumulatedAppUsageTimes(totalUsageTime)
    }

    private fun convertLongFormat(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val remainSeconds = totalSeconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, remainSeconds)
    }

    // 클릭 이벤트 관련 함수
    private fun setupListeners() {
        // [!] setOnClickListener 가 아니라 setOnItemSelectedListener 이기 때문에, 하단 개별 뷰를 누르지 않더라도 selectedItemId 를 해당 뷰로 바꿔주면 동작한다.
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    findNavController(R.id.fragmentContainerView).navigate(R.id.homeFragment)
                    true
                }

                R.id.action_group -> {
                    findNavController(R.id.fragmentContainerView).navigate(R.id.groupFragment)
                    true
                }

                R.id.action_community -> {
                    findNavController(R.id.fragmentContainerView).navigate(R.id.communityFragment)
                    true
                }

                R.id.action_total -> {
                    findNavController(R.id.fragmentContainerView).navigate(R.id.totalFragment)
                    true
                }

                else -> false
            }
        }
    }

    // 자정을 기준으로 하루 앱 사용 시간을 측정하는 함수
    private fun getDailyUsageStats(context: Context): Map<String, Long> {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val eventMap = mutableMapOf<String, Long>()

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()

        var currentForegroundApp: String? = null
        var lastEventTime = 0L

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)

            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    currentForegroundApp = event.packageName
                    lastEventTime = event.timeStamp
                }

                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    if (currentForegroundApp != null && lastEventTime != 0L) {
                        val usageTime = event.timeStamp - lastEventTime
                        eventMap[currentForegroundApp] =
                            (eventMap[currentForegroundApp] ?: 0) + usageTime
                    }
                    currentForegroundApp = null
                    lastEventTime = 0L
                }
            }
        }

        return eventMap
    }

    // 차단 서비스 기능을 위한 접근성 권한 활성화 여부 확인
    private fun isAccessibilityPermitted(context: Context): Boolean {
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices =
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)

        for (serviceInfo in enabledServices) {
            if (serviceInfo.resolveInfo.serviceInfo.packageName == application.packageName) return true
        }

        return false
    }

    private fun requestAccessibilityPermission(context: Context) {
        if (!isAccessibilityPermitted(context)) {
            AlertDialog.Builder(context).apply {
                setTitle("접근성 권한 허용 필요")
                setMessage("앱을 사용하기 위해서는 접근성 권한이 필요합니다.")
                setPositiveButton("허용") { _, _ ->
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
                setCancelable(false)
                create().show()
            }
        }
    }

    // 앱 사용 시간 권한이 없을 시 다이얼로그 띄우고 설정 화면으로 이동하는 함수
    private fun requestUsageAccessPermission(context: Context) {
        if (!checkUsageAccessPermission(context)) {
            val dialog = AlertDialog.Builder(context).apply {
                setTitle("권한 요청 다이얼로그")
                setMessage("사용자의 수면시간 추적을 위해 사용 정보 접근 권한이 필요합니다. 설정에 들어가서 권한을 허용해주세요.")
                setPositiveButton("설정 이동") { _, _ ->
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    startActivity(intent)
                }
                setCancelable(false)
                create()
            }
            dialog.show()
        }
    }

    // 앱 사용 시간 권한 활성 여부를 확인하는 함수
    private fun checkUsageAccessPermission(context: Context): Boolean {
        val appOpsManager = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 이상
            appOpsManager.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            // Android 10 미만
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
}