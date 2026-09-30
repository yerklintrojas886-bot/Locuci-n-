package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioClipModel
import com.example.audio.AudioTrackModel
import com.example.ui.theme.AndeanGold
import com.example.ui.theme.StudioBorder
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TrackLaneView(
    track: AudioTrackModel,
    pixelsPerSec: Float,
    totalDurationSec: Float,
    selectedClipId: String?,
    onClipSelected: (String) -> Unit,
    onClipMoved: (String, Float) -> Unit,
    onMoveFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val laneWidthDp = (totalDurationSec * pixelsPerSec).dp
    val trackBaseColor = Color(track.colorHex)

    Box(
        modifier = modifier
            .width(laneWidthDp)
            .height(86.dp)
            .background(Color(0xFF131217))
            .border(0.5.dp, StudioBorder.copy(alpha = 0.5f))
    ) {
        // Grid background lines every 5 seconds
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalSec = totalDurationSec.toInt()
            for (sec in 0..totalSec) {
                if (sec % 5 == 0) {
                    val x = sec * pixelsPerSec
                    drawLine(
                        color = Color(0xFF201E27),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                }
            }
        }

        // Render each clip in this track
        for (clip in track.clips) {
            ClipItemView(
                clip = clip,
                trackColor = trackBaseColor,
                pixelsPerSec = pixelsPerSec,
                isSelected = clip.id == selectedClipId,
                onSelect = { onClipSelected(clip.id) },
                onMove = { newSec -> onClipMoved(clip.id, newSec) },
                onMoveFinished = onMoveFinished
            )
        }
    }
}

@Composable
private fun ClipItemView(
    clip: AudioClipModel,
    trackColor: Color,
    pixelsPerSec: Float,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onMove: (Float) -> Unit,
    onMoveFinished: () -> Unit
) {
    val density = LocalDensity.current
    val clipWidthDp = (clip.durationSec * pixelsPerSec).dp
    val clipOffsetXDp = (clip.startTimeSec * pixelsPerSec).dp

    var dragAccumulator by remember(clip.startTimeSec) { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = with(density) { clipOffsetXDp.roundToPx() },
                    y = 6
                )
            }
            .width(clipWidthDp)
            .height(74.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        trackColor.copy(alpha = 0.28f),
                        trackColor.copy(alpha = 0.14f)
                    )
                )
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) AndeanGold else trackColor.copy(alpha = 0.8f),
                shape = RoundedCornerShape(6.dp)
            )
            .pointerInput(clip.id) {
                detectTapGestures(
                    onTap = { onSelect() }
                )
            }
            .pointerInput(clip.id) {
                detectDragGestures(
                    onDragStart = {
                        onSelect()
                        dragAccumulator = 0f
                    },
                    onDragEnd = {
                        onMoveFinished()
                    },
                    onDragCancel = {
                        onMoveFinished()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulator += dragAmount.x
                        val deltaSec = dragAccumulator / pixelsPerSec
                        if (abs(deltaSec) >= 0.05f) {
                            val targetSec = (clip.startTimeSec + deltaSec).coerceAtLeast(0f)
                            onMove(targetSec)
                            dragAccumulator = 0f
                        }
                    }
                )
            }
    ) {
        // Waveform preview canvas
        WaveformCanvas(
            samples = clip.samples,
            trackColor = trackColor,
            fadeInSec = clip.fadeInSec,
            fadeOutSec = clip.fadeOutSec,
            durationSec = clip.durationSec,
            modifier = Modifier.fillMaxSize()
        )

        // Clip label
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color(0x99100F14), RoundedCornerShape(bottomEnd = 6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = clip.name,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${String.format("%.1f", clip.durationSec)}s • Vol ${(clip.volume * 100).toInt()}%",
                color = Color(0xFFD0CBD9),
                fontSize = 9.sp
            )
        }

        // Selection badge
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(AndeanGold, RoundedCornerShape(3.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "EDITANDO",
                    color = Color.Black,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun WaveformCanvas(
    samples: FloatArray,
    trackColor: Color,
    fadeInSec: Float,
    fadeOutSec: Float,
    durationSec: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val midY = height / 2f

        if (samples.isEmpty()) {
            // Draw placeholder wave line
            drawLine(
                color = trackColor.copy(alpha = 0.5f),
                start = Offset(0f, midY),
                end = Offset(width, midY),
                strokeWidth = 2f
            )
            return@Canvas
        }

        val step = maxOf(1, samples.size / width.toInt().coerceAtLeast(1))
        val barWidth = 2.5f

        var x = 0f
        var sampleIdx = 0

        while (x < width && sampleIdx < samples.size) {
            var peak = 0f
            val endIdx = minOf(sampleIdx + step, samples.size)
            for (i in sampleIdx until endIdx) {
                val amp = abs(samples[i])
                if (amp > peak) peak = amp
            }

            // Normalization & visual scaling
            val barHeight = (peak * (height * 0.85f)).coerceIn(4f, height - 6f)
            val halfH = barHeight / 2f

            drawLine(
                color = trackColor.copy(alpha = 0.85f),
                start = Offset(x, midY - halfH),
                end = Offset(x, midY + halfH),
                strokeWidth = barWidth
            )

            x += barWidth + 1f
            sampleIdx += step
        }

        // Fade in ramp overlay
        if (fadeInSec > 0f && durationSec > 0f) {
            val fadeRatio = (fadeInSec / durationSec).coerceIn(0f, 1f)
            val fadeX = width * fadeRatio
            val fadePath = Path().apply {
                moveTo(0f, 0f)
                lineTo(fadeX, 0f)
                lineTo(0f, height)
                close()
            }
            drawPath(fadePath, Color(0x33FFFFFF))
        }

        // Fade out ramp overlay
        if (fadeOutSec > 0f && durationSec > 0f) {
            val fadeRatio = (fadeOutSec / durationSec).coerceIn(0f, 1f)
            val startX = width * (1f - fadeRatio)
            val fadePath = Path().apply {
                moveTo(startX, 0f)
                lineTo(width, 0f)
                lineTo(width, height)
                close()
            }
            drawPath(fadePath, Color(0x33FFFFFF))
        }
    }
}
