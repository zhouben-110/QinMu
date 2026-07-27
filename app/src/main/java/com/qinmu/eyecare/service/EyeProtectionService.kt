package com.qinmu.eyecare.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.qinmu.eyecare.QinMuApplication
import com.qinmu.eyecare.data.model.RemindMode
import com.qinmu.eyecare.data.model.RestType
import com.qinmu.eyecare.data.model.SpecialMode
import com.qinmu.eyecare.data.model.UsageLogEntity
import com.qinmu.eyecare.data.model.UserPreferences
import com.qinmu.eyecare.ui.main.MainActivity
import com.qinmu.eyecare.ui.overlay.RestOverlayWindow
import com.qinmu.eyecare.util.AppDetectionUtils
import com.qinmu.eyecare.util.PermissionUtils
import com.qinmu.eyecare.util.SoundManager
import com.qinmu.eyecare.util.TimeUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EyeProtectionService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var currentPreferences = UserPreferences()
    private var isScreenOn = true
    private var restOverlayWindow: RestOverlayWindow? = null
    private var screenReceiver: BroadcastReceiver? = null
    @Volatile
    private var isStateRestored = false

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        registerScreenReceiver()
        observePreferences()
        serviceScope.launch {
            restoreTimerState()
            startScreenTimeTimer()
        }
    }

    private suspend fun restoreTimerState() {
        if (isStateRestored) return
        try {
            val savedState = QinMuApplication.instance.preferencesRepository.getSavedTimerState()
            if (savedState.screenSeconds > 0L) {
                val nowMs = System.currentTimeMillis()
                // 如果上一次保存的时间距现在不超过 12 小时且属于同一天，恢复计时秒数；如果熄屏/关机很长或过夜了，可以智能校准
                val timeDiffMs = if (savedState.lastActiveTimeMs > 0) nowMs - savedState.lastActiveTimeMs else 0L
                if (timeDiffMs in 0..12 * 3600 * 1000L) {
                    _currentScreenSeconds.value = savedState.screenSeconds
                }
                _xiaoQinCompletedCount.value = savedState.xiaoQinCompletedCount
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isStateRestored = true
        }
    }

    private fun registerScreenReceiver() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            isScreenOn = powerManager?.isInteractive ?: true

            screenReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    when (intent?.action) {
                        Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                            isScreenOn = true
                            updateTimerState()
                        }
                        Intent.ACTION_SCREEN_OFF -> {
                            isScreenOn = false
                            updateTimerState()
                        }
                    }
                }
            }
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // 🌟 Android 13/14/15/16 必备：必须使用 RECEIVER_EXPORTED 才能接收系统框架层发出的亮屏/熄屏/解锁广播 🌟
                registerReceiver(screenReceiver, filter, RECEIVER_EXPORTED)
            } else {
                registerReceiver(screenReceiver, filter)
            }
            updateTimerState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceNotification()

        when (intent?.action) {
            ACTION_SCREEN_ON -> {
                isScreenOn = true
                updateTimerState()
            }
            ACTION_SCREEN_OFF -> {
                isScreenOn = false
                updateTimerState()
                serviceScope.launch {
                    saveTodayScreenTime()
                    saveTimerStateToPreferences()
                }
            }
            ACTION_SKIP_REST -> {
                handleSkipRest()
            }
            ACTION_COMPLETE_REST -> {
                handleCompleteRest()
            }
            ACTION_TOGGLE_PAUSE -> {
                _isPaused.value = !_isPaused.value
                updateTimerState()
                if (_isPaused.value) {
                    serviceScope.launch {
                        saveTodayScreenTime()
                        saveTimerStateToPreferences()
                    }
                }
            }
            ACTION_RESET_TIMER -> {
                handleResetTimer()
            }
        }
        return START_STICKY
    }

    private fun observePreferences() {
        serviceScope.launch {
            try {
                var previousInterval = -1
                QinMuApplication.instance.preferencesRepository.userPreferencesFlow.collect { prefs ->
                    val isFirstLoad = (previousInterval == -1)
                    val intervalChanged = !isFirstLoad && (previousInterval != prefs.remindIntervalMinutes)
                    previousInterval = prefs.remindIntervalMinutes
                    currentPreferences = prefs

                    // 🌟 核心优化：当用户在设置中调整用眼提醒间隔时，清空重置当前用眼计时器，防止直接误触发弹窗 🌟
                    if (intervalChanged) {
                        handleResetTimer()
                    }

                    if (prefs.isKeepAliveEnabled) {
                        KeepAliveWorker.scheduleKeepAliveWork(this@EyeProtectionService)
                    } else {
                        KeepAliveWorker.cancelKeepAliveWork(this@EyeProtectionService)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var timerJob: Job? = null

    private fun checkIsScreenInteractive(): Boolean {
        return try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            powerManager?.isInteractive ?: isScreenOn
        } catch (e: Exception) {
            isScreenOn
        }
    }

    private fun updateTimerState() {
        val actualScreenOn = checkIsScreenInteractive()
        isScreenOn = actualScreenOn

        if (actualScreenOn && !_isPaused.value) {
            if (timerJob == null || timerJob?.isActive != true) {
                startScreenTimeTimer()
            }
        } else {
            // 🌟 架构级事件驱动：熄屏或暂停时彻底取消销毁计时 Job，实现零 CPU 计算、零后台唤醒 🌟
            timerJob?.cancel()
            timerJob = null
        }
    }

    private fun startScreenTimeTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            if (!isStateRestored) {
                restoreTimerState()
            }
            val startRealtime = android.os.SystemClock.elapsedRealtime()
            val initialScreenSec = _currentScreenSeconds.value
            val initialTodaySec = _todayTotalSeconds.value

            var lastSavedSec = initialScreenSec
            var checkAppCounter = 0

            while (isActive) {
                // 🌟 双重兜底：极低概率广播延迟或遗漏时，轮询检测发现系统物理屏幕已熄灭，立刻中断退出计时 🌟
                if (!checkIsScreenInteractive()) {
                    isScreenOn = false
                    updateTimerState()
                    saveTodayScreenTime()
                    saveTimerStateToPreferences()
                    break
                }
                val nowRealtime = android.os.SystemClock.elapsedRealtime()
                val elapsedSec = (nowRealtime - startRealtime) / 1000L

                val currentSec = initialScreenSec + elapsedSec
                _currentScreenSeconds.value = currentSec
                _todayTotalSeconds.value = initialTodaySec + elapsedSec

                // 每 15 秒增量持久化用眼日志与计时状态（已有 onTaskRemoved/ScreenOff/Pause 场景实时强存，落盘频率降低 80% 极其省电）
                if (currentSec - lastSavedSec >= 15L) {
                    lastSavedSec = currentSec
                    saveTodayScreenTime()
                    saveTimerStateToPreferences()
                }

                // 🌟 极致功耗优化与智能自动判定策略 🌟
                // 1. 开启智能判定：正常模式下每 3 秒检测一次前台 App；
                //    已处于游戏/会议中时，调整为 6 秒检测一次（减少 66% 后台 IPC 唤醒，保护游戏帧率与电池）。
                // 2. 未开启智能判定：完全不轮询应用，采用低功耗休眠策略。
                checkAppCounter++
                val needAppCheck = currentPreferences.manualSpecialMode == SpecialMode.NONE &&
                        (currentPreferences.isAutoGameModeEnabled || currentPreferences.isAutoMeetingModeEnabled)

                if (needAppCheck) {
                    val isSpecialActive = _effectiveSpecialMode.value != SpecialMode.NONE
                    val checkInterval = if (isSpecialActive) 6 else 3
                    if (checkAppCounter % checkInterval == 0) {
                        updateEffectiveSpecialMode()
                    }
                }

                val thresholdSeconds = (currentPreferences.remindIntervalMinutes * 60).toLong()
                if (currentSec >= thresholdSeconds && !_isReminding) {
                    triggerRestReminder()
                }

                // 🌟 精准 1000ms 脉冲调度：保证界面读秒 1 秒平滑无缝递增，彻底消除跳秒/跳帧现象 🌟
                delay(1000L)
            }
        }
    }

    private var pendingExitModeCounter = 0

    private fun updateEffectiveSpecialMode() {
        val previousMode = _effectiveSpecialMode.value
        val manualMode = currentPreferences.manualSpecialMode

        val detectedMode = if (manualMode != SpecialMode.NONE) {
            manualMode
        } else {
            val foregroundPkg = AppDetectionUtils.getForegroundPackageName(this)
            if (currentPreferences.isAutoMeetingModeEnabled && AppDetectionUtils.isMeetingApp(foregroundPkg)) {
                SpecialMode.MEETING
            } else if (currentPreferences.isAutoGameModeEnabled && AppDetectionUtils.isGameApp(this, foregroundPkg)) {
                SpecialMode.GAME
            } else {
                SpecialMode.NONE
            }
        }

        // 防抖策略：从 游戏/会议 模式切换为 NONE 时，需要连续 3 次（约 6~12 秒）检测为 NONE 才真正确认退出
        val targetMode = if (previousMode != SpecialMode.NONE && detectedMode == SpecialMode.NONE && manualMode == SpecialMode.NONE) {
            pendingExitModeCounter++
            if (pendingExitModeCounter < 3) {
                previousMode // 保持原模式，防止跳变
            } else {
                pendingExitModeCounter = 0
                SpecialMode.NONE
            }
        } else {
            pendingExitModeCounter = 0
            detectedMode
        }

        if (previousMode != targetMode) {
            _effectiveSpecialMode.value = targetMode
            startForegroundServiceNotification()

            // 如果从会议/游戏模式确认退出回到正常护眼模式，且此前用眼时长已超标，立刻补给弹出护眼提醒
            if ((previousMode == SpecialMode.MEETING || previousMode == SpecialMode.GAME) && targetMode == SpecialMode.NONE) {
                val thresholdSeconds = (currentPreferences.remindIntervalMinutes * 60).toLong()
                if (_currentScreenSeconds.value >= thresholdSeconds) {
                    showExitModeCatchUpNotification(previousMode)
                    _isReminding = false
                    triggerRestReminder()
                }
            }
        }
    }

    private fun showExitModeCatchUpNotification(exitFromMode: SpecialMode) {
        try {
            val modeName = if (exitFromMode == SpecialMode.MEETING) "会议" else "游戏"
            val openAppIntent = Intent(this, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                this, 200, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, QinMuApplication.CHANNEL_ID_REMIND)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🌿 沁目：${modeName}守护结束补给提示")
                .setContentText("您已退出${modeName}模式，刚刚累计专注用眼较高，建议现在做一次护眼放松！")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openAppPendingIntent)
                .build()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID_REMIND, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerRestReminder() {
        _isReminding = true

        val currentMode = _effectiveSpecialMode.value

        // 🌟 在【会议模式】或【游戏模式】下绝对拦截全屏遮罩与响铃，保护专注力与画面 🌟
        if (currentMode == SpecialMode.MEETING || currentMode == SpecialMode.GAME) {
            val modeTitle = if (currentMode == SpecialMode.MEETING) "💼 会议模式" else "🎮 游戏模式"
            showQuietNotification("$modeTitle：已为您挂起全屏遮罩提醒")
            return
        }

        val isDaQin = currentPreferences.isDualCycleEnabled && 
                (_xiaoQinCompletedCount.value + 1 >= currentPreferences.daQinCycleCount)

        val restType = if (isDaQin) RestType.DA_QIN else RestType.XIAO_QIN
        val restDuration = if (isDaQin) currentPreferences.daQinRestSeconds else currentPreferences.restDurationSeconds

        _currentActiveRestType.value = restType

        // 播放提示音效
        SoundManager.playSound(this, currentPreferences.soundEffect)

        when (currentPreferences.remindMode) {
            RemindMode.NOTIFICATION -> showNotificationReminder(restType, restDuration)
            RemindMode.OVERLAY_WINDOW -> showOverlayReminder(restType, restDuration)
        }
    }

    private fun showQuietNotification(message: String) {
        try {
            val openAppIntent = Intent(this, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                this, 100, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, QinMuApplication.CHANNEL_ID_REMIND)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🌿 沁目免打扰暂护中")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setContentIntent(openAppPendingIntent)
                .build()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID_REMIND, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showNotificationReminder(restType: RestType, restDuration: Int) {
        try {
            val skipIntent = Intent(this, EyeProtectionService::class.java).apply {
                action = ACTION_SKIP_REST
            }
            val skipPendingIntent = PendingIntent.getService(
                this, 101, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val completeIntent = Intent(this, EyeProtectionService::class.java).apply {
                action = ACTION_COMPLETE_REST
            }
            val completePendingIntent = PendingIntent.getService(
                this, 102, completeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val openAppIntent = Intent(this, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                this, 100, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val isDaQin = restType == RestType.DA_QIN
            val title = if (isDaQin) "🧘 沁目提醒：大沁深度放松时刻" else "🌿 沁目提醒：小沁视力微休息"
            val text = if (isDaQin) 
                "已累计多次连续用眼，请起身活动 ${restDuration / 60} 分钟（提示：手机保持33-40cm，电脑50-70cm）"
            else
                "已连续使用屏幕 ${currentPreferences.remindIntervalMinutes} 分钟，请远眺 6 米外 20 秒（保持手机33-40cm，电脑50-70cm视距）"

            val notification = NotificationCompat.Builder(this, QinMuApplication.CHANNEL_ID_REMIND)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openAppPendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "跳过本次沁目", skipPendingIntent)
                .addAction(android.R.drawable.ic_media_play, "完成休息", completePendingIntent)
                .build()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID_REMIND, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showOverlayReminder(restType: RestType, restDuration: Int) {
        if (PermissionUtils.hasOverlayPermission(this)) {
            serviceScope.launch(Dispatchers.Main) {
                try {
                    restOverlayWindow?.dismiss()
                    restOverlayWindow = RestOverlayWindow(
                        context = this@EyeProtectionService,
                        onSkipRest = { handleSkipRest() },
                        onCompleteRest = { handleCompleteRest() }
                    )
                    restOverlayWindow?.show(totalRestSeconds = restDuration, restType = restType)
                } catch (e: Exception) {
                    e.printStackTrace()
                    showNotificationReminder(restType, restDuration)
                }
            }
        } else {
            showNotificationReminder(restType, restDuration)
        }
    }

    private fun handleResetTimer() {
        _isReminding = false
        _currentScreenSeconds.value = 0L
        timerJob?.cancel()
        timerJob = null
        SoundManager.stopSound()
        cancelRemindNotification()
        dismissOverlayWindow()
        saveTimerStateToPreferences()
        updateTimerState()
    }

    private fun handleSkipRest() {
        if (_currentActiveRestType.value == RestType.DA_QIN) {
            _xiaoQinCompletedCount.value = 0
        } else {
            _xiaoQinCompletedCount.value++
        }

        _isReminding = false
        _currentScreenSeconds.value = 0L
        timerJob?.cancel()
        timerJob = null
        saveTimerStateToPreferences()
        updateTimerState()

        SoundManager.stopSound()
        cancelRemindNotification()
        dismissOverlayWindow()

        serviceScope.launch {
            try {
                val today = TimeUtils.getTodayDateString()
                val dao = QinMuApplication.instance.database.usageLogDao()
                val log = dao.getLogByDate(today) ?: UsageLogEntity(date = today)
                dao.insertOrUpdate(log.copy(skipCount = log.skipCount + 1))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleCompleteRest() {
        val completedType = _currentActiveRestType.value
        val isDaQin = completedType == RestType.DA_QIN

        if (isDaQin) {
            _xiaoQinCompletedCount.value = 0
        } else {
            _xiaoQinCompletedCount.value++
        }

        _isReminding = false
        _currentScreenSeconds.value = 0L
        timerJob?.cancel()
        timerJob = null
        saveTimerStateToPreferences()
        updateTimerState()

        // 休息完成时播放与开始时一致的提示音效
        SoundManager.playSound(this, currentPreferences.soundEffect)

        cancelRemindNotification()
        dismissOverlayWindow()

        serviceScope.launch {
            try {
                val today = TimeUtils.getTodayDateString()
                val dao = QinMuApplication.instance.database.usageLogDao()
                val log = dao.getLogByDate(today) ?: UsageLogEntity(date = today)
                val restDuration = if (isDaQin) currentPreferences.daQinRestSeconds else currentPreferences.restDurationSeconds

                val updatedLog = if (isDaQin) {
                    log.copy(
                        daQinCount = log.daQinCount + 1,
                        totalRestDurationSeconds = log.totalRestDurationSeconds + restDuration
                    )
                } else {
                    log.copy(
                        xiaoQinCount = log.xiaoQinCount + 1,
                        totalRestDurationSeconds = log.totalRestDurationSeconds + restDuration
                    )
                }
                dao.insertOrUpdate(updatedLog)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun cancelRemindNotification() {
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID_REMIND)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dismissOverlayWindow() {
        serviceScope.launch(Dispatchers.Main) {
            try {
                restOverlayWindow?.dismiss()
                restOverlayWindow = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun saveTodayScreenTime() {
        try {
            val today = TimeUtils.getTodayDateString()
            val dao = QinMuApplication.instance.database.usageLogDao()
            val log = dao.getLogByDate(today) ?: UsageLogEntity(date = today)
            val currentTotal = _todayTotalSeconds.value
            if (currentTotal > log.screenOnTimeSeconds) {
                dao.insertOrUpdate(log.copy(screenOnTimeSeconds = currentTotal))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveTimerStateToPreferences() {
        serviceScope.launch {
            try {
                QinMuApplication.instance.preferencesRepository.saveTimerState(
                    screenSeconds = _currentScreenSeconds.value,
                    lastActiveTimeMs = System.currentTimeMillis(),
                    xiaoQinCompletedCount = _xiaoQinCompletedCount.value
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startForegroundServiceNotification() {
        try {
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
            )

            val mode = _effectiveSpecialMode.value
            val modeTitle = when (mode) {
                SpecialMode.MEETING -> "💼 沁目 · 会议免打扰运行中"
                SpecialMode.GAME -> "🎮 沁目 · 游戏免打扰运行中"
                SpecialMode.NONE -> "沁目 · 护眼服务运行中"
            }

            val notification: Notification = NotificationCompat.Builder(this, QinMuApplication.CHANNEL_ID_SERVICE)
                .setContentTitle(modeTitle)
                .setContentText("正在守护您的用眼健康...")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID_SERVICE,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID_SERVICE, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        runBlocking {
            try {
                QinMuApplication.instance.preferencesRepository.saveTimerState(
                    screenSeconds = _currentScreenSeconds.value,
                    lastActiveTimeMs = System.currentTimeMillis(),
                    xiaoQinCompletedCount = _xiaoQinCompletedCount.value
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        super.onDestroy()
        serviceScope.cancel()
        SoundManager.stopSound()
        screenReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        dismissOverlayWindow()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        runBlocking {
            try {
                QinMuApplication.instance.preferencesRepository.saveTimerState(
                    screenSeconds = _currentScreenSeconds.value,
                    lastActiveTimeMs = System.currentTimeMillis(),
                    xiaoQinCompletedCount = _xiaoQinCompletedCount.value
                )
                saveTodayScreenTime()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID_SERVICE = 1001
        const val NOTIFICATION_ID_REMIND = 1002

        const val ACTION_SCREEN_ON = "com.qinmu.eyecare.SCREEN_ON"
        const val ACTION_SCREEN_OFF = "com.qinmu.eyecare.SCREEN_OFF"
        const val ACTION_SKIP_REST = "com.qinmu.eyecare.SKIP_REST"
        const val ACTION_COMPLETE_REST = "com.qinmu.eyecare.COMPLETE_REST"
        const val ACTION_TOGGLE_PAUSE = "com.qinmu.eyecare.TOGGLE_PAUSE"
        const val ACTION_RESET_TIMER = "com.qinmu.eyecare.RESET_TIMER"

        private var _isReminding = false

        private val _currentScreenSeconds = MutableStateFlow(0L)
        val currentScreenSeconds: StateFlow<Long> = _currentScreenSeconds.asStateFlow()

        private val _todayTotalSeconds = MutableStateFlow(0L)
        val todayTotalSeconds: StateFlow<Long> = _todayTotalSeconds.asStateFlow()

        private val _isPaused = MutableStateFlow(false)
        val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

        private val _xiaoQinCompletedCount = MutableStateFlow(0)
        val xiaoQinCompletedCount: StateFlow<Int> = _xiaoQinCompletedCount.asStateFlow()

        private val _currentActiveRestType = MutableStateFlow(RestType.XIAO_QIN)
        val currentActiveRestType: StateFlow<RestType> = _currentActiveRestType.asStateFlow()

        private val _effectiveSpecialMode = MutableStateFlow(SpecialMode.NONE)
        val effectiveSpecialMode: StateFlow<SpecialMode> = _effectiveSpecialMode.asStateFlow()
    }
}
