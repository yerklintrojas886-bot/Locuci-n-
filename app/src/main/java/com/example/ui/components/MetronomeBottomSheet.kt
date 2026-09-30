package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HuaynoMeter
import com.example.audio.MetronomePreset
import com.example.audio.MetronomeTimbre
import com.example.ui.theme.AndeanGold
import com.example.ui.theme.AndeanRed
import com.example.ui.theme.AndeanTurquoise
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetronomeBottomSheet(
    viewModel: EditorViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val project by viewModel.project.collectAsState()
    val config = project.metronome

    val isActive by viewModel.isMetronomeActive.collectAsState()
    val currentBeat by viewModel.currentMetronomeBeat.collectAsState()
    val isAccent by viewModel.isMetronomeAccent.collectAsState()
    val isCountingIn by viewModel.isCountInActive.collectAsState()
    val countInBeat by viewModel.countInBeat.collectAsState()
    val countInTotal by viewModel.countInTotal.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = StudioSurface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AndeanGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Metrónomo Andino",
                            tint = AndeanGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Metrónomo Andino para Huayno",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Compases típicos 2/4 y 4/4 para grabación vocal",
                            color = Color(0xFFA8A3B4),
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("close_metronome_sheet_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color(0xFFA8A3B4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BIG VISUAL BEAT INDICATOR & PULSE
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131119)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isCountingIn) {
                        // Banner de Pre-Cuenta
                        Text(
                            text = "🎙️ PRE-CUENTA ANTES DE GRABAR",
                            color = AndeanGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Tiempo $countInBeat de $countInTotal",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "¡Respira y prepárate para entrar con la pista!",
                            color = AndeanTurquoise,
                            fontSize = 11.sp
                        )
                    } else {
                        // Compás & Tempo en tiempo real
                        Text(
                            text = "COMPÁS ${config.meter.label} • ${config.bpm} BPM",
                            color = AndeanGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Large Beat Number
                        val beatScale by animateFloatAsState(
                            targetValue = if (isActive) 1.15f else 1.0f,
                            animationSpec = tween(120, easing = FastOutSlowInEasing),
                            label = "beatScale"
                        )
                        val beatColor by animateColorAsState(
                            targetValue = if (isActive && isAccent) AndeanGold else if (isActive) AndeanTurquoise else Color(0xFF555060),
                            label = "beatColor"
                        )

                        Text(
                            text = if (isActive) "$currentBeat" else "-",
                            color = beatColor,
                            fontSize = 58.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.scale(beatScale)
                        )

                        Text(
                            text = if (!isActive) "Metrónomo en pausa" else if (isAccent) "TIEMPO 1 • ACENTUADO (GOLPE FUERTE)" else "TIEMPO $currentBeat • CONTRATIEMPO",
                            color = if (isAccent && isActive) AndeanGold else Color(0xFFA8A3B4),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Beat Dots Indicator (e.g. 2 dots for 2/4; 4 dots for 4/4)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..config.meter.beatsPerBar) {
                            val isCurrent = isActive && (currentBeat == i)
                            val dotScale by animateFloatAsState(
                                targetValue = if (isCurrent) 1.35f else 1.0f,
                                animationSpec = tween(90),
                                label = "dotScale$i"
                            )
                            val dotColor = when {
                                !isActive -> Color(0xFF282333)
                                isCurrent && i == 1 -> AndeanGold
                                isCurrent -> AndeanTurquoise
                                i == 1 -> AndeanGold.copy(alpha = 0.35f)
                                else -> Color(0xFF383247)
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(if (i == 1) 24.dp else 20.dp)
                                        .scale(dotScale)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                        .border(
                                            width = if (i == 1) 2.dp else 1.dp,
                                            color = if (isCurrent) Color.White else if (i == 1) AndeanGold else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                                Text(
                                    text = if (i == 1) "1 (Fuerte)" else "$i",
                                    color = if (isCurrent) Color.White else Color(0xFF7E798B),
                                    fontSize = 10.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Main Start/Stop Metronome Toggle Button
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.toggleMetronome() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) AndeanRed else AndeanGold,
                                contentColor = if (isActive) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(46.dp)
                                .weight(1f)
                                .testTag("metronome_toggle_playback_btn")
                        ) {
                            Icon(
                                imageVector = if (isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isActive) "Detener" else "Iniciar",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isActive) "Detener Metrónomo" else "Iniciar Metrónomo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // Tap Tempo Button
                        OutlinedButton(
                            onClick = { viewModel.tapTempo() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AndeanTurquoise
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AndeanTurquoise.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("metronome_tap_tempo_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = "Tap Tempo",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TAP TEMPO",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECCIÓN: SELECCIÓN DE COMPASES TÍPICOS DEL HUAYNO (2/4 y 4/4)
            Text(
                text = "COMPASES TÍPICOS DEL HUAYNO",
                color = AndeanGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Selecciona la métrica adecuada para el estilo de canto y acompañamiento:",
                color = Color(0xFFA8A3B4),
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Primary 2/4 and 4/4 Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 2/4 (Tradicional)
                MeterSelectionCard(
                    meter = HuaynoMeter.TWO_FOUR,
                    isSelected = config.meter == HuaynoMeter.TWO_FOUR,
                    title = "2/4 Tradicional",
                    tag = "Esencial Huayno",
                    description = "2 tiempos: bombo acentuado en 1 y zapateo en 2. Estilo Ayacuchano, Sureño y Huaylash.",
                    modifier = Modifier.weight(1f),
                    onSelect = { viewModel.setMetronomeMeter(HuaynoMeter.TWO_FOUR) }
                )

                // Card 4/4 (Moderno con Arpa)
                MeterSelectionCard(
                    meter = HuaynoMeter.FOUR_FOUR,
                    isSelected = config.meter == HuaynoMeter.FOUR_FOUR,
                    title = "4/4 Moderno",
                    tag = "Arpa y Cumbia",
                    description = "4 tiempos: huayno contemporáneo con arpa, teclado y fusión chicha / balada andina.",
                    modifier = Modifier.weight(1f),
                    onSelect = { viewModel.setMetronomeMeter(HuaynoMeter.FOUR_FOUR) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary meters (3/4 and 6/8)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = config.meter == HuaynoMeter.THREE_FOUR,
                    onClick = { viewModel.setMetronomeMeter(HuaynoMeter.THREE_FOUR) },
                    label = { Text("3/4 Pasacalle / Vals", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AndeanTurquoise,
                        selectedLabelColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = config.meter == HuaynoMeter.SIX_EIGHT,
                    onClick = { viewModel.setMetronomeMeter(HuaynoMeter.SIX_EIGHT) },
                    label = { Text("6/8 Carnaval Andino", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AndeanTurquoise,
                        selectedLabelColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECCIÓN: TEMPO Y VELOCIDAD (BPM)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TEMPO (BPM)",
                    color = AndeanGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${config.bpm} BPM",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // BPM Adjuster Controls (-5, -1, +1, +5)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.setMetronomeBpm(config.bpm - 5) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("-5", fontSize = 12.sp, color = Color.White)
                }

                OutlinedButton(
                    onClick = { viewModel.setMetronomeBpm(config.bpm - 1) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("-1", fontSize = 12.sp, color = Color.White)
                }

                Slider(
                    value = config.bpm.toFloat(),
                    onValueChange = { viewModel.setMetronomeBpm(it.toInt()) },
                    valueRange = 40f..220f,
                    colors = SliderDefaults.colors(
                        thumbColor = AndeanGold,
                        activeTrackColor = AndeanGold,
                        inactiveTrackColor = Color(0xFF282333)
                    ),
                    modifier = Modifier.weight(1f)
                )

                OutlinedButton(
                    onClick = { viewModel.setMetronomeBpm(config.bpm + 1) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("+1", fontSize = 12.sp, color = Color.White)
                }

                OutlinedButton(
                    onClick = { viewModel.setMetronomeBpm(config.bpm + 5) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("+5", fontSize = 12.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PRESETS DE VELOCIDAD HUAYNO
            Text(
                text = "PRESETS RÁPIDOS REGIONALES",
                color = Color(0xFFA8A3B4),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.huaynoPresets) { preset ->
                    val isMatch = config.bpm == preset.bpm && config.meter == preset.meter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isMatch) AndeanGold.copy(alpha = 0.25f) else Color(0xFF1F1C28))
                            .border(
                                1.dp,
                                if (isMatch) AndeanGold else StudioBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.applyMetronomePreset(preset) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = preset.name,
                                    color = if (isMatch) AndeanGold else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${preset.meter.label} • ${preset.bpm}BPM",
                                    color = AndeanTurquoise,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = preset.style,
                                color = Color(0xFFA8A3B4),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECCIÓN: TIMBRE DEL SONIDO ANDINO
            Text(
                text = "TIMBRE DEL SONIDO DE REFERENCIA",
                color = AndeanGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetronomeTimbre.entries.forEach { t ->
                    val isSelected = config.timbre == t
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) AndeanTurquoise.copy(alpha = 0.2f) else Color(0xFF1B1824))
                            .border(
                                1.dp,
                                if (isSelected) AndeanTurquoise else StudioBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.setMetronomeTimbre(t) }
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = t.displayName.substringBefore("/"),
                                color = if (isSelected) AndeanTurquoise else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = t.shortDesc,
                                color = Color(0xFFA8A3B4),
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Volumen del metrónomo
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Volumen",
                    tint = AndeanGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Volumen del Clic:",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = config.volume,
                    onValueChange = { viewModel.setMetronomeVolume(it) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = AndeanTurquoise,
                        activeTrackColor = AndeanTurquoise,
                        inactiveTrackColor = Color(0xFF282333)
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(config.volume * 100).toInt()}%",
                    color = Color(0xFFA8A3B4),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ASISTENCIA A LA GRABACIÓN DE VOCES (MÓDULO CLAVE)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1524)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AndeanGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Grabación de Voces",
                            tint = AndeanGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Asistencia para Cantantes en Grabación",
                            color = AndeanGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pre-cuenta (Count-In) selector
                    Text(
                        text = "Pre-cuenta previa (Count-In):",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Genera compases de clic previo antes de activar el micrófono para que el artista ingrese a tiempo:",
                        color = Color(0xFFA8A3B4),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            0 to "Sin pre-cuenta",
                            1 to "1 Compás (${config.meter.beatsPerBar} t)",
                            2 to "2 Compases (${config.meter.beatsPerBar * 2} t)"
                        ).forEach { (bars, label) ->
                            val isSel = config.countInBars == bars
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) AndeanGold else Color(0xFF241F30))
                                    .clickable { viewModel.setMetronomeCountInBars(bars) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sonar durante grabación de voces
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sonar durante grabación de voz",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "El clic continúa sonando en audífonos mientras el micrófono graba para mantener la cadencia.",
                                color = Color(0xFFA8A3B4),
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = config.playDuringRecording,
                            onCheckedChange = { viewModel.setMetronomeDuringRecording(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = AndeanGold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sonar durante reproducción general
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sonar al reproducir pistas",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Activa el metrónomo automáticamente al presionar Play en la barra de transporte.",
                                color = Color(0xFFA8A3B4),
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = config.playDuringPlayback,
                            onCheckedChange = { viewModel.setMetronomeDuringPlayback(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = AndeanTurquoise
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MeterSelectionCard(
    meter: HuaynoMeter,
    isSelected: Boolean,
    title: String,
    tag: String,
    description: String,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AndeanGold.copy(alpha = 0.16f) else Color(0xFF191622)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) AndeanGold else StudioBorder
        ),
        modifier = modifier
            .clickable { onSelect() }
            .testTag("meter_${meter.label}_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = if (isSelected) AndeanGold else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) AndeanGold else Color(0xFF282333))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tag,
                        color = if (isSelected) Color.Black else AndeanTurquoise,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                color = Color(0xFFA8A3B4),
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}
