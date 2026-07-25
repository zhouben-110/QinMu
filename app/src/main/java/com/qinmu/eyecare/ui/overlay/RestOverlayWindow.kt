package com.qinmu.eyecare.ui.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.qinmu.eyecare.ui.theme.QinMuTheme
import com.qinmu.eyecare.util.TimeUtils
import kotlinx.coroutines.delay

import com.qinmu.eyecare.data.model.RestType

/**
 * 解决 WindowManager 悬浮窗中 ComposeView 缺乏 LifecycleOwner 导致的致命崩溃
 */
private class OverlayLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    fun performCreate() {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun performDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}

/**
 * 强效护眼全屏悬浮遮罩窗口
 */
class RestOverlayWindow(
    private val context: Context,
    private val onSkipRest: () -> Unit,
    private val onCompleteRest: () -> Unit
) {
    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    @SuppressLint("InflateParams")
    fun show(totalRestSeconds: Int, restType: RestType = RestType.XIAO_QIN) {
        if (overlayView != null) return

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

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
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        lifecycleOwner = OverlayLifecycleOwner().apply {
            performCreate()
        }

        overlayView = ComposeView(context).apply {
            lifecycleOwner?.let { owner ->
                setViewTreeLifecycleOwner(owner)
                setViewTreeViewModelStoreOwner(owner)
                setViewTreeSavedStateRegistryOwner(owner)
            }
            setContent {
                QinMuTheme {
                    RestOverlayContent(
                        totalRestSeconds = totalRestSeconds,
                        restType = restType,
                        onSkip = {
                            dismiss()
                            onSkipRest()
                        },
                        onFinish = {
                            dismiss()
                            onCompleteRest()
                        }
                    )
                }
            }
        }

        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dismiss() {
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        lifecycleOwner?.performDestroy()
        lifecycleOwner = null
        overlayView = null
    }
}

@Composable
private fun RestOverlayContent(
    totalRestSeconds: Int,
    restType: RestType,
    onSkip: () -> Unit,
    onFinish: () -> Unit
) {
    var remainingSeconds by remember { mutableStateOf(totalRestSeconds) }

    LaunchedEffect(remainingSeconds) {
        if (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        } else {
            onFinish()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breatheScale"
    )

    val isDaQin = restType == RestType.DA_QIN

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDaQin) Color(0xF2091424) else Color(0xF20A1A12)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // 模式标题
            Surface(
                color = if (isDaQin) com.qinmu.eyecare.ui.theme.SpotifyBlue.copy(alpha = 0.2f) else com.qinmu.eyecare.ui.theme.SpotifyGreen.copy(alpha = 0.2f),
                shape = RoundedCornerShape(500.dp)
            ) {
                Text(
                    text = if (isDaQin) "🧘 大沁 · 深度放松时刻" else "🌿 小沁 · 视力微休息",
                    color = if (isDaQin) com.qinmu.eyecare.ui.theme.SpotifyBlue else com.qinmu.eyecare.ui.theme.SpotifyGreen,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isDaQin)
                    "连续专注久坐，请起身活动身体、深呼吸并做眼保健操"
                else
                    "请将视线移开屏幕，看向 6 米外的远处放松眼肌",
                color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 倒计时呼吸圆环
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(170.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .scale(breatheScale)
                        .background(
                            if (isDaQin) com.qinmu.eyecare.ui.theme.SpotifyBlue.copy(alpha = 0.25f) else com.qinmu.eyecare.ui.theme.SpotifyGreen.copy(alpha = 0.25f),
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(124.dp)
                        .background(
                            if (isDaQin) com.qinmu.eyecare.ui.theme.SpotifyBlue else com.qinmu.eyecare.ui.theme.SpotifyGreen,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = TimeUtils.formatSecondsToMS(remainingSeconds),
                            color = Color.Black,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isDaQin) "深度休息" else "远眺倒计时",
                            color = Color.Black.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🌟 护眼姿势与电子设备视距指南卡片 🌟
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📐 正确用眼与设备安全距离",
                        color = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📱 手机/平板视距",
                                color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "保持 33 ~ 40 cm\n(约半臂距离)",
                                color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "💻 电脑显示器视距",
                                color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "保持 50 ~ 70 cm\n(约一臂直伸距离)",
                                color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = com.qinmu.eyecare.ui.theme.SpotifyBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isDaQin)
                            "💡 护眼贴士：搓热双手掌心温敷双眼，起身接杯水活动腰颈关节"
                        else
                            "💡 护眼贴士：保持环境光充足，多做完整眨眼动作润泽角膜",
                        color = com.qinmu.eyecare.ui.theme.SpotifyOrange,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 全胶囊 Pill 按键
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onSkip,
                    shape = RoundedCornerShape(500.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                        contentColor = com.qinmu.eyecare.ui.theme.SpotifyOrange
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.qinmu.eyecare.ui.theme.SpotifyOrange.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text(text = "跳过本次沁目", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onFinish,
                    shape = RoundedCornerShape(500.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDaQin) com.qinmu.eyecare.ui.theme.SpotifyBlue else com.qinmu.eyecare.ui.theme.SpotifyGreen,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text(text = "完成休息", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
