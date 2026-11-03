package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.KeyEvent
import com.example.data.STARK_SYNTH_TRACKS
import com.example.data.SynthTrack
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val TAG = "JarvisSoundEngine"
private const val SAMPLE_RATE = 24000

class JarvisSoundEngine(private val context: Context) {

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    var isSfxEnabled: Boolean = true
    var isVoiceTtsEnabled: Boolean = true

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Music synthesizer state
    private var musicJob: Job? = null
    private val _isMusicPlaying = MutableStateFlow(false)
    val isMusicPlaying: StateFlow<Boolean> = _isMusicPlaying.asStateFlow()

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex.asStateFlow()

    private val _trackProgress = MutableStateFlow(0f)
    val trackProgress: StateFlow<Float> = _trackProgress.asStateFlow()

    private val _spectrumBands = MutableStateFlow(List(16) { 0.12f })
    val spectrumBands: StateFlow<List<Float>> = _spectrumBands.asStateFlow()

    init {
        initTextToSpeech()
    }

    private fun initTextToSpeech() {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.UK)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.US)
                    }
                    tts?.setPitch(0.92f)
                    tts?.setSpeechRate(1.06f)
                    isTtsReady = true
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "TTS initialization skipped in current environment", e)
        }
    }

    fun speak(message: String) {
        if (!isVoiceTtsEnabled) return
        engineScope.launch {
            _isSpeaking.value = true
            try {
                if (isTtsReady) {
                    tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "jarvis_utt_${System.currentTimeMillis()}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Speak error", e)
            }
            val durationMs = (message.length * 48L).coerceIn(1200L, 5500L)
            delay(durationMs)
            _isSpeaking.value = false
        }
    }

    fun triggerHaptic(durationMs: Long = 18L) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun playHudTap() {
        triggerHaptic(14L)
        if (!isSfxEnabled) return
        engineScope.launch {
            playSynthesizedBuffer(
                durationMs = 65,
                generator = { t, progress ->
                    val freq = 1650.0 + progress * 1100.0
                    val env = (1.0 - progress)
                    (sin(2.0 * PI * freq * t) * 0.55 + sin(2.0 * PI * (freq * 1.5) * t) * 0.25) * env
                }
            )
        }
    }

    fun playArcPulse() {
        triggerHaptic(32L)
        if (!isSfxEnabled) return
        engineScope.launch {
            playSynthesizedBuffer(
                durationMs = 260,
                generator = { t, progress ->
                    val sweep = 140.0 + sin(progress * PI) * 520.0
                    val sub = sin(2.0 * PI * 90.0 * t) * 0.45
                    val harmonic = sin(2.0 * PI * sweep * t) * 0.45
                    val shimmer = sin(2.0 * PI * (sweep * 2.5) * t) * 0.18
                    val env = sin(progress * PI)
                    (sub + harmonic + shimmer) * env
                }
            )
        }
    }

    fun playRedAlertSiren() {
        triggerHaptic(55L)
        if (!isSfxEnabled) return
        engineScope.launch {
            playSynthesizedBuffer(
                durationMs = 420,
                generator = { t, progress ->
                    val cycle = (progress * 2.0) % 1.0
                    val freq = 520.0 + cycle * 560.0
                    val env = if (cycle < 0.85) 0.8 else 0.2
                    (sin(2.0 * PI * freq * t) * 0.6 + sin(2.0 * PI * (freq * 0.5) * t) * 0.3) * env
                }
            )
        }
    }

    fun playFlightEngage() {
        triggerHaptic(40L)
        if (!isSfxEnabled) return
        engineScope.launch {
            playSynthesizedBuffer(
                durationMs = 340,
                generator = { t, progress ->
                    val freq = 220.0 + (progress * progress) * 1480.0
                    val env = sin(progress * PI)
                    (sin(2.0 * PI * freq * t) * 0.55 + sin(2.0 * PI * (freq * 1.25) * t) * 0.3) * env
                }
            )
        }
    }

    fun playBootChord() {
        if (!isSfxEnabled) return
        engineScope.launch {
            playSynthesizedBuffer(
                durationMs = 480,
                generator = { t, progress ->
                    val env = sin(progress * PI)
                    val c1 = sin(2.0 * PI * 220.0 * t) * 0.30
                    val c2 = sin(2.0 * PI * 329.63 * t) * 0.25
                    val c3 = sin(2.0 * PI * 440.0 * t) * 0.25
                    val c4 = sin(2.0 * PI * (659.25 + progress * 220.0) * t) * 0.20
                    (c1 + c2 + c3 + c4) * env
                }
            )
        }
    }

    // Stark Audio Matrix Synthesizer + System Media Dispatch
    fun toggleMusicPlayback() {
        playHudTap()
        if (_isMusicPlaying.value) {
            stopMusic()
        } else {
            startMusic()
        }
        dispatchSystemMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    }

    fun nextTrack() {
        playHudTap()
        _currentTrackIndex.value = (_currentTrackIndex.value + 1) % STARK_SYNTH_TRACKS.size
        _trackProgress.value = 0f
        dispatchSystemMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    fun previousTrack() {
        playHudTap()
        val prev = _currentTrackIndex.value - 1
        _currentTrackIndex.value = if (prev < 0) STARK_SYNTH_TRACKS.lastIndex else prev
        _trackProgress.value = 0f
        dispatchSystemMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }

    fun seekTo(progress: Float) {
        _trackProgress.value = progress.coerceIn(0f, 1f)
    }

    private fun startMusic() {
        _isMusicPlaying.value = true
        musicJob?.cancel()
        musicJob = engineScope.launch {
            var step = 0
            while (isActive && _isMusicPlaying.value) {
                val track: SynthTrack = STARK_SYNTH_TRACKS[_currentTrackIndex.value]
                val noteFreq = track.sequenceFreqs[step % track.sequenceFreqs.size]
                val stepDurationMs = (60_000 / track.bpm / 2).coerceIn(160, 320)

                // Update 16-band spectrum analyzer with live harmonic levels
                val newSpectrum = List(16) { band ->
                    val center = (step + band * 2) % 16
                    val resonance = sin((center / 16.0) * PI).toFloat()
                    (0.20f + resonance * 0.72f).coerceIn(0.12f, 0.98f)
                }
                _spectrumBands.value = newSpectrum

                if (isSfxEnabled) {
                    playSynthesizedBuffer(
                        durationMs = stepDurationMs - 25,
                        generator = { t, progress ->
                            val env = (1.0 - progress * 0.75) * sin(progress.coerceAtMost(0.15) / 0.15 * (PI / 2.0))
                            val lead = sin(2.0 * PI * noteFreq * t) * 0.28
                            val octave = sin(2.0 * PI * (noteFreq * 2.0) * t) * 0.12
                            val bass = sin(2.0 * PI * track.bassFreq * t) * 0.24
                            (lead + octave + bass) * env
                        }
                    )
                }

                _trackProgress.value = ((_trackProgress.value + 0.015f) % 1.0f)
                step++
                delay(stepDurationMs.toLong())
            }
        }
    }

    fun stopMusic() {
        _isMusicPlaying.value = false
        musicJob?.cancel()
        _spectrumBands.value = List(16) { 0.12f }
    }

    private fun dispatchSystemMediaKey(keyCode: Int) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
            audioManager.dispatchMediaKeyEvent(downEvent)
            audioManager.dispatchMediaKeyEvent(upEvent)
        } catch (_: Exception) {
        }
    }

    private fun playSynthesizedBuffer(
        durationMs: Int,
        generator: (timeSec: Double, progress: Double) -> Double
    ) {
        try {
            val numSamples = (SAMPLE_RATE * durationMs) / 1000
            if (numSamples <= 0) return
            val pcm = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                val progress = i.toDouble() / numSamples
                val sample = generator(t, progress).coerceIn(-1.0, 1.0)
                pcm[i] = (sample * Short.MAX_VALUE * 0.65).toInt().toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(pcm, 0, pcm.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 15L)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {
        }
    }

    fun shutdown() {
        stopMusic()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        }
    }
}
