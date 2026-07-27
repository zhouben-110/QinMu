package com.qinmu.eyecare.util

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

object AppDetectionUtils {

    // 常见会议软件包名前缀/关键字
    private val MEETING_PACKAGE_KEYWORDS = listOf(
        "com.tencent.wemeet",       // 腾讯会议
        "com.alibaba.android.rimet", // 钉钉
        "com.ss.android.lark",       // 飞书
        "us.zoom.videomeetings",    // Zoom
        "com.microsoft.teams",      // Teams
        "com.tencent.wework",       // 企业微信/微会议
        "com.cisco.webex",          // Webex
        "wemeet", "voovmeeting", "welink", "feishu", "meeting", "conference"
    )

    // 常见游戏包名特征关键字
    private val GAME_PACKAGE_KEYWORDS = listOf(
        "tmgp", "mihoyo", "hypergryph", "yostar", "proxima", "pubg", "codm",
        "lol", "genshin", "honorofkings", "epicsaga", "ea.gp", "riotgames",
        "netease", "bilibili.game", "perfectworld", "supercell", "moonton", "aligames"
    )

    /**
     * 获取当前前台应用包名
     */
    fun getForegroundPackageName(context: Context): String? {
        if (!PermissionUtils.hasUsageStatsPermission(context)) return null

        try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return null

            val endTime = System.currentTimeMillis()
            val startTime = endTime - 10000 // 10秒窗口

            val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
            var currentPackage: String? = null
            val event = UsageEvents.Event()

            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    currentPackage = event.packageName
                }
            }
            return currentPackage
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private val gamePackageCache = java.util.concurrent.ConcurrentHashMap<String, Boolean>()

    /**
     * 判断指定包名是否为游戏 (使用 ConcurrentHashMap 缓存提升性能，极低功耗)
     */
    fun isGameApp(context: Context, packageName: String?): Boolean {
        if (packageName.isNullOrEmpty()) return false
        gamePackageCache[packageName]?.let { return it }

        val pkg = packageName.lowercase()

        // 1. 关键字快速匹配
        if (GAME_PACKAGE_KEYWORDS.any { pkg.contains(it) }) {
            gamePackageCache[packageName] = true
            return true
        }

        // 2. Android 官方 ApplicationInfo Category 分类检查
        try {
            val pm = context.packageManager
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (appInfo.category == ApplicationInfo.CATEGORY_GAME) {
                    gamePackageCache[packageName] = true
                    return true
                }
            }
            @Suppress("DEPRECATION")
            if ((appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0) {
                gamePackageCache[packageName] = true
                return true
            }
        } catch (e: Exception) {
            // 包名未找到或异常
        }
        gamePackageCache[packageName] = false
        return false
    }

    /**
     * 判断指定包名是否为会议软件
     */
    fun isMeetingApp(packageName: String?): Boolean {
        if (packageName.isNullOrEmpty()) return false
        val pkg = packageName.lowercase()
        return MEETING_PACKAGE_KEYWORDS.any { pkg.contains(it) }
    }
}
