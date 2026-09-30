package com.example.audio

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object AndeanAudioSynthesizer {
    const val SAMPLE_RATE = 44100

    /**
     * Synthesizes Andean Harp & Bordón rhythm (typical Huayno tempo ~110 BPM, 2/4 rhythm)
     */
    fun generateHarpHuayno(durationSec: Float): FloatArray {
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = FloatArray(totalSamples)
        val bpm = 108.0
        val beatDuration = 60.0 / bpm
        val eighthNote = beatDuration / 2.0

        // Andean pentatonic frequencies in A minor: A3, C4, D4, E4, G4, A4, C5, E5
        val scale = doubleArrayOf(220.0, 261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 659.25)
        val bassNotes = doubleArrayOf(110.0, 146.83, 164.81, 110.0) // A2, D3, E3, A2

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val beatIndex = (t / eighthNote).toInt()
            val tInNote = t - (beatIndex * eighthNote)

            // Harp arpeggio melody
            val noteIndex = (beatIndex * 3 + (beatIndex / 4)) % scale.size
            val freq = scale[noteIndex]

            // Plucked harp string model (exponential decay)
            val pluckEnvelope = exp(-tInNote * 8.0).toFloat()
            val harpNote = (sin(2 * PI * freq * t) +
                    0.5 * sin(2 * PI * freq * 2.0 * t) +
                    0.25 * sin(2 * PI * freq * 3.0 * t)).toFloat() * pluckEnvelope

            // Bordón bass on 1st and 3rd 16th notes of beat (classic huayno zapateo / bordoneo)
            val bassIndex = (beatIndex / 4) % bassNotes.size
            val bassFreq = bassNotes[bassIndex]
            val tInBass = t - ((beatIndex / 2) * beatDuration)
            val bassEnv = if (tInBass >= 0) exp(-tInBass * 5.0).toFloat() else 0f
            val bassSound = (sin(2 * PI * bassFreq * t) + 0.3 * sin(2 * PI * bassFreq * 2.0 * t)).toFloat() * bassEnv * 0.7f

            // Huayno rhythm strum / cascabel shaker on off-beats
            val shakerNoise = if ((beatIndex % 2) == 1 && tInNote < 0.08) {
                ((((i * 1103515245 + 12345) and 0x7FFFFFFF) / 0x7FFFFFFF.toFloat()) * 2f - 1f) * 0.15f * exp(-tInNote * 30.0).toFloat()
            } else 0f

            samples[i] = ((harpNote * 0.5f + bassSound * 0.45f + shakerNoise) * 0.85f).coerceIn(-1f, 1f)
        }
        return samples
    }

    /**
     * Synthesizes Andean radio announcer voice cadence simulation ("¡Atención Perú, gran concierto!")
     */
    fun generateAnnouncerVoice(durationSec: Float): FloatArray {
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = FloatArray(totalSamples)

        // Phrasing cadence: speech bursts with short pauses
        // Phrase 1: 0.5s - 4.5s
        // Pause: 4.5s - 5.5s
        // Phrase 2: 5.5s - 10.0s
        // Pause: 10.0s - 11.0s
        // Phrase 3: 11.0s - 16.0s
        val phraseSegments = listOf(
            0.5f to 4.5f,
            5.2f to 9.8f,
            10.8f to 15.5f,
            16.5f to 21.0f
        )

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE

            // Check if current time falls into a speech phrase
            val activeSegment = phraseSegments.find { t >= it.first && t <= it.second }
            if (activeSegment != null) {
                val phraseT = t - activeSegment.first

                // Pitch intonation typical of Andean radio animador: dynamic vibrato and rising cadence
                val basePitch = 145.0 + 25.0 * sin(2 * PI * 1.8 * phraseT) + 10.0 * sin(2 * PI * 5.0 * phraseT)

                // Vocal formants (F1, F2 vowel synthesis simulation)
                val vowelCycle = (phraseT * 3.5).toInt() % 4
                val f1 = when (vowelCycle) {
                    0 -> 750.0 // 'a'
                    1 -> 350.0 // 'i'
                    2 -> 500.0 // 'o'
                    else -> 600.0 // 'e'
                }
                val f2 = when (vowelCycle) {
                    0 -> 1200.0
                    1 -> 2200.0
                    2 -> 900.0
                    else -> 1800.0
                }

                // Glottal pulse excitation + formants
                val glottal = (sin(2 * PI * basePitch * t) + 0.6 * sin(2 * PI * basePitch * 2.0 * t) + 0.3 * sin(2 * PI * basePitch * 3.0 * t)).toFloat()
                val vocalic = (sin(2 * PI * f1 * t) * 0.4 + sin(2 * PI * f2 * t) * 0.3).toFloat()

                // Consonant bursts / radio mic coloration
                val radioCrunch = sin(2 * PI * 2800.0 * t).toFloat() * 0.08f

                // Syllable rhythm envelope
                val syllableEnv = (0.5 + 0.5 * sin(2 * PI * 4.2 * phraseT)).toFloat()

                val voiceSample = (glottal * 0.4f + vocalic * 0.5f + radioCrunch) * syllableEnv
                samples[i] = (voiceSample * 0.85f).coerceIn(-1f, 1f)
            } else {
                samples[i] = 0f
            }
        }
        return samples
    }

    /**
     * Synthesizes Andean Quena / Zampoña melody
     */
    fun generateQuenaMelody(durationSec: Float): FloatArray {
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = FloatArray(totalSamples)

        val notes = doubleArrayOf(587.33, 659.25, 783.99, 880.00, 1046.50, 880.00, 783.99) // D5, E5, G5, A5, C6
        val noteDur = 0.75

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val noteIdx = (t / noteDur).toInt() % notes.size
            val freq = notes[noteIdx]
            val tInNote = t % noteDur

            val breathNoise = ((((i * 1664525 + 1013904223) and 0x7FFFFFFF) / 0x7FFFFFFF.toFloat()) * 2f - 1f) * 0.08f
            val vibrato = 1.0 + 0.025 * sin(2 * PI * 5.5 * t)
            val flute = sin(2 * PI * freq * vibrato * t).toFloat() + 0.25f * sin(2 * PI * freq * 2.0 * vibrato * t).toFloat()

            // Envelope
            val attack = (tInNote / 0.08).coerceIn(0.0, 1.0).toFloat()
            val release = ((noteDur - tInNote) / 0.12).coerceIn(0.0, 1.0).toFloat()
            val env = attack * release

            samples[i] = ((flute + breathNoise) * env * 0.65f).coerceIn(-1f, 1f)
        }
        return samples
    }

    /**
     * Synthesizes Andean festival cheering, bombo drum accents & applause
     */
    fun generateCheeringAndApplause(durationSec: Float): FloatArray {
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = FloatArray(totalSamples)

        var seed = 54321
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // White noise filtered for applause crackle
            seed = (seed * 1103515245 + 12345) and 0x7FFFFFFF
            val noise = (seed.toFloat() / 0x7FFFFFFF) * 2f - 1f

            // Bombo drum hits every 2 seconds
            val tInBeat = t % 2.0
            val bomboFreq = 65.0 * exp(-tInBeat * 15.0)
            val bomboEnv = exp(-tInBeat * 6.0).toFloat()
            val bombo = (sin(2 * PI * bomboFreq * t) * bomboEnv * 0.8f).toFloat()

            // Crowd cheering swelling
            val crowdSwell = (0.6f + 0.4f * sin(2 * PI * 0.3 * t).toFloat())

            samples[i] = ((noise * 0.22f * crowdSwell) + bombo).coerceIn(-1f, 1f)
        }
        return samples
    }

    /**
     * Generates regional Huayno backing music according to the selected style
     */
    fun generateRegionalHuayno(style: String, durationSec: Float): FloatArray {
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = FloatArray(totalSamples)

        val isHuanca = style.contains("Huanca", ignoreCase = true) || style.contains("Huaylash", ignoreCase = true)
        val isCusco = style.contains("Cusqu", ignoreCase = true) || style.contains("Sureño", ignoreCase = true)
        val isPuno = style.contains("Pune", ignoreCase = true) || style.contains("Sikuri", ignoreCase = true)
        val isAncash = style.contains("Ancash", ignoreCase = true)

        val bpm = when {
            isHuanca -> 122.0
            isAncash -> 116.0
            isPuno -> 114.0
            isCusco -> 112.0
            else -> 108.0 // Ayacuchano
        }

        val beatDuration = 60.0 / bpm
        val eighthNote = beatDuration / 2.0

        // Escalas pentatónicas regionales
        val scale = when {
            isHuanca -> doubleArrayOf(261.63, 293.66, 329.63, 392.00, 440.00, 523.25) // Huaylash C mayor/A menor
            isCusco -> doubleArrayOf(196.00, 220.00, 261.63, 293.66, 329.63, 392.00) // Cusco pentatónica
            isPuno -> doubleArrayOf(293.66, 329.63, 392.00, 440.00, 587.33, 659.25) // Siku D menor
            else -> doubleArrayOf(220.0, 261.63, 293.66, 329.63, 392.00, 440.00, 523.25) // Ayacucho arpa
        }

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val beatIndex = (t / eighthNote).toInt()
            val tInNote = t - (beatIndex * eighthNote)

            // Lead instrument
            val noteIdx = (beatIndex * 2 + (beatIndex / 3)) % scale.size
            val leadFreq = scale[noteIdx]

            val leadSound = if (isHuanca) {
                // Saxofón / clarinete huanca (armónicos ricos y vibrato rápido)
                val vib = 1.0 + 0.02 * sin(2 * PI * 6.0 * t)
                val sax = sin(2 * PI * leadFreq * vib * t) +
                        0.5 * sin(2 * PI * leadFreq * 2.0 * vib * t) +
                        0.3 * sin(2 * PI * leadFreq * 3.0 * vib * t)
                val env = exp(-tInNote * 5.0).toFloat()
                (sax * env * 0.45).toFloat()
            } else if (isPuno) {
                // Siku / Zampoña doble caña
                val breath = ((((i * 12345 + 54321) and 0x7FFFFFFF) / 0x7FFFFFFF.toFloat()) * 2f - 1f) * 0.09f
                val pipe = sin(2 * PI * leadFreq * t).toFloat() + 0.3f * sin(2 * PI * leadFreq * 3.0 * t).toFloat()
                val env = exp(-tInNote * 7.0).toFloat()
                (pipe * env * 0.42f) + breath
            } else {
                // Arpa andina punteada
                val harp = (sin(2 * PI * leadFreq * t) + 0.5 * sin(2 * PI * leadFreq * 2.0 * t)).toFloat()
                val env = exp(-tInNote * 9.0).toFloat()
                harp * env * 0.5f
            }

            // Bordón / Bajo rítmico
            val bassFreq = if (isHuanca) 130.81 else 110.0 // C3 o A2
            val tInBeat = t % beatDuration
            val bassEnv = exp(-tInBeat * 6.0).toFloat()
            val bass = (sin(2 * PI * bassFreq * t) + 0.4 * sin(2 * PI * bassFreq * 2.0 * t)).toFloat() * bassEnv * 0.45f

            // Zapateo / Shaker
            val shaker = if ((beatIndex % 2 == 1) && tInNote < 0.07) {
                ((((i * 1103515245 + 12345) and 0x7FFFFFFF) / 0x7FFFFFFF.toFloat()) * 2f - 1f) * 0.16f
            } else 0f

            samples[i] = ((leadSound + bass + shaker) * 0.88f).coerceIn(-1f, 1f)
        }
        return samples
    }

    /**
     * Synthesizes Andean Spot Intro FX: Cohete andino ascendente con silbido + bombazo festivo + fanfarria
     */
    fun generateSpotIntroFx(durationSec: Float = 3.5f): FloatArray {
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = FloatArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE

            var sound = 0f

            // 0.0s - 1.2s: Cohete silbador ascendente
            if (t in 0.0..1.2) {
                val whistleFreq = 600.0 + (t / 1.2) * (t / 1.2) * 2200.0
                val whistle = sin(2 * PI * whistleFreq * t).toFloat()
                val flutter = sin(2 * PI * 25.0 * t).toFloat() * 0.3f
                val sparkle = ((((i * 12345 + 6789) and 0x7FFFFFFF) / 0x7FFFFFFF.toFloat()) * 2f - 1f) * 0.12f
                val env = (t / 1.0).toFloat().coerceIn(0f, 1f)
                sound += (whistle * 0.35f + flutter + sparkle) * env
            }

            // 1.2s - 2.8s: Bombazo / Estruendo andino
            if (t >= 1.2) {
                val tBomb = t - 1.2
                val bombFreq = 75.0 * exp(-tBomb * 12.0)
                val bombEnv = exp(-tBomb * 3.5).toFloat()
                val boom = (sin(2 * PI * bombFreq * t) * bombEnv * 0.85f).toFloat()

                // Resonancia de coliseo
                val noiseTail = ((((i * 98765 + 4321) and 0x7FFFFFFF) / 0x7FFFFFFF.toFloat()) * 2f - 1f) * exp(-tBomb * 4.0).toFloat() * 0.25f
                sound += boom + noiseTail
            }

            // 2.0s - 3.5s: Fanfarria de clarín andino de entrada
            if (t >= 2.0) {
                val tFan = t - 2.0
                val fanFreq = 880.0 // A5
                val fanEnv = exp(-tFan * 2.5).toFloat()
                val trumpet = (sin(2 * PI * fanFreq * t) + 0.4 * sin(2 * PI * fanFreq * 2.0 * t)).toFloat() * fanEnv * 0.35f
                sound += trumpet
            }

            samples[i] = sound.coerceIn(-1f, 1f)
        }
        return samples
    }

    /**
     * Synthesizes Andean Spot Outro FX: Gran ovación de coliseo, repique de bombo andino y aplausos
     */
    fun generateSpotOutroFx(durationSec: Float = 4.0f): FloatArray {
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = FloatArray(totalSamples)

        var seed = 98765
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE

            seed = (seed * 1103515245 + 12345) and 0x7FFFFFFF
            val applauseNoise = (seed.toFloat() / 0x7FFFFFFF) * 2f - 1f

            // Repique rítmico final: 3 golpes de bombo andino
            val hits = listOf(0.4, 0.9, 1.4)
            var bomboHits = 0f
            for (hitTime in hits) {
                if (t >= hitTime) {
                    val tHit = t - hitTime
                    if (tHit < 0.8) {
                        val bf = 60.0 * exp(-tHit * 14.0)
                        val bEnv = exp(-tHit * 5.0).toFloat()
                        bomboHits += (sin(2 * PI * bf * t) * bEnv * 0.7f).toFloat()
                    }
                }
            }

            // Clímax de aplausos
            val env = if (t < 1.0) (t.toFloat() / 1.0f) else exp(-(t - 1.0) * 0.7).toFloat()
            val totalSound = (applauseNoise * 0.25f * env) + bomboHits

            samples[i] = totalSound.coerceIn(-1f, 1f)
        }
        return samples
    }

    /**
     * Synthesizes speech cadence audio matching the chosen synthetic voice model, speed, and emotion
     */
    fun generateVoiceWithStyle(
        voiceName: String,
        speed: Float,
        emotion: String,
        durationSec: Float
    ): FloatArray {
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val samples = FloatArray(totalSamples)

        // Configuración según tipo de voz
        val isFeminine = voiceName.contains("Lucero", ignoreCase = true) || voiceName.contains("Femenina", ignoreCase = true)
        val isDeep = voiceName.contains("Pascual", ignoreCase = true) || voiceName.contains("Don", ignoreCase = true)

        val pitchOffset = when {
            isFeminine -> 110.0 // ~255 Hz
            isDeep -> -35.0     // ~110 Hz
            else -> 20.0        // ~165 Hz (animador enérgico)
        }

        val vibratoRate = when (emotion) {
            "Festivo eufórico" -> 3.2
            "Urgente / Impacto" -> 4.5
            "Sentimental / Tradicional" -> 1.5
            else -> 2.2
        }

        val speechPace = (3.5 * speed).coerceIn(2.0, 6.0)

        // Generar segmentos de ráfaga de locución acordes a la duración
        val segmentCount = (durationSec / (3.2f / speed)).toInt().coerceAtLeast(1)
        val segDuration = durationSec / segmentCount

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE

            val segIndex = (t / segDuration).toInt()
            val tInSeg = t - (segIndex * segDuration)

            // Pausa entre frases radiales
            val activeDur = segDuration * 0.82
            if (tInSeg < activeDur) {
                val phraseT = tInSeg

                val basePitch = 145.0 + pitchOffset + 20.0 * sin(2 * PI * vibratoRate * phraseT) + 8.0 * sin(2 * PI * 5.0 * phraseT)

                val vowelCycle = (phraseT * speechPace).toInt() % 4
                val f1 = when (vowelCycle) {
                    0 -> 750.0 // 'a'
                    1 -> 350.0 // 'i'
                    2 -> 500.0 // 'o'
                    else -> 600.0 // 'e'
                }
                val f2 = when (vowelCycle) {
                    0 -> 1200.0
                    1 -> 2200.0
                    2 -> 900.0
                    else -> 1800.0
                }

                val glottal = (sin(2 * PI * basePitch * t) + 0.6 * sin(2 * PI * basePitch * 2.0 * t) + 0.3 * sin(2 * PI * basePitch * 3.0 * t)).toFloat()
                val vocalic = (sin(2 * PI * f1 * t) * 0.4 + sin(2 * PI * f2 * t) * 0.3).toFloat()
                val radioCrunch = sin(2 * PI * 3100.0 * t).toFloat() * 0.07f

                val syllableEnv = (0.5 + 0.5 * sin(2 * PI * speechPace * phraseT)).toFloat()
                val sample = (glottal * 0.42f + vocalic * 0.52f + radioCrunch) * syllableEnv
                samples[i] = (sample * 0.9f).coerceIn(-1f, 1f)
            } else {
                samples[i] = 0f
            }
        }
        return samples
    }
}
