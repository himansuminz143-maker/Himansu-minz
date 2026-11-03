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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanBright
import com.example.ui.theme.ArcElectricBlue
import com.example.ui.theme.HudPitchBlack
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TelemetryNominalGreen
import kotlin.math.roundToInt

@Composable
fun BootSequenceOverlay(
    bootProgress: Float,
    bootStageLabel: String,
    onSkipBoot: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "boot_anim")
    val ringAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "boot_ring"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF071A30),
                        HudPitchBlack
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "STARK INDUSTRIES // DEFENSE DIVISION",
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 12.sp,
                color = ArcCyan,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "J.A.R.V.I.S. OS v85.4",
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.White,
                letterSpacing = 1.8.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Retinal Biometric & Arc Ignition Ring
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(190.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val r = size.minDimension / 2f

                    drawCircle(
                        color = ArcCyan.copy(alpha = 0.2f),
                        radius = r * 0.95f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    rotate(degrees = ringAngle, pivot = center) {
                        drawArc(
                            color = ArcCyan,
                            startAngle = 0f,
                            sweepAngle = 110f,
                            useCenter = false,
                            topLeft = Offset(center.x - r * 0.86f, center.y - r * 0.86f),
                            size = Size(r * 1.72f, r * 1.72f),
                            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = StarkGold,
                            startAngle = 180f,
                            sweepAngle = 95f,
                            useCenter = false,
                            topLeft = Offset(center.x - r * 0.86f, center.y - r * 0.86f),
                            size = Size(r * 1.72f, r * 1.72f),
                            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    rotate(degrees = -ringAngle * 1.4f, pivot = center) {
                        drawArc(
                            color = ArcElectricBlue,
                            startAngle = 45f,
                            sweepAngle = 220f,
                            useCenter = false,
                            topLeft = Offset(center.x - r * 0.65f, center.y - r * 0.65f),
                            size = Size(r * 1.30f, r * 1.30f),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ArcCyanBright.copy(alpha = 0.7f),
                                ArcCyan.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = r * 0.5f
                        ),
                        radius = r * 0.5f,
                        center = center
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(bootProgress * 100).roundToInt()}%",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = Color.White
                    )
                    Text(
                        text = "CORE SYNC",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 10.sp,
                        color = ArcCyanBright
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = bootStageLabel,
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 13.sp,
                color = TelemetryNominalGreen,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { bootProgress },
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .height(6.dp)
                    .clip(CutCornerShape(3.dp)),
                color = ArcCyan,
                trackColor = Color(0xFF0A2239)
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Subsystem Diagnostic Checkpoints
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .clip(CutCornerShape(8.dp))
                    .background(Color(0x99061224))
                    .border(1.dp, ArcCyan.copy(alpha = 0.35f), CutCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                BootCheckRow("BIOMETRIC RETINAL LOCK // STARK, T.", bootProgress >= 0.20f)
                BootCheckRow("VIBRANIUM ARC REACTOR // 3.20 GW", bootProgress >= 0.45f)
                BootCheckRow("ATMOSPHERIC & ORBITAL TELEMETRY", bootProgress >= 0.70f)
                BootCheckRow("J.A.R.V.I.S. VOICE NEURAL MATRIX", bootProgress >= 0.90f)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .testTag("skip_boot_button")
                    .clip(CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
                    .background(ArcCyan.copy(alpha = 0.18f))
                    .border(1.2.dp, ArcCyan, CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
                    .clickable(onClick = onSkipBoot)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "BYPASS INITIALIZATION >>",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ArcCyanBright,
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}

@Composable
private fun BootCheckRow(label: String, completed: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 11.sp,
            color = if (completed) Color.White else Color.White.copy(alpha = 0.45f)
        )
        Text(
            text = if (completed) "[ONLINE]" else "[SYNC...]",
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 11.sp,
            color = if (completed) TelemetryNominalGreen else StarkGold
        )
    }
}
