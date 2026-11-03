package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ArcPowerMode
import com.example.data.ConnectivityTelemetry
import com.example.data.HudWidgetConfig
import com.example.data.HudWidgetType
import com.example.data.STARK_SYNTH_TRACKS
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

// 6. Wi-Fi & Bluetooth Status Widget
@Composable
fun ConnectivityDatalinkWidget(
    size: WidgetSize,
    isRedAlert: Boolean,
    connectivity: ConnectivityTelemetry,
    onPingNetwork: () -> Unit,
    onCycleSize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (isRedAlert) StarkCrimson else ArcCyan

    HudWidgetCard(
        title = "COMMS & DATALINK",
        moduleCode = "MOD-06 // SAT-COMMS",
        size = size,
        isRedAlert = isRedAlert,
        onCycleSize = onCycleSize,
        testTag = "widget_connectivity",
        headerTrailing = {
            Box(
                modifier = Modifier
                    .testTag("connectivity_ping_button")
                    .clip(CutCornerShape(4.dp))
                    .background(accent.copy(alpha = 0.14f))
                    .border(1.dp, accent.copy(alpha = 0.5f), CutCornerShape(4.dp))
                    .clickable(onClick = onPingNetwork)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NetworkCheck,
                        contentDescription = "Run network diagnostic ping",
                        tint = accent,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${connectivity.pingLatencyMs}ms",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 10.sp,
                        color = TelemetryNominalGreen
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (connectivity.wifiConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = "Wi-Fi connectivity state",
                    tint = if (connectivity.wifiConnected) TelemetryNominalGreen else StarkCrimson,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = connectivity.networkTypeLabel,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Text(
                        text = "IP: ${connectivity.localIp}",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 10.sp,
                        color = accent
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (connectivity.bluetoothEnabled) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
                    contentDescription = "Bluetooth state",
                    tint = if (connectivity.bluetoothEnabled) ArcCyanBright else Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (connectivity.bluetoothEnabled) "BT LINKED" else "BT STBY",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    color = if (connectivity.bluetoothEnabled) ArcCyanBright else Color.White.copy(alpha = 0.6f)
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
                    text = "DOWNLINK: ${connectivity.downlinkMbps} MBPS",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = TelemetryNominalGreen
                )
                Text(
                    text = "UPLINK: ${connectivity.uplinkMbps} MBPS",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = StarkGold
                )
            }
        }
    }
}

// 7. Arc Reactor Power Indicator Widget
@Composable
fun ArcReactorTelemetryWidget(
    size: WidgetSize,
    isRedAlert: Boolean,
    powerMode: ArcPowerMode,
    onCyclePowerMode: () -> Unit,
    onCycleSize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (isRedAlert) StarkCrimson else ArcCyan
    val loadFraction = (powerMode.outputGigawatts / 12.40f).coerceIn(0.08f, 1f)

    HudWidgetCard(
        title = "ARC REACTOR CORE",
        moduleCode = "MOD-07 // PALLADIUM-FREE",
        size = size,
        isRedAlert = isRedAlert,
        onCycleSize = onCycleSize,
        testTag = "widget_arc_reactor",
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${String.format("%.2f", powerMode.outputGigawatts)} GW",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
                Text(
                    text = "${powerMode.label} // ${powerMode.coreTempKelvin}K",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    color = accent
                )
            }

            Box(
                modifier = Modifier
                    .testTag("widget_arc_boost_button")
                    .clip(CutCornerShape(6.dp))
                    .background(accent.copy(alpha = 0.2f))
                    .border(1.dp, accent, CutCornerShape(6.dp))
                    .clickable(onClick = onCyclePowerMode)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Cycle Arc Reactor output mode",
                        tint = StarkGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = powerMode.shortCode,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { loadFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CutCornerShape(4.dp)),
            color = if (powerMode == ArcPowerMode.UNIBEAM_OVERDRIVE) StarkGold else accent,
            trackColor = Color(0xFF091B2E)
        )
    }
}

// 8. Music Player Controls Widget
@Composable
fun MusicPlayerMatrixWidget(
    size: WidgetSize,
    isRedAlert: Boolean,
    isPlaying: Boolean,
    currentTrackIndex: Int,
    trackProgress: Float,
    spectrumBands: List<Float>,
    mediaVolumePercent: Int,
    onTogglePlay: () -> Unit,
    onPrevTrack: () -> Unit,
    onNextTrack: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onCycleSize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (isRedAlert) StarkCrimson else ArcCyan
    val track = STARK_SYNTH_TRACKS.getOrElse(currentTrackIndex) { STARK_SYNTH_TRACKS.first() }

    HudWidgetCard(
        title = "STARK AUDIO MATRIX",
        moduleCode = "MOD-08 // SONIC SYNTH",
        size = size,
        isRedAlert = isRedAlert,
        onCycleSize = onCycleSize,
        testTag = "widget_music_player",
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                Text(
                    text = "${track.subtitle} • ${track.bpm} BPM",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    color = accent
                )
            }

            // Transport Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onPrevTrack,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("music_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous track",
                        tint = ArcCyanBright
                    )
                }

                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("music_play_pause_button")
                        .clip(CutCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.24f))
                        .border(1.2.dp, accent, CutCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause music" else "Play music",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = onNextTrack,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("music_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next track",
                        tint = ArcCyanBright
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Live 16-Band Harmonic Spectrum Visualizer
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        ) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val count = spectrumBands.size.coerceAtLeast(1)
            val barWidth = canvasW / count
            spectrumBands.forEachIndexed { index, level ->
                val h = (canvasH * level.coerceIn(0.1f, 1f))
                drawRect(
                    color = if (index % 3 == 0) StarkGold else accent,
                    topLeft = Offset(index * barWidth + 2f, canvasH - h),
                    size = Size((barWidth - 4f).coerceAtLeast(2f), h)
                )
            }
        }

        if (size != WidgetSize.COMPACT) {
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { trackProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = StarkGold,
                trackColor = Color(0xFF0A2036)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GAIN $mediaVolumePercent%",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    color = accent,
                    modifier = Modifier.width(64.dp)
                )
                Slider(
                    value = mediaVolumePercent.toFloat(),
                    onValueChange = { onVolumeChange(it.toInt()) },
                    valueRange = 0f..100f,
                    modifier = Modifier
                        .weight(1f)
                        .height(26.dp)
                        .testTag("music_volume_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = accent,
                        activeTrackColor = accent
                    )
                )
            }
        }
    }
}

// 9. Flashlight Toggle Widget
@Composable
fun FlashlightPhotonWidget(
    size: WidgetSize,
    isRedAlert: Boolean,
    isFlashlightOn: Boolean,
    hasHardwareFlash: Boolean,
    onToggleFlashlight: () -> Unit,
    onCycleSize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (isFlashlightOn) StarkGold else if (isRedAlert) StarkCrimson else ArcCyan

    HudWidgetCard(
        title = "PHOTON TORCH BEAM",
        moduleCode = "MOD-09 // OPTIC EMITTER",
        size = size,
        isRedAlert = isRedAlert,
        onCycleSize = onCycleSize,
        accentColor = accent,
        testTag = "widget_flashlight",
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isFlashlightOn) "BEAM ENGAGED // 1200 LM" else "OPTIC EMITTER STANDBY",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isFlashlightOn) StarkGold else Color.White
                )
                Text(
                    text = if (hasHardwareFlash) "HARDWARE LED TORCH" else "HUD PHOTON STROBE READY",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    color = accent
                )
            }

            Box(
                modifier = Modifier
                    .testTag("flashlight_toggle_button")
                    .clip(CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
                    .background(if (isFlashlightOn) StarkGold.copy(alpha = 0.28f) else accent.copy(alpha = 0.14f))
                    .border(1.5.dp, accent, CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
                    .clickable(onClick = onToggleFlashlight)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = "Toggle flashlight",
                        tint = if (isFlashlightOn) StarkGold else accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFlashlightOn) "ACTIVE" else "ENGAGE",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// 10. Quick Settings Panel & Customizable Widget Size Matrix
@Composable
fun QuickSettingsMatrixWidget(
    size: WidgetSize,
    isRedAlert: Boolean,
    isFlightMode: Boolean,
    isSfxEnabled: Boolean,
    isVoiceTtsEnabled: Boolean,
    isFlashlightOn: Boolean,
    isParallaxEnabled: Boolean,
    widgetConfigs: List<HudWidgetConfig>,
    onToggleRedAlert: () -> Unit,
    onToggleFlightMode: () -> Unit,
    onToggleSfx: () -> Unit,
    onToggleVoiceTts: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onToggleParallax: () -> Unit,
    onCycleWidgetSize: (HudWidgetType) -> Unit,
    onToggleWidgetVisibility: (HudWidgetType) -> Unit,
    onResetWidgetLayout: () -> Unit,
    onCycleSize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (isRedAlert) StarkCrimson else ArcCyan

    HudWidgetCard(
        title = "TACTICAL QUICK MATRIX",
        moduleCode = "MOD-10 // SYSTEM OVERRIDES",
        size = size,
        isRedAlert = isRedAlert,
        onCycleSize = onCycleSize,
        testTag = "widget_quick_settings",
        headerTrailing = {
            Box(
                modifier = Modifier
                    .testTag("reset_hud_widgets_button")
                    .clip(CutCornerShape(4.dp))
                    .background(accent.copy(alpha = 0.14f))
                    .border(1.dp, accent.copy(alpha = 0.5f), CutCornerShape(4.dp))
                    .clickable(onClick = onResetWidgetLayout)
                    .padding(horizontal = 7.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Reset widget layout",
                        tint = accent,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RESET",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 10.sp,
                        color = accent
                    )
                }
            }
        },
        modifier = modifier
    ) {
        // Row 1 of Quick Tactical Toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickTileButton(
                label = "RED ALERT",
                subLabel = if (isRedAlert) "DEFCON-1" else "STANDBY",
                active = isRedAlert,
                activeColor = StarkCrimson,
                icon = Icons.Default.WarningAmber,
                onClick = onToggleRedAlert,
                testTag = "qs_red_alert_tile",
                modifier = Modifier.weight(1f)
            )
            QuickTileButton(
                label = "FLIGHT HUD",
                subLabel = if (isFlightMode) "MACH 1.4" else "GROUND",
                active = isFlightMode,
                activeColor = TelemetryNominalGreen,
                icon = Icons.Default.FlightTakeoff,
                onClick = onToggleFlightMode,
                testTag = "qs_flight_mode_tile",
                modifier = Modifier.weight(1f)
            )
            QuickTileButton(
                label = "TORCH",
                subLabel = if (isFlashlightOn) "1200 LM" else "OFF",
                active = isFlashlightOn,
                activeColor = StarkGold,
                icon = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                onClick = onToggleFlashlight,
                testTag = "qs_flashlight_tile",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 2 of Quick Tactical Toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickTileButton(
                label = "HUD SFX",
                subLabel = if (isSfxEnabled) "SYNTH ON" else "MUTED",
                active = isSfxEnabled,
                activeColor = ArcCyan,
                icon = if (isSfxEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                onClick = onToggleSfx,
                testTag = "qs_sfx_tile",
                modifier = Modifier.weight(1f)
            )
            QuickTileButton(
                label = "VOICE TTS",
                subLabel = if (isVoiceTtsEnabled) "UK MALE" else "SILENT",
                active = isVoiceTtsEnabled,
                activeColor = ArcCyanBright,
                icon = Icons.Default.RecordVoiceOver,
                onClick = onToggleVoiceTts,
                testTag = "qs_tts_tile",
                modifier = Modifier.weight(1f)
            )
            QuickTileButton(
                label = "3D PARALLAX",
                subLabel = if (isParallaxEnabled) "GYRO LOCK" else "STATIC",
                active = isParallaxEnabled,
                activeColor = ArcElectricBlue,
                icon = Icons.Default.Bolt,
                onClick = onToggleParallax,
                testTag = "qs_parallax_tile",
                modifier = Modifier.weight(1f)
            )
        }

        // Customizable Widget Sizes & Visibility Matrix when Wide or Expanded
        if (size != WidgetSize.COMPACT) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "HOLOGRAPHIC WIDGET SIZE & VISIBILITY MATRIX",
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 11.sp,
                color = accent,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                widgetConfigs.forEach { cfg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CutCornerShape(6.dp))
                            .background(Color(0x66040D1A))
                            .border(1.dp, accent.copy(alpha = 0.25f), CutCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cfg.type.title,
                            fontFamily = RajdhaniFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = if (cfg.isVisible) Color.White else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.weight(1f)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .testTag("matrix_size_${cfg.type.id}")
                                    .clip(CutCornerShape(4.dp))
                                    .background(accent.copy(alpha = 0.16f))
                                    .border(1.dp, accent.copy(alpha = 0.55f), CutCornerShape(4.dp))
                                    .clickable { onCycleWidgetSize(cfg.type) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "SIZE: ${cfg.size.badge}",
                                    fontFamily = ShareTechMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = accent
                                )
                            }

                            if (cfg.type != HudWidgetType.QUICK_SETTINGS) {
                                Box(
                                    modifier = Modifier
                                        .testTag("matrix_vis_${cfg.type.id}")
                                        .clip(CutCornerShape(4.dp))
                                        .background(
                                            if (cfg.isVisible) TelemetryNominalGreen.copy(alpha = 0.16f)
                                            else StarkCrimson.copy(alpha = 0.16f)
                                        )
                                        .clickable { onToggleWidgetVisibility(cfg.type) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (cfg.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle visibility for ${cfg.type.title}",
                                        tint = if (cfg.isVisible) TelemetryNominalGreen else StarkCrimson,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickTileButton(
    label: String,
    subLabel: String,
    active: Boolean,
    activeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp)
    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(shape)
            .background(if (active) activeColor.copy(alpha = 0.24f) else Color(0x66040D1A))
            .border(1.2.dp, if (active) activeColor else activeColor.copy(alpha = 0.35f), shape)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) activeColor else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.White
            )
            Text(
                text = subLabel,
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 9.sp,
                color = if (active) activeColor else Color.White.copy(alpha = 0.55f)
            )
        }
    }
}
