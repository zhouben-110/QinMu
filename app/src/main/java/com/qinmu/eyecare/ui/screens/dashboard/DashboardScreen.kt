package com.qinmu.eyecare.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
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
import com.qinmu.eyecare.ui.components.QinMuEmoji
import com.qinmu.eyecare.ui.theme.*
import com.qinmu.eyecare.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progress"
    )

    // Disc rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "discRotate")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "discAngle"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header Banner: Device & Status (Inspired by Figma "Kazuya's Air Pods Pro" subtitle)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DUSK TILL DOWN",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimaryDarkNavy,
                    letterSpacing = 0.5.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QinMuEmoji(symbol = "🌿", size = 20.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "沁目 · 智能护眼",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentRoyalBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• QinMu Active",
                        fontSize = 13.sp,
                        color = TextMutedSky
                    )
                }
            }

            // Neumorphic Device Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(500.dp))
                    .background(NeumorphicCardSurface)
                    .border(1.dp, Color.White, RoundedCornerShape(500.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isPaused) AccentWarmOrange else AccentMintGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPaused) "暂停中" else "护眼中",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDarkNavy
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // =========================================================================
        // Figma Home Screen Core Section: Floating Control Capsule + Circular Vinyl Disc
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Figma Vertical Neumorphic Capsule Control Bar (Left Side)
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight()
                    .neumorphicShadow(cornerRadius = 32.dp, elevation = 6.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(NeumorphicSurface)
                    .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(32.dp))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Refresh / Reset button
                    NeumorphicIconButton(
                        onClick = { viewModel.resetTimer(context) },
                        size = 40.dp,
                        containerColor = NeumorphicCardElevated
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "重置清零",
                            tint = AccentWarmOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Next / Skip button
                    NeumorphicIconButton(
                        onClick = { viewModel.skipCurrentRest(context) },
                        size = 40.dp,
                        containerColor = NeumorphicCardElevated
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "跳过本次",
                            tint = AccentRoyalBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Main Play/Pause Button (Vibrant Highlight)
                    NeumorphicIconButton(
                        onClick = { viewModel.togglePause(context) },
                        size = 44.dp,
                        containerColor = if (isPaused) AccentRoyalBlue else Color.White
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "暂停/恢复",
                            tint = if (isPaused) Color.White else AccentRoyalBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Previous track / Rewind
                    NeumorphicIconButton(
                        onClick = { viewModel.resetTimer(context) },
                        size = 40.dp,
                        containerColor = NeumorphicCardElevated
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "重置",
                            tint = AccentRoyalBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Heart / Strict Mode Toggle
                    NeumorphicIconButton(
                        onClick = { },
                        size = 40.dp,
                        containerColor = NeumorphicCardElevated
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "收藏",
                            tint = AccentCoralRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Figma Music Disc Vinyl Disc & Concentric Progress Gauge (Right Side)
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                // 🌟 动态自适应宽高计算：根据屏幕真实剩余宽度自动缩放，并强制 1:1 宽高比，决不发生椭圆挤压变形 🌟
                val discSize = minOf(maxWidth, maxHeight, 260.dp)
                val innerDiscSize = discSize * 0.71f

                // Large Outer Neumorphic Disc Surface
                Box(
                    modifier = Modifier
                        .size(discSize)
                        .aspectRatio(1f)
                        .neumorphicShadow(cornerRadius = discSize / 2, elevation = 10.dp)
                        .clip(CircleShape)
                        .background(NeumorphicSurface)
                        .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Canvas Draw Arc Progress Ring (Coral Red progress ring from Figma)
                    Canvas(modifier = Modifier.fillMaxSize().padding(discSize * 0.05f)) {
                        val strokeWidth = (discSize * 0.05f).toPx()
                        // Track ring
                        drawArc(
                            color = Color(0xFFCBE3F0),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        // Active Progress Arc (Figma Coral Red #FF3B30 gradient)
                        drawArc(
                            brush = Brush.linearGradient(
                                listOf(
                                    AccentCoralRed,
                                    AccentWarmOrange,
                                    AccentRoyalBlue
                                )
                            ),
                            startAngle = 135f,
                            sweepAngle = 270f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Inner Vinyl Disc Container
                    Box(
                        modifier = Modifier
                            .size(innerDiscSize)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFF1E3A5F),
                                        Color(0xFF0F233C)
                                    )
                                )
                            )
                            .border(4.dp, Color(0xFFB1D6EA).copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Rotating Vinyl Grooves Effect (graphicsLayer 图层级旋转，零 Recomposition 渲染功耗)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationZ = if (isPaused) 0f else rotationAngle
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.08f),
                                    radius = size.width * 0.42f,
                                    style = Stroke(width = 1.dp.toPx())
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.08f),
                                    radius = size.width * 0.32f,
                                    style = Stroke(width = 1.dp.toPx())
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.08f),
                                    radius = size.width * 0.22f,
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            }
                        }

                        // Center Vinyl Label with Time Text
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = TimeUtils.formatSecondsToHMS(currentSeconds),
                                fontSize = if (discSize < 220.dp) 24.sp else 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "目标: ${prefs.remindIntervalMinutes} 分钟",
                                fontSize = 11.sp,
                                color = Color(0xFFB1D6EA)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // =========================================================================
        // Figma Playlist Screen Core Section: Popular Playlists Cards (2 Cards)
        // =========================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Popular Playlist",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDarkNavy
            )
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = null,
                tint = TextMutedSky
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Preset Card 1: Pop Playlist (20-20-20 Rule)
            NeumorphicCard(
                modifier = Modifier.weight(1f),
                cornerRadius = 24.dp,
                elevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFFF9A9E),
                                        Color(0xFFFECFEF)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            QinMuEmoji(symbol = "🌿", size = 44.dp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Pop Playlist", fontWeight = FontWeight.ExtraBold, color = Color(0xFF4A154B), fontSize = 15.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Pop Playlist",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimaryDarkNavy
                    )
                    Text(
                        text = "20-20-20 护眼规则",
                        fontSize = 12.sp,
                        color = TextSecondaryBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NeumorphicPillButton(
                        onClick = {
                            viewModel.setRemindInterval(20)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = AccentRoyalBlue,
                        contentColor = Color.White
                    ) {
                        Text("一键套用", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Preset Card 2: Top Beats (45min Deep Work)
            NeumorphicCard(
                modifier = Modifier.weight(1f),
                cornerRadius = 24.dp,
                elevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFA1C4FD),
                                        Color(0xFFC2E9FB)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            QinMuEmoji(symbol = "🧘", size = 44.dp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Top Beats", fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F3460), fontSize = 15.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Top Beats",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimaryDarkNavy
                    )
                    Text(
                        text = "45分钟 深度专注",
                        fontSize = 12.sp,
                        color = TextSecondaryBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NeumorphicPillButton(
                        onClick = {
                            viewModel.setRemindInterval(45)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = AccentRoyalBlue,
                        contentColor = Color.White
                    ) {
                        Text("一键套用", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // =========================================================================
        // Meeting & Gaming Do Not Disturb Mode Chips Card
        // =========================================================================
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
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
                        color = TextPrimaryDarkNavy
                    )
                    if (effectiveMode != SpecialMode.NONE) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(500.dp))
                                .background(AccentWarmOrange)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "生效中: ",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                QinMuEmoji(symbol = effectiveMode.iconRes, size = 13.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = effectiveMode.displayName,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpecialMode.values().forEach { mode ->
                        val isSelected = prefs.manualSpecialMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setManualSpecialMode(mode) },
                            shape = RoundedCornerShape(500.dp),
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    QinMuEmoji(symbol = mode.iconRes, size = 18.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(mode.displayName)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentRoyalBlue,
                                selectedLabelColor = Color.White,
                                containerColor = NeumorphicCardSurface,
                                labelColor = TextSecondaryBlue
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // =========================================================================
        // Figma News & Health Tips Section (Extracted directly from Figma Screen `News`)
        // =========================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                QinMuEmoji(symbol = "📖", size = 20.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "News & Health Tips",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDarkNavy
                )
            }
            Text(
                text = "科学护眼指南",
                fontSize = 12.sp,
                color = AccentRoyalBlue,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🌟 护眼的重要性与日常技巧 Banner 🌟
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            elevation = 6.dp,
            containerColor = NeumorphicCardElevated
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentRoyalBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        QinMuEmoji(symbol = "👁️", size = 22.dp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "为什么日常护眼至关重要？",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDarkNavy
                        )
                        Text(
                            text = "长时间注视屏幕会显著降低眨眼频率，引发视疲劳",
                            fontSize = 11.sp,
                            color = TextSecondaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Highlight Stat 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(NeumorphicSurface)
                            .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("眨眼频率", fontSize = 11.sp, color = TextMutedSky)
                            Text("降低 60%", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = AccentCoralRed)
                            Text("易致泪膜破裂", fontSize = 10.sp, color = TextSecondaryBlue)
                        }
                    }

                    // Highlight Stat 2
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(NeumorphicSurface)
                            .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("睫状肌状态", fontSize = 11.sp, color = TextMutedSky)
                            Text("持续痉挛", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = AccentWarmOrange)
                            Text("视力调节下降", fontSize = 10.sp, color = TextSecondaryBlue)
                        }
                    }

                    // Highlight Stat 3
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(NeumorphicSurface)
                            .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("建议视距", fontSize = 11.sp, color = TextMutedSky)
                            Text("33-50cm", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = AccentRoyalBlue)
                            Text("手机/电脑视距", fontSize = 10.sp, color = TextSecondaryBlue)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // News & Tips Item 1: 20-20-20 Rule
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QinMuEmoji(symbol = "👀", size = 36.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "20-20-20 黄金护眼法则",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDarkNavy
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AccentRoyalBlue.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("眼科专家推荐", fontSize = 10.sp, color = AccentRoyalBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "每连续看屏幕 20 分钟，请抬眼远眺 6 米 (20英尺) 外至少 20 秒，让睫状肌彻底放松。",
                        fontSize = 11.sp,
                        color = TextSecondaryBlue,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // News & Tips Item 2: Light & Color Temp
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QinMuEmoji(symbol = "💡", size = 36.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "环境光线与暖色温调节",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDarkNavy
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "切勿在全黑暗光下玩手机！建议开启房间顶灯，并将屏幕夜间色温调至 3500K~4500K 暖光，减少有害高能蓝光。",
                        fontSize = 11.sp,
                        color = TextSecondaryBlue,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // News & Tips Item 3: Conscious Blinking & Hot Compress
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QinMuEmoji(symbol = "🧘", size = 36.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "主动眨眼与睡眠热敷秘诀",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDarkNavy
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "看屏幕时每分钟主动完全闭眼眨眼 15 次以补充泪膜；睡前可使用 40°C 蒸汽眼罩热敷 15 分钟，促进睑板腺油脂分泌。",
                        fontSize = 11.sp,
                        color = TextSecondaryBlue,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
