package com.example.audio

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.max

object WavFileExporter {

    /**
     * Exports stereo float samples (-1.0f to 1.0f) to a standard 16-bit 44.1kHz Stereo WAV file.
     */
    fun exportToWavFile(
        context: Context,
        fileName: String,
        leftChannel: FloatArray,
        rightChannel: FloatArray,
        sampleRate: Int = 44100,
        normalizeLoudness: Boolean = true
    ): File {
        val count = minOf(leftChannel.size, rightChannel.size)

        // Peak normalization to -0.3 dB (0.96f) to prevent clipping and achieve punchy radio loudness
        var peak = 0.001f
        for (i in 0 until count) {
            val l = abs(leftChannel[i])
            val r = abs(rightChannel[i])
            if (l > peak) peak = l
            if (r > peak) peak = r
        }

        val normGain = if (normalizeLoudness && peak > 0.05f) {
            (0.96f / peak).coerceIn(0.5f, 3.5f)
        } else {
            1.0f
        }

        val outputDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val outputFile = File(outputDir, if (fileName.endsWith(".wav")) fileName else "$fileName.wav")

        val byteRate = sampleRate * 2 * 2 // 2 channels * 16 bits (2 bytes)
        val blockAlign = 4
        val dataSize = count * 4
        val chunkSize = 36 + dataSize

        FileOutputStream(outputFile).use { fos ->
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)

            // RIFF chunk descriptor
            header.put("RIFF".toByteArray())
            header.putInt(chunkSize)
            header.put("WAVE".toByteArray())

            // "fmt " sub-chunk
            header.put("fmt ".toByteArray())
            header.putInt(16) // Subchunk1Size for PCM
            header.putShort(1) // AudioFormat 1 = PCM
            header.putShort(2) // NumChannels = 2 (Stereo)
            header.putInt(sampleRate)
            header.putInt(byteRate)
            header.putShort(blockAlign.toShort())
            header.putShort(16) // BitsPerSample = 16

            // "data" sub-chunk
            header.put("data".toByteArray())
            header.putInt(dataSize)

            fos.write(header.array())

            // Buffer for audio PCM samples (Left, Right interleaved)
            val bufferSize = 4096
            val byteBuffer = ByteBuffer.allocate(bufferSize * 4).order(ByteOrder.LITTLE_ENDIAN)

            for (i in 0 until count) {
                val lSample = (leftChannel[i] * normGain).coerceIn(-1.0f, 1.0f)
                val rSample = (rightChannel[i] * normGain).coerceIn(-1.0f, 1.0f)

                val lShort = (lSample * 32767f).toInt().toShort()
                val rShort = (rSample * 32767f).toInt().toShort()

                byteBuffer.putShort(lShort)
                byteBuffer.putShort(rShort)

                if (byteBuffer.position() == byteBuffer.capacity()) {
                    fos.write(byteBuffer.array())
                    byteBuffer.clear()
                }
            }

            if (byteBuffer.position() > 0) {
                fos.write(byteBuffer.array(), 0, byteBuffer.position())
            }
        }

        return outputFile
    }

    /**
     * Opens Android share sheet to share the exported audio
     */
    fun shareAudioFile(context: Context, file: File, title: String = "Spot HuaynoStudio") {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/wav"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Audio producido con HuaynoStudio: $title")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Compartir audio de HuaynoStudio").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Fallback for direct intent without fileprovider if not registered
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/wav"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Audio producido en HuaynoStudio: ${file.name}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir audio"))
        }
    }
}
