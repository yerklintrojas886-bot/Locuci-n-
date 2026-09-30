package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.AudioClipModel
import com.example.ui.theme.AndeanGold
import com.example.ui.theme.AndeanRed
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface

@Composable
fun ClipEditorDialog(
    clip: AudioClipModel,
    onDismiss: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onFadesChange: (fadeIn: Float, fadeOut: Float) -> Unit,
    onSplitAtPlayhead: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var volume by remember { mutableFloatStateOf(clip.volume) }
    var fadeIn by remember { mutableFloatStateOf(clip.fadeInSec) }
    var fadeOut by remember { mutableFloatStateOf(clip.fadeOutSec) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Propiedades del Clip",
                            color = AndeanGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = clip.name,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "${String.format("%.2f", clip.durationSec)}s",
                        color = Color(0xFFA8A3B4),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Volume slider
                Text(
                    text = "Volumen del clip: ${(volume * 100).toInt()}%",
                    color = Color(0xFFD3CFDC),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = volume,
                    onValueChange = {
                        volume = it
                        onVolumeChange(it)
                    },
                    valueRange = 0f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = AndeanGold,
                        activeTrackColor = AndeanGold
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Fade In Slider
                Text(
                    text = "Fade In (Entrada suave): ${String.format("%.2f", fadeIn)}s",
                    color = Color(0xFFD3CFDC),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = fadeIn,
                    onValueChange = {
                        fadeIn = it
                        onFadesChange(fadeIn, fadeOut)
                    },
                    valueRange = 0f..minOf(4.0f, clip.durationSec / 2f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF06D6A0),
                        activeTrackColor = Color(0xFF06D6A0)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Fade Out Slider
                Text(
                    text = "Fade Out (Salida suave): ${String.format("%.2f", fadeOut)}s",
                    color = Color(0xFFD3CFDC),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = fadeOut,
                    onValueChange = {
                        fadeOut = it
                        onFadesChange(fadeIn, fadeOut)
                    },
                    valueRange = 0f..minOf(4.0f, clip.durationSec / 2f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF06D6A0),
                        activeTrackColor = Color(0xFF06D6A0)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Split at playhead
                    OutlinedButton(
                        onClick = {
                            onSplitAtPlayhead()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.ContentCut, contentDescription = "Cortar", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cortar", fontSize = 11.sp)
                    }

                    // Duplicate
                    OutlinedButton(
                        onClick = {
                            onDuplicate()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicar", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Duplicar", fontSize = 11.sp)
                    }

                    // Delete
                    Button(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AndeanRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Borrar", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2836))
                ) {
                    Text("Listo", color = Color.White)
                }
            }
        }
    }
}
