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
import com.qinmu.eyecare.ui.components.QinMuEmoji
import com.qinmu.eyecare.ui.theme.*
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            QinMuEmoji(symbol = "📊", size = 26.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "护眼守护统计",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = TextPrimaryDarkNavy
            )
        }
        Text(
            text = "小沁与大沁微休息历史趋势看板",
            fontSize = 13.sp,
            color = TextSecondaryBlue
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. 核心四大指标看板 (2x2 Grid using Neumorphic Cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NeumorphicStatCard(
                modifier = Modifier.weight(1f),
                icon = "🌿",
                title = "小沁微休息",
                value = "$totalXiaoQin 次",
                subtitle = "20s 远眺放松",
                color = AccentRoyalBlue
            )
            NeumorphicStatCard(
                modifier = Modifier.weight(1f),
                icon = "🧘",
                title = "大沁深度放松",
                value = "$totalDaQin 次",
                subtitle = "3-5min 伸展拉伸",
                color = AccentSoftSky
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NeumorphicStatCard(
                modifier = Modifier.weight(1f),
                icon = "⏱️",
                title = "护眼休息总时长",
                value = TimeUtils.formatSecondsToMS(totalRestSeconds.toInt()),
                subtitle = "给眼睛真实放假",
                color = AccentMintGreen
            )
            NeumorphicStatCard(
                modifier = Modifier.weight(1f),
                icon = "🎯",
                title = "护眼依从率",
                value = "$complianceRate%",
                subtitle = "跳过 $totalSkipCount 次",
                color = if (complianceRate >= 80) AccentMintGreen else AccentWarmOrange
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. 小沁 vs 大沁 完成结构分布卡片
        if (totalRestCount > 0) {
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "小沁与大沁完成分布占比",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimaryDarkNavy
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val xiaoRatio = totalXiaoQin.toFloat() / totalRestCount.toFloat()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(500.dp))
                            .background(NeumorphicCardElevated)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(xiaoRatio.coerceAtLeast(0.01f))
                                .background(AccentRoyalBlue)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight((1f - xiaoRatio).coerceAtLeast(0.01f))
                                .background(AccentSoftSky)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(AccentRoyalBlue, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "小沁 (微休息): $totalXiaoQin 次 (${(xiaoRatio * 100).toInt()}%)",
                                fontSize = 11.sp,
                                color = TextSecondaryBlue
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(AccentSoftSky, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "大沁 (深度): $totalDaQin 次 (${((1f - xiaoRatio) * 100).toInt()}%)",
                                fontSize = 11.sp,
                                color = TextSecondaryBlue
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 3. 近7天用眼趋势 Canvas 柱状图
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
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
                        color = TextPrimaryDarkNavy
                    )
                    Text(
                        text = "总计: ${totalScreenSeconds / 3600}小时 ${(totalScreenSeconds % 3600) / 60}分",
                        fontSize = 11.sp,
                        color = TextMutedSky
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
                        Text(text = "暂无数据，服务开启后自动记录", color = TextMutedSky, fontSize = 13.sp)
                    }
                } else {
                    NeumorphicWeeklyBarChart(logs = logs.reversed())
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. 历史明细列表
        Text(
            text = "📅 每日用眼与大小沁记录明细",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = AccentRoyalBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (logs.isEmpty()) {
            Text(
                text = "暂无历史统计日志",
                fontSize = 13.sp,
                color = TextMutedSky,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            logs.forEach { item ->
                NeumorphicDetailedLogItemRow(log = item)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun NeumorphicStatCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    value: String,
    subtitle: String,
    color: Color
) {
    NeumorphicCard(
        modifier = modifier,
        cornerRadius = 18.dp,
        elevation = 5.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                QinMuEmoji(symbol = icon, size = 20.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, fontSize = 12.sp, color = TextSecondaryBlue, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 10.sp, color = TextMutedSky)
        }
    }
}

@Composable
private fun NeumorphicWeeklyBarChart(logs: List<UsageLogEntity>) {
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

            // 绘制底托 (Neumorphic Inset)
            drawRoundRect(
                color = Color(0xFFD3E7F3),
                topLeft = Offset(left, 0f),
                size = Size(barWidth, height - 20.dp.toPx()),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // 绘制柱体 (Soft Accent Royal Blue)
            drawRoundRect(
                color = AccentRoyalBlue,
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
                color = TextSecondaryBlue,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NeumorphicDetailedLogItemRow(log: UsageLogEntity) {
    NeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        elevation = 4.dp
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
                Text(text = log.date, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimaryDarkNavy)
                Text(
                    text = "屏幕使用: ${TimeUtils.formatSecondsToHMS(log.screenOnTimeSeconds)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondaryBlue
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(500.dp))
                        .background(AccentRoyalBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        QinMuEmoji(symbol = "🌿", size = 14.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "小沁 ${log.xiaoQinCount} 次",
                            fontSize = 11.sp,
                            color = AccentRoyalBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(500.dp))
                        .background(AccentSoftSky.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        QinMuEmoji(symbol = "🧘", size = 14.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "大沁 ${log.daQinCount} 次",
                            fontSize = 11.sp,
                            color = AccentRoyalBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(500.dp))
                        .background(NeumorphicCardElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        QinMuEmoji(symbol = "⏱️", size = 14.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "放假 ${TimeUtils.formatSecondsToMS(log.totalRestDurationSeconds.toInt())}",
                            fontSize = 11.sp,
                            color = TextSecondaryBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (log.skipCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(500.dp))
                            .background(AccentWarmOrange.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            QinMuEmoji(symbol = "⚠️", size = 14.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "跳过 ${log.skipCount} 次",
                                fontSize = 11.sp,
                                color = AccentWarmOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
