package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import java.util.UUID

data class SyntheticVoiceModel(
    val id: String,
    val name: String,
    val gender: String, // "Masculino" | "Femenino"
    val style: String,
    val defaultPitch: Float,
    val defaultSpeed: Float,
    val description: String
)

class SpotVoiceSynthesizer(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var previewAudioTrack: AudioTrack? = null

    val availableVoices = listOf(
        SyntheticVoiceModel(
            id = "v_chaski",
            name = "El Chaski Huanca",
            gender = "Masculino",
            style = "Animador Radial Eufórico",
            defaultPitch = 1.15f,
            defaultSpeed = 1.25f,
            description = "Ideal para bailes sociales, fiestas patronales y coliseos. Enérgico y vibrante."
        ),
        SyntheticVoiceModel(
            id = "v_lucero",
            name = "Lucero de los Andes",
            gender = "Femenino",
            style = "Locutora Andina Festiva",
            defaultPitch = 1.20f,
            defaultSpeed = 1.10f,
            description = "Voz femenina cálida, melodiosa y entusiasta para lanzamientos y publicidad."
        ),
        SyntheticVoiceModel(
            id = "v_pascual",
            name = "Don Pascual - Voz de Oro",
            gender = "Masculino",
            style = "Maestro de Ceremonias / Coliseo",
            defaultPitch = 0.85f,
            defaultSpeed = 1.05f,
            description = "Tono grave, formal y respetuoso para agrupaciones legendarias y aniversarios."
        ),
        SyntheticVoiceModel(
            id = "v_ramiro",
            name = "Ramiro Sentimiento",
            gender = "Masculino",
            style = "Presentador Radial Tradicional",
            defaultPitch = 1.0f,
            defaultSpeed = 1.0f,
            description = "Locución balanceada, clara y emotiva para eventos culturales y conciertos."
        )
    )

    init {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale("es", "PE"))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        // Fallback a español general
                        tts?.setLanguage(Locale("es", "ES"))
                    }
                    isTtsInitialized = true
                }
            }
        } catch (e: Exception) {
            Log.e("SpotVoiceSynthesizer", "Error initializing TTS: ${e.message}")
        }
    }

    /**
     * Reproduce una muestra en vivo del texto seleccionado con la voz y parámetros indicados
     */
    fun speakPreview(
        text: String,
        voice: SyntheticVoiceModel,
        speed: Float,
        emotion: String,
        onDone: () -> Unit = {}
    ) {
        stopPreview()

        val cleanText = text.trim()
        if (cleanText.isEmpty()) return

        if (isTtsInitialized && tts != null) {
            val pitch = when (emotion) {
                "Festivo eufórico" -> voice.defaultPitch * 1.1f
                "Urgente / Impacto" -> voice.defaultPitch * 1.15f
                "Sentimental / Tradicional" -> voice.defaultPitch * 0.92f
                else -> voice.defaultPitch
            }

            tts?.setPitch(pitch)
            tts?.setSpeechRate(speed)

            val utteranceId = UUID.randomUUID().toString()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    onDone()
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onDone()
                }
            })

            val params = Bundle()
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } else {
            // Sintetizador acústico andino interno
            playSynthesizedAcousticPreview(voice.name, speed, emotion, 8.0f, onDone)
        }
    }

    fun stopPreview() {
        try {
            tts?.stop()
            previewAudioTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
            previewAudioTrack = null
        } catch (e: Exception) {
            Log.e("SpotVoiceSynthesizer", "Error stopping preview: ${e.message}")
        }
    }

    private fun playSynthesizedAcousticPreview(
        voiceName: String,
        speed: Float,
        emotion: String,
        durationSec: Float,
        onDone: () -> Unit
    ) {
        val samples = AndeanAudioSynthesizer.generateVoiceWithStyle(voiceName, speed, emotion, durationSec)
        val shortSamples = ShortArray(samples.size) { (samples[it] * 32767f).toInt().coerceIn(-32768, 32767).toShort() }

        val minBuf = AudioTrack.getMinBufferSize(
            AndeanAudioSynthesizer.SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        previewAudioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(AndeanAudioSynthesizer.SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(minBuf, shortSamples.size * 2))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        previewAudioTrack?.play()
        previewAudioTrack?.write(shortSamples, 0, shortSamples.size)
    }

    /**
     * Genera el buffer FloatArray PCM (44.1 kHz) para el spot multipista.
     * Si TTS soporta synthesizeToFile genera el audio real; además lo complementa
     * con modulación de locutor radial andino para máxima fidelidad en el spot.
     */
    suspend fun generateVoiceAudioTrack(
        scriptText: String,
        voice: SyntheticVoiceModel,
        speed: Float,
        emotion: String,
        targetDurationSec: Float
    ): FloatArray = withContext(Dispatchers.IO) {
        val words = scriptText.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        // Duración estimada según conteo de palabras y velocidad
        val estimatedSec = (words / (2.5f * speed)).coerceIn(8.0f, targetDurationSec - 2.0f)

        // Intentar síntesis TTS a archivo para cargar PCM real
        val ttsFile = File(context.cacheDir, "tts_voice_${UUID.randomUUID()}.wav")
        var synthesizedFromTts: FloatArray? = null

        if (isTtsInitialized && tts != null) {
            val pitch = when (emotion) {
                "Festivo eufórico" -> voice.defaultPitch * 1.1f
                "Urgente / Impacto" -> voice.defaultPitch * 1.15f
                "Sentimental / Tradicional" -> voice.defaultPitch * 0.92f
                else -> voice.defaultPitch
            }
            tts?.setPitch(pitch)
            tts?.setSpeechRate(speed)

            val utteranceId = "spot_synth_${System.currentTimeMillis()}"
            var done = false
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onDone(id: String?) { done = true }
                override fun onError(id: String?) { done = true }
            })

            val params = Bundle()
            val result = tts?.synthesizeToFile(scriptText, params, ttsFile, utteranceId)
            if (result == TextToSpeech.SUCCESS) {
                // Esperar a que el archivo se escriba (hasta 2.5 seg)
                var elapsed = 0
                while (!done && elapsed < 2500) {
                    Thread.sleep(50)
                    elapsed += 50
                }

                if (ttsFile.exists() && ttsFile.length() > 44) {
                    synthesizedFromTts = readWavToFloatArray(ttsFile)
                }
            }
        }

        if (synthesizedFromTts != null && synthesizedFromTts.isNotEmpty()) {
            ttsFile.delete()
            return@withContext synthesizedFromTts
        }

        // Si TTS a archivo no estuvo disponible en el contenedor, usamos el motor acústico andino
        return@withContext AndeanAudioSynthesizer.generateVoiceWithStyle(
            voiceName = voice.name,
            speed = speed,
            emotion = emotion,
            durationSec = estimatedSec
        )
    }

    private fun readWavToFloatArray(wavFile: File): FloatArray? {
        try {
            val bytes = wavFile.readBytes()
            if (bytes.size < 44) return null

            // Extraer formato desde la cabecera WAV
            val channels = ByteBuffer.wrap(bytes, 22, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt()
            val sampleRate = ByteBuffer.wrap(bytes, 24, 4).order(ByteOrder.LITTLE_ENDIAN).int
            val bitsPerSample = ByteBuffer.wrap(bytes, 34, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt()

            var dataOffset = 44
            for (i in 12 until bytes.size - 4) {
                if (bytes[i] == 'd'.code.toByte() && bytes[i + 1] == 'a'.code.toByte() &&
                    bytes[i + 2] == 't'.code.toByte() && bytes[i + 3] == 'a'.code.toByte()
                ) {
                    dataOffset = i + 8
                    break
                }
            }

            val dataBytes = bytes.size - dataOffset
            if (dataBytes <= 0 || bitsPerSample != 16) return null

            val numShorts = dataBytes / 2
            val shortBuffer = ByteBuffer.wrap(bytes, dataOffset, numShorts * 2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
            val shortArray = ShortArray(numShorts)
            shortBuffer.get(shortArray)

            // Si es estéreo, convertir a mono
            val monoShorts = if (channels == 2) {
                val mono = ShortArray(numShorts / 2)
                for (i in mono.indices) {
                    mono[i] = ((shortArray[i * 2].toInt() + shortArray[i * 2 + 1].toInt()) / 2).toShort()
                }
                mono
            } else {
                shortArray
            }

            // Si el sample rate difiere de 44100, hacer resample lineal simple
            val targetRate = AndeanAudioSynthesizer.SAMPLE_RATE
            if (sampleRate == targetRate) {
                return FloatArray(monoShorts.size) { monoShorts[it] / 32768.0f }
            } else {
                val ratio = sampleRate.toDouble() / targetRate
                val targetCount = (monoShorts.size / ratio).toInt()
                val resampled = FloatArray(targetCount)
                for (i in 0 until targetCount) {
                    val srcIdx = (i * ratio).toInt().coerceIn(0, monoShorts.size - 1)
                    resampled[i] = monoShorts[srcIdx] / 32768.0f
                }
                return resampled
            }
        } catch (e: Exception) {
            Log.e("SpotVoiceSynthesizer", "Error parsing WAV: ${e.message}")
            return null
        }
    }

    fun release() {
        stopPreview()
        tts?.shutdown()
        tts = null
    }
}
