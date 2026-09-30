package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioTrackModel
import com.example.audio.DuckingSettings
import com.example.audio.MasterSettings
import com.example.ui.theme.AndeanGold
import com.example.ui.theme.AndeanTurquoise
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectsBottomSheet(
    tracks: List<AudioTrackModel>,
    ducking: DuckingSettings,
    master: MasterSettings,
    onDismiss: () -> Unit,
    onUpdateDucking: (enabled: Boolean, voiceTrackId: String?, musicTrackId: String?, reductionDb: Float, attackSec: Float, releaseSec: Float) -> Unit,
    onUpdateMaster: (volume: Float, limiter: Boolean, normalize: Boolean, reverb: Float, echo: Float) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var duckingEnabled by remember { mutableStateOf(ducking.enabled) }
    var voiceTrackId by remember { mutableStateOf(ducking.voiceTrackId ?: tracks.getOrNull(1)?.id) }
    var musicTrackId by remember { mutableStateOf(ducking.musicTrackId ?: tracks.getOrNull(0)?.id) }
    var reductionDb by remember { mutableFloatStateOf(ducking.reductionDb) }
    var attackSec by remember { mutableFloatStateOf(ducking.attackSec) }
    var releaseSec by remember { mutableFloatStateOf(ducking.releaseSec) }

    var masterVol by remember { mutableFloatStateOf(master.volume) }
    var limiter by remember { mutableStateOf(master.limiterEnabled) }
    var normalize by remember { mutableStateOf(master.radioNormalize) }
    var reverb by remember { mutableFloatStateOf(master.reverbLevel) }
    var echo by remember { mutableFloatStateOf(master.echoLevel) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurface
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Efectos & Ducking Automático",
                color = AndeanGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ajusta la atenuación de música para locución y los efectos de estudio radial",
                color = Color(0xFFA8A3B4),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // AUTO-DUCKING CARD
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Ducking Automático",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Baja la música cuando entra la voz",
                                color = Color(0xFFA8A3B4),
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = duckingEnabled,
                            onCheckedChange = {
                                duckingEnabled = it
                                onUpdateDucking(duckingEnabled, voiceTrackId, musicTrackId, reductionDb, attackSec, releaseSec)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AndeanGold,
                                checkedTrackColor = Color(0xFF5A4400)
                            )
                        )
                    }

                    if (duckingEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Voice Trigger Track Selector
                        Text(
                            text = "Pista de Voz (Disparador):",
                            color = Color(0xFFD4CEE0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (tr in tracks) {
                                val isSelected = tr.id == voiceTrackId
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) AndeanTurquoise.copy(alpha = 0.25f) else Color(0xFF2B2835),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) AndeanTurquoise else Color(0xFF3F3B4B),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            voiceTrackId = tr.id
                                            onUpdateDucking(duckingEnabled, voiceTrackId, musicTrackId, reductionDb, attackSec, releaseSec)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tr.name,
                                        fontSize = 10.sp,
                                        color = if (isSelected) AndeanTurquoise else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Ducking Reduction dB
                        Text(
                            text = "Atenuación: ${reductionDb.toInt()} dB",
                            color = Color(0xFFD4CEE0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = reductionDb,
                            onValueChange = {
                                reductionDb = it
                                onUpdateDucking(duckingEnabled, voiceTrackId, musicTrackId, reductionDb, attackSec, releaseSec)
                            },
                            valueRange = -24f..-4f,
                            colors = SliderDefaults.colors(
                                thumbColor = AndeanTurquoise,
                                activeTrackColor = AndeanTurquoise
                            )
                        )

                        // Attack & Release
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ataque: ${(attackSec * 1000).toInt()}ms",
                                    color = Color(0xFFA8A3B4),
                                    fontSize = 10.sp
                                )
                                Slider(
                                    value = attackSec,
                                    onValueChange = {
                                        attackSec = it
                                        onUpdateDucking(duckingEnabled, voiceTrackId, musicTrackId, reductionDb, attackSec, releaseSec)
                                    },
                                    valueRange = 0.01f..0.20f
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Recuperación: ${(releaseSec * 1000).toInt()}ms",
                                    color = Color(0xFFA8A3B4),
                                    fontSize = 10.sp
                                )
                                Slider(
                                    value = releaseSec,
                                    onValueChange = {
                                        releaseSec = it
                                        onUpdateDucking(duckingEnabled, voiceTrackId, musicTrackId, reductionDb, attackSec, releaseSec)
                                    },
                                    valueRange = 0.1f..1.0f
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // MASTER EFFECTS CARD
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Efectos Master de Estudio",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Reverb
                    Text(
                        text = "Reverb Andino (Coliseo/Estadio): ${(reverb * 100).toInt()}%",
                        color = Color(0xFFD4CEE0),
                        fontSize = 11.sp
                    )
                    Slider(
                        value = reverb,
                        onValueChange = {
                            reverb = it
                            onUpdateMaster(masterVol, limiter, normalize, reverb, echo)
                        },
                        valueRange = 0f..0.50f,
                        colors = SliderDefaults.colors(thumbColor = AndeanGold, activeTrackColor = AndeanGold)
                    )

                    // Echo
                    Text(
                        text = "Eco de Animador Radial (Delay 220ms): ${(echo * 100).toInt()}%",
                        color = Color(0xFFD4CEE0),
                        fontSize = 11.sp
                    )
                    Slider(
                        value = echo,
                        onValueChange = {
                            echo = it
                            onUpdateMaster(masterVol, limiter, normalize, reverb, echo)
                        },
                        valueRange = 0f..0.45f,
                        colors = SliderDefaults.colors(thumbColor = AndeanGold, activeTrackColor = AndeanGold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Limiter switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Limitador Radial Anti-Saturación", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Evita distorsión digital en altavoces", color = Color(0xFFA8A3B4), fontSize = 10.sp)
                        }
                        Switch(
                            checked = limiter,
                            onCheckedChange = {
                                limiter = it
                                onUpdateMaster(masterVol, limiter, normalize, reverb, echo)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Normalize switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Normalización a Nivel de Radio (-14 LUFS)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Volumen estándar para FM y WhatsApp", color = Color(0xFFA8A3B4), fontSize = 10.sp)
                        }
                        Switch(
                            checked = normalize,
                            onCheckedChange = {
                                normalize = it
                                onUpdateMaster(masterVol, limiter, normalize, reverb, echo)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AndeanGold, contentColor = Color.Black)
            ) {
                Text("Guardar y Volver al Editor", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
