package com.qinmu.eyecare.util

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings

object PermissionUtils {

    /**
     * 检查是否有悬浮窗 (Overlay) 权限
     */
    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * 跳转悬浮窗授权页面
     */
    fun requestOverlayPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * 检查是否有应用使用情况统计 (Usage Access) 权限
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * 跳转应用使用情况授权页面
     */
    fun requestUsageStatsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * 检查是否已开启【允许后台活动】(忽略电池优化)
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            return powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
        }
        return true
    }

    /**
     * 跳转电池优化设置页面 (引导开启【允许后台活动】)
     */
    fun requestIgnoreBatteryOptimizations(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    /**
     * 跳转各厂商手机系统【自启动权限 / 后台弹窗权限】设置页面
     */
    fun requestAutoStartPermission(context: Context) {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val intentList = mutableListOf<Intent>()

        try {
            when {
                manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> {
                    // 小米 / 红米 MIUI & HyperOS
                    intentList.add(Intent().apply {
                        setClassName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
                    })
                }
                manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                    // 华为 / 荣耀 EMUI & HarmonyOS
                    intentList.add(Intent().apply {
                        setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
                    })
                    intentList.add(Intent().apply {
                        setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
                    })
                    intentList.add(Intent().apply {
                        setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")
                    })
                }
                manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus") -> {
                    // OPPO / 极真 / 加一 ColorOS
                    intentList.add(Intent().apply {
                        setClassName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
                    })
                    intentList.add(Intent().apply {
                        setClassName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")
                    })
                    intentList.add(Intent().apply {
                        setClassName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")
                    })
                }
                manufacturer.contains("vivo") || manufacturer.contains("iqoo") -> {
                    // Vivo / iQOO FuntouchOS / OriginOS
                    intentList.add(Intent().apply {
                        setClassName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")
                    })
                    intentList.add(Intent().apply {
                        setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
                    })
                    intentList.add(Intent().apply {
                        setClassName("com.iqoo.secure", "com.iqoo.secure.safeguard.PurifyActivity")
                    })
                }
                manufacturer.contains("meizu") -> {
                    // 魅族 Flyme
                    intentList.add(Intent().apply {
                        setClassName("com.meizu.safe", "com.meizu.safe.permission.AutoStartActivity")
                    })
                }
                manufacturer.contains("samsung") -> {
                    // 三星 OneUI
                    intentList.add(Intent().apply {
                        setClassName("com.samsung.android.looper", "com.samsung.android.sm.ui.battery.BatteryActivity")
                    })
                }
            }

            // 应用详情通用降级兜底
            intentList.add(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            })

            for (intent in intentList) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (context.packageManager.resolveActivity(intent, 0) != null) {
                    context.startActivity(intent)
                    return
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 兜底打开应用详情页
            try {
                val detailIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(detailIntent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}
