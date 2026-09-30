package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Motor de metrónomo de precisión para ritmos andinos y grabación en estudio multipista.
 * Especializado en los compases típicos del Huayno: 2/4 (tradicional) y 4/4 (moderno con arpa).
 */
class HuaynoMetronomeEngine(
    val sampleRate: Int = 44100
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var audioTrack: AudioTrack? = null
    private var metronomeJob: Job? = null

    // Estados observables en tiempo real para la interfaz
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private val _currentBeat = MutableStateFlow(1) // 1 a N
    val currentBeat: StateFlow<Int> = _currentBeat.asStateFlow()

    private val _isAccent = MutableStateFlow(false)
    val isAccent: StateFlow<Boolean> = _isAccent.asStateFlow()

    private val _isCountingIn = MutableStateFlow(false)
    val isCountingIn: StateFlow<Boolean> = _isCountingIn.asStateFlow()

    private val _countInBeat = MutableStateFlow(0)
    val countInBeat: StateFlow<Int> = _countInBeat.asStateFlow()

    private val _countInTotal = MutableStateFlow(0)
    val countInTotal: StateFlow<Int> = _countInTotal.asStateFlow()

    // Configuración activa
    @Volatile
    var bpm: Int = 108
        private set

    @Volatile
    var meter: HuaynoMeter = HuaynoMeter.TWO_FOUR
        private set

    @Volatile
    var timbre: MetronomeTimbre = MetronomeTimbre.WANKAR
        private set

    @Volatile
    var volume: Float = 0.85f
        private set

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        try {
            audioTrack?.release()
        } catch (_: Exception) {}

        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufSize = max(minBuf, 4096)

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    }

    fun updateConfig(config: MetronomeConfig) {
        this.bpm = config.bpm.coerceIn(40, 240)
        this.meter = config.meter
        this.timbre = config.timbre
        this.volume = config.volume.coerceIn(0f, 1f)
    }

    /**
     * Inicia el metrónomo continuo (para ensayo, calentamiento vocal o reproducción)
     */
    fun startContinuous(config: MetronomeConfig) {
        stop()
        updateConfig(config)
        _isActive.value = true
        _isCountingIn.value = false

        metronomeJob = scope.launch {
            runMetronomeLoop(
                totalBeats = -1, // Infinito
                onFinished = {}
            )
        }
    }

    /**
     * Ejecuta una pre-cuenta (Count-In) antes de iniciar la grabación del cantante.
     * Al terminar la cuenta previa, llama a [onCountInFinished] para iniciar la grabación.
     */
    fun startCountIn(
        config: MetronomeConfig,
        bars: Int,
        keepPlayingAfterCountIn: Boolean,
        onCountInFinished: () -> Unit
    ) {
        stop()
        updateConfig(config)

        val beatsPerBar = config.meter.beatsPerBar
        val totalBeats = max(1, bars * beatsPerBar)

        _isActive.value = true
        _isCountingIn.value = true
        _countInTotal.value = totalBeats
        _countInBeat.value = 1

        metronomeJob = scope.launch {
            runMetronomeLoop(
                totalBeats = totalBeats,
                isCountInPhase = true,
                onFinished = {
                    _isCountingIn.value = false
                    onCountInFinished()
                    if (keepPlayingAfterCountIn) {
                        // Continúa sonando el metrónomo durante la grabación
                        _isActive.value = true
                        runMetronomeLoop(totalBeats = -1, onFinished = {})
                    } else {
                        _isActive.value = false
                    }
                }
            )
        }
    }

    private suspend fun runMetronomeLoop(
        totalBeats: Int,
        isCountInPhase: Boolean = false,
        onFinished: suspend () -> Unit
    ) {
        try {
            audioTrack?.play()
        } catch (_: Exception) {
            initAudioTrack()
            audioTrack?.play()
        }

        val beatsPerBar = meter.beatsPerBar
        var beatIndex = 0
        var totalPlayed = 0

        while (scope.isActive && _isActive.value) {
            val isFirstBeat = (beatIndex % beatsPerBar) == 0
            val beatNumber = (beatIndex % beatsPerBar) + 1

            _currentBeat.value = beatNumber
            _isAccent.value = isFirstBeat

            if (isCountInPhase) {
                _countInBeat.value = totalPlayed + 1
            }

            // Reproducir sonido del clic
            playClickSound(isAccent = isFirstBeat)

            totalPlayed++
            beatIndex++

            if (totalBeats > 0 && totalPlayed >= totalBeats) {
                // Pre-cuenta completada
                val beatIntervalMs = (60_000.0 / bpm).toLong()
                delay(beatIntervalMs)
                onFinished()
                return
            }

            // Esperar el intervalo del tempo (BPM)
            val beatIntervalMs = (60_000.0 / bpm).toLong()
            delay(beatIntervalMs)
        }
    }

    /**
     * Emite un único golpe de prueba para preescucha de timbres
     */
    fun previewClick(timbre: MetronomeTimbre, isAccent: Boolean = true, vol: Float = 0.9f) {
        scope.launch {
            try {
                audioTrack?.play()
            } catch (_: Exception) {
                initAudioTrack()
                audioTrack?.play()
            }
            val samples = generateClickSamples(timbre, isAccent, vol)
            audioTrack?.write(samples, 0, samples.size)
        }
    }

    private fun playClickSound(isAccent: Boolean) {
        val samples = generateClickSamples(timbre, isAccent, volume)
        audioTrack?.write(samples, 0, samples.size)
    }

    /**
     * Genera muestras sintetizadas en tiempo real de alta fidelidad para cada timbre andino
     */
    private fun generateClickSamples(
        timbre: MetronomeTimbre,
        isAccent: Boolean,
        volume: Float
    ): ShortArray {
        return when (timbre) {
            MetronomeTimbre.WANKAR -> generateWankarClick(isAccent, volume)
            MetronomeTimbre.COWBELL -> generateCowbellClick(isAccent, volume)
            MetronomeTimbre.DIGITAL -> generateDigitalClick(isAccent, volume)
        }
    }

    /**
     * Wankar / Madera Andina:
     * Sonido orgánico y cálido de madera / caja andina. El acento en el tiempo 1 añade un cuerpo grave
     * que simula el golpe del bombo huayno tradicional sin saturar los audífonos del cantante.
     */
    private fun generateWankarClick(isAccent: Boolean, vol: Float): ShortArray {
        val durationSec = if (isAccent) 0.045f else 0.028f
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        val baseFreq = if (isAccent) 1050.0 else 780.0
        val endFreq = if (isAccent) 720.0 else 620.0
        val decayRate = if (isAccent) 75.0 else 110.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / durationSec
            val freq = baseFreq + (endFreq - baseFreq) * progress

            val env = exp(-decayRate * t).toFloat()
            // Tono madera con resonancia armónica
            val tone = (sin(2.0 * PI * freq * t) + 0.35 * sin(4.0 * PI * freq * t)).toFloat()

            // Transiente de golpe de madera al inicio
            val snap = if (i < 30) (1.0f - i / 30f) * 0.4f else 0.0f
            val sample = (tone * 0.8f + snap) * env * vol
            val clamped = (sample.coerceIn(-1.0f, 1.0f) * 32767f).toInt().toShort()
            buffer[i] = clamped
        }
        return buffer
    }

    /**
     * Cencerro Huaylash:
     * Golpe metálico nítido característico de las orquestas de Huancayo / Junín.
     * Ideal para cortar en mezclas con muchas notas de arpa y saxofón.
     */
    private fun generateCowbellClick(isAccent: Boolean, vol: Float): ShortArray {
        val durationSec = if (isAccent) 0.060f else 0.038f
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        val f1 = if (isAccent) 840.0 else 580.0
        val f2 = if (isAccent) 1260.0 else 890.0
        val decayRate = if (isAccent) 60.0 else 95.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val env = exp(-decayRate * t).toFloat()
            // Onda metálica con dos parciales inarmónicos
            val wave = (0.6 * sin(2.0 * PI * f1 * t) + 0.4 * sin(2.0 * PI * f2 * t)).toFloat()
            val sample = wave * env * vol
            val clamped = (sample.coerceIn(-1.0f, 1.0f) * 32767f).toInt().toShort()
            buffer[i] = clamped
        }
        return buffer
    }

    /**
     * Beep Digital de Estudio:
     * Pulso electrónico estándar de DAW: agudo (1600 Hz) en el compás 1, medio (980 Hz) en el resto.
     */
    private fun generateDigitalClick(isAccent: Boolean, vol: Float): ShortArray {
        val durationSec = if (isAccent) 0.030f else 0.018f
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        val freq = if (isAccent) 1600.0 else 980.0
        val decayRate = if (isAccent) 90.0 else 130.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val env = exp(-decayRate * t).toFloat()
            val wave = sin(2.0 * PI * freq * t).toFloat()
            val sample = wave * env * vol
            val clamped = (sample.coerceIn(-1.0f, 1.0f) * 32767f).toInt().toShort()
            buffer[i] = clamped
        }
        return buffer
    }

    fun stop() {
        _isActive.value = false
        _isCountingIn.value = false
        metronomeJob?.cancel()
        metronomeJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
        } catch (_: Exception) {}
    }

    fun release() {
        stop()
        try {
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
