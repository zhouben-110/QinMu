package com.qinmu.eyecare.ui.screens.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qinmu.eyecare.data.model.SpecialMode
import com.qinmu.eyecare.ui.theme.GreenPrimary
import com.qinmu.eyecare.ui.theme.WarmOrange
import com.qinmu.eyecare.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs by viewModel.userPreferences.collectAsState()
    val currentSeconds by viewModel.currentScreenSeconds.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val completedCount by viewModel.xiaoQinCompletedCount.collectAsState()
    val effectiveMode by viewModel.effectiveSpecialMode.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startService(context)
    }

    val totalIntervalSeconds = (prefs.remindIntervalMinutes * 60).coerceAtLeast(1)
    val progress = (currentSeconds.toFloat() / totalIntervalSeconds.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🌿 沁目 · 护眼看板",
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 环形倒计时进度看板
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isPaused) "护眼计时已暂停" else "已连续使用屏幕",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(200.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 14.dp.toPx()
                            drawArc(
                                color = Color(0xFFE8F5E9),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            drawArc(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFF81C784), GreenPrimary, WarmOrange)
                                ),
                                startAngle = 135f,
                                sweepAngle = 270f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = TimeUtils.formatSecondsToHMS(currentSeconds),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GreenPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "目标: ${prefs.remindIntervalMinutes}分钟",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 快捷控制按键栏 (暂停 / 跳过本次沁目)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.togglePause(context) },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (isPaused) "恢复" else "暂停")
                        }

                        Button(
                            onClick = { viewModel.skipCurrentRest(context) },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarmOrange)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "跳过本次沁目", color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 💼 会议 & 🎮 游戏特例免打扰模式快捷选择卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (effectiveMode != SpecialMode.NONE) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "场景免打扰快捷控制",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (effectiveMode != SpecialMode.NONE) {
                            Surface(
                                color = WarmOrange,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "生效中: ${effectiveMode.iconRes} ${effectiveMode.displayName}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpecialMode.values().forEach { mode ->
                            FilterChip(
                                selected = prefs.manualSpecialMode == mode,
                                onClick = { viewModel.setManualSpecialMode(mode) },
                                label = { Text("${mode.iconRes} ${mode.displayName}") }
                            )
                        }
                    }

                    if (effectiveMode != SpecialMode.NONE) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "提示：全屏遮罩与强提示音已暂护挂起，不会中断您的画面或演示。",
                            fontSize = 11.sp,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            // 小大沁守护模式与交替进度卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBE7))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCEDC8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = GreenPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "提醒形式: ${prefs.remindMode.displayName}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = if (prefs.isDualCycleEnabled) "🌿 小沁 + 🧘 大沁 智能交替" else "🌿 小沁 (微休息模式)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                        }
                    }

                    if (prefs.isDualCycleEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFFE0E0E0))
                        Spacer(modifier = Modifier.height(10.dp))

                        val currentCycleIndex = (completedCount % prefs.daQinCycleCount) + 1
                        val isNextDaQin = currentCycleIndex == prefs.daQinCycleCount

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "交替进度：第 $currentCycleIndex / ${prefs.daQinCycleCount} 轮",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isNextDaQin) Color(0xFF0288D1) else Color(0xFF388E3C)
                            )
                            Surface(
                                color = if (isNextDaQin) Color(0xFFE1F5FE) else Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isNextDaQin) "下一次：🧘 大沁 (${prefs.daQinRestSeconds / 60}分钟)" else "下一次：🌿 小沁 (${prefs.restDurationSeconds}秒)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNextDaQin) Color(0xFF0288D1) else Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 正确用眼距离常识卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📐 视距建议：",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57F17)
                    )
                    Text(
                        text = "📱 手机 33~40cm | 💻 电脑 50~70cm",
                        fontSize = 12.sp,
                        color = Color(0xFFE65100)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 护眼滤镜调节卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = WarmOrange
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "暖色护眼防蓝光滤镜",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                        Switch(
                            checked = prefs.isFilterEnabled,
                            onCheckedChange = { viewModel.toggleFilter(it, context) }
                        )
                    }

                    if (prefs.isFilterEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "滤镜不透明度: ${(prefs.filterAlpha * 100).toInt()}%",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Slider(
                            value = prefs.filterAlpha,
                            onValueChange = { viewModel.updateFilterAlpha(it) },
                            valueRange = 0.05f..0.6f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
