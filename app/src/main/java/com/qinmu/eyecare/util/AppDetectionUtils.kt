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
        "bilibili.game", "perfectworld", "supercell", "moonton", "aligames",
        "netease.g", "netease.onmyoji", "netease.moba", "netease.hyxd", "netease.identity5",
        "netease.sky", "netease.h75", "netease.dunk", "gameloft", "ea.game"
    )

    @Volatile
    private var cachedDefaultLauncherPackage: String? = null

    /**
     * 判断指定包名是否为桌面/系统 UI/Launcher (支持缓存极大降低系统 PackageManager IPC 开销)
     */
    fun isHomeOrLauncher(context: Context, packageName: String?): Boolean {
        if (packageName.isNullOrEmpty()) return true
        val lower = packageName.lowercase()
        if (lower.contains("launcher") || lower.contains("desktop") ||
            lower.contains("systemui") || lower.contains("trebuchet") || lower == "android") {
            return true
        }

        var defaultLauncher = cachedDefaultLauncherPackage
        if (defaultLauncher == null) {
            try {
                val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                    addCategory(android.content.Intent.CATEGORY_HOME)
                }
                val resolveInfo = context.packageManager.resolveActivity(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
                defaultLauncher = resolveInfo?.activityInfo?.packageName
                cachedDefaultLauncherPackage = defaultLauncher
            } catch (e: Exception) {
                // ignore
            }
        }
        return defaultLauncher == packageName
    }

    /**
     * 获取当前前台应用包名（精准判定：捕获真正活跃的前台 Activity，退至桌面或关闭应用时精准切出）
     */
    fun getForegroundPackageName(context: Context): String? {
        if (!PermissionUtils.hasUsageStatsPermission(context)) return null

        try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return null

            val endTime = System.currentTimeMillis()
            val startTime = endTime - 8000 // 8秒时间窗口

            val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
            var lastForegroundPkg: String? = null
            var lastForegroundTime = 0L
            var lastBackgroundTime = 0L
            val event = UsageEvents.Event()

            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                val pkg = event.packageName ?: continue

                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND || 
                    event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    if (event.timeStamp >= lastForegroundTime) {
                        lastForegroundPkg = pkg
                        lastForegroundTime = event.timeStamp
                    }
                } else if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND || 
                           event.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                    if (pkg == lastForegroundPkg) {
                        lastBackgroundTime = event.timeStamp
                    }
                }
            }

            // 如果最后一次退后台的时间晚于切前台的时间，说明该应用已不在前台
            if (lastBackgroundTime > lastForegroundTime) {
                return null
            }

            // 过滤桌面、系统 UI 及应用自身
            if (lastForegroundPkg == null || 
                lastForegroundPkg == context.packageName || 
                isHomeOrLauncher(context, lastForegroundPkg)) {
                return null
            }

            return lastForegroundPkg
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
