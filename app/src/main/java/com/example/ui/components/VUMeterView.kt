package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MeterGreen
import com.example.ui.theme.MeterRed
import com.example.ui.theme.MeterYellow
import com.example.ui.theme.StudioBlack
import com.example.ui.theme.StudioBorder

@Composable
fun VUMeterView(
    leftPeak: Float,
    rightPeak: Float,
    modifier: Modifier = Modifier
) {
    val animLeft by animateFloatAsState(
        targetValue = leftPeak.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 60),
        label = "animLeft"
    )
    val animRight by animateFloatAsState(
        targetValue = rightPeak.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 60),
        label = "animRight"
    )

    Column(
        modifier = modifier
            .background(StudioBlack, RoundedCornerShape(6.dp))
            .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.Center
    ) {
        MeterBar(label = "L", peak = animLeft)
        Spacer(modifier = Modifier.height(3.dp))
        MeterBar(label = "R", peak = animRight)
    }
}

@Composable
private fun MeterBar(label: String, peak: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFA8A3B4),
            modifier = Modifier.width(10.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF1E1C24))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = peak)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MeterGreen,
                                MeterYellow,
                                MeterRed
                            )
                        )
                    )
            )
        }
    }
}
