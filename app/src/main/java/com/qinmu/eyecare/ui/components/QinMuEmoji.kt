package com.qinmu.eyecare.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 沁目 - 全局 Emoji 3D 浮雕与新拟态矢量美化渲染组件
 * 将代码中的文本 Emoji 自动升级为具有立体渐变、硬核 3D 矢量绘制与精致微效的 UI 图标
 */
@Composable
fun QinMuEmoji(
    symbol: String,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    val cleanKey = symbol.trim()

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        when (cleanKey) {
            "🌿", "leaf", "小沁" -> LeafEmojiIcon(size = size)
            "👀", "eye", "远眺" -> EyeEmojiIcon(size = size)
            "🧘", "meditate", "大沁" -> MeditateEmojiIcon(size = size)
            "💼", "work", "meeting", "会议" -> WorkEmojiIcon(size = size)
            "🎮", "game", "gaming", "游戏" -> GameEmojiIcon(size = size)
            "📱", "phone", "手机" -> PhoneEmojiIcon(size = size)
            "💻", "laptop", "computer", "电脑" -> LaptopEmojiIcon(size = size)
            "💡", "bulb", "tip", "提示" -> BulbEmojiIcon(size = size)
            "📐", "ruler", "视距" -> RulerEmojiIcon(size = size)
            "⚙️", "gear", "settings", "设置" -> SettingsEmojiIcon(size = size)
            "📊", "stats", "chart", "统计" -> StatsEmojiIcon(size = size)
            "🔔", "bell", "remind", "提醒" -> BellEmojiIcon(size = size)
            "🎵", "music", "sound", "音效" -> MusicEmojiIcon(size = size)
            "⏰", "clock", "timer", "时间" -> ClockEmojiIcon(size = size)
            "🛡️", "shield", "protect", "守护" -> ShieldEmojiIcon(size = size)
            "☕", "coffee", "rest", "休息" -> CoffeeEmojiIcon(size = size)
            "✨", "spark", "star" -> SparkEmojiIcon(size = size)
            "❤️", "heart", "like" -> HeartEmojiIcon(size = size)
            "🎯", "target", "goal" -> TargetEmojiIcon(size = size)
            "⏱️", "stopwatch" -> StopwatchEmojiIcon(size = size)
            "⚠️", "warning" -> WarningEmojiIcon(size = size)
            "🏆", "trophy" -> TrophyEmojiIcon(size = size)
            "🔊", "volume" -> VolumeEmojiIcon(size = size)
            else -> FallbackEmojiBadge(symbol = cleanKey, size = size)
        }
    }
}

/** 1. 🌿 小沁·嫩叶护眼矢量图标 (精致立体绿叶与水滴高光) */
@Composable
private fun LeafEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF34C759), Color(0xFF10B981), Color(0xFF059669))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.68f)) {
            val w = this.size.width
            val h = this.size.height

            // 叶子主轮廓
            val leafPath = Path().apply {
                moveTo(w * 0.2f, h * 0.85f)
                cubicTo(w * 0.05f, h * 0.4f, w * 0.45f, h * 0.08f, w * 0.9f, h * 0.12f)
                cubicTo(w * 0.95f, h * 0.55f, w * 0.65f, h * 0.95f, w * 0.2f, h * 0.85f)
                close()
            }
            drawPath(path = leafPath, color = Color.White)

            // 叶脉
            drawLine(
                color = Color(0xFF047857),
                start = Offset(w * 0.22f, h * 0.8f),
                end = Offset(w * 0.78f, h * 0.22f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            // 侧脉
            drawLine(
                color = Color(0xFF047857).copy(alpha = 0.7f),
                start = Offset(w * 0.45f, h * 0.55f),
                end = Offset(w * 0.62f, h * 0.42f),
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 露珠高光
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                center = Offset(w * 0.4f, h * 0.35f),
                radius = w * 0.08f
            )
        }
    }
}

/** 2. 👀 远眺护眼矢量图标 (立体发光明眸与双高光) */
@Composable
private fun EyeEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF00E5FF), Color(0xFF2368A4), Color(0xFF1E3A5F))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.7f)) {
            val w = this.size.width
            val h = this.size.height

            // 眼眶外形
            val eyeOutline = Path().apply {
                moveTo(w * 0.05f, h * 0.5f)
                quadraticBezierTo(w * 0.5f, h * 0.08f, w * 0.95f, h * 0.5f)
                quadraticBezierTo(w * 0.5f, h * 0.92f, w * 0.05f, h * 0.5f)
                close()
            }
            drawPath(path = eyeOutline, color = Color.White)

            // 虹膜彩环
            drawCircle(
                color = Color(0xFF00F0FF),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = w * 0.28f
            )

            // 瞳孔
            drawCircle(
                color = Color(0xFF0A192F),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = w * 0.2f
            )

            // 主高光与次高光
            drawCircle(
                color = Color.White,
                center = Offset(w * 0.42f, h * 0.42f),
                radius = w * 0.09f
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                center = Offset(w * 0.6f, h * 0.6f),
                radius = w * 0.04f
            )
        }
    }
}

/** 3. 🧘 大沁·冥想放松矢量图标 (禅意莲花与禅修气场) */
@Composable
private fun MeditateEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFF2A85), Color(0xFF9C27B0), Color(0xFF3B0764))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.72f)) {
            val w = this.size.width
            val h = this.size.height

            // 气场光晕
            drawCircle(
                color = Color(0xFFF472B6).copy(alpha = 0.25f),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = w * 0.48f
            )

            // 头部与头部光环
            drawCircle(
                color = Color.White,
                center = Offset(w * 0.5f, h * 0.28f),
                radius = w * 0.12f
            )
            drawCircle(
                color = Color(0xFFFDE047),
                center = Offset(w * 0.5f, h * 0.28f),
                radius = w * 0.16f,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 盘坐身躯 Path
            val bodyPath = Path().apply {
                moveTo(w * 0.5f, h * 0.42f)
                cubicTo(w * 0.35f, h * 0.55f, w * 0.2f, h * 0.7f, w * 0.15f, h * 0.85f)
                quadraticBezierTo(w * 0.5f, h * 0.95f, w * 0.85f, h * 0.85f)
                cubicTo(w * 0.8f, h * 0.7f, w * 0.65f, h * 0.55f, w * 0.5f, h * 0.42f)
                close()
            }
            drawPath(path = bodyPath, color = Color.White)

            // 底部莲花瓣弧
            val petalLeft = Path().apply {
                moveTo(w * 0.15f, h * 0.85f)
                quadraticBezierTo(w * 0.05f, h * 0.72f, w * 0.3f, h * 0.72f)
            }
            drawPath(path = petalLeft, color = Color(0xFFF472B6), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

            val petalRight = Path().apply {
                moveTo(w * 0.85f, h * 0.85f)
                quadraticBezierTo(w * 0.95f, h * 0.72f, w * 0.7f, h * 0.72f)
            }
            drawPath(path = petalRight, color = Color(0xFFF472B6), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

/** 4. 💼 会议免打扰矢量图标 (金属提手与商务公文包) */
@Composable
private fun WorkEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8), Color(0xFF1E3A8A))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.68f)) {
            val w = this.size.width
            val h = this.size.height

            // 提手
            val handlePath = Path().apply {
                moveTo(w * 0.35f, h * 0.3f)
                cubicTo(w * 0.35f, h * 0.12f, w * 0.65f, h * 0.12f, w * 0.65f, h * 0.3f)
            }
            drawPath(path = handlePath, color = Color.White, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))

            // 包身
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.12f, h * 0.3f),
                size = Size(w * 0.76f, h * 0.55f),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // 金属扣锁
            drawRoundRect(
                color = Color(0xFFFBBF24),
                topLeft = Offset(w * 0.42f, h * 0.48f),
                size = Size(w * 0.16f, h * 0.18f),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // 缝合线条
            drawLine(
                color = Color(0xFF1D4ED8),
                start = Offset(w * 0.12f, h * 0.35f),
                end = Offset(w * 0.88f, h * 0.35f),
                strokeWidth = 1.2.dp.toPx()
            )
        }
    }
}

/** 5. 🎮 游戏免打扰矢量图标 (3D 手柄与极光按键) */
@Composable
private fun GameEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFA855F7), Color(0xFF7C3AED), Color(0xFF4C1D95))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.72f)) {
            val w = this.size.width
            val h = this.size.height

            // 手柄主体
            val padPath = Path().apply {
                moveTo(w * 0.25f, h * 0.28f)
                cubicTo(w * 0.4f, h * 0.28f, w * 0.6f, h * 0.28f, w * 0.75f, h * 0.28f)
                cubicTo(w * 0.95f, h * 0.32f, w * 0.95f, h * 0.82f, w * 0.75f, h * 0.8f)
                cubicTo(w * 0.65f, h * 0.78f, w * 0.58f, h * 0.5f, w * 0.5f, h * 0.5f)
                cubicTo(w * 0.42f, h * 0.5f, w * 0.35f, h * 0.78f, w * 0.25f, h * 0.8f)
                cubicTo(w * 0.05f, h * 0.82f, w * 0.05f, h * 0.32f, w * 0.25f, h * 0.28f)
                close()
            }
            drawPath(path = padPath, color = Color.White)

            // 十字键 (左)
            val strokeW = 2.dp.toPx()
            drawLine(Color(0xFF6D28D9), Offset(w * 0.28f, h * 0.45f), Offset(w * 0.28f, h * 0.65f), strokeWidth = strokeW, cap = StrokeCap.Round)
            drawLine(Color(0xFF6D28D9), Offset(w * 0.18f, h * 0.55f), Offset(w * 0.38f, h * 0.55f), strokeWidth = strokeW, cap = StrokeCap.Round)

            // ABXY 动作按键 (右)
            drawCircle(Color(0xFFEF4444), center = Offset(w * 0.72f, h * 0.44f), radius = w * 0.045f)
            drawCircle(Color(0xFF10B981), center = Offset(w * 0.72f, h * 0.66f), radius = w * 0.045f)
            drawCircle(Color(0xFF3B82F6), center = Offset(w * 0.62f, h * 0.55f), radius = w * 0.045f)
            drawCircle(Color(0xFFF59E0B), center = Offset(w * 0.82f, h * 0.55f), radius = w * 0.045f)
        }
    }
}

/** 6. 📱 手机视距矢量图标 (高光全面屏与边框) */
@Composable
private fun PhoneEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFF92400E))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.68f)) {
            val w = this.size.width
            val h = this.size.height

            // 手机外框
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.22f, h * 0.12f),
                size = Size(w * 0.56f, h * 0.76f),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // 内屏
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(w * 0.27f, h * 0.18f),
                size = Size(w * 0.46f, h * 0.64f),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // 屏幕发光内容与线条
            drawLine(
                color = Color(0xFF38BDF8),
                start = Offset(w * 0.35f, h * 0.35f),
                end = Offset(w * 0.65f, h * 0.35f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF34C759),
                start = Offset(w * 0.35f, h * 0.5f),
                end = Offset(w * 0.55f, h * 0.5f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 顶部摄像头听筒
            drawCircle(Color.White, center = Offset(w * 0.5f, h * 0.15f), radius = w * 0.03f)
        }
    }
}

/** 7. 💻 电脑显示器视距矢量图标 (极简超薄本与悬浮视网膜屏) */
@Composable
private fun LaptopEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF6366F1), Color(0xFF4338CA), Color(0xFF312E81))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.7f)) {
            val w = this.size.width
            val h = this.size.height

            // 屏幕外框
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.15f, h * 0.18f),
                size = Size(w * 0.7f, h * 0.48f),
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            )

            // 屏幕亮显
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(w * 0.2f, h * 0.23f),
                size = Size(w * 0.6f, h * 0.38f),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )

            // 屏幕代码/图表光效
            drawLine(
                color = Color(0xFF818CF8),
                start = Offset(w * 0.28f, h * 0.35f),
                end = Offset(w * 0.55f, h * 0.35f),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF38BDF8),
                start = Offset(w * 0.28f, h * 0.45f),
                end = Offset(w * 0.68f, h * 0.45f),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 底座键盘面
            val baseTrapezoid = Path().apply {
                moveTo(w * 0.1f, h * 0.74f)
                lineTo(w * 0.9f, h * 0.74f)
                lineTo(w * 0.95f, h * 0.82f)
                lineTo(w * 0.05f, h * 0.82f)
                close()
            }
            drawPath(path = baseTrapezoid, color = Color.White)
        }
    }
}

/** 8. 💡 护眼小提示矢量图标 (透明白炽灯泡与璀璨钨丝) */
@Composable
private fun BulbEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFBBF24), Color(0xFFF59E0B), Color(0xFFD97706))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.7f)) {
            val w = this.size.width
            val h = this.size.height

            // 辐射星芒
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                center = Offset(w * 0.5f, h * 0.42f),
                radius = w * 0.44f
            )

            // 灯泡主体 Path
            val bulbPath = Path().apply {
                moveTo(w * 0.35f, h * 0.68f)
                cubicTo(w * 0.15f, h * 0.52f, w * 0.18f, h * 0.2f, w * 0.5f, h * 0.18f)
                cubicTo(w * 0.82f, h * 0.2f, w * 0.85f, h * 0.52f, w * 0.65f, h * 0.68f)
                close()
            }
            drawPath(path = bulbPath, color = Color.White)

            // 钨丝
            val filamentPath = Path().apply {
                moveTo(w * 0.42f, h * 0.48f)
                lineTo(w * 0.47f, h * 0.35f)
                lineTo(w * 0.53f, h * 0.35f)
                lineTo(w * 0.58f, h * 0.48f)
            }
            drawPath(
                path = filamentPath,
                color = Color(0xFFD97706),
                style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 灯座螺纹
            drawRoundRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(w * 0.36f, h * 0.68f),
                size = Size(w * 0.28f, h * 0.14f),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

/** 9. 📐 视距测算矢量图标 (3D 直角绘图三角尺) */
@Composable
private fun RulerEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFEC4899), Color(0xFFBE185D), Color(0xFF831843))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.68f)) {
            val w = this.size.width
            val h = this.size.height

            // 外三角
            val outerTriangle = Path().apply {
                moveTo(w * 0.15f, h * 0.85f)
                lineTo(w * 0.15f, h * 0.15f)
                lineTo(w * 0.85f, h * 0.85f)
                close()
            }
            drawPath(path = outerTriangle, color = Color.White)

            // 内空心三角
            val innerTriangle = Path().apply {
                moveTo(w * 0.28f, h * 0.72f)
                lineTo(w * 0.28f, h * 0.42f)
                lineTo(w * 0.58f, h * 0.72f)
                close()
            }
            drawPath(path = innerTriangle, color = Color(0xFFBE185D))

            // 刻度线
            for (i in 0..4) {
                val offset = h * (0.22f + i * 0.12f)
                drawLine(
                    color = Color(0xFFBE185D),
                    start = Offset(w * 0.15f, offset),
                    end = Offset(w * (0.22f + (if (i % 2 == 0) 0.06f else 0.03f)), offset),
                    strokeWidth = 1.2.dp.toPx()
                )
            }
        }
    }
}

/** 10. ⚙️ 设置齿轮矢量图标 (精密金属机械轮轴) */
@Composable
private fun SettingsEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF64748B), Color(0xFF334155), Color(0xFF1E293B))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.7f)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w * 0.5f
            val cy = h * 0.5f

            // 齿轮主体
            drawCircle(
                color = Color.White,
                center = Offset(cx, cy),
                radius = w * 0.32f
            )

            // 8 个轮齿
            for (i in 0 until 8) {
                val angle = i * (Math.PI / 4)
                val tx = cx + (w * 0.35f * Math.cos(angle)).toFloat()
                val ty = cy + (h * 0.35f * Math.sin(angle)).toFloat()
                drawCircle(
                    color = Color.White,
                    center = Offset(tx, ty),
                    radius = w * 0.08f
                )
            }

            // 中心轴孔
            drawCircle(
                color = Color(0xFF1E293B),
                center = Offset(cx, cy),
                radius = w * 0.14f
            )
        }
    }
}

/** 11. 📊 护眼统计矢量图标 (3D 柱状分析图) */
@Composable
private fun StatsEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF06B6D4), Color(0xFF0891B2), Color(0xFF164E63))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.68f)) {
            val w = this.size.width
            val h = this.size.height

            // 3 根柱子
            drawRoundRect(
                color = Color.White.copy(alpha = 0.7f),
                topLeft = Offset(w * 0.15f, h * 0.5f),
                size = Size(w * 0.18f, h * 0.35f),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.41f, h * 0.25f),
                size = Size(w * 0.18f, h * 0.6f),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFFFDE047),
                topLeft = Offset(w * 0.67f, h * 0.38f),
                size = Size(w * 0.18f, h * 0.47f),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )

            // 折线趋势
            val linePath = Path().apply {
                moveTo(w * 0.24f, h * 0.45f)
                lineTo(w * 0.5f, h * 0.22f)
                lineTo(w * 0.76f, h * 0.32f)
            }
            drawPath(path = linePath, color = Color.White, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

/** 12. 🔔 提醒响铃矢量图标 (立体金钟与声波弧) */
@Composable
private fun BellEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFF6B6B), Color(0xFFEE5253), Color(0xFFC0392B))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.68f)) {
            val w = this.size.width
            val h = this.size.height

            // 顶环
            drawCircle(Color.White, center = Offset(w * 0.5f, h * 0.18f), radius = w * 0.08f, style = Stroke(width = 1.8.dp.toPx()))

            // 钟罩
            val bellPath = Path().apply {
                moveTo(w * 0.32f, h * 0.68f)
                quadraticBezierTo(w * 0.32f, h * 0.28f, w * 0.5f, h * 0.25f)
                quadraticBezierTo(w * 0.68f, h * 0.28f, w * 0.68f, h * 0.68f)
                lineTo(w * 0.78f, h * 0.72f)
                lineTo(w * 0.22f, h * 0.72f)
                close()
            }
            drawPath(path = bellPath, color = Color.White)

            // 锤心
            drawCircle(Color(0xFFF1C40F), center = Offset(w * 0.5f, h * 0.8f), radius = w * 0.09f)
        }
    }
}

/** 13. 🎵 音效与音乐矢量图标 (连音符与跳跃音轨) */
@Composable
private fun MusicEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF38BDF8), Color(0xFF6366F1), Color(0xFF4338CA))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.68f)) {
            val w = this.size.width
            val h = this.size.height

            // 音符 1 & 2
            drawCircle(Color.White, center = Offset(w * 0.32f, h * 0.72f), radius = w * 0.12f)
            drawCircle(Color.White, center = Offset(w * 0.72f, h * 0.62f), radius = w * 0.12f)

            // 符干
            drawLine(Color.White, Offset(w * 0.42f, h * 0.72f), Offset(w * 0.42f, h * 0.25f), strokeWidth = 2.dp.toPx())
            drawLine(Color.White, Offset(w * 0.82f, h * 0.62f), Offset(w * 0.82f, h * 0.15f), strokeWidth = 2.dp.toPx())

            // 符杠
            val beamPath = Path().apply {
                moveTo(w * 0.4f, h * 0.25f)
                lineTo(w * 0.84f, h * 0.15f)
                lineTo(w * 0.84f, h * 0.28f)
                lineTo(w * 0.4f, h * 0.38f)
                close()
            }
            drawPath(path = beamPath, color = Color.White)
        }
    }
}

/** 14. ⏰ 护眼计时矢量图标 (精致指针表盘与闹铃) */
@Composable
private fun ClockEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0EA5E9), Color(0xFF0284C7), Color(0xFF0369A1))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.7f)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w * 0.5f
            val cy = h * 0.52f

            // 表盘
            drawCircle(Color.White, center = Offset(cx, cy), radius = w * 0.38f)
            drawCircle(Color(0xFF0369A1), center = Offset(cx, cy), radius = w * 0.32f)

            // 时针分针 (指向 10:10 / 20-20 规则意象)
            drawLine(Color.White, Offset(cx, cy), Offset(cx - w * 0.16f, cy - h * 0.16f), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
            drawLine(Color(0xFF38BDF8), Offset(cx, cy), Offset(cx + w * 0.2f, cy - h * 0.12f), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(Color.White, center = Offset(cx, cy), radius = w * 0.05f)

            // 两侧双发喇叭耳
            drawCircle(Color.White, center = Offset(w * 0.2f, h * 0.18f), radius = w * 0.08f)
            drawCircle(Color.White, center = Offset(w * 0.8f, h * 0.18f), radius = w * 0.08f)
        }
    }
}

/** 15. 🛡️ 护眼守护矢量图标 (盾牌与坚固纹章) */
@Composable
private fun ShieldEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF2563EB), Color(0xFF1D4ED8), Color(0xFF1E40AF))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.68f)) {
            val w = this.size.width
            val h = this.size.height

            // 盾牌 Body
            val shieldPath = Path().apply {
                moveTo(w * 0.5f, h * 0.1f)
                lineTo(w * 0.85f, h * 0.22f)
                cubicTo(w * 0.85f, h * 0.65f, w * 0.65f, h * 0.85f, w * 0.5f, h * 0.95f)
                cubicTo(w * 0.35f, h * 0.85f, w * 0.15f, h * 0.65f, w * 0.15f, h * 0.22f)
                close()
            }
            drawPath(path = shieldPath, color = Color.White)

            // 内对勾
            val checkPath = Path().apply {
                moveTo(w * 0.32f, h * 0.5f)
                lineTo(w * 0.45f, h * 0.62f)
                lineTo(w * 0.68f, h * 0.38f)
            }
            drawPath(path = checkPath, color = Color(0xFF1D4ED8), style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

/** 16. ☕ 放假休息矢量图标 (热气腾腾咖啡杯) */
@Composable
private fun CoffeeEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFD97706), Color(0xFFB45309), Color(0xFF78350F))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.68f)) {
            val w = this.size.width
            val h = this.size.height

            // 杯身
            val cupPath = Path().apply {
                moveTo(w * 0.2f, h * 0.4f)
                lineTo(w * 0.8f, h * 0.4f)
                cubicTo(w * 0.78f, h * 0.78f, w * 0.68f, h * 0.85f, w * 0.5f, h * 0.85f)
                cubicTo(w * 0.32f, h * 0.85f, w * 0.22f, h * 0.78f, w * 0.2f, h * 0.4f)
                close()
            }
            drawPath(path = cupPath, color = Color.White)

            // 杯把
            val handlePath = Path().apply {
                moveTo(w * 0.76f, h * 0.48f)
                cubicTo(w * 0.95f, h * 0.48f, w * 0.95f, h * 0.72f, w * 0.72f, h * 0.72f)
            }
            drawPath(path = handlePath, color = Color.White, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

            // 热气 1 & 2
            val steam1 = Path().apply {
                moveTo(w * 0.35f, h * 0.32f)
                quadraticBezierTo(w * 0.4f, h * 0.22f, w * 0.35f, h * 0.15f)
            }
            drawPath(path = steam1, color = Color.White, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
            val steam2 = Path().apply {
                moveTo(w * 0.6f, h * 0.32f)
                quadraticBezierTo(w * 0.65f, h * 0.22f, w * 0.6f, h * 0.15f)
            }
            drawPath(path = steam2, color = Color.White, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

/** 17. ✨ 璀璨星芒矢量图标 (4角钻石闪耀) */
@Composable
private fun SparkEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFACC15), Color(0xFFEAB308), Color(0xFFCA8A04))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.7f)) {
            val w = this.size.width
            val h = this.size.height

            // 主星
            val starPath = Path().apply {
                moveTo(w * 0.5f, h * 0.1f)
                quadraticBezierTo(w * 0.5f, h * 0.5f, w * 0.9f, h * 0.5f)
                quadraticBezierTo(w * 0.5f, h * 0.5f, w * 0.5f, h * 0.9f)
                quadraticBezierTo(w * 0.5f, h * 0.5f, w * 0.1f, h * 0.5f)
                quadraticBezierTo(w * 0.5f, h * 0.5f, w * 0.5f, h * 0.1f)
                close()
            }
            drawPath(path = starPath, color = Color.White)
        }
    }
}

/** 18. ❤️ 收藏爱心矢量图标 (高光立体红心) */
@Composable
private fun HeartEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFF4D4D), Color(0xFFE60000), Color(0xFFB30000))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.68f)) {
            val w = this.size.width
            val h = this.size.height

            // 爱心 Path
            val heartPath = Path().apply {
                moveTo(w * 0.5f, h * 0.82f)
                cubicTo(w * 0.15f, h * 0.55f, w * 0.05f, h * 0.25f, w * 0.28f, h * 0.15f)
                cubicTo(w * 0.42f, h * 0.1f, w * 0.5f, h * 0.25f, w * 0.5f, h * 0.25f)
                cubicTo(w * 0.5f, h * 0.25f, w * 0.58f, h * 0.1f, w * 0.72f, h * 0.15f)
                cubicTo(w * 0.95f, h * 0.25f, w * 0.85f, h * 0.55f, w * 0.5f, h * 0.82f)
                close()
            }
            drawPath(path = heartPath, color = Color.White)

            // 左上角高光弧
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                center = Offset(w * 0.32f, h * 0.28f),
                radius = w * 0.07f
            )
        }
    }
}

/** 19. 🎯 依从率标靶矢量图标 (同心靶心) */
@Composable
private fun TargetEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFEF4444), Color(0xFFDC2626), Color(0xFF991B1B))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.7f)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w * 0.5f
            val cy = h * 0.5f

            drawCircle(Color.White, center = Offset(cx, cy), radius = w * 0.42f)
            drawCircle(Color(0xFFDC2626), center = Offset(cx, cy), radius = w * 0.3f)
            drawCircle(Color.White, center = Offset(cx, cy), radius = w * 0.18f)
            drawCircle(Color(0xFFFBBF24), center = Offset(cx, cy), radius = w * 0.09f)
        }
    }
}

/** 20. ⏱️ 连屏秒表矢量图标 */
@Composable
private fun StopwatchEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF14B8A6), Color(0xFF0D9488), Color(0xFF115E59))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.7f)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w * 0.5f
            val cy = h * 0.54f

            // 顶部按键
            drawRoundRect(Color.White, topLeft = Offset(w * 0.44f, h * 0.06f), size = Size(w * 0.12f, h * 0.1f), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()))

            // 表盘
            drawCircle(Color.White, center = Offset(cx, cy), radius = w * 0.38f)
            drawCircle(Color(0xFF115E59), center = Offset(cx, cy), radius = w * 0.32f)

            // 指针
            drawLine(Color.White, Offset(cx, cy), Offset(cx + w * 0.18f, cy - h * 0.18f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(Color(0xFF14B8A6), center = Offset(cx, cy), radius = w * 0.05f)
        }
    }
}

/** 21. ⚠️ 跳过警告矢量图标 */
@Composable
private fun WarningEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFF97316), Color(0xFFEA580C), Color(0xFFC2410C))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.7f)) {
            val w = this.size.width
            val h = this.size.height

            val triangle = Path().apply {
                moveTo(w * 0.5f, h * 0.12f)
                lineTo(w * 0.9f, h * 0.85f)
                lineTo(w * 0.1f, h * 0.85f)
                close()
            }
            drawPath(path = triangle, color = Color.White)

            // 叹号
            drawLine(Color(0xFFEA580C), Offset(w * 0.5f, h * 0.35f), Offset(w * 0.5f, h * 0.58f), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(Color(0xFFEA580C), center = Offset(w * 0.5f, h * 0.72f), radius = w * 0.045f)
        }
    }
}

/** 22. 🏆 奖杯成就矢量图标 */
@Composable
private fun TrophyEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFFBBF24), Color(0xFFD97706), Color(0xFFB45309))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.7f)) {
            val w = this.size.width
            val h = this.size.height

            // 奖杯杯身
            val cupPath = Path().apply {
                moveTo(w * 0.22f, h * 0.18f)
                lineTo(w * 0.78f, h * 0.18f)
                lineTo(w * 0.68f, h * 0.55f)
                quadraticBezierTo(w * 0.5f, h * 0.68f, w * 0.32f, h * 0.55f)
                close()
            }
            drawPath(path = cupPath, color = Color.White)

            // 底座
            drawRoundRect(Color.White, topLeft = Offset(w * 0.3f, h * 0.76f), size = Size(w * 0.4f, h * 0.12f), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()))
            drawLine(Color.White, Offset(w * 0.5f, h * 0.62f), Offset(w * 0.5f, h * 0.76f), strokeWidth = 3.dp.toPx())
        }
    }
}

/** 23. 🔊 音量矢量图标 */
@Composable
private fun VolumeEmojiIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF60A5FA), Color(0xFF2563EB), Color(0xFF1E40AF))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.68f)) {
            val w = this.size.width
            val h = this.size.height

            // 喇叭盒
            drawRect(Color.White, topLeft = Offset(w * 0.18f, h * 0.38f), size = Size(w * 0.18f, h * 0.24f))
            val cone = Path().apply {
                moveTo(w * 0.36f, h * 0.38f)
                lineTo(w * 0.54f, h * 0.22f)
                lineTo(w * 0.54f, h * 0.78f)
                lineTo(w * 0.36f, h * 0.62f)
                close()
            }
            drawPath(path = cone, color = Color.White)

            // 音浪弧 1 & 2
            val arc1 = Path().apply {
                moveTo(w * 0.65f, h * 0.35f)
                quadraticBezierTo(w * 0.75f, h * 0.5f, w * 0.65f, h * 0.65f)
            }
            drawPath(path = arc1, color = Color.White, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round))
            val arc2 = Path().apply {
                moveTo(w * 0.78f, h * 0.25f)
                quadraticBezierTo(w * 0.92f, h * 0.5f, w * 0.78f, h * 0.75f)
            }
            drawPath(path = arc2, color = Color.White, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

/** 兜底精致新拟态 3D 金币 Badge */
@Composable
private fun FallbackEmojiBadge(symbol: String, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontSize = (size.value * 0.52f).sp
        )
    }
}
