package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HudWidgetConfig
import com.example.data.HudWidgetType
import com.example.data.WidgetSize
import com.example.ui.components.ArcReactorCenterpiece
import com.example.ui.components.ArcReactorTelemetryWidget
import com.example.ui.components.BatteryPowerWidget
import com.example.ui.components.BootSequenceOverlay
import com.example.ui.components.ConnectivityDatalinkWidget
import com.example.ui.components.CpuRamTelemetryWidget
import com.example.ui.components.FlashlightPhotonWidget
import com.example.ui.components.HudHologramBackground
import com.example.ui.components.JarvisVoiceAssistantWidget
import com.example.ui.components.LiveDigitalClockWidget
import com.example.ui.components.MusicPlayerMatrixWidget
import com.example.ui.components.QuickSettingsMatrixWidget
import com.example.ui.components.WeatherSensorWidget
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanBright
import com.example.ui.theme.HudDeepSpace
import com.example.ui.theme.HudPitchBlack
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.RajdhaniFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.StarkCrimson
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TelemetryNominalGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisHudScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val isBooting by viewModel.isBooting.collectAsStateWithLifecycle()
    val bootProgress by viewModel.bootProgress.collectAsStateWithLifecycle()
    val bootStageLabel by viewModel.bootStageLabel.collectAsStateWithLifecycle()

    val isRedAlert by viewModel.isRedAlert.collectAsStateWithLifecycle()
    val isFlightMode by viewModel.isFlightMode.collectAsStateWithLifecycle()
    val arcPowerMode by viewModel.arcPowerMode.collectAsStateWithLifecycle()
    val isSfxEnabled by viewModel.isSfxEnabled.collectAsStateWithLifecycle()
    val isVoiceTtsEnabled by viewModel.isVoiceTtsEnabled.collectAsStateWithLifecycle()
    val isParallaxEnabled by viewModel.isParallaxEnabled.collectAsStateWithLifecycle()

    val timeString by viewModel.timeString.collectAsStateWithLifecycle()
    val secondsString by viewModel.secondsString.collectAsStateWithLifecycle()
    val dateString by viewModel.dateString.collectAsStateWithLifecycle()
    val utcString by viewModel.utcString.collectAsStateWithLifecycle()
    val uptimeFormatted by viewModel.uptimeFormatted.collectAsStateWithLifecycle()

    val battery by viewModel.batteryTelemetry.collectAsStateWithLifecycle()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsStateWithLifecycle()
    val hasHardwareFlash by viewModel.hasHardwareFlash.collectAsStateWithLifecycle()
    val systemTelemetry by viewModel.systemTelemetry.collectAsStateWithLifecycle()
    val connectivity by viewModel.connectivityTelemetry.collectAsStateWithLifecycle()
    val weather by viewModel.weatherTelemetry.collectAsStateWithLifecycle()
    val flightTelemetry by viewModel.flightTelemetry.collectAsStateWithLifecycle()
    val mediaVolumePercent by viewModel.mediaVolumePercent.collectAsStateWithLifecycle()

    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isProcessingAi by viewModel.isProcessingAi.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.soundEngine.isSpeaking.collectAsStateWithLifecycle()
    val voiceMessages by viewModel.voiceMessages.collectAsStateWithLifecycle()

    val isMusicPlaying by viewModel.soundEngine.isMusicPlaying.collectAsStateWithLifecycle()
    val currentTrackIndex by viewModel.soundEngine.currentTrackIndex.collectAsStateWithLifecycle()
    val trackProgress by viewModel.soundEngine.trackProgress.collectAsStateWithLifecycle()
    val spectrumBands by viewModel.soundEngine.spectrumBands.collectAsStateWithLifecycle()

    val widgetConfigs by viewModel.widgetConfigs.collectAsStateWithLifecycle()
    val apkMetadata by viewModel.apkMetadata.collectAsStateWithLifecycle()
    val apkExportStatus by viewModel.apkExportStatus.collectAsStateWithLifecycle()

    var showWidgetCustomizerSheet by remember { mutableStateOf(false) }
    var showApkDownloadSheet by remember { mutableStateOf(false) }

    val saveApkDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")
    ) { targetUri ->
        if (targetUri != null) {
            viewModel.saveApkToChosenUri(targetUri)
        }
    }

    // Runtime Permission Launchers
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startSpeechRecognition()
        } else {
            viewModel.processVoiceOrTextCommand("Status Report")
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.updateWeatherFromDeviceGps()
        } else {
            viewModel.refreshWeather()
        }
    }

    val onRequestMic = {
        val hasPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            viewModel.startSpeechRecognition()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val onRequestLocationWeather = {
        val hasPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            viewModel.updateWeatherFromDeviceGps()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    val effectivePitch = if (isParallaxEnabled) flightTelemetry.pitchDegrees else 0f
    val effectiveRoll = if (isParallaxEnabled) flightTelemetry.rollDegrees else 0f
    val primaryAccent = if (isRedAlert) StarkCrimson else ArcCyan

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HudPitchBlack)
    ) {
        // 1. Animated Holographic Background Grid & Laser Scanner
        HudHologramBackground(
            isRedAlert = isRedAlert,
            isFlightMode = isFlightMode,
            parallaxPitch = effectivePitch,
            parallaxRoll = effectiveRoll
        )

        // 2. Main Interactive Iron Man HUD Interface
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onRequestMic,
                    shape = CircleShape,
                    containerColor = if (isListening) StarkCrimson else primaryAccent,
                    contentColor = HudPitchBlack,
                    modifier = Modifier.testTag("fab_jarvis_mic")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Speak to JARVIS"
                    )
                }
            }
        ) { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val isWideTablet = maxWidth >= 700.dp

                // Group visible widgets into logical rows:
                // - COMPACT (1x1) widgets pair up 2-per-row
                // - WIDE (2x1) and EXPANDED (2x2) widgets span full width on handheld (or pair on wide tablets)
                val visibleWidgets = remember(widgetConfigs) {
                    widgetConfigs.filter { it.isVisible }
                }
                val widgetRows = remember(visibleWidgets, isWideTablet) {
                    buildAdaptiveWidgetRows(visibleWidgets, isWideTablet)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("jarvis_hud_scroll_container"),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 8.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Top Visor Telemetry Header Bar
                    item(key = "hud_top_visor_bar") {
                        HudTopVisorBar(
                            isRedAlert = isRedAlert,
                            isFlightMode = isFlightMode,
                            isFlashlightOn = isFlashlightOn,
                            batteryPct = battery.percentage,
                            arcGw = arcPowerMode.outputGigawatts,
                            onOpenApkDownloader = {
                                viewModel.soundEngine.playHudTap()
                                showApkDownloadSheet = true
                            },
                            onOpenWidgetCustomizer = {
                                viewModel.soundEngine.playHudTap()
                                showWidgetCustomizerSheet = true
                            },
                            onToggleFlashlight = { viewModel.toggleFlashlight() }
                        )
                    }

                    // Photon Torch High-Luminance Banner when Flashlight is active
                    if (isFlashlightOn) {
                        item(key = "photon_torch_active_banner") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CutCornerShape(8.dp))
                                    .background(Color(0xEEFFFDF5))
                                    .border(2.dp, StarkGold, CutCornerShape(8.dp))
                                    .clickable { viewModel.toggleFlashlight(false) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FlashlightOn,
                                        contentDescription = null,
                                        tint = HudPitchBlack
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "PHOTON TORCH EMITTER ACTIVE // TAP TO EXTINGUISH",
                                        fontFamily = OrbitronFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = HudPitchBlack
                                    )
                                }
                                Text(
                                    text = "1200 LM",
                                    fontFamily = ShareTechMonoFontFamily,
                                    fontSize = 11.sp,
                                    color = StarkCrimson
                                )
                            }
                        }
                    }

                    // Centerpiece Animated 3D Arc Reactor & Flight Horizon HUD
                    item(key = "arc_reactor_centerpiece") {
                        ArcReactorCenterpiece(
                            powerMode = arcPowerMode,
                            isRedAlert = isRedAlert,
                            isFlightMode = isFlightMode,
                            isVoiceActive = isListening || isSpeaking || isProcessingAi,
                            flightTelemetry = flightTelemetry,
                            onCyclePowerMode = viewModel::cycleArcPowerMode,
                            onSelectPowerMode = viewModel::selectArcPowerMode,
                            onToggleRedAlert = { viewModel.toggleRedAlert() },
                            onToggleFlightMode = { viewModel.toggleFlightMode() },
                            onUpdateFlightTrim = viewModel::updateFlightTrim
                        )
                    }

                    // Render all 10 Interactive HUD Widgets in adaptive rows
                    items(
                        count = widgetRows.size,
                        key = { idx -> widgetRows[idx].joinToString("_") { it.type.id } }
                    ) { rowIndex ->
                        val rowConfigs = widgetRows[rowIndex]
                        if (rowConfigs.size == 1) {
                            RenderHudWidget(
                                config = rowConfigs[0],
                                viewModel = viewModel,
                                isRedAlert = isRedAlert,
                                isFlightMode = isFlightMode,
                                isSfxEnabled = isSfxEnabled,
                                isVoiceTtsEnabled = isVoiceTtsEnabled,
                                isParallaxEnabled = isParallaxEnabled,
                                isListening = isListening,
                                isSpeaking = isSpeaking,
                                isProcessingAi = isProcessingAi,
                                voiceMessages = voiceMessages,
                                timeString = timeString,
                                secondsString = secondsString,
                                dateString = dateString,
                                utcString = utcString,
                                uptimeFormatted = uptimeFormatted,
                                battery = battery,
                                weather = weather,
                                systemTelemetry = systemTelemetry,
                                connectivity = connectivity,
                                arcPowerMode = arcPowerMode,
                                isMusicPlaying = isMusicPlaying,
                                currentTrackIndex = currentTrackIndex,
                                trackProgress = trackProgress,
                                spectrumBands = spectrumBands,
                                mediaVolumePercent = mediaVolumePercent,
                                isFlashlightOn = isFlashlightOn,
                                hasHardwareFlash = hasHardwareFlash,
                                allWidgetConfigs = widgetConfigs,
                                onRequestMic = onRequestMic,
                                onRequestLocationWeather = onRequestLocationWeather,
                                parallaxRoll = effectiveRoll,
                                parallaxPitch = effectivePitch,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                rowConfigs.forEach { cfg ->
                                    RenderHudWidget(
                                        config = cfg,
                                        viewModel = viewModel,
                                        isRedAlert = isRedAlert,
                                        isFlightMode = isFlightMode,
                                        isSfxEnabled = isSfxEnabled,
                                        isVoiceTtsEnabled = isVoiceTtsEnabled,
                                        isParallaxEnabled = isParallaxEnabled,
                                        isListening = isListening,
                                        isSpeaking = isSpeaking,
                                        isProcessingAi = isProcessingAi,
                                        voiceMessages = voiceMessages,
                                        timeString = timeString,
                                        secondsString = secondsString,
                                        dateString = dateString,
                                        utcString = utcString,
                                        uptimeFormatted = uptimeFormatted,
                                        battery = battery,
                                        weather = weather,
                                        systemTelemetry = systemTelemetry,
                                        connectivity = connectivity,
                                        arcPowerMode = arcPowerMode,
                                        isMusicPlaying = isMusicPlaying,
                                        currentTrackIndex = currentTrackIndex,
                                        trackProgress = trackProgress,
                                        spectrumBands = spectrumBands,
                                        mediaVolumePercent = mediaVolumePercent,
                                        isFlashlightOn = isFlashlightOn,
                                        hasHardwareFlash = hasHardwareFlash,
                                        allWidgetConfigs = widgetConfigs,
                                        onRequestMic = onRequestMic,
                                        onRequestLocationWeather = onRequestLocationWeather,
                                        parallaxRoll = effectiveRoll,
                                        parallaxPitch = effectivePitch,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Widget Size & Visibility Customizer Bottom Sheet
        if (showWidgetCustomizerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showWidgetCustomizerSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = HudDeepSpace,
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "STARK HOLOGRAM MATRIX CONFIG",
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = primaryAccent
                            )
                            Text(
                                text = "Customize widget sizes (1x1 Compact, 2x1 Wide, 2x2 Expanded) & visibility",
                                fontFamily = RajdhaniFontFamily,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(CutCornerShape(6.dp))
                                .background(primaryAccent.copy(alpha = 0.2f))
                                .border(1.dp, primaryAccent, CutCornerShape(6.dp))
                                .clickable { viewModel.resetWidgetLayout() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "RESET DEFAULT",
                                fontFamily = ShareTechMonoFontFamily,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    widgetConfigs.forEach { cfg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(CutCornerShape(6.dp))
                                .background(Color(0x99040C18))
                                .border(1.dp, primaryAccent.copy(alpha = 0.3f), CutCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cfg.type.title,
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (cfg.isVisible) Color.White else Color.White.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = cfg.size.label,
                                    fontFamily = ShareTechMonoFontFamily,
                                    fontSize = 10.sp,
                                    color = primaryAccent
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                WidgetSize.entries.forEach { targetSize ->
                                    val selected = cfg.size == targetSize
                                    Box(
                                        modifier = Modifier
                                            .clip(CutCornerShape(4.dp))
                                            .background(
                                                if (selected) primaryAccent.copy(alpha = 0.3f)
                                                else Color.Transparent
                                            )
                                            .border(
                                                1.dp,
                                                if (selected) primaryAccent else primaryAccent.copy(alpha = 0.35f),
                                                CutCornerShape(4.dp)
                                            )
                                            .clickable {
                                                while (
                                                    viewModel.widgetConfigs.value
                                                        .first { it.type == cfg.type }.size != targetSize
                                                ) {
                                                    viewModel.cycleWidgetSize(cfg.type)
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = targetSize.badge,
                                            fontFamily = ShareTechMonoFontFamily,
                                            fontSize = 11.sp,
                                            color = if (selected) Color.White else primaryAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // 3b. Stark APK Mobile Download & Export Console Bottom Sheet
        if (showApkDownloadSheet) {
            ModalBottomSheet(
                onDismissRequest = { showApkDownloadSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = HudDeepSpace,
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "STARK APK MOBILE DOWNLOAD CONSOLE",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = primaryAccent
                    )
                    Text(
                        text = "Export or download the compiled, signed Mark LXXXV Android package directly from code.",
                        fontFamily = RajdhaniFontFamily,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    // APK Binary Metadata Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CutCornerShape(8.dp))
                            .background(Color(0xAA040C18))
                            .border(1.dp, primaryAccent.copy(alpha = 0.45f), CutCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "FILE: ${apkMetadata.fileName} (v${apkMetadata.versionName})",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        Text(
                            text = "PACKAGE ID: ${apkMetadata.packageName}",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 11.sp,
                            color = primaryAccent
                        )
                        Text(
                            text = "BINARY SIZE: ${apkMetadata.sizeMegabytes} • SHA-256: ${apkMetadata.sha256Short}",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 11.sp,
                            color = StarkGold
                        )
                    }

                    if (!apkExportStatus.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CutCornerShape(6.dp))
                                .background(TelemetryNominalGreen.copy(alpha = 0.16f))
                                .border(1.dp, TelemetryNominalGreen, CutCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = apkExportStatus.orEmpty(),
                                fontFamily = ShareTechMonoFontFamily,
                                fontSize = 11.sp,
                                color = TelemetryNominalGreen
                            )
                        }
                    }

                    // Action 1: Direct Download to Device Downloads Folder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("apk_direct_download_button")
                            .clip(CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
                            .background(primaryAccent.copy(alpha = 0.24f))
                            .border(1.5.dp, primaryAccent, CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
                            .clickable { viewModel.downloadApkToMobileDownloads() }
                            .padding(vertical = 12.dp, horizontal = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "1. DOWNLOAD APK TO DOWNLOADS FOLDER",
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }

                    // Action 2: Save APK File As (Storage Picker)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("apk_save_as_button")
                            .clip(CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
                            .background(StarkGold.copy(alpha = 0.18f))
                            .border(1.2.dp, StarkGold, CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
                            .clickable {
                                saveApkDocumentLauncher.launch(apkMetadata.fileName)
                            }
                            .padding(vertical = 12.dp, horizontal = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = StarkGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "2. SAVE APK FILE TO CHOSEN FOLDER...",
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }

                    // Action 3: Share APK via System Share Sheet
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("apk_share_button")
                            .clip(CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
                            .background(Color(0x9906162A))
                            .border(1.dp, ArcCyanBright.copy(alpha = 0.6f), CutCornerShape(topStart = 10.dp, bottomEnd = 10.dp))
                            .clickable { viewModel.shareCompiledApk() }
                            .padding(vertical = 12.dp, horizontal = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = ArcCyanBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "3. SHARE / SEND APK PACKAGE",
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // 4. Futuristic Animated Boot Sequence Overlay
        AnimatedVisibility(
            visible = isBooting,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BootSequenceOverlay(
                bootProgress = bootProgress,
                bootStageLabel = bootStageLabel,
                onSkipBoot = viewModel::skipBoot
            )
        }
    }
}

@Composable
private fun HudTopVisorBar(
    isRedAlert: Boolean,
    isFlightMode: Boolean,
    isFlashlightOn: Boolean,
    batteryPct: Int,
    arcGw: Float,
    onOpenApkDownloader: () -> Unit,
    onOpenWidgetCustomizer: () -> Unit,
    onToggleFlashlight: () -> Unit
) {
    val accent = if (isRedAlert) StarkCrimson else ArcCyan
    val shape = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp, topEnd = 4.dp, bottomStart = 4.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xD9051020))
            .border(1.2.dp, accent.copy(alpha = 0.7f), shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isRedAlert) StarkCrimson else TelemetryNominalGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "J.A.R.V.I.S. // MARK LXXXV",
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isRedAlert) "STATUS: RED ALERT" else if (isFlightMode) "STATUS: FLIGHT HUD" else "STATUS: ONLINE",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 10.sp,
                        color = if (isRedAlert) StarkCrimson else ArcCyanBright
                    )
                    Text(
                        text = "• PWR $batteryPct% • ${String.format("%.1f", arcGw)}GW",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 10.sp,
                        color = StarkGold
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // In-App APK Download Button
            Box(
                modifier = Modifier
                    .testTag("header_apk_download_button")
                    .clip(CutCornerShape(6.dp))
                    .background(StarkGold.copy(alpha = 0.20f))
                    .border(1.dp, StarkGold, CutCornerShape(6.dp))
                    .clickable(onClick = onOpenApkDownloader)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download APK package",
                        tint = StarkGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "APK",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }

            // Quick Torch Button in Visor Header
            Box(
                modifier = Modifier
                    .testTag("header_torch_button")
                    .clip(CutCornerShape(6.dp))
                    .background(if (isFlashlightOn) StarkGold.copy(alpha = 0.25f) else accent.copy(alpha = 0.14f))
                    .border(1.dp, if (isFlashlightOn) StarkGold else accent.copy(alpha = 0.5f), CutCornerShape(6.dp))
                    .clickable(onClick = onToggleFlashlight)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashlightOn,
                    contentDescription = "Quick flashlight toggle",
                    tint = if (isFlashlightOn) StarkGold else accent,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Widget Size Customizer Button
            Box(
                modifier = Modifier
                    .testTag("open_widget_customizer_button")
                    .clip(CutCornerShape(6.dp))
                    .background(accent.copy(alpha = 0.18f))
                    .border(1.dp, accent, CutCornerShape(6.dp))
                    .clickable(onClick = onOpenWidgetCustomizer)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Customize HUD widgets",
                        tint = accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "WIDGETS",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderHudWidget(
    config: HudWidgetConfig,
    viewModel: JarvisViewModel,
    isRedAlert: Boolean,
    isFlightMode: Boolean,
    isSfxEnabled: Boolean,
    isVoiceTtsEnabled: Boolean,
    isParallaxEnabled: Boolean,
    isListening: Boolean,
    isSpeaking: Boolean,
    isProcessingAi: Boolean,
    voiceMessages: List< com.example.data.VoiceMessage>,
    timeString: String,
    secondsString: String,
    dateString: String,
    utcString: String,
    uptimeFormatted: String,
    battery: com.example.data.BatteryTelemetry,
    weather: com.example.data.WeatherTelemetry,
    systemTelemetry: com.example.data.SystemTelemetry,
    connectivity: com.example.data.ConnectivityTelemetry,
    arcPowerMode: com.example.data.ArcPowerMode,
    isMusicPlaying: Boolean,
    currentTrackIndex: Int,
    trackProgress: Float,
    spectrumBands: List<Float>,
    mediaVolumePercent: Int,
    isFlashlightOn: Boolean,
    hasHardwareFlash: Boolean,
    allWidgetConfigs: List<HudWidgetConfig>,
    onRequestMic: () -> Unit,
    onRequestLocationWeather: () -> Unit,
    parallaxRoll: Float,
    parallaxPitch: Float,
    modifier: Modifier = Modifier
) {
    val onCycleSize = { viewModel.cycleWidgetSize(config.type) }

    when (config.type) {
        HudWidgetType.VOICE_ASSISTANT -> JarvisVoiceAssistantWidget(
            size = config.size,
            isRedAlert = isRedAlert,
            isListening = isListening,
            isSpeaking = isSpeaking,
            isProcessingAi = isProcessingAi,
            messages = voiceMessages,
            onToggleMicListening = onRequestMic,
            onSendCommand = viewModel::processVoiceOrTextCommand,
            onSpeakMessage = viewModel.soundEngine::speak,
            onCycleSize = onCycleSize,
            modifier = modifier
        )

        HudWidgetType.CLOCK_DATE -> LiveDigitalClockWidget(
            size = config.size,
            isRedAlert = isRedAlert,
            timeString = timeString,
            secondsString = secondsString,
            dateString = dateString,
            utcString = utcString,
            uptimeFormatted = uptimeFormatted,
            onCycleSize = onCycleSize,
            modifier = modifier
        )

        HudWidgetType.BATTERY_STATUS -> BatteryPowerWidget(
            size = config.size,
            isRedAlert = isRedAlert,
            battery = battery,
            onCycleSize = onCycleSize,
            modifier = modifier
        )

        HudWidgetType.WEATHER_RADAR -> WeatherSensorWidget(
            size = config.size,
            isRedAlert = isRedAlert,
            weather = weather,
            onSelectFacility = viewModel::selectWeatherFacility,
            onRequestGpsWeather = onRequestLocationWeather,
            onRefreshWeather = viewModel::refreshWeather,
            onCycleSize = onCycleSize,
            modifier = modifier
        )

        HudWidgetType.CPU_RAM -> CpuRamTelemetryWidget(
            size = config.size,
            isRedAlert = isRedAlert,
            telemetry = systemTelemetry,
            onPurgeMemoryCache = viewModel::purgeMemoryCache,
            onCycleSize = onCycleSize,
            modifier = modifier
        )

        HudWidgetType.CONNECTIVITY -> ConnectivityDatalinkWidget(
            size = config.size,
            isRedAlert = isRedAlert,
            connectivity = connectivity,
            onPingNetwork = viewModel::pingNetworkDatalink,
            onCycleSize = onCycleSize,
            modifier = modifier
        )

        HudWidgetType.ARC_REACTOR -> ArcReactorTelemetryWidget(
            size = config.size,
            isRedAlert = isRedAlert,
            powerMode = arcPowerMode,
            onCyclePowerMode = viewModel::cycleArcPowerMode,
            onCycleSize = onCycleSize,
            modifier = modifier
        )

        HudWidgetType.MUSIC_PLAYER -> MusicPlayerMatrixWidget(
            size = config.size,
            isRedAlert = isRedAlert,
            isPlaying = isMusicPlaying,
            currentTrackIndex = currentTrackIndex,
            trackProgress = trackProgress,
            spectrumBands = spectrumBands,
            mediaVolumePercent = mediaVolumePercent,
            onTogglePlay = viewModel.soundEngine::toggleMusicPlayback,
            onPrevTrack = viewModel.soundEngine::previousTrack,
            onNextTrack = viewModel.soundEngine::nextTrack,
            onVolumeChange = viewModel::setMediaVolume,
            onCycleSize = onCycleSize,
            modifier = modifier
        )

        HudWidgetType.FLASHLIGHT -> FlashlightPhotonWidget(
            size = config.size,
            isRedAlert = isRedAlert,
            isFlashlightOn = isFlashlightOn,
            hasHardwareFlash = hasHardwareFlash,
            onToggleFlashlight = { viewModel.toggleFlashlight() },
            onCycleSize = onCycleSize,
            modifier = modifier
        )

        HudWidgetType.QUICK_SETTINGS -> QuickSettingsMatrixWidget(
            size = config.size,
            isRedAlert = isRedAlert,
            isFlightMode = isFlightMode,
            isSfxEnabled = isSfxEnabled,
            isVoiceTtsEnabled = isVoiceTtsEnabled,
            isFlashlightOn = isFlashlightOn,
            isParallaxEnabled = isParallaxEnabled,
            widgetConfigs = allWidgetConfigs,
            onToggleRedAlert = { viewModel.toggleRedAlert() },
            onToggleFlightMode = { viewModel.toggleFlightMode() },
            onToggleSfx = viewModel::toggleSfx,
            onToggleVoiceTts = viewModel::toggleVoiceTts,
            onToggleFlashlight = { viewModel.toggleFlashlight() },
            onToggleParallax = viewModel::toggleParallax,
            onCycleWidgetSize = viewModel::cycleWidgetSize,
            onToggleWidgetVisibility = viewModel::toggleWidgetVisibility,
            onResetWidgetLayout = viewModel::resetWidgetLayout,
            onCycleSize = onCycleSize,
            modifier = modifier
        )
    }
}

private fun buildAdaptiveWidgetRows(
    configs: List<HudWidgetConfig>,
    isWideTablet: Boolean
): List<List<HudWidgetConfig>> {
    val rows = mutableListOf<List<HudWidgetConfig>>()
    val pendingCompact = mutableListOf<HudWidgetConfig>()

    for (cfg in configs) {
        val canPair = cfg.size == WidgetSize.COMPACT || (isWideTablet && cfg.size == WidgetSize.WIDE)
        if (canPair) {
            pendingCompact.add(cfg)
            if (pendingCompact.size == 2) {
                rows.add(pendingCompact.toList())
                pendingCompact.clear()
            }
        } else {
            if (pendingCompact.isNotEmpty()) {
                rows.add(pendingCompact.toList())
                pendingCompact.clear()
            }
            rows.add(listOf(cfg))
        }
    }
    if (pendingCompact.isNotEmpty()) {
        rows.add(pendingCompact.toList())
    }
    return rows
}
