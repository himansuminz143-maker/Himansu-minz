package com.example.data

enum class HudWidgetType(
    val id: String,
    val title: String,
    val code: String,
    val defaultSize: WidgetSize
) {
    VOICE_ASSISTANT("voice", "J.A.R.V.I.S. VOICE CORE", "MOD-01 // AI", WidgetSize.EXPANDED),
    CLOCK_DATE("clock", "CHRONO TELEMETRY", "MOD-02 // CLK", WidgetSize.COMPACT),
    BATTERY_STATUS("battery", "POWER CELLS & THERMAL", "MOD-03 // PWR", WidgetSize.COMPACT),
    WEATHER_RADAR("weather", "ATMOSPHERIC SENSORS", "MOD-04 // ATM", WidgetSize.WIDE),
    CPU_RAM("cpu_ram", "NEURAL CPU & MEMORY", "MOD-05 // SYS", WidgetSize.WIDE),
    CONNECTIVITY("connectivity", "COMMS & DATALINK", "MOD-06 // NET", WidgetSize.COMPACT),
    ARC_REACTOR("arc_reactor", "ARC REACTOR CORE", "MOD-07 // ARC", WidgetSize.COMPACT),
    MUSIC_PLAYER("music", "STARK AUDIO MATRIX", "MOD-08 // AUD", WidgetSize.WIDE),
    FLASHLIGHT("flashlight", "PHOTON TORCH BEAM", "MOD-09 // LUM", WidgetSize.COMPACT),
    QUICK_SETTINGS("quick_settings", "TACTICAL QUICK MATRIX", "MOD-10 // CFG", WidgetSize.EXPANDED)
}

enum class WidgetSize(val label: String, val badge: String) {
    COMPACT("Compact (1x1)", "1x1"),
    WIDE("Wide (2x1)", "2x1"),
    EXPANDED("Expanded (Full HUD)", "2x2");

    fun next(): WidgetSize = when (this) {
        COMPACT -> WIDE
        WIDE -> EXPANDED
        EXPANDED -> COMPACT
    }
}

data class HudWidgetConfig(
    val type: HudWidgetType,
    val size: WidgetSize = type.defaultSize,
    val isVisible: Boolean = true
)

enum class ArcPowerMode(
    val label: String,
    val shortCode: String,
    val outputGigawatts: Float,
    val coreRpm: Int,
    val speedMultiplier: Float,
    val coreTempKelvin: Int
) {
    ECO_STANDBY("STEALTH IDLE", "STBY", 0.85f, 1800, 0.55f, 2980),
    NOMINAL_CORE("MARK LXXXV NOMINAL", "NOM", 3.20f, 4600, 1.0f, 4150),
    COMBAT_BOOST("COMBAT OVERCLOCK", "CMBT", 7.80f, 9200, 1.85f, 6420),
    UNIBEAM_OVERDRIVE("UNIBEAM MAXIMA", "OVRD", 12.40f, 14800, 3.0f, 9850);

    fun next(): ArcPowerMode = when (this) {
        ECO_STANDBY -> NOMINAL_CORE
        NOMINAL_CORE -> COMBAT_BOOST
        COMBAT_BOOST -> UNIBEAM_OVERDRIVE
        UNIBEAM_OVERDRIVE -> ECO_STANDBY
    }
}

data class BatteryTelemetry(
    val percentage: Int = 94,
    val isCharging: Boolean = true,
    val chargeSource: String = "ARC INDUCTION",
    val voltageMv: Int = 4280,
    val temperatureC: Float = 31.4f,
    val healthStatus: String = "OPTIMAL",
    val estimatedHoursRemaining: Float = 18.5f
)

data class SystemTelemetry(
    val ramUsedMb: Long = 3840L,
    val ramTotalMb: Long = 8192L,
    val ramUsagePercent: Int = 47,
    val cpuCores: Int = 8,
    val cpuUsagePercent: Int = 28,
    val activeThreads: Int = 42,
    val jvmFreeMb: Long = 128L,
    val cpuHistory: List<Float> = listOf(0.24f, 0.30f, 0.28f, 0.35f, 0.26f, 0.31f, 0.29f, 0.28f),
    val ramHistory: List<Float> = listOf(0.45f, 0.46f, 0.46f, 0.47f, 0.47f, 0.48f, 0.47f, 0.47f)
)

data class ConnectivityTelemetry(
    val wifiConnected: Boolean = true,
    val networkTypeLabel: String = "STARK SAT-LINK // WIFI-6E",
    val downlinkMbps: Int = 860,
    val uplinkMbps: Int = 420,
    val signalStrengthPercent: Int = 96,
    val bluetoothEnabled: Boolean = false,
    val bluetoothStatusLabel: String = "STANDBY",
    val pingLatencyMs: Int = 11,
    val localIp: String = "10.88.0.142"
)

data class StarkLocationPreset(
    val name: String,
    val sectorCode: String,
    val latitude: Double,
    val longitude: Double
)

val STARK_LOCATIONS = listOf(
    StarkLocationPreset("MALIBU POINT 10880", "SEC-CA-01", 34.0016, -118.8068),
    StarkLocationPreset("STARK TOWER NYC", "SEC-NY-85", 40.7549, -73.9763),
    StarkLocationPreset("AVENGERS COMPOUND", "SEC-NY-09", 41.4501, -74.0412),
    StarkLocationPreset("TOKYO SHIBUYA GRID", "SEC-JP-04", 35.6595, 139.7004),
    StarkLocationPreset("LONDON WHITEHALL", "SEC-UK-02", 51.5045, -0.1265)
)

data class WeatherTelemetry(
    val locationName: String = "MALIBU POINT 10880",
    val sectorCode: String = "SEC-CA-01",
    val latitude: Double = 34.0016,
    val longitude: Double = -118.8068,
    val tempCelsius: Float = 21.8f,
    val feelsLikeCelsius: Float = 21.2f,
    val humidityPercent: Int = 54,
    val windSpeedKmh: Float = 14.2f,
    val windDirectionDeg: Int = 245,
    val pressureHpa: Float = 1015.4f,
    val conditionLabel: String = "CLEAR SKY",
    val flightCeilingStatus: String = "OPTIMAL VISIBILITY // VFR CLEAR",
    val isLoading: Boolean = false,
    val lastUpdated: String = "LIVE",
    val error: String? = null
)

data class FlightTelemetry(
    val pitchDegrees: Float = 0f,
    val rollDegrees: Float = 0f,
    val headingDegrees: Int = 285,
    val altitudeFeet: Int = 14250,
    val machSpeed: Float = 1.42f,
    val gForce: Float = 1.15f,
    val leftThrusterPct: Int = 92,
    val rightThrusterPct: Int = 92,
    val icingRisk: String = "NEGATIVE",
    val manualPitchTrim: Float = 0f,
    val manualRollTrim: Float = 0f
)

data class VoiceMessage(
    val id: String,
    val speaker: String, // "STARK" or "J.A.R.V.I.S."
    val text: String,
    val timestamp: String,
    val actionBadge: String? = null
)

data class SynthTrack(
    val id: Int,
    val title: String,
    val subtitle: String,
    val bpm: Int,
    val sequenceFreqs: List<Float>,
    val bassFreq: Float
)

val STARK_SYNTH_TRACKS = listOf(
    SynthTrack(
        id = 0,
        title = "ARC CORE RESONANCE",
        subtitle = "STARK LABS // HARMONIC SYNTH",
        bpm = 124,
        sequenceFreqs = listOf(220f, 277.18f, 329.63f, 440f, 329.63f, 554.37f, 440f, 329.63f),
        bassFreq = 110f
    ),
    SynthTrack(
        id = 1,
        title = "SHOOT TO THRILL // HUD MIX",
        subtitle = "MARK LXXXV TACTICAL OVERDRIVE",
        bpm = 140,
        sequenceFreqs = listOf(293.66f, 369.99f, 440f, 587.33f, 440f, 554.37f, 659.25f, 587.33f),
        bassFreq = 146.83f
    ),
    SynthTrack(
        id = 2,
        title = "STRATOSPHERE MACH-3",
        subtitle = "ORBITAL TELEMETRY ARPEGGIO",
        bpm = 116,
        sequenceFreqs = listOf(196f, 246.94f, 293.66f, 392f, 493.88f, 392f, 293.66f, 246.94f),
        bassFreq = 98f
    ),
    SynthTrack(
        id = 3,
        title = "DEFCON RED PROTOCOL",
        subtitle = "COMBAT MATRIX PULSE",
        bpm = 152,
        sequenceFreqs = listOf(164.81f, 196f, 246.94f, 329.63f, 369.99f, 329.63f, 246.94f, 196f),
        bassFreq = 82.41f
    )
)
