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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🌿 沁目",
                            fontWeight = FontWeight.ExtraBold,
                            color = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "· 护眼看板",
                            fontWeight = FontWeight.Medium,
                            color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary,
                            fontSize = 16.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkBase
                )
            )
        },
        containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkBase
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 环形倒计时进度看板 (Spotify Inspired Surface Card)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = if (isPaused) com.qinmu.eyecare.ui.theme.SpotifyDarkElevated else com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                        shape = RoundedCornerShape(500.dp)
                    ) {
                        Text(
                            text = if (isPaused) "⏸️ 护眼计时已暂停" else "⏱️ 已连续使用屏幕",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isPaused) com.qinmu.eyecare.ui.theme.SpotifyOrange else com.qinmu.eyecare.ui.theme.SpotifyTextSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(210.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 14.dp.toPx()
                            drawArc(
                                color = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            drawArc(
                                brush = Brush.linearGradient(
                                    listOf(
                                        com.qinmu.eyecare.ui.theme.SpotifyGreenDark,
                                        com.qinmu.eyecare.ui.theme.SpotifyGreen,
                                        com.qinmu.eyecare.ui.theme.SpotifyOrange
                                    )
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
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "目标: ${prefs.remindIntervalMinutes} 分钟",
                                fontSize = 13.sp,
                                color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 快捷控制按键栏 (全胶囊 pill buttons 遵循 design.md)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.togglePause(context) },
                            shape = RoundedCornerShape(500.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPaused) com.qinmu.eyecare.ui.theme.SpotifyGreen else com.qinmu.eyecare.ui.theme.SpotifyDarkElevated,
                                contentColor = if (isPaused) Color.Black else com.qinmu.eyecare.ui.theme.SpotifyTextPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPaused) "恢复" else "暂停",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.skipCurrentRest(context) },
                            shape = RoundedCornerShape(500.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                                contentColor = com.qinmu.eyecare.ui.theme.SpotifyOrange
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, com.qinmu.eyecare.ui.theme.SpotifyOrange.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = com.qinmu.eyecare.ui.theme.SpotifyOrange
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "跳过本次沁目",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 💼 会议 & 🎮 游戏特例免打扰模式快捷选择卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface
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
                            fontSize = 15.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary
                        )
                        if (effectiveMode != SpecialMode.NONE) {
                            Surface(
                                color = com.qinmu.eyecare.ui.theme.SpotifyOrange,
                                shape = RoundedCornerShape(500.dp)
                            ) {
                                Text(
                                    text = "生效中: ${effectiveMode.iconRes} ${effectiveMode.displayName}",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpecialMode.values().forEach { mode ->
                            val isSelected = prefs.manualSpecialMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setManualSpecialMode(mode) },
                                shape = RoundedCornerShape(500.dp),
                                label = { Text("${mode.iconRes} ${mode.displayName}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                                    selectedLabelColor = Color.Black,
                                    containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                                    labelColor = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                                )
                            )
                        }
                    }

                    if (effectiveMode != SpecialMode.NONE) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "提示：全屏遮罩与强提示音已挂起，保证演示与游戏流畅。",
                            fontSize = 12.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 小大沁守护模式与交替进度卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(com.qinmu.eyecare.ui.theme.SpotifyDarkControl),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = com.qinmu.eyecare.ui.theme.SpotifyGreen
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "提醒形式: ${prefs.remindMode.displayName}",
                                fontSize = 12.sp,
                                color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (prefs.isDualCycleEnabled) "🌿 小沁 + 🧘 大沁 智能交替" else "🌿 小沁 (微休息模式)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary
                            )
                        }
                    }

                    if (prefs.isDualCycleEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = com.qinmu.eyecare.ui.theme.SpotifyBorder)
                        Spacer(modifier = Modifier.height(12.dp))

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
                                color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                            )
                            Surface(
                                color = if (isNextDaQin) com.qinmu.eyecare.ui.theme.SpotifyBlue.copy(alpha = 0.2f) else com.qinmu.eyecare.ui.theme.SpotifyGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(500.dp)
                            ) {
                                Text(
                                    text = if (isNextDaQin) "下一次：🧘 大沁 (${prefs.daQinRestSeconds / 60}分钟)" else "下一次：🌿 小沁 (${prefs.restDurationSeconds}秒)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNextDaQin) com.qinmu.eyecare.ui.theme.SpotifyBlue else com.qinmu.eyecare.ui.theme.SpotifyGreen,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface)
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
                        color = com.qinmu.eyecare.ui.theme.SpotifyGreen
                    )
                    Text(
                        text = "📱 手机 33~40cm | 💻 电脑 50~70cm",
                        fontSize = 12.sp,
                        color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 电子设备最佳护眼色温建议卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "💡 电子设备最佳护眼色温建议",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = com.qinmu.eyecare.ui.theme.SpotifyGreen
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 📱 手机配置
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "📱 手机端（系统护眼模式）",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• 最佳色温：3500K ~ 4500K (暖白~暖黄)",
                            fontSize = 12.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                        )
                        Text(
                            text = "• 推荐强度/比率：40% ~ 50%",
                            fontSize = 12.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 💻 电脑配置
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "💻 电脑端（显示器 / 夜间模式）",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• 日间工作：4500K ~ 5000K (比率 30%)",
                            fontSize = 12.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                        )
                        Text(
                            text = "• 夜间加班：3400K ~ 4000K (比率 50%)",
                            fontSize = 12.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                        )
                    }
                }
            }



            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
