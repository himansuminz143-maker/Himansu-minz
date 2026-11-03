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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryTelemetry
import com.example.data.STARK_LOCATIONS
import com.example.data.StarkLocationPreset
import com.example.data.SystemTelemetry
import com.example.data.VoiceMessage
import com.example.data.WeatherTelemetry
import com.example.data.WidgetSize
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanBright
import com.example.ui.theme.ArcElectricBlue
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.StarkCrimson
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TelemetryNominalGreen
import kotlin.math.PI
import kotlin.math.sin

private val QUICK_VOICE_COMMANDS = listOf(
    "Status Report",
    "Red Alert",
    "Engage Flight Mode",
    "Overcharge Arc Reactor",
    "Toggle Flashlight",
    "Play Stark Mix",
    "Weather Scan",
    "Nominal Mode"
)

// 1. JARVIS AI Voice Assistant Widget with Microphone
@Composable
fun JarvisVoiceAssistantWidget(
    size: WidgetSize,
    isRedAlert: Boolean,
    isListening: Boolean,
    isSpeaking: Boolean,
    isProcessingAi: Boolean,
    messages: List<VoiceMessage>,
    onToggleMicListening: () -> Unit,
    onSendCommand: (String) -> Unit,
    onSpeakMessage: (String) -> Unit,
    onCycleSize: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputCommand by remember { mutableStateOf("") }
    val accent = if (isRedAlert) StarkCrimson else ArcCyan

    val infiniteTransition = rememberInfiniteTransition(label = "voice_wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    HudWidgetCard(
        title = "J.A.R.V.I.S. VOICE CORE",
        moduleCode = "MOD-01 // AI NEURAL LINK",
        size = size,
        isRedAlert = isRedAlert,
        onCycleSize = onCycleSize,
        testTag = "widget_voice_assistant",
        modifier = modifier
    ) {
        // Microphone Button + Live Waveform Visualizer Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Interactive Glowing Microphone Orb
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .testTag("jarvis_mic_button")
                    .clip(CircleShape)
                    .background(
                        if (isListening) StarkCrimson.copy(alpha = 0.28f)
                        else accent.copy(alpha = 0.18f)
                    )
                    .border(
                        width = 2.dp,
                        color = if (isListening) StarkCrimson else accent,
                        shape = CircleShape
                    )
                    .clickable(onClick = onToggleMicListening),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = "Activate JARVIS voice microphone",
                    tint = if (isListening) Color.White else accent,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Animated Voice Harmonic Waveform Canvas
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            isListening -> "MIC ACTIVE // LISTENING FOR VOICE..."
                            isProcessingAi -> "NEURAL MATRIX // COMPUTING..."
                            isSpeaking -> "J.A.R.V.I.S. // VOCALIZING"
                            else -> "VOICE LINK READY // TAP MIC OR COMMAND"
                        },
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp,
                        color = when {
                            isListening -> StarkCrimson
                            isSpeaking || isProcessingAi -> StarkGold
                            else -> accent
                        }
                    )
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                ) {
                    val canvasW = this.size.width
                    val canvasH = this.size.height
                    val barCount = 28
                    val spacing = canvasW / barCount
                    val activeWave = isListening || isSpeaking || isProcessingAi
                    for (i in 0 until barCount) {
                        val norm = i.toFloat() / barCount
                        val amp = if (activeWave) {
                            (0.25f + 0.72f * kotlin.math.abs(sin(wavePhase + norm * 6f)))
                        } else {
                            0.18f + 0.10f * kotlin.math.abs(sin(norm * 4f))
                        }
                        val barH = (canvasH * amp).coerceAtLeast(4f)
                        val topY = (canvasH - barH) / 2f
                        drawRect(
                            color = if (isListening) StarkCrimson else if (isSpeaking) StarkGold else accent,
                            topLeft = Offset(i * spacing + 2f, topY),
                            size = Size((spacing - 4f).coerceAtLeast(2f), barH)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Tactical Voice Command Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QUICK_VOICE_COMMANDS.forEach { preset ->
                Box(
                    modifier = Modifier
                        .testTag("voice_preset_${preset.lowercase().replace(" ", "_")}")
                        .clip(CutCornerShape(6.dp))
                        .background(accent.copy(alpha = 0.12f))
                        .border(1.dp, accent.copy(alpha = 0.45f), CutCornerShape(6.dp))
                        .clickable { onSendCommand(preset) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset.uppercase(),
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }
        }

        if (size != WidgetSize.COMPACT) {
            Spacer(modifier = Modifier.height(10.dp))

            // Latest JARVIS Console Messages
            val visibleMessages = if (size == WidgetSize.EXPANDED) messages.takeLast(3) else messages.takeLast(1)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CutCornerShape(8.dp))
                    .background(Color(0x99030A14))
                    .border(1.dp, accent.copy(alpha = 0.3f), CutCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                visibleMessages.forEach { msg ->
                    val isJarvis = msg.speaker == "J.A.R.V.I.S."
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "[${msg.timestamp}] ${msg.speaker}:",
                                    fontFamily = ShareTechMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = if (isJarvis) accent else StarkGold
                                )
                                if (msg.actionBadge != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = msg.actionBadge,
                                        fontFamily = ShareTechMonoFontFamily,
                                        fontSize = 9.sp,
                                        color = TelemetryNominalGreen
                                    )
                                }
                            }
                            Text(
                                text = msg.text,
                                fontFamily = RajdhaniFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        if (isJarvis) {
                            IconButton(
                                onClick = { onSpeakMessage(msg.text) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Speak JARVIS response",
                                    tint = accent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Direct Tactical Command Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputCommand,
                    onValueChange = { inputCommand = it },
                    placeholder = {
                        Text(
                            text = "Transmit command to J.A.R.V.I.S...",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 12.sp,
                            color = accent.copy(alpha = 0.5f)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputCommand.isNotBlank()) {
                                onSendCommand(inputCommand)
                                inputCommand = ""
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        unfocusedBorderColor = accent.copy(alpha = 0.4f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = accent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("jarvis_command_input")
                )

                IconButton(
                    onClick = {
                        if (inputCommand.isNotBlank()) {
                            onSendCommand(inputCommand)
                            inputCommand = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("jarvis_send_command_button")
                        .clip(CutCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.22f))
                        .border(1.dp, accent, CutCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send command to JARVIS",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// 2. Live Digital Clock & Date Widget
@Composable
fun LiveDigitalClockWidget(
    size: WidgetSize,
    isRedAlert: Boolean,
    timeString: String,
    secondsString: String,
    dateString: String,
    utcString: String,
    uptimeFormatted: String,
    onCycleSize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (isRedAlert) StarkCrimson else ArcCyan

    HudWidgetCard(
        title = "CHRONO TELEMETRY",
        moduleCode = "MOD-02 // ATOMIC SYNC",
        size = size,
        isRedAlert = isRedAlert,
        onCycleSize = onCycleSize,
        testTag = "widget_clock_date",
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = timeString,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (size == WidgetSize.COMPACT) 24.sp else 32.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = ":$secondsString",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 16.sp,
                    color = accent,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
            Text(
                text = "ATOMIC LOCK",
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 10.sp,
                color = TelemetryNominalGreen
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dateString.uppercase(),
            fontFamily = RajdhaniFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = ArcCyanBright
        )

        if (size != WidgetSize.COMPACT) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = utcString,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
                Text(
                    text = "MISSION T+ $uptimeFormatted",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = StarkGold
                )
            }
        }
    }
}

// 3. Battery Percentage & Charging Status Widget
@Composable
fun BatteryPowerWidget(
    size: WidgetSize,
    isRedAlert: Boolean,
    battery: BatteryTelemetry,
    onCycleSize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (isRedAlert) StarkCrimson else ArcCyan
    val statusColor = when {
        battery.percentage <= 20 -> StarkCrimson
        battery.isCharging -> TelemetryNominalGreen
        else -> accent
    }

    HudWidgetCard(
        title = "POWER CELLS & THERMAL",
        moduleCode = "MOD-03 // ENERGY BUS",
        size = size,
        isRedAlert = isRedAlert,
        onCycleSize = onCycleSize,
        testTag = "widget_battery_status",
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (battery.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryStd,
                    contentDescription = "Battery charging status",
                    tint = statusColor,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${battery.percentage}%",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (battery.isCharging) "CHARGING" else "DISCHARGING",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    color = statusColor
                )
                Text(
                    text = "${battery.temperatureC}°C // ${battery.voltageMv}mV",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 12-Segment Glowing Power Cell Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val activeCells = ((battery.percentage / 100f) * 12).toInt().coerceIn(1, 12)
            for (i in 1..12) {
                val lit = i <= activeCells
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(CutCornerShape(2.dp))
                        .background(
                            if (lit) statusColor else statusColor.copy(alpha = 0.14f)
                        )
                )
            }
        }

        if (size != WidgetSize.COMPACT) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SOURCE: ${battery.chargeSource}",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = ArcCyanBright
                )
                Text(
                    text = "EST: ${String.format("%.1f", battery.estimatedHoursRemaining)} HRS",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = StarkGold
                )
            }
        }
    }
}

// 4. Weather Information Widget
@Composable
fun WeatherSensorWidget(
    size: WidgetSize,
    isRedAlert: Boolean,
    weather: WeatherTelemetry,
    onSelectFacility: (StarkLocationPreset) -> Unit,
    onRequestGpsWeather: () -> Unit,
    onRefreshWeather: () -> Unit,
    onCycleSize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (isRedAlert) StarkCrimson else ArcCyan

    HudWidgetCard(
        title = "ATMOSPHERIC SENSORS",
        moduleCode = "MOD-04 // SAT-METEO",
        size = size,
        isRedAlert = isRedAlert,
        onCycleSize = onCycleSize,
        testTag = "widget_weather_radar",
        headerTrailing = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .testTag("weather_gps_button")
                        .clip(CutCornerShape(4.dp))
                        .background(accent.copy(alpha = 0.14f))
                        .border(1.dp, accent.copy(alpha = 0.5f), CutCornerShape(4.dp))
                        .clickable(onClick = onRequestGpsWeather)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Locate GPS sector for weather",
                        tint = accent,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .testTag("weather_refresh_button")
                        .clip(CutCornerShape(4.dp))
                        .background(accent.copy(alpha = 0.14f))
                        .border(1.dp, accent.copy(alpha = 0.5f), CutCornerShape(4.dp))
                        .clickable(onClick = onRefreshWeather)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Refresh satellite weather",
                        tint = accent,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${weather.locationName} [${weather.sectorCode}]",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = accent
                )
                Text(
                    text = weather.conditionLabel,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Text(
                    text = weather.flightCeilingStatus,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    color = TelemetryNominalGreen
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format("%.1f", weather.tempCelsius)}°C",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = Color.White
                )
                Text(
                    text = "FEELS ${String.format("%.1f", weather.feelsLikeCelsius)}°C",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    color = StarkGold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "WIND: ${weather.windSpeedKmh} KM/H (${weather.windDirectionDeg}°)",
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                text = "HUM: ${weather.humidityPercent}% // ${weather.pressureHpa.toInt()} hPa",
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 11.sp,
                color = ArcCyanBright
            )
        }

        if (size != WidgetSize.COMPACT) {
            Spacer(modifier = Modifier.height(8.dp))
            // Stark Global Sector Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                STARK_LOCATIONS.forEach { loc ->
                    val selected = weather.locationName == loc.name
                    Box(
                        modifier = Modifier
                            .testTag("weather_loc_${loc.sectorCode.lowercase()}")
                            .clip(CutCornerShape(5.dp))
                            .background(if (selected) accent.copy(alpha = 0.25f) else Color(0x55040D1A))
                            .border(1.dp, if (selected) accent else accent.copy(alpha = 0.35f), CutCornerShape(5.dp))
                            .clickable { onSelectFacility(loc) }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = loc.name,
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 10.sp,
                            color = if (selected) Color.White else accent
                        )
                    }
                }
            }
        }
    }
}

// 5. CPU & RAM Monitoring Widget
@Composable
fun CpuRamTelemetryWidget(
    size: WidgetSize,
    isRedAlert: Boolean,
    telemetry: SystemTelemetry,
    onPurgeMemoryCache: () -> Unit,
    onCycleSize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (isRedAlert) StarkCrimson else ArcCyan

    HudWidgetCard(
        title = "NEURAL CPU & MEMORY",
        moduleCode = "MOD-05 // CORE DIAGNOSTICS",
        size = size,
        isRedAlert = isRedAlert,
        onCycleSize = onCycleSize,
        testTag = "widget_cpu_ram",
        headerTrailing = {
            Box(
                modifier = Modifier
                    .testTag("cpu_ram_purge_button")
                    .clip(CutCornerShape(4.dp))
                    .background(StarkGold.copy(alpha = 0.16f))
                    .border(1.dp, StarkGold.copy(alpha = 0.6f), CutCornerShape(4.dp))
                    .clickable(onClick = onPurgeMemoryCache)
                    .padding(horizontal = 7.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Optimize neural memory buffer",
                        tint = StarkGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "BOOST",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 10.sp,
                        color = StarkGold
                    )
                }
            }
        },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "CPU LOAD (${telemetry.cpuCores} CORES)",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    color = accent
                )
                Text(
                    text = "${telemetry.cpuUsagePercent}%",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "RAM MATRIX (${telemetry.ramUsedMb}/${telemetry.ramTotalMb} MB)",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    color = StarkGold
                )
                Text(
                    text = "${telemetry.ramUsagePercent}%",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
            }
        }

        if (size != WidgetSize.COMPACT) {
            Spacer(modifier = Modifier.height(8.dp))

            // Dual-Trace Live Telemetry Waveform Graph
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(CutCornerShape(6.dp))
                    .background(Color(0x88020812))
                    .border(1.dp, accent.copy(alpha = 0.3f), CutCornerShape(6.dp))
                    .padding(6.dp)
            ) {
                val w = this.size.width
                val h = this.size.height

                // Draw CPU trace (Cyan/Crimson)
                if (telemetry.cpuHistory.size > 1) {
                    val cpuPath = Path()
                    telemetry.cpuHistory.forEachIndexed { idx, value ->
                        val x = (idx.toFloat() / (telemetry.cpuHistory.size - 1)) * w
                        val y = h - (value.coerceIn(0.05f, 0.98f) * h)
                        if (idx == 0) cpuPath.moveTo(x, y) else cpuPath.lineTo(x, y)
                    }
                    drawPath(cpuPath, color = accent, style = Stroke(width = 2.2.dp.toPx()))
                }

                // Draw RAM trace (Gold)
                if (telemetry.ramHistory.size > 1) {
                    val ramPath = Path()
                    telemetry.ramHistory.forEachIndexed { idx, value ->
                        val x = (idx.toFloat() / (telemetry.ramHistory.size - 1)) * w
                        val y = h - (value.coerceIn(0.05f, 0.98f) * h)
                        if (idx == 0) ramPath.moveTo(x, y) else ramPath.lineTo(x, y)
                    }
                    drawPath(ramPath, color = StarkGold, style = Stroke(width = 1.8.dp.toPx()))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ACTIVE THREADS: ${telemetry.activeThreads}",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    color = ArcElectricBlue
                )
                Text(
                    text = "JVM HEAP FREE: ${telemetry.jvmFreeMb} MB",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    color = TelemetryNominalGreen
                )
            }
        }
    }
}
