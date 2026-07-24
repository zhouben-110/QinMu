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

    val totalRestCount = logs.sumOf { it.restCount }
    val totalSkipCount = logs.sumOf { it.skipCount }
    val totalScreenSeconds = logs.sumOf { it.screenOnTimeSeconds }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "📊 用眼与休息统计",
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 核心统计指标卡片 Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "累计休息次数",
                    value = "$totalRestCount 次",
                    color = GreenPrimary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "跳过沁目次数",
                    value = "$totalSkipCount 次",
                    color = WarmOrange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 近7天用眼趋势 Canvas 柱状图
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "近7天连屏使用时长 (分钟)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (logs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "暂无统计数据", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        WeeklyBarChart(logs = logs.reversed())
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 历史明细列表
            Text(
                text = "每日记录明细",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            logs.forEach { item ->
                LogItemRow(log = item)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = title, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun WeeklyBarChart(logs: List<UsageLogEntity>) {
    val maxMinutes = (logs.maxOfOrNull { it.screenOnTimeSeconds / 60 } ?: 60L).coerceAtLeast(30L)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val width = size.width
        val height = size.height
        val barCount = logs.size.coerceAtLeast(1)
        val spacePerBar = width / barCount
        val barWidth = (spacePerBar * 0.45f).coerceAtMost(36.dp.toPx())

        logs.forEachIndexed { index, entity ->
            val minutes = entity.screenOnTimeSeconds / 60
            val barHeight = (minutes.toFloat() / maxMinutes.toFloat()) * (height * 0.8f)
            val left = index * spacePerBar + (spacePerBar - barWidth) / 2
            val top = height - barHeight - 20.dp.toPx()

            // 绘制柱体
            drawRoundRect(
                color = GreenPrimary,
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
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LogItemRow(log: UsageLogEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = log.date, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "屏幕累计: ${TimeUtils.formatSecondsToHMS(log.screenOnTimeSeconds)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text("休息: ${log.restCount}次", fontSize = 11.sp) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text("跳过: ${log.skipCount}次", fontSize = 11.sp) }
                )
            }
        }
    }
}
