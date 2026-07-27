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
     * 获取当前前台应用包名（更加精准的判定：过滤系统/桌面/SystemUI，返回用户真正交互的前台应用）
     */
    fun getForegroundPackageName(context: Context): String? {
        if (!PermissionUtils.hasUsageStatsPermission(context)) return null

        try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return null

            val endTime = System.currentTimeMillis()
            val startTime = endTime - 15000 // 15秒时间窗口

            val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
            var currentPackage: String? = null
            val event = UsageEvents.Event()

            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                // 捕获切前台或切换 Activity 的事件
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND || 
                    event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    val pkg = event.packageName
                    // 过滤掉系统 UI、输入法、桌面以及沁目自身短暂停留引起的误判
                    if (!isIgnoredPackage(pkg, context.packageName)) {
                        currentPackage = pkg
                    }
                }
            }
            
            // 备用方案：如果 UsageEvents 查不出，使用 queryUsageStats 按 lastTimeUsed 降序兜底
            if (currentPackage == null) {
                val stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    endTime
                )
                if (!stats.isNullOrEmpty()) {
                    currentPackage = stats
                        .filter { !isIgnoredPackage(it.packageName, context.packageName) }
                        .maxByOrNull { it.lastTimeUsed }
                        ?.packageName
                }
            }

            return currentPackage
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun isIgnoredPackage(pkgName: String, ownPackage: String): Boolean {
        if (pkgName == ownPackage) return true
        val lower = pkgName.lowercase()
        return lower.contains("systemui") ||
               lower.contains("launcher") ||
               lower.contains("inputmethod") ||
               lower.contains("nexuslauncher") ||
               lower.contains("trebuchet") ||
               lower == "android"
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
