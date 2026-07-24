package com.qinmu.eyecare.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import com.qinmu.eyecare.QinMuApplication
import com.qinmu.eyecare.util.PermissionUtils
import kotlinx.coroutines.*

/**
 * 屏幕护眼滤镜悬浮窗服务
 * 拦截蓝光，叠加暖黄色护眼图层，FLAG_NOT_TOUCHABLE 确保不影响手势操作
 */
class FilterOverlayService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var windowManager: WindowManager? = null
    private var filterView: View? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        observePreferences()
    }

    private fun observePreferences() {
        serviceScope.launch {
            QinMuApplication.instance.preferencesRepository.userPreferencesFlow.collect { prefs ->
                withContext(Dispatchers.Main) {
                    if (prefs.isFilterEnabled) {
                        showOrUpdateFilter(prefs.filterColorArgb, prefs.filterAlpha)
                    } else {
                        removeFilter()
                    }
                }
            }
        }
    }

    private fun showOrUpdateFilter(colorArgb: Long, alpha: Float) {
        if (!PermissionUtils.hasOverlayPermission(this)) return

        val colorInt = parseColorWithAlpha(colorArgb, alpha)

        if (filterView == null) {
            val layoutParamsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutParamsType,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )

            filterView = View(this).apply {
                setBackgroundColor(colorInt)
            }

            try {
                windowManager?.addView(filterView, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            filterView?.setBackgroundColor(colorInt)
        }
    }

    private fun removeFilter() {
        filterView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        filterView = null
    }

    private fun parseColorWithAlpha(colorArgb: Long, alpha: Float): Int {
        val baseColor = colorArgb.toInt()
        val alphaInt = (alpha * 255).toInt().coerceIn(0, 255)
        return (alphaInt shl 24) or (baseColor and 0x00FFFFFF)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        removeFilter()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
