package com.example.data

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Process
import android.os.SystemClock
import java.net.Inet4Address
import java.net.NetworkInterface
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HardwareTelemetryManager(private val context: Context) : SensorEventListener {

    private val appContext = context.applicationContext

    private val _batteryState = MutableStateFlow(BatteryTelemetry())
    val batteryState: StateFlow<BatteryTelemetry> = _batteryState.asStateFlow()

    private val _flashlightOn = MutableStateFlow(false)
    val flashlightOn: StateFlow<Boolean> = _flashlightOn.asStateFlow()

    private val _hasHardwareFlash = MutableStateFlow(false)
    val hasHardwareFlash: StateFlow<Boolean> = _hasHardwareFlash.asStateFlow()

    private val _sensorPitch = MutableStateFlow(0f)
    val sensorPitch: StateFlow<Float> = _sensorPitch.asStateFlow()

    private val _sensorRoll = MutableStateFlow(0f)
    val sensorRoll: StateFlow<Float> = _sensorRoll.asStateFlow()

    private val _sensorHeading = MutableStateFlow(285)
    val sensorHeading: StateFlow<Int> = _sensorHeading.asStateFlow()

    private var cameraManager: CameraManager? = null
    private var torchCameraId: String? = null
    private var sensorManager: SensorManager? = null
    private var batteryReceiverRegistered = false

    private var lastCpuTimeMs: Long = Process.getElapsedCpuTime()
    private var lastWallTimeMs: Long = SystemClock.elapsedRealtime()
    private val cpuHistoryBuffer = ArrayDeque(listOf(0.24f, 0.28f, 0.31f, 0.26f, 0.33f, 0.29f, 0.27f, 0.30f))
    private val ramHistoryBuffer = ArrayDeque(listOf(0.45f, 0.46f, 0.46f, 0.47f, 0.47f, 0.48f, 0.47f, 0.47f))

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            updateBatteryFromIntent(intent)
        }
    }

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            if (cameraId == torchCameraId) {
                _flashlightOn.value = enabled
            }
        }
    }

    init {
        registerBatteryMonitor()
        initTorchController()
        initSensors()
    }

    private fun registerBatteryMonitor() {
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val sticky = appContext.registerReceiver(batteryReceiver, filter)
            batteryReceiverRegistered = true
            if (sticky != null) {
                updateBatteryFromIntent(sticky)
            }
        } catch (_: Exception) {
        }
    }

    private fun updateBatteryFromIntent(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val pct = if (level >= 0 && scale > 0) ((level * 100f) / scale).roundToInt() else 92

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val source = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC REACTOR BUS"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB TELEMETRY LINK"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "INDUCTIVE COIL"
            else -> if (isCharging) "ARC CORE TRICKLE" else "INTERNAL CELLS"
        }

        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4180).let {
            if (it in 1..20) it * 1000 else if (it <= 0) 4180 else it
        }
        val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 312)
        val tempC = if (tempTenths > 0) tempTenths / 10f else 31.2f

        val healthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
        val healthLabel = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "OPTIMAL"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "THERMAL WARN"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "OVERVOLT"
            else -> "NOMINAL"
        }

        val estHours = (pct * 0.22f).coerceAtLeast(0.5f)
        _batteryState.value = BatteryTelemetry(
            percentage = pct.coerceIn(0, 100),
            isCharging = isCharging,
            chargeSource = source,
            voltageMv = voltage,
            temperatureC = tempC,
            healthStatus = healthLabel,
            estimatedHoursRemaining = estHours
        )
    }

    private fun initTorchController() {
        try {
            cameraManager = appContext.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cm = cameraManager ?: return
            for (id in cm.cameraIdList) {
                val chars = cm.getCameraCharacteristics(id)
                val flashAvailable = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                if (flashAvailable) {
                    torchCameraId = id
                    _hasHardwareFlash.value = true
                    break
                }
            }
            if (torchCameraId != null) {
                cm.registerTorchCallback(torchCallback, null)
            }
        } catch (_: Exception) {
            _hasHardwareFlash.value = false
        }
    }

    fun toggleFlashlight(forceState: Boolean? = null): Boolean {
        val target = forceState ?: !_flashlightOn.value
        val camId = torchCameraId
        if (camId != null && cameraManager != null) {
            try {
                cameraManager?.setTorchMode(camId, target)
                _flashlightOn.value = target
                return target
            } catch (_: Exception) {
            }
        }
        // Fallback high-luminance HUD photon strobe mode when hardware flash is absent on emulator
        _flashlightOn.value = target
        return target
    }

    private fun initSensors() {
        try {
            sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (accel != null) {
                sensorManager?.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI)
            }
        } catch (_: Exception) {
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val newRoll = (-x * 4.5f).coerceIn(-45f, 45f)
            val newPitch = ((z - 5.5f) * 3.8f).coerceIn(-40f, 40f)
            if (abs(newRoll - _sensorRoll.value) > 0.3f) {
                _sensorRoll.value = _sensorRoll.value * 0.8f + newRoll * 0.2f
            }
            if (abs(newPitch - _sensorPitch.value) > 0.3f) {
                _sensorPitch.value = _sensorPitch.value * 0.8f + newPitch * 0.2f
            }
            val headingShift = ((x * 2f + y).roundToInt())
            _sensorHeading.value = (285 + headingShift + 360) % 360
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    fun pollSystemTelemetry(): SystemTelemetry {
        val am = appContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(memInfo)

        val totalMb = if (memInfo.totalMem > 0) memInfo.totalMem / (1024 * 1024) else 8192L
        val availMb = if (memInfo.availMem > 0) memInfo.availMem / (1024 * 1024) else 4120L
        val usedMb = (totalMb - availMb).coerceAtLeast(512L)
        val ramPct = ((usedMb * 100f) / totalMb.coerceAtLeast(1L)).roundToInt().coerceIn(5, 99)

        val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        val nowCpuMs = Process.getElapsedCpuTime()
        val nowWallMs = SystemClock.elapsedRealtime()
        val deltaCpu = (nowCpuMs - lastCpuTimeMs).coerceAtLeast(1L)
        val deltaWall = (nowWallMs - lastWallTimeMs).coerceAtLeast(1L)
        lastCpuTimeMs = nowCpuMs
        lastWallTimeMs = nowWallMs

        val appCpuRatio = ((deltaCpu.toFloat() / deltaWall.toFloat()) * 100f)
        // Combine real process CPU activity + JVM memory pressure into a realistic HUD core utilization readout
        val runtime = Runtime.getRuntime()
        val jvmUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val jvmFree = runtime.freeMemory() / (1024 * 1024)
        val computedCpuPct = (16f + appCpuRatio * 2.4f + (jvmUsed % 19)).roundToInt().coerceIn(11, 96)

        if (cpuHistoryBuffer.size >= 12) cpuHistoryBuffer.removeFirst()
        cpuHistoryBuffer.addLast(computedCpuPct / 100f)

        if (ramHistoryBuffer.size >= 12) ramHistoryBuffer.removeFirst()
        ramHistoryBuffer.addLast(ramPct / 100f)

        return SystemTelemetry(
            ramUsedMb = usedMb,
            ramTotalMb = totalMb,
            ramUsagePercent = ramPct,
            cpuCores = cores,
            cpuUsagePercent = computedCpuPct,
            activeThreads = Thread.activeCount(),
            jvmFreeMb = jvmFree,
            cpuHistory = cpuHistoryBuffer.toList(),
            ramHistory = ramHistoryBuffer.toList()
        )
    }

    fun pollConnectivityTelemetry(): ConnectivityTelemetry {
        var wifiConnected = false
        var networkLabel = "OFFLINE // LOCAL CACHE"
        var downMbps = 0
        var upMbps = 0
        var signalPct = 0

        try {
            val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNet = cm?.activeNetwork
            val caps = if (activeNet != null) cm.getNetworkCapabilities(activeNet) else null
            if (caps != null) {
                val hasWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                val hasCellular = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                val hasEthernet = caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                wifiConnected = hasWifi || hasCellular || hasEthernet
                networkLabel = when {
                    hasWifi -> "STARK SAT-LINK // WIFI"
                    hasCellular -> "TACTICAL 5G CARRIER"
                    hasEthernet -> "HARDLINE FIBER OPTIC"
                    else -> "ENCRYPTED TUNNEL"
                }
                downMbps = (caps.linkDownstreamBandwidthKbps / 1000).coerceIn(45, 1200)
                upMbps = (caps.linkUpstreamBandwidthKbps / 1000).coerceIn(25, 650)
                signalPct = if (wifiConnected) 94 else 0
            }
        } catch (_: Exception) {
        }

        var btEnabled = false
        var btLabel = "DISABLED"
        try {
            val bm = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val adapter = bm?.adapter
            if (adapter != null) {
                btEnabled = adapter.isEnabled
                btLabel = if (btEnabled) "MARK LXXXV HELMET LINKED" else "STANDBY"
            }
        } catch (_: Exception) {
        }

        val ipAddr = getLocalIpAddress() ?: "10.88.0.142"
        val pingMs = if (wifiConnected) (9 + (SystemClock.elapsedRealtime() % 7).toInt()) else 0

        return ConnectivityTelemetry(
            wifiConnected = wifiConnected,
            networkTypeLabel = networkLabel,
            downlinkMbps = downMbps,
            uplinkMbps = upMbps,
            signalStrengthPercent = signalPct,
            bluetoothEnabled = btEnabled,
            bluetoothStatusLabel = btLabel,
            pingLatencyMs = pingMs,
            localIp = ipAddr
        )
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownDeviceLocation(): Pair<Double, Double>? {
        return try {
            val lm = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
            val providers = lm.getProviders(true)
            for (provider in providers) {
                val loc = lm.getLastKnownLocation(provider)
                if (loc != null) {
                    return loc.latitude to loc.longitude
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    fun getSystemMediaVolumePercent(): Int {
        return try {
            val am = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return 75
            val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
            ((cur * 100f) / max).roundToInt().coerceIn(0, 100)
        } catch (_: Exception) {
            75
        }
    }

    fun setSystemMediaVolumePercent(percent: Int) {
        try {
            val am = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
            val target = ((percent.coerceIn(0, 100) / 100f) * max).roundToInt()
            am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        } catch (_: Exception) {
        }
    }

    private fun getLocalIpAddress(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    fun cleanup() {
        try {
            if (batteryReceiverRegistered) {
                appContext.unregisterReceiver(batteryReceiver)
                batteryReceiverRegistered = false
            }
            cameraManager?.unregisterTorchCallback(torchCallback)
            sensorManager?.unregisterListener(this)
        } catch (_: Exception) {
        }
    }
}
