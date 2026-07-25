package com.qinmu.eyecare.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qinmu.eyecare.data.model.UsageLogEntity
import com.qinmu.eyecare.ui.theme.GreenPrimary
import com.qinmu.eyecare.ui.theme.WarmOrange
import com.qinmu.eyecare.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel()
) {
    val logs by viewModel.recent7DaysLogs.collectAsState()

    val totalXiaoQin = logs.sumOf { it.xiaoQinCount }
    val totalDaQin = logs.sumOf { it.daQinCount }
    val totalRestCount = totalXiaoQin + totalDaQin
    val totalSkipCount = logs.sumOf { it.skipCount }
    val totalRestSeconds = logs.sumOf { it.totalRestDurationSeconds }
    val totalScreenSeconds = logs.sumOf { it.screenOnTimeSeconds }

    val complianceRate = if (totalRestCount + totalSkipCount > 0) {
        ((totalRestCount.toFloat() / (totalRestCount + totalSkipCount).toFloat()) * 100).toInt()
    } else {
        100
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📊 小沁/大沁护眼详细统计",
                            fontWeight = FontWeight.Bold,
                            color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary,
                            fontSize = 18.sp
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. 核心四大指标看板 (2x2 Grid)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = "🌿",
                    title = "小沁微休息",
                    value = "$totalXiaoQin 次",
                    subtitle = "20s 远眺放松",
                    color = com.qinmu.eyecare.ui.theme.SpotifyGreen
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = "🧘",
                    title = "大沁深度放松",
                    value = "$totalDaQin 次",
                    subtitle = "3-5min 伸展拉伸",
                    color = com.qinmu.eyecare.ui.theme.SpotifyBlue
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = "⏱️",
                    title = "护眼休息总时长",
                    value = TimeUtils.formatSecondsToMS(totalRestSeconds.toInt()),
                    subtitle = "实际给眼睛放假",
                    color = com.qinmu.eyecare.ui.theme.SpotifyGreen
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = "🎯",
                    title = "护眼依从率",
                    value = "$complianceRate%",
                    subtitle = "跳过 $totalSkipCount 次",
                    color = if (complianceRate >= 80) com.qinmu.eyecare.ui.theme.SpotifyGreen else com.qinmu.eyecare.ui.theme.SpotifyOrange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 小沁 vs 大沁 完成结构分布卡片
            if (totalRestCount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "小沁与大沁完成分布占比",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val xiaoRatio = totalXiaoQin.toFloat() / totalRestCount.toFloat()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(500.dp))
                                .background(com.qinmu.eyecare.ui.theme.SpotifyDarkControl)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(xiaoRatio.coerceAtLeast(0.01f))
                                    .background(com.qinmu.eyecare.ui.theme.SpotifyGreen)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight((1f - xiaoRatio).coerceAtLeast(0.01f))
                                    .background(com.qinmu.eyecare.ui.theme.SpotifyBlue)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(com.qinmu.eyecare.ui.theme.SpotifyGreen, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "小沁 (微休息): $totalXiaoQin 次 (${(xiaoRatio * 100).toInt()}%)",
                                    fontSize = 11.sp,
                                    color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(com.qinmu.eyecare.ui.theme.SpotifyBlue, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "大沁 (深度): $totalDaQin 次 (${((1f - xiaoRatio) * 100).toInt()}%)",
                                    fontSize = 11.sp,
                                    color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. 近7天用眼趋势 Canvas 柱状图
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "近7天连屏使用时长 (分钟)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary
                        )
                        Text(
                            text = "近7天: ${totalScreenSeconds / 3600}小时 ${(totalScreenSeconds % 3600) / 60}分",
                            fontSize = 11.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (logs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "暂无数据，服务开启后自动记录", color = com.qinmu.eyecare.ui.theme.SpotifyTextMuted, fontSize = 13.sp)
                        }
                    } else {
                        WeeklyBarChart(logs = logs.reversed())
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. 历史明细列表
            Text(
                text = "📅 每日用眼与大小沁记录明细",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (logs.isEmpty()) {
                Text(
                    text = "暂无历史统计日志",
                    fontSize = 13.sp,
                    color = com.qinmu.eyecare.ui.theme.SpotifyTextMuted,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                logs.forEach { item ->
                    DetailedLogItemRow(log = item)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    value: String,
    subtitle: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, fontSize = 12.sp, color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 10.sp, color = com.qinmu.eyecare.ui.theme.SpotifyTextMuted)
        }
    }
}

@Composable
private fun WeeklyBarChart(logs: List<UsageLogEntity>) {
    val maxMinutes = (logs.maxOfOrNull { it.screenOnTimeSeconds / 60 } ?: 60L).coerceAtLeast(30L)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        val width = size.width
        val height = size.height
        val barCount = logs.size.coerceAtLeast(1)
        val spacePerBar = width / barCount
        val barWidth = (spacePerBar * 0.45f).coerceAtMost(36.dp.toPx())

        logs.forEachIndexed { index, entity ->
            val minutes = entity.screenOnTimeSeconds / 60
            val barHeight = (minutes.toFloat() / maxMinutes.toFloat()) * (height * 0.75f)
            val left = index * spacePerBar + (spacePerBar - barWidth) / 2
            val top = height - barHeight - 20.dp.toPx()

            // 绘制底托
            drawRoundRect(
                color = Color(0xFF1F1F1F),
                topLeft = Offset(left, 0f),
                size = Size(barWidth, height - 20.dp.toPx()),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // 绘制柱体
            drawRoundRect(
                color = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight.coerceAtLeast(6.dp.toPx())),
                cornerRadius = CornerRadius(12f, 12f)
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        logs.forEach { item ->
            val dateLabel = if (item.date.length >= 5) item.date.substring(5) else item.date
            Text(
                text = dateLabel,
                fontSize = 11.sp,
                color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DetailedLogItemRow(log: UsageLogEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = com.qinmu.eyecare.ui.theme.SpotifyDarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = log.date, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = com.qinmu.eyecare.ui.theme.SpotifyTextPrimary)
                Text(
                    text = "屏幕使用: ${TimeUtils.formatSecondsToHMS(log.screenOnTimeSeconds)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = com.qinmu.eyecare.ui.theme.SpotifyGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(500.dp)
                ) {
                    Text(
                        text = "🌿 小沁 ${log.xiaoQinCount} 次",
                        fontSize = 11.sp,
                        color = com.qinmu.eyecare.ui.theme.SpotifyGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = com.qinmu.eyecare.ui.theme.SpotifyBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(500.dp)
                ) {
                    Text(
                        text = "🧘 大沁 ${log.daQinCount} 次",
                        fontSize = 11.sp,
                        color = com.qinmu.eyecare.ui.theme.SpotifyBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = com.qinmu.eyecare.ui.theme.SpotifyDarkControl,
                    shape = RoundedCornerShape(500.dp)
                ) {
                    Text(
                        text = "⏱️ 放假 ${TimeUtils.formatSecondsToMS(log.totalRestDurationSeconds.toInt())}",
                        fontSize = 11.sp,
                        color = com.qinmu.eyecare.ui.theme.SpotifyTextSecondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (log.skipCount > 0) {
                    Surface(
                        color = com.qinmu.eyecare.ui.theme.SpotifyOrange.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(500.dp)
                    ) {
                        Text(
                            text = "⚠️ 跳过 ${log.skipCount} 次",
                            fontSize = 11.sp,
                            color = com.qinmu.eyecare.ui.theme.SpotifyOrange,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
