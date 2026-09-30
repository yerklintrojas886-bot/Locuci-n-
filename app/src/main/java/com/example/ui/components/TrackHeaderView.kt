package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioTrackModel
import com.example.audio.TrackType
import com.example.ui.theme.AndeanGold
import com.example.ui.theme.AndeanRed
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurfaceVariant

@Composable
fun TrackHeaderView(
    track: AudioTrackModel,
    onMuteToggle: () -> Unit,
    onSoloToggle: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onPanChange: (Float) -> Unit,
    onDeleteTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor = Color(track.colorHex)

    Row(
        modifier = modifier
            .width(170.dp)
            .height(86.dp)
            .background(StudioSurfaceVariant)
            .border(0.5.dp, StudioBorder)
            .padding(vertical = 4.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color indicator bar on the left
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(trackColor)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Track Name and Type Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val icon = when (track.type) {
                        TrackType.MUSIC -> Icons.Default.MusicNote
                        TrackType.VOICE -> Icons.Default.Mic
                        TrackType.FX -> Icons.Default.GraphicEq
                        TrackType.AUX -> Icons.Default.MusicNote
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = track.type.displayName,
                        tint = trackColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = track.name,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Delete track button
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar pista",
                    tint = Color(0xFF7E798B),
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onDeleteTrack() }
                )
            }

            // Mute & Solo buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // MUTE Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (track.isMuted) AndeanRed else Color(0xFF2C2836))
                        .border(
                            1.dp,
                            if (track.isMuted) AndeanRed else Color(0xFF3F3B4B),
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { onMuteToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MUTE",
                        color = if (track.isMuted) Color.White else Color(0xFFAFAAB9),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // SOLO Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (track.isSolo) AndeanGold else Color(0xFF2C2836))
                        .border(
                            1.dp,
                            if (track.isSolo) AndeanGold else Color(0xFF3F3B4B),
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { onSoloToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SOLO",
                        color = if (track.isSolo) Color.Black else Color(0xFFAFAAB9),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Volume mini slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VOL",
                    color = Color(0xFFA8A3B4),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(20.dp)
                )

                Slider(
                    value = track.volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = trackColor,
                        activeTrackColor = trackColor,
                        inactiveTrackColor = Color(0xFF2D2A37)
                    ),
                    modifier = Modifier
                        .height(18.dp)
                        .weight(1f)
                )

                Text(
                    text = "${(track.volume * 100).toInt()}%",
                    color = Color(0xFFDCD8E6),
                    fontSize = 8.sp,
                    modifier = Modifier.width(26.dp)
                )
            }
        }
    }
}
