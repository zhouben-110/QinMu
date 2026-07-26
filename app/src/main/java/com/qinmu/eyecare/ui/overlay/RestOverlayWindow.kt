package com.qinmu.eyecare.ui.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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
import com.qinmu.eyecare.data.model.RestType
import com.qinmu.eyecare.ui.theme.*
import com.qinmu.eyecare.util.TimeUtils
import kotlinx.coroutines.delay

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
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
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
        initialValue = 0.92f,
        targetValue = 1.08f,
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
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xE6B1D6EA),
                        Color(0xF0D5EAF5),
                        Color(0xF5F0F8FA)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // Mode Tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(500.dp))
                    .background(if (isDaQin) AccentRoyalBlue else AccentMintGreen)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isDaQin) "🧘 大沁 · 深度放松时刻" else "🌿 小沁 · 视力微休息",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isDaQin)
                    "连续专注久坐，请起身活动身体、深呼吸并做眼保健操"
                else
                    "请将视线移开屏幕，看向 6 米外的远处放松眼肌",
                color = TextSecondaryBlue,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Vinyl Breathing Disc
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(breatheScale)
                        .neumorphicShadow(cornerRadius = 100.dp, elevation = 12.dp)
                        .clip(CircleShape)
                        .background(NeumorphicSurface)
                        .border(3.dp, Color.White, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    AccentRoyalBlue,
                                    Color(0xFF133B61)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = TimeUtils.formatSecondsToMS(remainingSeconds),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isDaQin) "深度休息" else "远眺倒计时",
                            color = Color(0xFFB1D6EA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Distance Tip Card
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📐 正确用眼与设备安全距离",
                        color = AccentRoyalBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("📱 手机/平板视距", color = TextPrimaryDarkNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("保持 33 ~ 40 cm\n(约半臂距离)", color = TextSecondaryBlue, fontSize = 11.sp, lineHeight = 15.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("💻 电脑显示器视距", color = TextPrimaryDarkNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("保持 50 ~ 70 cm\n(约一臂直伸距离)", color = TextSecondaryBlue, fontSize = 11.sp, lineHeight = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeumorphicPillButton(
                    onClick = onSkip,
                    containerColor = NeumorphicCardElevated
                ) {
                    Text(text = "跳过本次", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentWarmOrange)
                }

                NeumorphicPillButton(
                    onClick = onFinish,
                    containerColor = AccentRoyalBlue
                ) {
                    Text(text = "完成休息", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
