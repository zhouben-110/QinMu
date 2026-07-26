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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.qinmu.eyecare.data.model.RestType
import com.qinmu.eyecare.ui.components.QinMuEmoji
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

        val flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutParamsType,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.FILL
            x = 0
            y = 0

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        lifecycleOwner = OverlayLifecycleOwner().apply {
            performCreate()
        }

        overlayView = ComposeView(context).apply {
            @Suppress("DEPRECATION")
            systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )

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

    val context = LocalContext.current
    val repository = remember { (context.applicationContext as com.qinmu.eyecare.QinMuApplication).preferencesRepository }
    val userPrefs by repository.userPreferencesFlow.collectAsState(initial = com.qinmu.eyecare.data.model.UserPreferences())

    val customBgPath = if (isDaQin) userPrefs.daQinBgUri else userPrefs.xiaoQinBgUri
    val customBitmap = remember(customBgPath) {
        if (!customBgPath.isNullOrEmpty()) {
            try {
                val file = java.io.File(customBgPath)
                if (file.exists() && file.length() > 0) {
                    android.graphics.BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else null
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (customBitmap != null) {
            Image(
                bitmap = customBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Pure Crystal Lens Dark Contrast Vignette (No White Haze)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.10f),
                                Color.Black.copy(alpha = 0.18f),
                                Color.Black.copy(alpha = 0.30f)
                            )
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFB1D6EA),
                                Color(0xFFD5EAF5),
                                Color(0xFFF0F8FA)
                            )
                        )
                    )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // Ultra-Clear Pure Glass Mode Tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(500.dp))
                    .background(
                        if (customBitmap != null)
                            Color.White.copy(alpha = 0.05f)
                        else
                            if (isDaQin) AccentRoyalBlue.copy(alpha = 0.4f) else AccentMintGreen.copy(alpha = 0.4f)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(500.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QinMuEmoji(symbol = if (isDaQin) "🧘" else "🌿", size = 20.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isDaQin) "大沁 · 深度放松时刻" else "小沁 · 视力微休息",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isDaQin)
                    "连续专注久坐，请起身活动身体、深呼吸并做眼保健操"
                else
                    "请将视线移开屏幕，看向 6 米外的远处放松眼肌",
                color = if (customBitmap != null) Color.White else TextPrimaryDarkNavy,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Ultra-Clear Pure Crystal Glass Timer Disc (0 White Haze)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(breatheScale)
                        .clip(CircleShape)
                        .background(
                            if (customBitmap != null)
                                Color.White.copy(alpha = 0.02f)
                            else
                                Color.White.copy(alpha = 0.12f)
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                listOf(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0.15f))
                            ),
                            shape = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(
                            if (customBitmap != null)
                                Color.White.copy(alpha = 0.03f)
                            else
                                Color.White.copy(alpha = 0.25f)
                        )
                        .border(
                            width = 1.2.dp,
                            brush = Brush.linearGradient(
                                listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0.35f))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = TimeUtils.formatSecondsToMS(remainingSeconds),
                            color = if (customBitmap != null) Color.White else TextPrimaryDarkNavy,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isDaQin) "深度休息" else "远眺倒计时",
                            color = if (customBitmap != null) Color.White.copy(alpha = 0.9f) else AccentRoyalBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Ultra-Clear Pure Glass Distance Tip Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (customBitmap != null)
                            Color.White.copy(alpha = 0.04f)
                        else
                            Color.White.copy(alpha = 0.25f)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = if (customBitmap != null) 0.5f else 0.8f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        QinMuEmoji(symbol = "📐", size = 18.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "正确用眼与设备安全距离",
                            color = if (customBitmap != null) Color.White else AccentRoyalBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                QinMuEmoji(symbol = "📱", size = 15.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("手机/平板视距", color = if (customBitmap != null) Color.White else TextPrimaryDarkNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("保持 33 ~ 40 cm\n(约半臂距离)", color = if (customBitmap != null) Color.White.copy(alpha = 0.9f) else TextSecondaryBlue, fontSize = 11.sp, lineHeight = 15.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                QinMuEmoji(symbol = "💻", size = 15.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("电脑显示器视距", color = if (customBitmap != null) Color.White else TextPrimaryDarkNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("保持 50 ~ 70 cm\n(约一臂直伸距离)", color = if (customBitmap != null) Color.White.copy(alpha = 0.9f) else TextSecondaryBlue, fontSize = 11.sp, lineHeight = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Ultra-Clear Pure Glass Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. 跳过本次 (Pure Glass Pill)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(500.dp))
                        .background(
                            if (customBitmap != null)
                                Color.White.copy(alpha = 0.05f)
                            else
                                Color.White.copy(alpha = 0.35f)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(500.dp)
                        )
                        .clickable(onClick = onSkip)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "跳过本次",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (customBitmap != null) Color.White else AccentWarmOrange
                    )
                }

                // 2. 完成休息 (Pure Glass Pill)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(500.dp))
                        .background(
                            if (customBitmap != null)
                                AccentRoyalBlue.copy(alpha = 0.45f)
                            else
                                AccentRoyalBlue.copy(alpha = 0.35f)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(500.dp)
                        )
                        .clickable(onClick = onFinish)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "完成休息",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
