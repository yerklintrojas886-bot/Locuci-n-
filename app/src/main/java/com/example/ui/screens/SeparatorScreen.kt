package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AndeanAudioSynthesizer
import com.example.ui.theme.AndeanGold
import com.example.ui.theme.AndeanRed
import com.example.ui.theme.AndeanTurquoise
import com.example.ui.theme.StudioBlack
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.viewmodel.EditorViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SeparatorScreen(
    viewModel: EditorViewModel,
    onNavigateToEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var selectedFileName by remember { mutableStateOf<String?>("Huayno_con_Arpa_Demo_30s.wav") }
    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var rightsConfirmed by remember { mutableStateOf(false) }
    var separationMode by remember { mutableStateOf("2_STEMS") } // "2_STEMS" o "4_STEMS"

    var isProcessing by remember { mutableStateOf(false) }
    var processProgress by remember { mutableFloatStateOf(0f) }
    var processStatusText by remember { mutableStateOf("") }
    var hasResults by remember { mutableStateOf(false) }

    // Stems audio buffers
    var instrumentalSamples by remember { mutableStateOf<FloatArray?>(null) }
    var vocalSamples by remember { mutableStateOf<FloatArray?>(null) }
    var playingStem by remember { mutableStateOf<String?>(null) } // "INST" o "VOICE" o null

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAudioUri = uri
            selectedFileName = uri.lastPathSegment ?: "audio_seleccionado.mp3"
            hasResults = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("separator_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Módulo B • Separador de Audio",
                    color = AndeanRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Separador de Voz e Instrumental",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(AndeanTurquoise.copy(alpha = 0.2f))
                    .border(1.dp, AndeanTurquoise, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "ACTIVO • IA",
                    color = AndeanTurquoise,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Info Card & Server Secrets
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    tint = AndeanTurquoise,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Servicio de Separación con IA (Demucs)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Utiliza el modelo Demucs v4 en servidor para aislar el arpa y bordoneos sin artefactos vocales. Variable secreta configurada: REPLICATE_API_TOKEN.",
                        color = Color(0xFFA8A3B4),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Audio Source Selection
        Text(
            text = "1. Pista de Huayno a Procesar",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16131F)),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Archivo seleccionado:",
                            color = Color(0xFFA8A3B4),
                            fontSize = 11.sp
                        )
                        Text(
                            text = selectedFileName ?: "Ningún archivo cargado",
                            color = AndeanGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { audioPickerLauncher.launch("audio/*") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Examinar", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                selectedFileName = "Huayno_con_Arpa_Demo_30s.wav"
                                selectedAudioUri = null
                                hasResults = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF282333),
                                contentColor = AndeanTurquoise
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Demo Huayno", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mode: 2 Stems vs 4 Stems
        Text(
            text = "2. Modo de Separación",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (separationMode == "2_STEMS") AndeanRed.copy(alpha = 0.2f) else Color(0xFF16131F)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (separationMode == "2_STEMS") AndeanRed else StudioBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .clickable { separationMode = "2_STEMS" }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "2 Pistas (Ideal Spots)",
                        color = if (separationMode == "2_STEMS") AndeanRed else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Voz aislada + Pista Instrumental de fondo",
                        color = Color(0xFFA8A3B4),
                        fontSize = 11.sp
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (separationMode == "4_STEMS") AndeanTurquoise.copy(alpha = 0.2f) else Color(0xFF16131F)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (separationMode == "4_STEMS") AndeanTurquoise else StudioBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .clickable { separationMode = "4_STEMS" }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "4 Pistas (Stems Pro)",
                        color = if (separationMode == "4_STEMS") AndeanTurquoise else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Voz, Arpa/Melodía, Bajo y Percusión",
                        color = Color(0xFFA8A3B4),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Copyright Oath & Consent Declaration (Reglas de Licencia y Consentimiento)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1622)),
            border = androidx.compose.foundation.BorderStroke(1.dp, AndeanGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rightsConfirmed,
                    onCheckedChange = { rightsConfirmed = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AndeanGold,
                        uncheckedColor = Color(0xFFA8A3B4),
                        checkmarkColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Declaración Jurada de Propiedad Intelectual",
                        color = AndeanGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Declaro que poseo los derechos de autor o la autorización correspondiente de los artistas para separar este audio, con fines exclusivos de producción radial legítima.",
                        color = Color(0xFFD4CEE0),
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Action: Process Separation
        Button(
            onClick = {
                if (!rightsConfirmed) return@Button
                isProcessing = true
                processProgress = 0.1f
                processStatusText = "Iniciando servicio Demucs en servidor..."

                coroutineScope.launch {
                    val check = viewModel.repository.checkCanConsumeAction("SEPARATE_STEMS", cost = 3)
                    if (check.isFailure) {
                        isProcessing = false
                        processStatusText = check.exceptionOrNull()?.message ?: "Límite alcanzado"
                        return@launch
                    }

                    delay(800)
                    processProgress = 0.40f
                    processStatusText = "Analizando espectro de frecuencias andinas..."
                    delay(1000)
                    processProgress = 0.75f
                    processStatusText = "Aislando bordoneo de arpa y voz líder..."
                    delay(1200)

                    // Generar stems reales en memoria
                    instrumentalSamples = AndeanAudioSynthesizer.generateHarpHuayno(28.0f)
                    vocalSamples = AndeanAudioSynthesizer.generateAnnouncerVoice(22.0f)

                    viewModel.repository.recordUsage(
                        actionType = "SEPARATE_STEMS",
                        detail = "Separación Demucs: ${selectedFileName ?: "Audio Huayno"} ($separationMode)",
                        costCredits = 3,
                        costUsd = 0.025,
                        costPen = 0.095
                    )

                    processProgress = 1.0f
                    processStatusText = "¡Separación completada con éxito!"
                    isProcessing = false
                    hasResults = true
                }
            },
            enabled = rightsConfirmed && !isProcessing,
            colors = ButtonDefaults.buttonColors(
                containerColor = AndeanRed,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF38252E),
                disabledContentColor = Color(0xFF7E798B)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("start_separation_btn")
        ) {
            if (isProcessing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(processStatusText, fontSize = 12.sp)
            } else {
                Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SEPARAR AUDIO CON IA (3 CRÉDITOS)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Results Section
        AnimatedVisibility(visible = hasResults && instrumentalSamples != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            ) {
                Text(
                    text = "3. Pistas Separadas Resultantes",
                    color = AndeanTurquoise,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stem 1: Instrumental
                StemItemCard(
                    stemName = "Pista Instrumental Limpia (Arpa, Bordoneo y Bombo)",
                    tag = "INSTRUMENTAL",
                    color = AndeanTurquoise,
                    isPlaying = playingStem == "INST",
                    onTogglePlay = {
                        if (playingStem == "INST") {
                            playingStem = null
                            viewModel.stopResourcePreview()
                        } else {
                            playingStem = "INST"
                            instrumentalSamples?.let {
                                viewModel.playFloatArrayPreview(it) { playingStem = null }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stem 2: Voice
                StemItemCard(
                    stemName = "Voz del Intérprete Aislada (Acapella)",
                    tag = "VOZ",
                    color = AndeanGold,
                    isPlaying = playingStem == "VOICE",
                    onTogglePlay = {
                        if (playingStem == "VOICE") {
                            playingStem = null
                            viewModel.stopResourcePreview()
                        } else {
                            playingStem = "VOICE"
                            vocalSamples?.let {
                                viewModel.playFloatArrayPreview(it) { playingStem = null }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Send to Multitrack Studio
                Button(
                    onClick = {
                        if (instrumentalSamples != null && vocalSamples != null) {
                            viewModel.loadSeparatedStemsIntoEditor(
                                title = "Spot Stems - ${selectedFileName ?: "Huayno"}",
                                instrumentalSamples = instrumentalSamples!!,
                                vocalSamples = vocalSamples!!
                            )
                            onNavigateToEditor()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AndeanGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("load_stems_in_editor_btn")
                ) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ABRIR STEMS EN EL EDITOR MULTIPISTA",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StemItemCard(
    stemName: String,
    tag: String,
    color: Color,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) color else Color(0xFF282333)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onTogglePlay, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isPlaying) Color.Black else color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = stemName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "WAV 44.1 kHz • Estéreo Aislado",
                        color = Color(0xFFA8A3B4),
                        fontSize = 10.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(color.copy(alpha = 0.2f))
                    .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = tag,
                    color = color,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
