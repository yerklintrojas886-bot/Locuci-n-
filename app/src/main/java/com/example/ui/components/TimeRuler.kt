package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AndeanGold
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurfaceVariant

@Composable
fun TimeRuler(
    totalDurationSec: Float,
    pixelsPerSec: Float,
    currentPlayheadSec: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalWidthDp = (totalDurationSec * pixelsPerSec).dp

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(StudioSurfaceVariant)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val tappedSec = offset.x / pixelsPerSec
                    onSeek(tappedSec.coerceIn(0f, totalDurationSec))
                }
            }
    ) {
        val totalSecInt = totalDurationSec.toInt()
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#A8A3B4")
            textSize = 24f
            isAntiAlias = true
        }

        // Bottom border
        drawLine(
            color = StudioBorder,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 2f
        )

        for (sec in 0..totalSecInt) {
            val x = sec * pixelsPerSec
            val isMajor = sec % 5 == 0

            val tickHeight = if (isMajor) size.height * 0.5f else size.height * 0.25f
            val tickColor = if (isMajor) Color(0xFFD4CEE0) else Color(0xFF5E596D)

            drawLine(
                color = tickColor,
                start = Offset(x, size.height - tickHeight),
                end = Offset(x, size.height),
                strokeWidth = if (isMajor) 2f else 1f
            )

            // Half-second tick
            val halfX = x + (pixelsPerSec * 0.5f)
            if (sec < totalSecInt) {
                drawLine(
                    color = Color(0xFF3F3B4B),
                    start = Offset(halfX, size.height - (size.height * 0.15f)),
                    end = Offset(halfX, size.height),
                    strokeWidth = 1f
                )
            }

            if (isMajor) {
                val mins = sec / 60
                val remSecs = sec % 60
                val label = String.format("%02d:%02d", mins, remSecs)
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    x + 4f,
                    size.height * 0.45f,
                    textPaint
                )
            }
        }

        // Draw playhead tick indicator on ruler
        val playheadX = currentPlayheadSec * pixelsPerSec
        drawCircle(
            color = AndeanGold,
            radius = 8f,
            center = Offset(playheadX, size.height * 0.5f)
        )
    }
}
