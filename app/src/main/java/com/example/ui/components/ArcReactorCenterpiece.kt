package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ArcPowerMode
import com.example.data.FlightTelemetry
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanBright
import com.example.ui.theme.ArcElectricBlue
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.StarkCrimson
import com.example.ui.theme.StarkCrimsonGlow
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TelemetryNominalGreen
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ArcReactorCenterpiece(
    powerMode: ArcPowerMode,
    isRedAlert: Boolean,
    isFlightMode: Boolean,
    isVoiceActive: Boolean,
    flightTelemetry: FlightTelemetry,
    onCyclePowerMode: () -> Unit,
    onSelectPowerMode: (ArcPowerMode) -> Unit,
    onToggleRedAlert: () -> Unit,
    onToggleFlightMode: () -> Unit,
    onUpdateFlightTrim: (pitch: Float, roll: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor")

    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (14000 / powerMode.speedMultiplier).roundToInt().coerceAtLeast(2500),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rot"
    )

    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (9000 / powerMode.speedMultiplier).roundToInt().coerceAtLeast(1800),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rot"
    )

    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isRedAlert) 520 else (1100 / powerMode.speedMultiplier).roundToInt().coerceAtLeast(350),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    val primaryColor = when {
        isRedAlert -> StarkCrimson
        powerMode == ArcPowerMode.UNIBEAM_OVERDRIVE -> ArcCyanBright
        else -> ArcCyan
    }
    val secondaryColor = when {
        isRedAlert -> StarkGold
        powerMode == ArcPowerMode.COMBAT_BOOST -> StarkGold
        else -> ArcElectricBlue
    }

    val shape = CutCornerShape(topStart = 20.dp, bottomEnd = 20.dp, topEnd = 6.dp, bottomStart = 6.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xD9050F1E),
                        if (isRedAlert) Color(0xE61F050B) else Color(0xE6030914),
                        Color(0xD9050F1E)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.9f),
                        secondaryColor.copy(alpha = 0.4f),
                        primaryColor.copy(alpha = 0.9f)
                    )
                ),
                shape = shape
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top HUD Tactical Header & Mode Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isRedAlert) "ALERT // DEFCON-1 COMBAT MATRIX" else "CORE // MARK LXXXV ARC REACTOR",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = primaryColor,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "${powerMode.label} • ${String.format("%.2f", powerMode.outputGigawatts)} GW",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Flight Mode Toggle Button
                TacticalModeChip(
                    label = "FLIGHT",
                    active = isFlightMode,
                    activeColor = TelemetryNominalGreen,
                    icon = Icons.Default.FlightTakeoff,
                    onClick = onToggleFlightMode,
                    testTag = "toggle_flight_mode_button"
                )
                // Red Alert Toggle Button
                TacticalModeChip(
                    label = "RED ALERT",
                    active = isRedAlert,
                    activeColor = StarkCrimson,
                    icon = Icons.Default.WarningAmber,
                    onClick = onToggleRedAlert,
                    testTag = "toggle_red_alert_button"
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Flight Mode Compass Tape Ribbon (when Flight Mode is active)
        AnimatedVisibility(visible = isFlightMode) {
            FlightCompassRibbon(
                headingDegrees = flightTelemetry.headingDegrees,
                altitudeFeet = flightTelemetry.altitudeFeet,
                machSpeed = flightTelemetry.machSpeed,
                gForce = flightTelemetry.gForce,
                accentColor = primaryColor
            )
        }

        // Main 3D Holographic Arc Reactor + Optional Flight Attitude Horizon Ladder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(265.dp),
            contentAlignment = Alignment.Center
        ) {
            // Left Side Telemetry Readout
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 2.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiniHudMetric(label = "COIL RPM", value = "${powerMode.coreRpm}", color = primaryColor)
                MiniHudMetric(label = "CORE TEMP", value = "${powerMode.coreTempKelvin} K", color = secondaryColor)
                MiniHudMetric(
                    label = "SHIELD CAP",
                    value = if (isRedAlert) "100% LOCK" else "98.4% NOM",
                    color = if (isRedAlert) StarkCrimsonGlow else TelemetryNominalGreen
                )
            }

            // Right Side Telemetry Readout
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 2.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiniHudMetric(
                    label = "OUTPUT",
                    value = "${String.format("%.2f", powerMode.outputGigawatts)} GW",
                    color = primaryColor,
                    alignEnd = true
                )
                MiniHudMetric(
                    label = "THRUST L/R",
                    value = "${flightTelemetry.leftThrusterPct}%/${flightTelemetry.rightThrusterPct}%",
                    color = secondaryColor,
                    alignEnd = true
                )
                MiniHudMetric(
                    label = "NEURAL SYNC",
                    value = if (isVoiceActive) "ACTIVE TX" else "0.02 ms",
                    color = if (isVoiceActive) StarkGold else ArcCyanBright,
                    alignEnd = true
                )
            }

            // Center Interactive Arc Reactor Canvas
            val effectivePitch = flightTelemetry.pitchDegrees + flightTelemetry.manualPitchTrim
            val effectiveRoll = flightTelemetry.rollDegrees + flightTelemetry.manualRollTrim

            Canvas(
                modifier = Modifier
                    .size(230.dp)
                    .testTag("arc_reactor_center_canvas")
                    .graphicsLayer {
                        rotationX = (-effectivePitch * 0.35f).coerceIn(-18f, 18f)
                        rotationY = (effectiveRoll * 0.35f).coerceIn(-18f, 18f)
                    }
                    .clickable(onClick = onCyclePowerMode)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxR = size.minDimension / 2f

                // 1. Ambient plasma corona glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.48f * corePulse.coerceIn(0.8f, 1.2f)),
                            secondaryColor.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = maxR * 1.05f
                    ),
                    radius = maxR * 1.05f,
                    center = center
                )

                // 2. Outer Azimuth Bezel & Degree Ticks
                drawCircle(
                    color = primaryColor.copy(alpha = 0.35f),
                    radius = maxR * 0.94f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                rotate(degrees = outerRotation, pivot = center) {
                    for (deg in 0 until 360 step 6) {
                        val rad = deg * (PI / 180.0)
                        val isMajor = deg % 30 == 0
                        val rStart = maxR * (if (isMajor) 0.86f else 0.90f)
                        val rEnd = maxR * 0.94f
                        drawLine(
                            color = if (isMajor) primaryColor else primaryColor.copy(alpha = 0.45f),
                            start = Offset(
                                center.x + (rStart * cos(rad)).toFloat(),
                                center.y + (rStart * sin(rad)).toFloat()
                            ),
                            end = Offset(
                                center.x + (rEnd * cos(rad)).toFloat(),
                                center.y + (rEnd * sin(rad)).toFloat()
                            ),
                            strokeWidth = if (isMajor) 2.2.dp.toPx() else 1.dp.toPx()
                        )
                    }

                    // Segmented outer containment arcs
                    for (arcIdx in 0 until 4) {
                        drawArc(
                            color = primaryColor.copy(alpha = 0.85f),
                            startAngle = arcIdx * 90f + 12f,
                            sweepAngle = 58f,
                            useCenter = false,
                            topLeft = Offset(center.x - maxR * 0.82f, center.y - maxR * 0.82f),
                            size = Size(maxR * 1.64f, maxR * 1.64f),
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // 3. 10 Copper/Cyan Magnetic Induction Coils around Reactor Ring
                val coilRingR = maxR * 0.68f
                for (i in 0 until 10) {
                    val baseAngle = i * 36f
                    drawArc(
                        color = if (i % 2 == 0) primaryColor else secondaryColor,
                        startAngle = baseAngle - 10f,
                        sweepAngle = 20f,
                        useCenter = false,
                        topLeft = Offset(center.x - coilRingR, center.y - coilRingR),
                        size = Size(coilRingR * 2f, coilRingR * 2f),
                        style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Butt)
                    )
                }

                // 4. Counter-Rotating Inner Turbine Ring
                rotate(degrees = innerRotation, pivot = center) {
                    val innerR = maxR * 0.54f
                    for (k in 0 until 6) {
                        drawArc(
                            color = ArcCyanBright.copy(alpha = 0.9f),
                            startAngle = k * 60f + 8f,
                            sweepAngle = 36f,
                            useCenter = false,
                            topLeft = Offset(center.x - innerR, center.y - innerR),
                            size = Size(innerR * 2f, innerR * 2f),
                            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // 5. Mark LXXXV Triangular Vibranium Core Housing
                val triRadius = maxR * 0.42f * corePulse.coerceIn(0.92f, 1.08f)
                val trianglePath = Path().apply {
                    val angles = listOf(-90.0, 30.0, 150.0).map { it * PI / 180.0 }
                    moveTo(
                        center.x + (triRadius * cos(angles[0])).toFloat(),
                        center.y + (triRadius * sin(angles[0])).toFloat()
                    )
                    lineTo(
                        center.x + (triRadius * cos(angles[1])).toFloat(),
                        center.y + (triRadius * sin(angles[1])).toFloat()
                    )
                    lineTo(
                        center.x + (triRadius * cos(angles[2])).toFloat(),
                        center.y + (triRadius * sin(angles[2])).toFloat()
                    )
                    close()
                }
                // Inverted outer triangle
                val invTriRadius = maxR * 0.45f
                val invTrianglePath = Path().apply {
                    val angles = listOf(90.0, 210.0, 330.0).map { it * PI / 180.0 }
                    moveTo(
                        center.x + (invTriRadius * cos(angles[0])).toFloat(),
                        center.y + (invTriRadius * sin(angles[0])).toFloat()
                    )
                    lineTo(
                        center.x + (invTriRadius * cos(angles[1])).toFloat(),
                        center.y + (invTriRadius * sin(angles[1])).toFloat()
                    )
                    lineTo(
                        center.x + (invTriRadius * cos(angles[2])).toFloat(),
                        center.y + (invTriRadius * sin(angles[2])).toFloat()
                    )
                    close()
                }

                drawPath(
                    path = invTrianglePath,
                    color = primaryColor.copy(alpha = 0.95f),
                    style = Stroke(width = 3.dp.toPx())
                )
                drawPath(
                    path = trianglePath,
                    color = secondaryColor.copy(alpha = 0.75f),
                    style = Stroke(width = 2.dp.toPx())
                )

                // 6. Glowing Plasma Heart
                val coreHeartR = maxR * 0.24f * corePulse
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            if (isRedAlert) StarkCrimsonGlow else ArcCyanBright,
                            primaryColor.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = coreHeartR * 1.4f
                    ),
                    radius = coreHeartR * 1.4f,
                    center = center
                )

                // 7. Flight Mode Pitch & Roll Attitude Horizon Ladder Overlay
                if (isFlightMode) {
                    rotate(degrees = effectiveRoll, pivot = center) {
                        val pitchOffsetPx = (effectivePitch * 2.2f).dp.toPx()
                        val horizonY = (center.y + pitchOffsetPx).coerceIn(center.y - maxR * 0.7f, center.y + maxR * 0.7f)
                        val ladderColor = TelemetryNominalGreen.copy(alpha = 0.88f)

                        // Artificial Horizon bars
                        drawLine(
                            color = ladderColor,
                            start = Offset(center.x - maxR * 0.75f, horizonY),
                            end = Offset(center.x - maxR * 0.22f, horizonY),
                            strokeWidth = 2.5.dp.toPx()
                        )
                        drawLine(
                            color = ladderColor,
                            start = Offset(center.x + maxR * 0.22f, horizonY),
                            end = Offset(center.x + maxR * 0.75f, horizonY),
                            strokeWidth = 2.5.dp.toPx()
                        )
                        // Pitch ladder rungs (+10, -10)
                        for (rung in listOf(-1, 1)) {
                            val ry = horizonY + rung * 24.dp.toPx()
                            drawLine(
                                color = ladderColor.copy(alpha = 0.65f),
                                start = Offset(center.x - maxR * 0.45f, ry),
                                end = Offset(center.x - maxR * 0.20f, ry),
                                strokeWidth = 1.8.dp.toPx()
                            )
                            drawLine(
                                color = ladderColor.copy(alpha = 0.65f),
                                start = Offset(center.x + maxR * 0.20f, ry),
                                end = Offset(center.x + maxR * 0.45f, ry),
                                strokeWidth = 1.8.dp.toPx()
                            )
                        }
                    }
                }
            }
        }

        // Flight Mode Interactive Attitude Trim Controls
        AnimatedVisibility(visible = isFlightMode) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 6.dp)
                    .clip(CutCornerShape(8.dp))
                    .background(Color(0x66041824))
                    .border(1.dp, TelemetryNominalGreen.copy(alpha = 0.45f), CutCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "FLIGHT STABILIZER TRIM // PITCH: ${(flightTelemetry.pitchDegrees + flightTelemetry.manualPitchTrim).roundToInt()}°",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp,
                        color = TelemetryNominalGreen
                    )
                    Text(
                        text = "BANK ROLL: ${(flightTelemetry.rollDegrees + flightTelemetry.manualRollTrim).roundToInt()}°",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp,
                        color = ArcCyanBright
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        value = flightTelemetry.manualPitchTrim,
                        onValueChange = { onUpdateFlightTrim(it, flightTelemetry.manualRollTrim) },
                        valueRange = -25f..25f,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("flight_pitch_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = TelemetryNominalGreen,
                            activeTrackColor = TelemetryNominalGreen
                        )
                    )
                    Slider(
                        value = flightTelemetry.manualRollTrim,
                        onValueChange = { onUpdateFlightTrim(flightTelemetry.manualPitchTrim, it) },
                        valueRange = -35f..35f,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("flight_roll_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = ArcCyan,
                            activeTrackColor = ArcCyan
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Interactive 4-stage Arc Reactor Power Output Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ArcPowerMode.entries.forEach { mode ->
                val isSelected = mode == powerMode
                val chipColor = when {
                    isRedAlert && isSelected -> StarkCrimson
                    mode == ArcPowerMode.UNIBEAM_OVERDRIVE && isSelected -> StarkGold
                    isSelected -> ArcCyan
                    else -> ArcCyan.copy(alpha = 0.35f)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("arc_mode_${mode.name.lowercase()}")
                        .clip(CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
                        .background(if (isSelected) chipColor.copy(alpha = 0.24f) else Color(0x55040C18))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = chipColor,
                            shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp)
                        )
                        .clickable { onSelectPowerMode(mode) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = chipColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = mode.shortCode,
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else chipColor
                            )
                        }
                        Text(
                            text = "${mode.outputGigawatts} GW",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 10.sp,
                            color = chipColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TacticalModeChip(
    label: String,
    active: Boolean,
    activeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    val shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp)
    val borderColor = if (active) activeColor else ArcCyan.copy(alpha = 0.4f)
    val bgColor = if (active) activeColor.copy(alpha = 0.25f) else Color(0x66061224)

    Row(
        modifier = Modifier
            .testTag(testTag)
            .clip(shape)
            .background(bgColor)
            .border(1.2.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) activeColor else ArcCyan,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = if (active) Color.White else ArcCyan
        )
    }
}

@Composable
private fun MiniHudMetric(
    label: String,
    value: String,
    color: Color,
    alignEnd: Boolean = false
) {
    Column(
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Text(
            text = label,
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 9.sp,
            color = color.copy(alpha = 0.75f)
        )
        Text(
            text = value,
            fontFamily = RajdhaniFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@Composable
private fun FlightCompassRibbon(
    headingDegrees: Int,
    altitudeFeet: Int,
    machSpeed: Float,
    gForce: Float,
    accentColor: Color
) {
    val cardinal = when (((headingDegrees % 360) + 360) % 360) {
        in 338..360, in 0..22 -> "N"
        in 23..67 -> "NE"
        in 68..112 -> "E"
        in 113..157 -> "SE"
        in 158..202 -> "S"
        in 203..247 -> "SW"
        in 248..292 -> "W"
        else -> "NW"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(CutCornerShape(6.dp))
            .background(Color(0x88031622))
            .border(1.dp, TelemetryNominalGreen.copy(alpha = 0.55f), CutCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "HDG ${headingDegrees.toString().padStart(3, '0')}° $cardinal",
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 12.sp,
            color = TelemetryNominalGreen
        )
        Text(
            text = "ALT $altitudeFeet FT",
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 12.sp,
            color = accentColor
        )
        Text(
            text = "MACH ${String.format("%.2f", machSpeed)}",
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 12.sp,
            color = StarkGold
        )
        Text(
            text = "${String.format("%.1f", gForce)} G",
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 12.sp,
            color = Color.White
        )
    }
}
