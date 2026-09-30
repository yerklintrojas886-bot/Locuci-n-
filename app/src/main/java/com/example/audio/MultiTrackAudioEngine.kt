package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class MultiTrackAudioEngine(
    val sampleRate: Int = 44100,
    private val onPositionUpdate: (Float) -> Unit,
    private val onPeakUpdate: (Float, Float) -> Unit,
    private val onPlaybackFinished: () -> Unit
) {
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile
    var isPlaying: Boolean = false
        private set

    @Volatile
    var currentPositionSec: Float = 0f

    // Echo buffer for stadium / andean fiesta reverb effect
    private val echoDelaySamples = (sampleRate * 0.22f).toInt() // ~220ms
    private val echoBufferLeft = FloatArray(echoDelaySamples)
    private val echoBufferRight = FloatArray(echoDelaySamples)
    private var echoIndex = 0

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = max(minBufferSize, 8192)

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    }

    fun play(
        project: ProjectModel,
        startSec: Float = currentPositionSec,
        isLooping: Boolean = false
    ) {
        if (isPlaying) {
            stop()
        }

        currentPositionSec = startSec.coerceIn(0f, project.durationSec)
        isPlaying = true

        try {
            audioTrack?.play()
        } catch (e: Exception) {
            initAudioTrack()
            audioTrack?.play()
        }

        playbackJob = scope.launch {
            val bufferFrames = 1024
            val shortBuffer = ShortArray(bufferFrames * 2) // Stereo interleaved
            val leftBlock = FloatArray(bufferFrames)
            val rightBlock = FloatArray(bufferFrames)

            var sampleCursor = (currentPositionSec * sampleRate).toLong()
            val totalSamples = (project.durationSec * sampleRate).toLong()

            // Pre-calculate ducking envelope if enabled
            val duckingEnvelopes = computeDuckingEnvelope(project)

            while (isActive && isPlaying && sampleCursor < totalSamples) {
                // Clear blocks
                leftBlock.fill(0f)
                rightBlock.fill(0f)

                val framesToProcess = min(bufferFrames.toLong(), totalSamples - sampleCursor).toInt()
                val currentSecInBlock = sampleCursor.toFloat() / sampleRate

                val hasSoloTrack = project.tracks.any { it.isSolo }

                for (track in project.tracks) {
                    if (track.isMuted) continue
                    if (hasSoloTrack && !track.isSolo) continue

                    val trackVol = track.volume
                    val panL = ((1f - track.pan) / 2f + 0.5f).coerceIn(0f, 1f)
                    val panR = ((1f + track.pan) / 2f + 0.5f).coerceIn(0f, 1f)

                    // Check ducking attenuation for music track
                    val isDuckedTrack = project.ducking.enabled && track.id == project.ducking.musicTrackId

                    for (clip in track.clips) {
                        val clipStartSample = (clip.startTimeSec * sampleRate).toLong()
                        val clipEndSample = ((clip.startTimeSec + clip.durationSec) * sampleRate).toLong()

                        val overlapStart = max(sampleCursor, clipStartSample)
                        val overlapEnd = min(sampleCursor + framesToProcess, clipEndSample)

                        if (overlapStart < overlapEnd) {
                            val blockOffset = (overlapStart - sampleCursor).toInt()
                            val count = (overlapEnd - overlapStart).toInt()
                            val clipSamples = clip.samples

                            for (f in 0 until count) {
                                val globalSample = overlapStart + f
                                val timeInClipSec = (globalSample - clipStartSample).toFloat() / sampleRate

                                // Fade in / fade out
                                var fade = 1.0f
                                if (clip.fadeInSec > 0f && timeInClipSec < clip.fadeInSec) {
                                    fade *= (timeInClipSec / clip.fadeInSec).coerceIn(0f, 1f)
                                }
                                val timeUntilEnd = clip.durationSec - timeInClipSec
                                if (clip.fadeOutSec > 0f && timeUntilEnd < clip.fadeOutSec) {
                                    fade *= (timeUntilEnd / clip.fadeOutSec).coerceIn(0f, 1f)
                                }

                                val sampleInAudio = ((timeInClipSec + clip.offsetSec) * sampleRate).toInt()
                                val rawSample = if (sampleInAudio in clipSamples.indices) {
                                    clipSamples[sampleInAudio]
                                } else 0f

                                var duckGain = 1.0f
                                if (isDuckedTrack && duckingEnvelopes != null) {
                                    val idx = (globalSample.toInt()).coerceIn(0, duckingEnvelopes.size - 1)
                                    duckGain = duckingEnvelopes[idx]
                                }

                                val processed = rawSample * clip.volume * trackVol * fade * duckGain
                                val targetIdx = blockOffset + f
                                leftBlock[targetIdx] += processed * panL
                                rightBlock[targetIdx] += processed * panR
                            }
                        }
                    }
                }

                // Master bus processing: Reverb/Echo + Normalizer/Limiter
                val masterVol = project.master.volume
                val echoLevel = project.master.echoLevel
                val reverbLevel = project.master.reverbLevel
                var peakLeft = 0f
                var peakRight = 0f

                for (f in 0 until framesToProcess) {
                    var l = leftBlock[f] * masterVol
                    var r = rightBlock[f] * masterVol

                    // Echo delay line
                    if (echoLevel > 0f || reverbLevel > 0f) {
                        val delayedL = echoBufferLeft[echoIndex]
                        val delayedR = echoBufferRight[echoIndex]

                        echoBufferLeft[echoIndex] = l + delayedL * 0.45f
                        echoBufferRight[echoIndex] = r + delayedR * 0.45f
                        echoIndex = (echoIndex + 1) % echoDelaySamples

                        l += (delayedL * echoLevel) + (delayedR * reverbLevel * 0.5f)
                        r += (delayedR * echoLevel) + (delayedL * reverbLevel * 0.5f)
                    }

                    // Soft-knee broadcast limiter to prevent harsh digital clipping
                    if (project.master.limiterEnabled) {
                        l = softClip(l)
                        r = softClip(r)
                    } else {
                        l = l.coerceIn(-1.0f, 1.0f)
                        r = r.coerceIn(-1.0f, 1.0f)
                    }

                    val absL = abs(l)
                    val absR = abs(r)
                    if (absL > peakLeft) peakLeft = absL
                    if (absR > peakRight) peakRight = absR

                    shortBuffer[f * 2] = (l * 32767f).toInt().toShort()
                    shortBuffer[f * 2 + 1] = (r * 32767f).toInt().toShort()
                }

                // Write to AudioTrack
                audioTrack?.write(shortBuffer, 0, framesToProcess * 2)

                sampleCursor += framesToProcess
                currentPositionSec = sampleCursor.toFloat() / sampleRate
                onPositionUpdate(currentPositionSec)
                onPeakUpdate(peakLeft, peakRight)
            }

            if (isActive && isPlaying) {
                if (isLooping) {
                    play(project, 0f, isLooping = true)
                } else {
                    stop()
                    onPlaybackFinished()
                }
            }
        }
    }

    private fun softClip(x: Float): Float {
        return if (x > 0.85f) {
            0.85f + (1f - 0.85f) * ((x - 0.85f) / (1f + (x - 0.85f)))
        } else if (x < -0.85f) {
            -0.85f - (1f - 0.85f) * ((-x - 0.85f) / (1f + (-x - 0.85f)))
        } else {
            x
        }.coerceIn(-1.0f, 1.0f)
    }

    /**
     * Pre-computes smooth ducking envelope curve for the target track based on voice presence
     */
    private fun computeDuckingEnvelope(project: ProjectModel): FloatArray? {
        if (!project.ducking.enabled) return null
        val voiceTrack = project.tracks.find { it.id == project.ducking.voiceTrackId } ?: return null
        val totalSamples = (project.durationSec * sampleRate).toInt()
        val envelope = FloatArray(totalSamples) { 1.0f }

        // Reduction ratio from dB
        // e.g. -12 dB -> 10^(-12/20) ~ 0.25f
        val duckMinGain = Math.pow(10.0, (project.ducking.reductionDb / 20.0).toDouble()).toFloat().coerceIn(0.05f, 0.9f)
        val attackSamples = (project.ducking.attackSec * sampleRate).toInt().coerceAtLeast(1)
        val releaseSamples = (project.ducking.releaseSec * sampleRate).toInt().coerceAtLeast(1)

        val voicePresence = BooleanArray(totalSamples)
        for (clip in voiceTrack.clips) {
            val start = (clip.startTimeSec * sampleRate).toInt().coerceIn(0, totalSamples - 1)
            val end = ((clip.startTimeSec + clip.durationSec) * sampleRate).toInt().coerceIn(0, totalSamples)
            for (i in start until end) {
                voicePresence[i] = true
            }
        }

        var currentGain = 1.0f
        for (i in 0 until totalSamples) {
            val targetGain = if (voicePresence[i]) duckMinGain else 1.0f
            if (currentGain > targetGain) {
                // Attack: quick drop
                currentGain = max(targetGain, currentGain - (1.0f - duckMinGain) / attackSamples)
            } else if (currentGain < targetGain) {
                // Release: smooth return
                currentGain = min(targetGain, currentGain + (1.0f - duckMinGain) / releaseSamples)
            }
            envelope[i] = currentGain
        }

        return envelope
    }

    fun pause() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.pause()
        } catch (_: Exception) {}
    }

    fun stop() {
        isPlaying = false
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.stop()
            audioTrack?.flush()
        } catch (_: Exception) {}
        onPeakUpdate(0f, 0f)
    }

    fun seekTo(seconds: Float, project: ProjectModel? = null) {
        val wasPlaying = isPlaying
        stop()
        currentPositionSec = seconds.coerceAtLeast(0f)
        onPositionUpdate(currentPositionSec)
        if (wasPlaying && project != null) {
            play(project, currentPositionSec)
        }
    }

    /**
     * Mixes entire project in memory into full stereo channels for WAV export
     */
    fun renderFullMix(project: ProjectModel): Pair<FloatArray, FloatArray> {
        val totalSamples = (project.durationSec * sampleRate).toInt()
        val leftChannel = FloatArray(totalSamples)
        val rightChannel = FloatArray(totalSamples)

        val duckingEnvelopes = computeDuckingEnvelope(project)
        val hasSoloTrack = project.tracks.any { it.isSolo }

        for (track in project.tracks) {
            if (track.isMuted) continue
            if (hasSoloTrack && !track.isSolo) continue

            val trackVol = track.volume
            val panL = ((1f - track.pan) / 2f + 0.5f).coerceIn(0f, 1f)
            val panR = ((1f + track.pan) / 2f + 0.5f).coerceIn(0f, 1f)
            val isDuckedTrack = project.ducking.enabled && track.id == project.ducking.musicTrackId

            for (clip in track.clips) {
                val clipStartSample = (clip.startTimeSec * sampleRate).toInt()
                val clipEndSample = min(totalSamples, ((clip.startTimeSec + clip.durationSec) * sampleRate).toInt())

                for (s in clipStartSample until clipEndSample) {
                    val timeInClipSec = (s - clipStartSample).toFloat() / sampleRate

                    var fade = 1.0f
                    if (clip.fadeInSec > 0f && timeInClipSec < clip.fadeInSec) {
                        fade *= (timeInClipSec / clip.fadeInSec).coerceIn(0f, 1f)
                    }
                    val timeUntilEnd = clip.durationSec - timeInClipSec
                    if (clip.fadeOutSec > 0f && timeUntilEnd < clip.fadeOutSec) {
                        fade *= (timeUntilEnd / clip.fadeOutSec).coerceIn(0f, 1f)
                    }

                    val sampleInAudio = ((timeInClipSec + clip.offsetSec) * sampleRate).toInt()
                    val rawSample = if (sampleInAudio in clip.samples.indices) {
                        clip.samples[sampleInAudio]
                    } else 0f

                    var duckGain = 1.0f
                    if (isDuckedTrack && duckingEnvelopes != null) {
                        duckGain = duckingEnvelopes[s.coerceIn(0, duckingEnvelopes.size - 1)]
                    }

                    val processed = rawSample * clip.volume * trackVol * fade * duckGain
                    leftChannel[s] += processed * panL
                    rightChannel[s] += processed * panR
                }
            }
        }

        // Apply master effects to render
        val masterVol = project.master.volume
        val echoLevel = project.master.echoLevel
        val delaySamples = (sampleRate * 0.22f).toInt()
        val echoL = FloatArray(delaySamples)
        val echoR = FloatArray(delaySamples)
        var echoIdx = 0

        for (i in 0 until totalSamples) {
            var l = leftChannel[i] * masterVol
            var r = rightChannel[i] * masterVol

            if (echoLevel > 0f) {
                val delL = echoL[echoIdx]
                val delR = echoR[echoIdx]
                echoL[echoIdx] = l + delL * 0.4f
                echoR[echoIdx] = r + delR * 0.4f
                echoIdx = (echoIdx + 1) % delaySamples
                l += delL * echoLevel
                r += delR * echoLevel
            }

            if (project.master.limiterEnabled) {
                l = softClip(l)
                r = softClip(r)
            }

            leftChannel[i] = l
            rightChannel[i] = r
        }

        return Pair(leftChannel, rightChannel)
    }

    fun release() {
        stop()
        audioTrack?.release()
        audioTrack = null
    }
}
