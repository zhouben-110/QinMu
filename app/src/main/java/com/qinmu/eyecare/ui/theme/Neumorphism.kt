package com.qinmu.eyecare.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 3D Soft Neumorphism Shadow Modifier
 * High-fidelity representation of Figma neumorphic soft-extrusion materials
 */
fun Modifier.neumorphicShadow(
    cornerRadius: Dp = 20.dp,
    lightShadowColor: Color = Color.White.copy(alpha = 0.9f),
    darkShadowColor: Color = Color(0xFF99BCCF).copy(alpha = 0.45f),
    elevation: Dp = 8.dp
): Modifier = this.drawBehind {
    val shadowRadius = elevation.toPx()
    val offsetX = (elevation / 2).toPx()
    val offsetY = (elevation / 2).toPx()
    val radiusPx = cornerRadius.toPx()

    drawIntoCanvas { canvas ->
        // 1. Bottom-right dark soft shadow
        val darkPaint = Paint().apply {
            val frameworkPaint = asFrameworkPaint()
            frameworkPaint.color = darkShadowColor.toArgb()
            frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(
                shadowRadius,
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
        }
        canvas.drawRoundRect(
            left = offsetX,
            top = offsetY,
            right = size.width + offsetX,
            bottom = size.height + offsetY,
            radiusX = radiusPx,
            radiusY = radiusPx,
            paint = darkPaint
        )

        // 2. Top-left light specular highlight
        val lightPaint = Paint().apply {
            val frameworkPaint = asFrameworkPaint()
            frameworkPaint.color = lightShadowColor.toArgb()
            frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(
                shadowRadius * 0.8f,
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
        }
        canvas.drawRoundRect(
            left = -offsetX * 0.8f,
            top = -offsetY * 0.8f,
            right = size.width - offsetX * 0.8f,
            bottom = size.height - offsetY * 0.8f,
            radiusX = radiusPx,
            radiusY = radiusPx,
            paint = lightPaint
        )
    }
}

/**
 * Neumorphic Card Container
 */
@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    cornerRadius: Dp = 20.dp,
    containerColor: Color = NeumorphicSurface,
    borderColor: Color = Color.White.copy(alpha = 0.6f),
    elevation: Dp = 6.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .neumorphicShadow(
                cornerRadius = cornerRadius,
                elevation = elevation
            )
            .clip(shape)
            .background(containerColor)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.8f),
                        Color(0xFFB1D6EA).copy(alpha = 0.3f)
                    )
                ),
                shape = shape
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

/**
 * Neumorphic Pill Button
 */
@Composable
fun NeumorphicPillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPressed: Boolean = false,
    containerColor: Color = NeumorphicCardElevated,
    contentColor: Color = TextPrimaryDarkNavy,
    elevation: Dp = 5.dp,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(500.dp)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .then(
                if (!isPressed) {
                    Modifier.neumorphicShadow(
                        cornerRadius = 500.dp,
                        elevation = elevation
                    )
                } else Modifier
            )
            .clip(shape)
            .background(
                if (isPressed) Color(0xFFD3E7F3) else containerColor
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.7f),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

/**
 * Neumorphic Circular Icon Button
 */
@Composable
fun NeumorphicIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    containerColor: Color = NeumorphicSurface,
    elevation: Dp = 6.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .neumorphicShadow(
                cornerRadius = size / 2,
                elevation = elevation
            )
            .clip(CircleShape)
            .background(containerColor)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.7f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
