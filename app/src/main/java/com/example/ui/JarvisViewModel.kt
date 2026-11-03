package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.JarvisSoundEngine
import com.example.data.ArcPowerMode
import com.example.data.BatteryTelemetry
import com.example.data.ConnectivityTelemetry
import com.example.data.FlightTelemetry
import com.example.data.HardwareTelemetryManager
import com.example.data.HudWidgetConfig
import com.example.data.HudWidgetType
import com.example.data.JarvisNetworkService
import com.example.data.STARK_LOCATIONS
import com.example.data.StarkLocationPreset
import com.example.data.SystemTelemetry
import com.example.data.VoiceMessage
import android.net.Uri
import com.example.data.WeatherTelemetry
import com.example.data.WidgetSize
import com.example.util.ApkExportHelper
import com.example.util.ApkPackageMetadata
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val prefs = appContext.getSharedPreferences("jarvis_hud_prefs", Context.MODE_PRIVATE)

    val soundEngine = JarvisSoundEngine(appContext)
    private val hardwareManager = HardwareTelemetryManager(appContext)
    private val networkService = JarvisNetworkService()

    // Boot Sequence State
    private val _isBooting = MutableStateFlow(true)
    val isBooting: StateFlow<Boolean> = _isBooting.asStateFlow()

    private val _bootProgress = MutableStateFlow(0.05f)
    val bootProgress: StateFlow<Float> = _bootProgress.asStateFlow()

    private val _bootStageLabel = MutableStateFlow("INITIALIZING STARK NEURAL KERNEL...")
    val bootStageLabel: StateFlow<String> = _bootStageLabel.asStateFlow()

    // Tactical HUD Modes
    private val _isRedAlert = MutableStateFlow(false)
    val isRedAlert: StateFlow<Boolean> = _isRedAlert.asStateFlow()

    private val _isFlightMode = MutableStateFlow(false)
    val isFlightMode: StateFlow<Boolean> = _isFlightMode.asStateFlow()

    private val _arcPowerMode = MutableStateFlow(ArcPowerMode.NOMINAL_CORE)
    val arcPowerMode: StateFlow<ArcPowerMode> = _arcPowerMode.asStateFlow()

    private val _isSfxEnabled = MutableStateFlow(true)
    val isSfxEnabled: StateFlow<Boolean> = _isSfxEnabled.asStateFlow()

    private val _isVoiceTtsEnabled = MutableStateFlow(true)
    val isVoiceTtsEnabled: StateFlow<Boolean> = _isVoiceTtsEnabled.asStateFlow()

    private val _isParallaxEnabled = MutableStateFlow(true)
    val isParallaxEnabled: StateFlow<Boolean> = _isParallaxEnabled.asStateFlow()

    // Clock & Chrono State
    private val _timeString = MutableStateFlow("00:00")
    val timeString: StateFlow<String> = _timeString.asStateFlow()

    private val _secondsString = MutableStateFlow("00")
    val secondsString: StateFlow<String> = _secondsString.asStateFlow()

    private val _dateString = MutableStateFlow("MON, NOV 02, 2026")
    val dateString: StateFlow<String> = _dateString.asStateFlow()

    private val _utcString = MutableStateFlow("UTC 00:00:00")
    val utcString: StateFlow<String> = _utcString.asStateFlow()

    private val _uptimeFormatted = MutableStateFlow("00:00:00")
    val uptimeFormatted: StateFlow<String> = _uptimeFormatted.asStateFlow()

    // Hardware & Sensor Telemetry
    val batteryTelemetry: StateFlow<BatteryTelemetry> = hardwareManager.batteryState
    val isFlashlightOn: StateFlow<Boolean> = hardwareManager.flashlightOn
    val hasHardwareFlash: StateFlow<Boolean> = hardwareManager.hasHardwareFlash

    private val _systemTelemetry = MutableStateFlow(SystemTelemetry())
    val systemTelemetry: StateFlow<SystemTelemetry> = _systemTelemetry.asStateFlow()

    private val _connectivityTelemetry = MutableStateFlow(ConnectivityTelemetry())
    val connectivityTelemetry: StateFlow<ConnectivityTelemetry> = _connectivityTelemetry.asStateFlow()

    private val _weatherTelemetry = MutableStateFlow(WeatherTelemetry())
    val weatherTelemetry: StateFlow<WeatherTelemetry> = _weatherTelemetry.asStateFlow()

    private val _flightTelemetry = MutableStateFlow(FlightTelemetry())
    val flightTelemetry: StateFlow<FlightTelemetry> = _flightTelemetry.asStateFlow()

    private val _mediaVolumePercent = MutableStateFlow(hardwareManager.getSystemMediaVolumePercent())
    val mediaVolumePercent: StateFlow<Int> = _mediaVolumePercent.asStateFlow()

    // Voice Assistant & SpeechRecognizer State
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isProcessingAi = MutableStateFlow(false)
    val isProcessingAi: StateFlow<Boolean> = _isProcessingAi.asStateFlow()

    private val _voiceMessages = MutableStateFlow(
        listOf(
            VoiceMessage(
                id = "init_1",
                speaker = "J.A.R.V.I.S.",
                text = "Good evening, Sir. Mark LXXXV HUD and all 10 tactical hologram modules are online. Vibranium Arc Reactor steady at 3.20 gigawatts.",
                timestamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()),
                actionBadge = "SYSTEM ONLINE"
            )
        )
    )
    val voiceMessages: StateFlow<List<VoiceMessage>> = _voiceMessages.asStateFlow()

    // Customizable Widget Sizes & Visibility
    private val _widgetConfigs = MutableStateFlow(loadInitialWidgetConfigs())
    val widgetConfigs: StateFlow<List<HudWidgetConfig>> = _widgetConfigs.asStateFlow()

    // In-App APK Package Metadata & Mobile Export Status
    private val _apkMetadata = MutableStateFlow(ApkPackageMetadata())
    val apkMetadata: StateFlow<ApkPackageMetadata> = _apkMetadata.asStateFlow()

    private val _apkExportStatus = MutableStateFlow<String?>(null)
    val apkExportStatus: StateFlow<String?> = _apkExportStatus.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private val sessionStartMs = SystemClock.elapsedRealtime()

    init {
        runBootSequence()
        startTelemetryLoops()
        refreshWeather()
        loadApkMetadata()
    }

    private fun loadInitialWidgetConfigs(): List<HudWidgetConfig> {
        return HudWidgetType.entries.map { type ->
            val savedSizeName = prefs.getString("size_${type.id}", type.defaultSize.name)
            val resolvedSize = WidgetSize.entries.find { it.name == savedSizeName } ?: type.defaultSize
            val visible = prefs.getBoolean("vis_${type.id}", true)
            HudWidgetConfig(type = type, size = resolvedSize, isVisible = visible)
        }
    }

    private fun saveWidgetConfigs(configs: List<HudWidgetConfig>) {
        val editor = prefs.edit()
        configs.forEach { cfg ->
            editor.putString("size_${cfg.type.id}", cfg.size.name)
            editor.putBoolean("vis_${cfg.type.id}", cfg.isVisible)
        }
        editor.apply()
    }

    private fun runBootSequence() {
        viewModelScope.launch {
            soundEngine.playBootChord()
            val stages = listOf(
                0.22f to "CALIBRATING BIOMETRIC RETINAL LOCK...",
                0.48f to "IGNITING PALLADIUM-FREE ARC REACTOR CORE...",
                0.74f to "LINKING STARK ORBITAL WEATHER & COMMS...",
                0.96f to "SYNCHRONIZING J.A.R.V.I.S. VOICE MATRIX...",
                1.00f to "MARK LXXXV HUD READY"
            )
            for ((targetProgress, label) in stages) {
                if (!_isBooting.value) break
                _bootStageLabel.value = label
                _bootProgress.value = targetProgress
                delay(420L)
            }
            if (_isBooting.value) {
                _isBooting.value = false
                soundEngine.speak("Welcome back, Sir. All Mark 85 tactical systems are online.")
            }
        }
    }

    fun skipBoot() {
        soundEngine.playHudTap()
        _isBooting.value = false
        soundEngine.speak("HUD initialization bypassed. Systems nominal, Sir.")
    }

    private fun startTelemetryLoops() {
        // 1-second Chrono & Hardware Telemetry Loop
        viewModelScope.launch {
            val timeFmt = SimpleDateFormat("HH:mm", Locale.US)
            val secFmt = SimpleDateFormat("ss", Locale.US)
            val dateFmt = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US)
            val utcFmt = SimpleDateFormat("HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            while (isActive) {
                val now = Date()
                _timeString.value = timeFmt.format(now)
                _secondsString.value = secFmt.format(now)
                _dateString.value = dateFmt.format(now)
                _utcString.value = "UTC ${utcFmt.format(now)}"

                val elapsedSec = ((SystemClock.elapsedRealtime() - sessionStartMs) / 1000L).coerceAtLeast(0L)
                val hrs = elapsedSec / 3600
                val mins = (elapsedSec % 3600) / 60
                val secs = elapsedSec % 60
                _uptimeFormatted.value = String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)

                _systemTelemetry.value = hardwareManager.pollSystemTelemetry()
                _connectivityTelemetry.value = hardwareManager.pollConnectivityTelemetry()

                // Update Flight Telemetry from real sensors + power mode
                val pitch = hardwareManager.sensorPitch.value
                val roll = hardwareManager.sensorRoll.value
                val heading = hardwareManager.sensorHeading.value
                val currentFlight = _flightTelemetry.value
                val mach = when (_arcPowerMode.value) {
                    ArcPowerMode.ECO_STANDBY -> 0.65f
                    ArcPowerMode.NOMINAL_CORE -> 1.42f
                    ArcPowerMode.COMBAT_BOOST -> 2.38f
                    ArcPowerMode.UNIBEAM_OVERDRIVE -> 3.15f
                }
                val baseAlt = if (_isFlightMode.value) 24800 else 1240
                val altDelta = ((pitch + currentFlight.manualPitchTrim) * 45f).roundToInt()

                _flightTelemetry.value = currentFlight.copy(
                    pitchDegrees = pitch,
                    rollDegrees = roll,
                    headingDegrees = heading,
                    altitudeFeet = (baseAlt + altDelta).coerceAtLeast(0),
                    machSpeed = mach,
                    gForce = (1.0f + kotlin.math.abs(roll + currentFlight.manualRollTrim) * 0.04f).coerceIn(1.0f, 4.8f),
                    leftThrusterPct = (90 + (roll * 0.3f).roundToInt()).coerceIn(50, 100),
                    rightThrusterPct = (90 - (roll * 0.3f).roundToInt()).coerceIn(50, 100)
                )

                delay(1000L)
            }
        }
    }

    fun toggleRedAlert(forceState: Boolean? = null) {
        val next = forceState ?: !_isRedAlert.value
        _isRedAlert.value = next
        if (next) {
            soundEngine.playRedAlertSiren()
            _arcPowerMode.value = ArcPowerMode.COMBAT_BOOST
            soundEngine.speak("Warning. Red Alert protocol engaged. Combat shields and repulsors at maximum capacity.")
        } else {
            soundEngine.playHudTap()
            _arcPowerMode.value = ArcPowerMode.NOMINAL_CORE
            soundEngine.speak("Standing down from Red Alert. Returning to nominal HUD parameters.")
        }
    }

    fun toggleFlightMode(forceState: Boolean? = null) {
        val next = forceState ?: !_isFlightMode.value
        _isFlightMode.value = next
        if (next) {
            soundEngine.playFlightEngage()
            soundEngine.speak("Flight mode engaged. Artificial horizon, thruster stabilizers, and Mach telemetry locked.")
        } else {
            soundEngine.playHudTap()
            soundEngine.speak("Landing gear stabilized. Flight HUD disengaged.")
        }
    }

    fun updateFlightTrim(pitchTrim: Float, rollTrim: Float) {
        _flightTelemetry.value = _flightTelemetry.value.copy(
            manualPitchTrim = pitchTrim,
            manualRollTrim = rollTrim
        )
    }

    fun cycleArcPowerMode() {
        val nextMode = _arcPowerMode.value.next()
        selectArcPowerMode(nextMode)
    }

    fun selectArcPowerMode(mode: ArcPowerMode) {
        soundEngine.playArcPulse()
        _arcPowerMode.value = mode
        val msg = when (mode) {
            ArcPowerMode.ECO_STANDBY -> "Arc Reactor shifted to stealth idle at 0.85 gigawatts."
            ArcPowerMode.NOMINAL_CORE -> "Arc Reactor stabilized at nominal 3.20 gigawatts."
            ArcPowerMode.COMBAT_BOOST -> "Combat overclock active. Output at 7.80 gigawatts."
            ArcPowerMode.UNIBEAM_OVERDRIVE -> "Maximum Unibeam capacitor charge! Output surging to 12.40 gigawatts."
        }
        soundEngine.speak(msg)
    }

    fun toggleFlashlight(forceState: Boolean? = null) {
        soundEngine.playHudTap()
        val active = hardwareManager.toggleFlashlight(forceState)
        if (active) {
            soundEngine.speak("Photon torch beam activated.")
        } else {
            soundEngine.speak("Photon torch deactivated.")
        }
    }

    fun toggleSfx() {
        val next = !_isSfxEnabled.value
        _isSfxEnabled.value = next
        soundEngine.isSfxEnabled = next
        if (next) soundEngine.playHudTap()
    }

    fun toggleVoiceTts() {
        soundEngine.playHudTap()
        val next = !_isVoiceTtsEnabled.value
        _isVoiceTtsEnabled.value = next
        soundEngine.isVoiceTtsEnabled = next
        if (next) {
            soundEngine.speak("J.A.R.V.I.S. vocal synthesizer online, Sir.")
        }
    }

    fun toggleParallax() {
        soundEngine.playHudTap()
        _isParallaxEnabled.value = !_isParallaxEnabled.value
    }

    fun setMediaVolume(percent: Int) {
        _mediaVolumePercent.value = percent.coerceIn(0, 100)
        hardwareManager.setSystemMediaVolumePercent(percent)
    }

    fun purgeMemoryCache() {
        soundEngine.playArcPulse()
        System.gc()
        _systemTelemetry.value = hardwareManager.pollSystemTelemetry()
        appendJarvisMessage(
            speaker = "J.A.R.V.I.S.",
            text = "Neural memory buffers purged and defragmented, Sir. CPU and heap allocation optimized.",
            badge = "MEM OPTIMIZED"
        )
        soundEngine.speak("Neural memory buffers purged and optimized, Sir.")
    }

    fun pingNetworkDatalink() {
        soundEngine.playHudTap()
        _connectivityTelemetry.value = hardwareManager.pollConnectivityTelemetry()
        val net = _connectivityTelemetry.value
        appendJarvisMessage(
            speaker = "J.A.R.V.I.S.",
            text = "Datalink verified on ${net.networkTypeLabel} (${net.localIp}). Round-trip latency ${net.pingLatencyMs} ms.",
            badge = "PING ${net.pingLatencyMs}MS"
        )
    }

    fun selectWeatherFacility(preset: StarkLocationPreset) {
        soundEngine.playHudTap()
        _weatherTelemetry.value = _weatherTelemetry.value.copy(
            locationName = preset.name,
            sectorCode = preset.sectorCode,
            latitude = preset.latitude,
            longitude = preset.longitude,
            isLoading = true
        )
        refreshWeather()
    }

    fun updateWeatherFromDeviceGps() {
        soundEngine.playHudTap()
        val coords = hardwareManager.getLastKnownDeviceLocation()
        if (coords != null) {
            _weatherTelemetry.value = _weatherTelemetry.value.copy(
                locationName = "LIVE GPS SECTOR",
                sectorCode = "SEC-GPS-01",
                latitude = coords.first,
                longitude = coords.second,
                isLoading = true
            )
            refreshWeather()
            soundEngine.speak("Locking atmospheric sensors onto your live GPS coordinates, Sir.")
        } else {
            refreshWeather()
            soundEngine.speak("GPS satellite fix unavailable in indoor chamber; refreshing active Stark sector telemetry.")
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            val current = _weatherTelemetry.value
            _weatherTelemetry.value = current.copy(isLoading = true)
            val updated = networkService.fetchLiveWeather(
                locationName = current.locationName,
                sectorCode = current.sectorCode,
                latitude = current.latitude,
                longitude = current.longitude
            )
            _weatherTelemetry.value = updated
        }
    }

    // Customizable Widget Size & Visibility Functions
    fun cycleWidgetSize(type: HudWidgetType) {
        soundEngine.playHudTap()
        val updated = _widgetConfigs.value.map { cfg ->
            if (cfg.type == type) cfg.copy(size = cfg.size.next()) else cfg
        }
        _widgetConfigs.value = updated
        saveWidgetConfigs(updated)
    }

    fun toggleWidgetVisibility(type: HudWidgetType) {
        if (type == HudWidgetType.QUICK_SETTINGS) return
        soundEngine.playHudTap()
        val updated = _widgetConfigs.value.map { cfg ->
            if (cfg.type == type) cfg.copy(isVisible = !cfg.isVisible) else cfg
        }
        _widgetConfigs.value = updated
        saveWidgetConfigs(updated)
    }

    fun resetWidgetLayout() {
        soundEngine.playArcPulse()
        val defaults = HudWidgetType.entries.map { type ->
            HudWidgetConfig(type = type, size = type.defaultSize, isVisible = true)
        }
        _widgetConfigs.value = defaults
        saveWidgetConfigs(defaults)
        soundEngine.speak("Holographic widget matrix restored to factory Mark 85 layout.")
    }

    // Voice Recognition & Command Processing
    fun startSpeechRecognition() {
        soundEngine.playHudTap()
        if (_isListening.value) {
            stopSpeechRecognition()
            return
        }
        try {
            if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
                appendJarvisMessage(
                    speaker = "J.A.R.V.I.S.",
                    text = "Hardware voice recognition service is unavailable on this simulator image, Sir. Please use the tactical command presets or console input below.",
                    badge = "MIC FALLBACK"
                )
                soundEngine.speak("Voice recognition service is unavailable on this simulator, Sir. Please tap a preset or type your command.")
                return
            }

            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
            speechRecognizer = recognizer

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                }

                override fun onBeginningOfSpeech() {
                    _isListening.value = true
                }

                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit

                override fun onEndOfSpeech() {
                    _isListening.value = false
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    appendJarvisMessage(
                        speaker = "J.A.R.V.I.S.",
                        text = "Audio stream closed without vocal lock (Code $error). Tap any tactical command chip or type below, Sir.",
                        badge = "AUDIO IDLE"
                    )
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull()
                    if (!spoken.isNullOrBlank()) {
                        processVoiceOrTextCommand(spoken)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            _isListening.value = true
            recognizer.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            appendJarvisMessage(
                speaker = "J.A.R.V.I.S.",
                text = "Microphone stream unavailable: ${e.localizedMessage ?: "Audio hardware busy"}. Use tactical command chips below.",
                badge = "AUDIO NOTICE"
            )
        }
    }

    fun stopSpeechRecognition() {
        _isListening.value = false
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {
        }
    }

    fun processVoiceOrTextCommand(rawCommand: String) {
        val cmd = rawCommand.trim()
        if (cmd.isEmpty()) return
        soundEngine.playHudTap()

        appendJarvisMessage(speaker = "STARK", text = cmd)
        val lower = cmd.lowercase(Locale.US)

        // 1. Check for immediate hardware/HUD system commands
        when {
            lower.contains("red alert") || lower.contains("combat mode") || lower.contains("defcon") -> {
                val enable = !lower.contains("off") && !lower.contains("cancel") && !lower.contains("stand down")
                toggleRedAlert(enable)
                appendJarvisMessage(
                    speaker = "J.A.R.V.I.S.",
                    text = if (enable) {
                        "Red Alert protocol activated, Sir! Combat shields locked at 100%, Arc Reactor boosted to 7.80 GW."
                    } else {
                        "Standing down from Red Alert, Sir. Returning HUD to nominal cyan telemetry."
                    },
                    badge = if (enable) "RED ALERT ON" else "NOMINAL"
                )
                return
            }

            lower.contains("nominal mode") || lower.contains("stand down") -> {
                toggleRedAlert(false)
                selectArcPowerMode(ArcPowerMode.NOMINAL_CORE)
                appendJarvisMessage(
                    speaker = "J.A.R.V.I.S.",
                    text = "All systems restored to Mark LXXXV nominal operating parameters, Sir.",
                    badge = "NOMINAL"
                )
                return
            }

            lower.contains("flight") || lower.contains("fly") || lower.contains("thruster") -> {
                val enable = !lower.contains("off") && !lower.contains("land") && !lower.contains("disengage")
                toggleFlightMode(enable)
                appendJarvisMessage(
                    speaker = "J.A.R.V.I.S.",
                    text = if (enable) {
                        "Flight HUD engaged, Sir. Pitch/roll horizon ladder and Mach 1.42 vectoring online."
                    } else {
                        "Flight HUD disengaged. Ground stabilization active."
                    },
                    badge = if (enable) "FLIGHT HUD ON" else "GROUNDED"
                )
                return
            }

            lower.contains("flashlight") || lower.contains("torch") || lower.contains("light") -> {
                val target = when {
                    lower.contains("off") -> false
                    lower.contains("on") -> true
                    else -> null
                }
                val newState = hardwareManager.toggleFlashlight(target)
                val reply = if (newState) {
                    "Photon torch beam engaged at 1,200 lumens, Sir."
                } else {
                    "Photon torch emitter powered down, Sir."
                }
                appendJarvisMessage(speaker = "J.A.R.V.I.S.", text = reply, badge = if (newState) "TORCH ON" else "TORCH OFF")
                soundEngine.speak(reply)
                return
            }

            lower.contains("overcharge") || lower.contains("unibeam") || lower.contains("maximum power") -> {
                selectArcPowerMode(ArcPowerMode.UNIBEAM_OVERDRIVE)
                appendJarvisMessage(
                    speaker = "J.A.R.V.I.S.",
                    text = "Arc Reactor surged to Unibeam Maxima — 12.40 gigawatts at 14,800 RPM, Sir!",
                    badge = "12.40 GW MAX"
                )
                return
            }

            lower.contains("play") || lower.contains("music") || lower.contains("stark mix") -> {
                if (!soundEngine.isMusicPlaying.value) {
                    soundEngine.toggleMusicPlayback()
                }
                val track = com.example.data.STARK_SYNTH_TRACKS[soundEngine.currentTrackIndex.value]
                val reply = "Playing ${track.title} at ${track.bpm} BPM on the Stark Audio Matrix, Sir."
                appendJarvisMessage(speaker = "J.A.R.V.I.S.", text = reply, badge = "AUDIO PLAYING")
                soundEngine.speak(reply)
                return
            }

            lower.contains("stop music") || lower.contains("pause music") -> {
                soundEngine.stopMusic()
                val reply = "Stark Audio Matrix playback paused, Sir."
                appendJarvisMessage(speaker = "J.A.R.V.I.S.", text = reply, badge = "AUDIO PAUSED")
                soundEngine.speak(reply)
                return
            }

            lower.contains("weather") -> {
                refreshWeather()
                val w = _weatherTelemetry.value
                val reply = "Atmospheric scan for ${w.locationName}: ${w.conditionLabel}, ${String.format("%.1f", w.tempCelsius)}°C, winds ${w.windSpeedKmh} km/h, pressure ${w.pressureHpa.toInt()} hPa. ${w.flightCeilingStatus}."
                appendJarvisMessage(speaker = "J.A.R.V.I.S.", text = reply, badge = "METEO SYNC")
                soundEngine.speak(reply)
                return
            }

            lower.contains("apk") || lower.contains("download") || lower.contains("export") -> {
                downloadApkToMobileDownloads()
                return
            }
        }

        // 2. Otherwise query JARVIS AI Brain (Gemini API + live hardware telemetry synthesis)
        viewModelScope.launch {
            _isProcessingAi.value = true
            try {
                val bat = batteryTelemetry.value
                val sys = _systemTelemetry.value
                val w = _weatherTelemetry.value
                val arc = _arcPowerMode.value
                val snapshot =
                    "Arc Reactor: ${arc.label} (${arc.outputGigawatts} GW), Battery: ${bat.percentage}% (${bat.temperatureC}°C), " +
                        "CPU: ${sys.cpuUsagePercent}%, RAM: ${sys.ramUsedMb}/${sys.ramTotalMb} MB, " +
                        "Weather at ${w.locationName}: ${w.tempCelsius}°C ${w.conditionLabel}, RedAlert=${_isRedAlert.value}, FlightMode=${_isFlightMode.value}"

                val responseText = networkService.queryJarvisBrain(cmd, snapshot)
                appendJarvisMessage(
                    speaker = "J.A.R.V.I.S.",
                    text = responseText,
                    badge = "AI TELEMETRY"
                )
                soundEngine.speak(responseText)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                appendJarvisMessage(
                    speaker = "J.A.R.V.I.S.",
                    text = "Neural uplink experienced brief interference: ${e.localizedMessage ?: "Unknown error"}. All onboard Mark LXXXV sensors remain operational, Sir.",
                    badge = "LOCAL CORE"
                )
            } finally {
                _isProcessingAi.value = false
            }
        }
    }

    private fun loadApkMetadata() {
        viewModelScope.launch {
            _apkMetadata.value = ApkExportHelper.inspectInstalledApk(appContext)
        }
    }

    fun downloadApkToMobileDownloads() {
        soundEngine.playArcPulse()
        viewModelScope.launch {
            _apkExportStatus.value = "PACKAGING SIGNED MARK LXXXV APK..."
            val result = ApkExportHelper.saveApkToDeviceDownloads(appContext)
            result.fold(
                onSuccess = { msg ->
                    _apkExportStatus.value = msg
                    appendJarvisMessage(
                        speaker = "J.A.R.V.I.S.",
                        text = "Mark LXXXV APK binary ($msg) has been written to mobile storage, Sir.",
                        badge = "APK DOWNLOADED"
                    )
                    soundEngine.speak("APK binary downloaded to mobile storage, Sir.")
                },
                onFailure = { err ->
                    _apkExportStatus.value = "Export failed: ${err.localizedMessage}"
                }
            )
        }
    }

    fun saveApkToChosenUri(uri: Uri) {
        soundEngine.playArcPulse()
        viewModelScope.launch {
            _apkExportStatus.value = "WRITING APK BINARY TO SELECTED PATH..."
            val result = ApkExportHelper.copyApkToCustomUri(appContext, uri)
            result.fold(
                onSuccess = { msg ->
                    _apkExportStatus.value = msg
                    appendJarvisMessage(
                        speaker = "J.A.R.V.I.S.",
                        text = msg,
                        badge = "APK SAVED"
                    )
                    soundEngine.speak("APK package saved to your selected mobile folder, Sir.")
                },
                onFailure = { err ->
                    _apkExportStatus.value = "Save failed: ${err.localizedMessage}"
                }
            )
        }
    }

    fun shareCompiledApk() {
        soundEngine.playHudTap()
        viewModelScope.launch {
            val res = ApkExportHelper.shareApkPackage(appContext)
            res.onSuccess {
                _apkExportStatus.value = "APK Share Sheet launched (${ApkExportHelper.DEFAULT_APK_FILENAME})"
            }.onFailure { err ->
                _apkExportStatus.value = "Share error: ${err.localizedMessage}"
            }
        }
    }

    private fun appendJarvisMessage(speaker: String, text: String, badge: String? = null) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        val msg = VoiceMessage(
            id = "msg_${System.currentTimeMillis()}_${_voiceMessages.value.size}",
            speaker = speaker,
            text = text,
            timestamp = time,
            actionBadge = badge
        )
        _voiceMessages.value = (_voiceMessages.value + msg).takeLast(12)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        }
        hardwareManager.cleanup()
        soundEngine.shutdown()
    }
}
