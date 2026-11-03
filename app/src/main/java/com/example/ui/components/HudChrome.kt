package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WidgetSize
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcElectricBlue
import com.example.ui.theme.HudPanelGlass
import com.example.ui.theme.HudPanelRedGlass
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.StarkCrimson
import com.example.ui.theme.StarkGold

@Composable
fun HudHologramBackground(
    isRedAlert: Boolean,
    isFlightMode: Boolean,
    parallaxPitch: Float,
    parallaxRoll: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hud_bg")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_line"
    )
    val redAlertPulse by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "red_alert_pulse"
    )

    val primaryNeon = if (isRedAlert) StarkCrimson else ArcCyan
    val secondaryNeon = if (isRedAlert) StarkGold else ArcElectricBlue

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Deep holographic radial atmosphere
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryNeon.copy(alpha = if (isRedAlert) 0.16f else 0.10f),
                    secondaryNeon.copy(alpha = 0.04f),
                    Color(0xFF020409)
                ),
                center = Offset(
                    x = w * 0.5f + parallaxRoll * 3.5f,
                    y = h * 0.32f + parallaxPitch * 3.5f
                ),
                radius = w.coerceAtLeast(h) * 0.78f
            )
        )

        // Perspective technical grid lines
        val gridSpacing = 44.dp.toPx()
        val offsetX = (parallaxRoll * 2f) % gridSpacing
        val offsetY = (parallaxPitch * 2f) % gridSpacing

        var x = offsetX
        while (x < w) {
            if (x >= 0f) {
                drawLine(
                    color = primaryNeon.copy(alpha = 0.055f),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1f
                )
            }
            x += gridSpacing
        }

        var y = offsetY
        while (y < h) {
            if (y >= 0f) {
                drawLine(
                    color = primaryNeon.copy(alpha = 0.055f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }
            y += gridSpacing
        }

        // Moving holographic horizontal laser scanline
        val currentScanY = scanLineY * h
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    primaryNeon.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.55f),
                    primaryNeon.copy(alpha = 0.35f),
                    Color.Transparent
                )
            ),
            start = Offset(0f, currentScanY),
            end = Offset(w, currentScanY),
            strokeWidth = 2.dp.toPx()
        )

        // Flight Mode corner HUD rangefinders
        if (isFlightMode) {
            val bracketLen = 36.dp.toPx()
            val pad = 12.dp.toPx()
            val stroke = 2.dp.toPx()
            val flightColor = ArcCyan.copy(alpha = 0.55f)
            // Top-left reticle
            drawLine(flightColor, Offset(pad, pad), Offset(pad + bracketLen, pad), stroke)
            drawLine(flightColor, Offset(pad, pad), Offset(pad, pad + bracketLen), stroke)
            // Top-right reticle
            drawLine(flightColor, Offset(w - pad, pad), Offset(w - pad - bracketLen, pad), stroke)
            drawLine(flightColor, Offset(w - pad, pad), Offset(w - pad, pad + bracketLen), stroke)
        }

        // Red Alert pulsing perimeter warning frame
        if (isRedAlert) {
            val borderStroke = 3.dp.toPx()
            drawRect(
                color = StarkCrimson.copy(alpha = redAlertPulse * 0.65f),
                topLeft = Offset(borderStroke, borderStroke),
                size = Size(w - borderStroke * 2, h - borderStroke * 2),
                style = Stroke(width = borderStroke)
            )
        }
    }
}

@Composable
fun HudWidgetCard(
    title: String,
    moduleCode: String,
    size: WidgetSize,
    isRedAlert: Boolean,
    onCycleSize: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = if (isRedAlert) StarkCrimson else ArcCyan,
    parallaxRoll: Float = 0f,
    parallaxPitch: Float = 0f,
    testTag: String = "hud_widget_card",
    headerTrailing: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardShape = CutCornerShape(topStart = 14.dp, bottomEnd = 14.dp, topEnd = 4.dp, bottomStart = 4.dp)
    val glassBg = if (isRedAlert) HudPanelRedGlass else HudPanelGlass
    val secondaryAccent = if (isRedAlert) StarkGold else ArcElectricBlue

    Box(
        modifier = modifier
            .testTag(testTag)
            .graphicsLayer {
                rotationY = (parallaxRoll * 0.12f).coerceIn(-4f, 4f)
                rotationX = (-parallaxPitch * 0.10f).coerceIn(-4f, 4f)
            }
            .clip(cardShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        glassBg,
                        Color(0xE6040A14),
                        glassBg.copy(alpha = 0.90f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.85f),
                        secondaryAccent.copy(alpha = 0.35f),
                        accentColor.copy(alpha = 0.75f)
                    )
                ),
                shape = cardShape
            )
            .drawBehind {
                // Tactical HUD corner bracket accents
                val stroke = 2.5.dp.toPx()
                val corner = 18.dp.toPx()
                // Top-right corner tick
                drawLine(
                    color = accentColor,
                    start = Offset(this.size.width - corner, 0f),
                    end = Offset(this.size.width, 0f),
                    strokeWidth = stroke
                )
                drawLine(
                    color = accentColor,
                    start = Offset(this.size.width, 0f),
                    end = Offset(this.size.width, corner),
                    strokeWidth = stroke
                )
                // Bottom-left corner tick
                drawLine(
                    color = accentColor,
                    start = Offset(0f, this.size.height - corner),
                    end = Offset(0f, this.size.height),
                    strokeWidth = stroke
                )
                drawLine(
                    color = accentColor,
                    start = Offset(0f, this.size.height),
                    end = Offset(corner, this.size.height),
                    strokeWidth = stroke
                )
            }
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // HUD Module Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 4.dp, height = 16.dp)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = moduleCode,
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 10.sp,
                            color = accentColor.copy(alpha = 0.85f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = title,
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (headerTrailing != null) {
                        headerTrailing()
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    // Widget Size Cycle Button (1x1 / 2x1 / 2x2)
                    Box(
                        modifier = Modifier
                            .testTag("${testTag}_resize_btn")
                            .clip(CutCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.14f))
                            .border(1.dp, accentColor.copy(alpha = 0.5f), CutCornerShape(4.dp))
                            .clickable(onClick = onCycleSize)
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = "Resize widget $title",
                                tint = accentColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = size.badge,
                                fontFamily = ShareTechMonoFontFamily,
                                fontSize = 10.sp,
                                color = accentColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            // Thin glowing divider line
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            ) {
                val canvasW = this.size.width
                val canvasH = this.size.height
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(canvasW * 0.35f, 0f)
                    lineTo(canvasW * 0.38f, canvasH)
                    lineTo(canvasW, canvasH)
                }
                drawPath(
                    path = path,
                    color = accentColor.copy(alpha = 0.45f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            content()
        }
    }
}
