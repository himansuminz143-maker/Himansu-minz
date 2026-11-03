package com.example.data

import com.example.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class JarvisNetworkService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetchLiveWeather(
        locationName: String,
        sectorCode: String,
        latitude: Double,
        longitude: Double
    ): WeatherTelemetry = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m" +
                "&wind_speed_unit=kmh"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "StarkHudOS/85.0")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("SAT-WEATHER HTTP ${response.code}")
                }
                val bodyStr = response.body?.string() ?: throw IllegalStateException("Empty telemetry payload")
                val root = JSONObject(bodyStr)
                val current = root.getJSONObject("current")

                val tempC = current.optDouble("temperature_2m", 21.5).toFloat()
                val apparentC = current.optDouble("apparent_temperature", tempC.toDouble()).toFloat()
                val humidity = current.optInt("relative_humidity_2m", 52)
                val windKmh = current.optDouble("wind_speed_10m", 12.0).toFloat()
                val windDir = current.optInt("wind_direction_10m", 240)
                val pressure = current.optDouble("surface_pressure", 1013.2).toFloat()
                val weatherCode = current.optInt("weather_code", 0)

                val (conditionText, flightStatus) = decodeWmoWeatherCode(weatherCode, windKmh)
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

                WeatherTelemetry(
                    locationName = locationName,
                    sectorCode = sectorCode,
                    latitude = latitude,
                    longitude = longitude,
                    tempCelsius = tempC,
                    feelsLikeCelsius = apparentC,
                    humidityPercent = humidity,
                    windSpeedKmh = windKmh,
                    windDirectionDeg = windDir,
                    pressureHpa = pressure,
                    conditionLabel = conditionText,
                    flightCeilingStatus = flightStatus,
                    isLoading = false,
                    lastUpdated = "SYNC $timeStr",
                    error = null
                )
            }
        } catch (e: Exception) {
            WeatherTelemetry(
                locationName = locationName,
                sectorCode = sectorCode,
                latitude = latitude,
                longitude = longitude,
                tempCelsius = 21.4f,
                feelsLikeCelsius = 20.8f,
                humidityPercent = 50,
                windSpeedKmh = 13.6f,
                windDirectionDeg = 255,
                pressureHpa = 1014.8f,
                conditionLabel = "CLEAR // LOCAL SENSOR",
                flightCeilingStatus = "OPTIMAL VISIBILITY // VFR",
                isLoading = false,
                lastUpdated = "CACHED TELEMETRY",
                error = e.localizedMessage
            )
        }
    }

    private fun decodeWmoWeatherCode(code: Int, windKmh: Float): Pair<String, String> {
        val cond = when (code) {
            0 -> "CLEAR STRATOSPHERE"
            1, 2 -> "SCATTERED CLOUDS"
            3 -> "OVERCAST CANOPY"
            45, 48 -> "LOW VISIBILITY FOG"
            51, 53, 55 -> "LIGHT DRIZZLE"
            61, 63, 65 -> "ACTIVE PRECIPITATION"
            71, 73, 75 -> "CRYOGENIC SNOWFALL"
            80, 81, 82 -> "CONVECTIVE SHOWERS"
            95, 96, 99 -> "ELECTRICAL STORM"
            else -> "NOMINAL ATMOSPHERE"
        }
        val flight = when {
            code >= 95 -> "LIGHTNING HAZARD // ENGAGE FARADAY SHIELD"
            windKmh > 40f -> "HIGH SHEAR TURBULENCE // STABILIZERS AT 100%"
            code in 45..48 -> "IFR INSTRUMENT FLIGHT // INFRARED OPTICS ON"
            else -> "OPTIMAL FLIGHT ENVELOPE // MACH 3.2 CLEARED"
        }
        return cond to flight
    }

    suspend fun queryJarvisBrain(
        userPrompt: String,
        systemSnapshotSummary: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            val field = BuildConfig::class.java.getDeclaredField("GEMINI_API_KEY")
            field.isAccessible = true
            (field.get(null) as? String).orEmpty()
        } catch (_: Exception) {
            ""
        }

        // If the user configured a real Gemini API key in AI Studio Secrets panel, invoke Gemini API directly
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && !apiKey.startsWith("YOUR_")) {
            try {
                val endpoint =
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
                val systemInstruction =
                    "You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), Tony Stark's ultra-sophisticated British AI embedded in the Mark LXXXV Iron Man HUD. " +
                        "Respond concisely (2-3 crisp sentences max), addressing the user as 'Sir' or 'Boss', citing the real live suit telemetry provided: $systemSnapshotSummary"

                val payload = JSONObject().apply {
                    put(
                        "contents",
                        JSONArray().put(
                            JSONObject().put(
                                "parts",
                                JSONArray().put(
                                    JSONObject().put("text", "$systemInstruction\n\nUser query: $userPrompt")
                                )
                            )
                        )
                    )
                }

                val req = Request.Builder()
                    .url(endpoint)
                    .post(payload.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                httpClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string().orEmpty()
                        val json = JSONObject(body)
                        val candidates = json.optJSONArray("candidates")
                        val first = candidates?.optJSONObject(0)
                        val content = first?.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        val text = parts?.optJSONObject(0)?.optString("text")
                        if (!text.isNullOrBlank()) {
                            return@withContext text.trim()
                        }
                    }
                }
            } catch (_: Exception) {
                // Fall through to onboard tactical telemetry synthesizer
            }
        }

        // Onboard J.A.R.V.I.S. tactical heuristic core using live hardware telemetry
        return@withContext synthesizeOnboardJarvisResponse(userPrompt, systemSnapshotSummary)
    }

    private fun synthesizeOnboardJarvisResponse(
        prompt: String,
        telemetrySummary: String
    ): String {
        val lower = prompt.lowercase(Locale.US)
        return when {
            lower.contains("status") || lower.contains("diagnostic") || lower.contains("report") ->
                "All Mark LXXXV primary systems are online, Sir. $telemetrySummary. Structural integrity and neural link remain at peak efficiency."
            lower.contains("weather") || lower.contains("temperature") || lower.contains("wind") ->
                "Atmospheric sensors are locked onto your sector, Sir. $telemetrySummary. Flight surfaces are calibrated for current barometric pressure."
            lower.contains("battery") || lower.contains("power") || lower.contains("arc") ->
                "Palladium-free vibranium Arc Reactor core is stable, Sir. $telemetrySummary. Reserve capacitors are ready for Unibeam discharge on your mark."
            lower.contains("who are you") || lower.contains("jarvis") || lower.contains("hello") || lower.contains("hi") ->
                "At your service, Sir. I am J.A.R.V.I.S., operating on the Mark LXXXV tactical hologram matrix. $telemetrySummary."
            else ->
                "Directive logged, Sir. Onboard Mark LXXXV tactical processor reports: $telemetrySummary. (Tip: Add GEMINI_API_KEY in the AI Studio Secrets panel for open-ended cloud intelligence.)"
        }
    }
}
