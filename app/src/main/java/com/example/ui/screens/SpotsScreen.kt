package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AndeanAudioSynthesizer
import com.example.audio.PronunciationDictionary
import com.example.audio.PronunciationRule
import com.example.audio.SpotVoiceSynthesizer
import com.example.audio.SyntheticVoiceModel
import com.example.gemini.GeminiSpotGenerator
import com.example.gemini.SpotScriptOption
import com.example.ui.components.TemplatesDialog
import com.example.ui.theme.AndeanGold
import com.example.ui.theme.AndeanRed
import com.example.ui.theme.AndeanTurquoise
import com.example.ui.theme.StudioBlack
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.viewmodel.EditorViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpotsScreen(
    viewModel: EditorViewModel,
    onNavigateToEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Voice synthesizer
    val voiceSynthesizer = remember { SpotVoiceSynthesizer(context) }
    DisposableEffect(Unit) {
        onDispose {
            voiceSynthesizer.release()
        }
    }

    // Form states
    var eventType by remember { mutableStateOf("Fiesta Patronal") }
    var eventName by remember { mutableStateOf("Gran Fiesta Patronal y Aniversario Andino") }
    var eventDate by remember { mutableStateOf("Sábado 15 de Octubre - 8:00 PM") }
    var location by remember { mutableStateOf("Complejo Deportivo El Huarangal") }
    var artists by remember { mutableStateOf("Las Estrellas del Huayno con Arpa y Orquesta") }
    var whatsapp by remember { mutableStateOf("987 654 321") }
    var ticketPrice by remember { mutableStateOf("Pre-venta S/ 25 • Zona VIP S/ 40") }
    var durationSec by remember { mutableIntStateOf(30) }
    var tone by remember { mutableStateOf("Animador Radial Eufórico") }
    var regionalStyle by remember { mutableStateOf("Huayno Ayacuchano") }
    var mandatoryPhrases by remember { mutableStateOf("¡Zapateo asegurado! ¡Cerveza bien helada! ¡Seguridad garantizada!") }

    // Gemini generation states
    var isGeneratingScripts by remember { mutableStateOf(false) }
    var generatedScripts by remember { mutableStateOf<List<SpotScriptOption>>(emptyList()) }
    var selectedScriptIndex by remember { mutableIntStateOf(0) }

    // Script editor state
    var editableScriptText by remember {
        mutableStateOf(
            "¡Atención Perú, llegó el momento más esperado! Este sábado 15 de octubre, toda la emoción de nuestra música andina se traslada al Complejo El Huarangal para vivir la Gran Fiesta Patronal. En el escenario mayor: Las Estrellas del Huayno con Arpa, brindando lo mejor de su repertorio con arpa y bordones. Seguridad garantizada, cerveza bien helada y el mejor zapateo. Informes y reservas al WhatsApp 987 654 321. ¡HuaynoStudio invita, no te lo pierdas!"
        )
    }

    // Pronunciation dictionary state
    var showPronunciationDialog by remember { mutableStateOf(false) }
    var showTemplatesDialog by remember { mutableStateOf(false) }
    var customPronunciationRules by remember { mutableStateOf<List<PronunciationRule>>(emptyList()) }

    // Voice selection and synthesis states
    var selectedVoice by remember { mutableStateOf(voiceSynthesizer.availableVoices.first()) }
    var speechSpeed by remember { mutableFloatStateOf(1.20f) }
    var speechEmotion by remember { mutableStateOf("Festivo eufórico") }
    var isPreviewSpeaking by remember { mutableStateOf(false) }

    // Mix generation state
    var isProducingMix by remember { mutableStateOf(false) }
    var mixReady by remember { mutableStateOf(false) }
    var lastMixedTitle by remember { mutableStateOf("") }
    var cachedMusicSamples by remember { mutableStateOf<FloatArray?>(null) }
    var cachedVoiceSamples by remember { mutableStateOf<FloatArray?>(null) }
    var cachedIntroFxSamples by remember { mutableStateOf<FloatArray?>(null) }
    var cachedOutroFxSamples by remember { mutableStateOf<FloatArray?>(null) }

    val wordCount = remember(editableScriptText) {
        editableScriptText.split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }
    val estimatedDuration = remember(wordCount, speechSpeed) {
        wordCount / (2.5f * speechSpeed)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = AndeanGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MÓDULO A • SPOTS RADIALES",
                        color = AndeanGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "Generador de Spots de Huayno",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1B3D2B))
                    .border(1.dp, AndeanTurquoise, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "IA + DUCKING ACTIVO",
                    color = AndeanTurquoise,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Paso 1: Formulario de Parámetros
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
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
                            text = "1. Parámetros del Evento Andino",
                            color = AndeanGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configura los datos que Gemini usará para escribir los 3 guiones de spot.",
                            color = Color(0xFFA8A3B4),
                            fontSize = 11.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { showTemplatesDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AndeanGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AndeanGold.copy(alpha = 0.7f)),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Plantillas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tipo de Evento
                Text(text = "Tipo de Evento:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Fiesta Patronal", "Baile Social", "Aniversario", "Lanzamiento", "Concierto", "Negocio", "Radio").forEach { item ->
                        val isSel = item == eventType
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) AndeanGold else StudioSurfaceVariant)
                                .border(1.dp, if (isSel) AndeanGold else StudioBorder, RoundedCornerShape(6.dp))
                                .clickable { eventType = item }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = item,
                                color = if (isSel) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Campos de datos
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Nombre o Motivo del Evento") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AndeanGold,
                        unfocusedBorderColor = StudioBorder,
                        focusedLabelColor = AndeanGold,
                        unfocusedLabelColor = Color(0xFFA8A3B4),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = eventDate,
                        onValueChange = { eventDate = it },
                        label = { Text("Fecha y Hora") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AndeanGold,
                            unfocusedBorderColor = StudioBorder,
                            focusedLabelColor = AndeanGold,
                            unfocusedLabelColor = Color(0xFFA8A3B4),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Lugar / Local") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AndeanGold,
                            unfocusedBorderColor = StudioBorder,
                            focusedLabelColor = AndeanGold,
                            unfocusedLabelColor = Color(0xFFA8A3B4),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = artists,
                    onValueChange = { artists = it },
                    label = { Text("Artistas / Agrupaciones Estelares") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AndeanGold,
                        unfocusedBorderColor = StudioBorder,
                        focusedLabelColor = AndeanGold,
                        unfocusedLabelColor = Color(0xFFA8A3B4),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = { Text("WhatsApp Contacto") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AndeanGold,
                            unfocusedBorderColor = StudioBorder,
                            focusedLabelColor = AndeanGold,
                            unfocusedLabelColor = Color(0xFFA8A3B4),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = ticketPrice,
                        onValueChange = { ticketPrice = it },
                        label = { Text("Entradas / Precios") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AndeanGold,
                            unfocusedBorderColor = StudioBorder,
                            focusedLabelColor = AndeanGold,
                            unfocusedLabelColor = Color(0xFFA8A3B4),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Duración
                Text(text = "Duración del Spot Radial:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(15, 30, 45, 60).forEach { sec ->
                        val isSel = sec == durationSec
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) AndeanGold else StudioSurfaceVariant)
                                .border(1.dp, if (isSel) AndeanGold else StudioBorder, RoundedCornerShape(6.dp))
                                .clickable { durationSec = sec }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${sec}s",
                                color = if (isSel) Color.Black else Color.White,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Estilo regional
                Text(text = "Estilo Regional Andino:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Huayno Ayacuchano", "Huayno Huanca / Huaylash", "Huayno Cusqueño", "Huayno Puneño", "Huayno Ancashino").forEach { st ->
                        val isSel = st == regionalStyle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) AndeanGold else StudioSurfaceVariant)
                                .border(1.dp, if (isSel) AndeanGold else StudioBorder, RoundedCornerShape(6.dp))
                                .clickable { regionalStyle = st }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = st,
                                color = if (isSel) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tono
                Text(text = "Tono de Locución:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Animador Radial Eufórico", "Festivo Popular", "Formal Ceremonial").forEach { item ->
                        val isSel = item == tone
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) AndeanGold else StudioSurfaceVariant)
                                .border(1.dp, if (isSel) AndeanGold else StudioBorder, RoundedCornerShape(6.dp))
                                .clickable { tone = item }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item,
                                color = if (isSel) Color.Black else Color.White,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = mandatoryPhrases,
                    onValueChange = { mandatoryPhrases = it },
                    label = { Text("Frases Obligatorias a incluir") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AndeanGold,
                        unfocusedBorderColor = StudioBorder,
                        focusedLabelColor = AndeanGold,
                        unfocusedLabelColor = Color(0xFFA8A3B4),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Botón generar guiones con Gemini
                Button(
                    onClick = {
                        scope.launch {
                            val canConsume = viewModel.repository.checkCanConsumeAction("generar_guion", cost = 1)
                            if (canConsume.isFailure) {
                                snackbarHostState.showSnackbar(canConsume.exceptionOrNull()?.message ?: "Sin créditos")
                                return@launch
                            }

                            isGeneratingScripts = true
                            val result = GeminiSpotGenerator.generateHuaynoSpots(
                                eventType = eventType,
                                eventName = eventName,
                                eventDate = eventDate,
                                location = location,
                                artists = artists,
                                whatsapp = whatsapp,
                                ticketPrice = ticketPrice,
                                durationSec = durationSec,
                                tone = tone,
                                regionalStyle = regionalStyle,
                                mandatoryPhrases = mandatoryPhrases
                            )
                            isGeneratingScripts = false

                            result.onSuccess { scripts ->
                                generatedScripts = scripts
                                if (scripts.isNotEmpty()) {
                                    selectedScriptIndex = 0
                                    editableScriptText = scripts[0].text
                                    viewModel.repository.recordUsage(
                                        actionType = "generar_guion",
                                        detail = "Spot $durationSec s: $eventName",
                                        cost = 1
                                    )
                                    snackbarHostState.showSnackbar("¡3 guiones generados con Gemini!")
                                }
                            }.onFailure { err ->
                                snackbarHostState.showSnackbar("Error al generar: ${err.message}")
                            }
                        }
                    },
                    enabled = !isGeneratingScripts,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AndeanGold, contentColor = Color.Black)
                ) {
                    if (isGeneratingScripts) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini está escribiendo 3 guiones andinos...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generar 3 Guiones con Gemini (IA)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Paso 2: Selección y Editor de Guion
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
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
                            text = "2. Editor de Guion Andino",
                            color = AndeanGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ajusta palabras, apodos y fonética para la locución.",
                            color = Color(0xFFA8A3B4),
                            fontSize = 11.sp
                        )
                    }

                    // Botón Diccionario
                    OutlinedButton(
                        onClick = { showPronunciationDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AndeanGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AndeanGold),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Diccionario", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Opciones de guion si se generaron
                if (generatedScripts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Selecciona una opción generada:", color = Color.White, fontSize = 11.sp)
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        generatedScripts.forEachIndexed { index, option ->
                            val isSel = index == selectedScriptIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) AndeanTurquoise else StudioSurfaceVariant)
                                    .border(1.dp, if (isSel) AndeanTurquoise else StudioBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedScriptIndex = index
                                        editableScriptText = option.text
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Opción ${index + 1}",
                                    color = if (isSel) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Área de edición del texto
                OutlinedTextField(
                    value = editableScriptText,
                    onValueChange = { editableScriptText = it },
                    label = { Text("Texto del Spot para Locutar") },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AndeanGold,
                        unfocusedBorderColor = StudioBorder,
                        focusedLabelColor = AndeanGold,
                        unfocusedLabelColor = Color(0xFFA8A3B4),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Métricas de palabras y duración estimada
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = AndeanGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$wordCount palabras • Est. ${String.format("%.1f", estimatedDuration)}s / ${durationSec}s",
                            color = if (estimatedDuration > durationSec + 3) AndeanRed else AndeanGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = {
                            val phoneticallyClean = PronunciationDictionary.applyDictionary(editableScriptText, customPronunciationRules)
                            editableScriptText = phoneticallyClean
                            scope.launch {
                                snackbarHostState.showSnackbar("¡Diccionario fonético aplicado al texto!")
                            }
                        }
                    ) {
                        Text("Optimizar Fonética", color = AndeanTurquoise, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Paso 3: Biblioteca de Voces y Síntesis
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "3. Locución y Voz Andina Sintética",
                    color = AndeanGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Elige la voz del locutor, la velocidad y la emoción radial.",
                    color = Color(0xFFA8A3B4),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Selector de voces
                voiceSynthesizer.availableVoices.forEach { voice ->
                    val isSel = voice.id == selectedVoice.id
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSel) Color(0xFF2B210F) else StudioSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AndeanGold else StudioBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                selectedVoice = voice
                                speechSpeed = voice.defaultSpeed
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel) AndeanGold else Color(0xFF383344)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = if (isSel) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = voice.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = voice.gender,
                                        color = if (isSel) AndeanGold else Color(0xFFA8A3B4),
                                        fontSize = 10.sp
                                    )
                                }
                                Text(text = voice.style, color = Color(0xFFA8A3B4), fontSize = 11.sp)
                            }

                            if (isSel) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = AndeanGold, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sliders de velocidad
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Velocidad de Locución:", color = Color.White, fontSize = 11.sp)
                    Text(text = "${String.format("%.2f", speechSpeed)}x", color = AndeanGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = speechSpeed,
                    onValueChange = { speechSpeed = it },
                    valueRange = 0.8f..1.4f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = AndeanGold,
                        activeTrackColor = AndeanGold,
                        inactiveTrackColor = StudioBorder
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Emoción
                Text(text = "Emoción e Intención:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Festivo eufórico", "Sentimental / Tradicional", "Urgente / Impacto", "Formal ceremonial").forEach { emo ->
                        val isSel = emo == speechEmotion
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) AndeanGold else StudioSurfaceVariant)
                                .border(1.dp, if (isSel) AndeanGold else StudioBorder, RoundedCornerShape(6.dp))
                                .clickable { speechEmotion = emo }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emo,
                                color = if (isSel) Color.Black else Color.White,
                                fontSize = 9.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón Previa de Voz
                OutlinedButton(
                    onClick = {
                        if (isPreviewSpeaking) {
                            voiceSynthesizer.stopPreview()
                            isPreviewSpeaking = false
                        } else {
                            isPreviewSpeaking = true
                            voiceSynthesizer.speakPreview(
                                text = editableScriptText,
                                voice = selectedVoice,
                                speed = speechSpeed,
                                emotion = speechEmotion,
                                onDone = { isPreviewSpeaking = false }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AndeanGold),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AndeanGold)
                ) {
                    Icon(
                        imageVector = if (isPreviewSpeaking) Icons.Default.Pause else Icons.Default.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPreviewSpeaking) "Detener Previa de Voz" else "Escuchar Muestra de Locución",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Paso 4: Mezcla Automática con Ducking
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "4. Mezcla Automática con Ducking",
                    color = AndeanGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Combina la pista de música huayno ($regionalStyle) + locución + cohete intro y aplausos.",
                    color = Color(0xFFA8A3B4),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Detalles técnicos de la mezcla
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioSurfaceVariant)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Ducking Automático:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "-14 dB atenuación música", color = AndeanTurquoise, fontSize = 10.sp)
                    }
                    Column {
                        Text(text = "Música Huayno:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "$durationSec s estéreo 44.1 kHz", color = AndeanGold, fontSize = 10.sp)
                    }
                    Column {
                        Text(text = "Efectos Andinos:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Intro cohete + Cierre", color = Color(0xFFFF5C70), fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val canConsume = viewModel.repository.checkCanConsumeAction("generar_voz", cost = 1)
                            if (canConsume.isFailure) {
                                snackbarHostState.showSnackbar(canConsume.exceptionOrNull()?.message ?: "Sin créditos")
                                return@launch
                            }

                            isProducingMix = true
                            voiceSynthesizer.stopPreview()
                            isPreviewSpeaking = false

                            // 1. Generar voz sintetizada
                            val voiceAudio = voiceSynthesizer.generateVoiceAudioTrack(
                                scriptText = editableScriptText,
                                voice = selectedVoice,
                                speed = speechSpeed,
                                emotion = speechEmotion,
                                targetDurationSec = durationSec.toFloat()
                            )

                            // 2. Generar música instrumental regional
                            val musicAudio = AndeanAudioSynthesizer.generateRegionalHuayno(
                                style = regionalStyle,
                                durationSec = durationSec.toFloat()
                            )

                            // 3. Generar intro y outro FX
                            val introFx = AndeanAudioSynthesizer.generateSpotIntroFx()
                            val outroFx = AndeanAudioSynthesizer.generateSpotOutroFx(durationSec = 4.0f)

                            cachedVoiceSamples = voiceAudio
                            cachedMusicSamples = musicAudio
                            cachedIntroFxSamples = introFx
                            cachedOutroFxSamples = outroFx

                            lastMixedTitle = "Spot $durationSec s - $eventName"
                            mixReady = true
                            isProducingMix = false

                            viewModel.repository.recordUsage(
                                actionType = "generar_voz",
                                detail = "Mezcla $durationSec s: ${selectedVoice.name}",
                                cost = 1
                            )

                            snackbarHostState.showSnackbar("¡Mezcla de spot completada con Ducking!")
                        }
                    },
                    enabled = !isProducingMix,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946), contentColor = Color.White)
                ) {
                    if (isProducingMix) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sintetizando locución y mezclando pistas...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Producir Mezcla Automática con Ducking", fontWeight = FontWeight.Bold)
                    }
                }

                // Resultado: abrir en editor
                AnimatedVisibility(visible = mixReady) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E281F)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AndeanTurquoise),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = AndeanTurquoise, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "¡Spot Producido con Éxito!",
                                        color = AndeanTurquoise,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$lastMixedTitle • Voz: ${selectedVoice.name} • Ducking activo",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val mSamples = cachedMusicSamples ?: FloatArray(0)
                                        val vSamples = cachedVoiceSamples ?: FloatArray(0)
                                        val inFx = cachedIntroFxSamples ?: FloatArray(0)
                                        val outFx = cachedOutroFxSamples ?: FloatArray(0)

                                        viewModel.loadAutoMixedSpot(
                                            title = lastMixedTitle,
                                            regionalStyle = regionalStyle,
                                            durationSec = durationSec.toFloat(),
                                            musicSamples = mSamples,
                                            voiceSamples = vSamples,
                                            introFxSamples = inFx,
                                            outroFxSamples = outFx,
                                            scriptTitle = selectedVoice.name
                                        )
                                        onNavigateToEditor()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AndeanGold, contentColor = Color.Black)
                                ) {
                                    Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Abrir en el Editor Multipista para Retoques", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        SnackbarHost(hostState = snackbarHostState)
    }

    // Modal Diccionario de Pronunciación
    if (showPronunciationDialog) {
        PronunciationDialog(
            customRules = customPronunciationRules,
            onAddRule = { word, repl, cat ->
                val newRule = PronunciationRule(
                    id = "cust_${System.currentTimeMillis()}",
                    originalWord = word,
                    phoneticReplacement = repl,
                    category = cat
                )
                customPronunciationRules = customPronunciationRules + newRule
            },
            onDeleteRule = { id ->
                customPronunciationRules = customPronunciationRules.filter { it.id != id }
            },
            onApplyToScript = {
                val phoneticallyClean = PronunciationDictionary.applyDictionary(editableScriptText, customPronunciationRules)
                editableScriptText = phoneticallyClean
                showPronunciationDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("¡Pronunciación fonética aplicada al guion!")
                }
            },
            onDismiss = { showPronunciationDialog = false }
        )
    }

    if (showTemplatesDialog) {
        TemplatesDialog(
            viewModel = viewModel,
            onDismiss = { showTemplatesDialog = false },
            onUseInSpotGenerator = { template ->
                eventType = template.category
                eventName = template.eventName
                eventDate = template.dateTime
                location = template.venue
                artists = template.artists
                whatsapp = template.contactWhatsapp
                ticketPrice = template.ticketPrice
                durationSec = template.durationSec.toInt()
                regionalStyle = template.regionalStyle
                editableScriptText = template.scriptText
                speechSpeed = template.voiceSpeed
                showTemplatesDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Plantilla '${template.title}' cargada en el generador")
                }
            },
            onLoadInEditor = { template ->
                showTemplatesDialog = false
                viewModel.loadSpotTemplateIntoEditor(template)
                onNavigateToEditor()
            }
        )
    }
}

@Composable
fun PronunciationDialog(
    customRules: List<PronunciationRule>,
    onAddRule: (word: String, replacement: String, category: String) -> Unit,
    onDeleteRule: (id: String) -> Unit,
    onApplyToScript: () -> Unit,
    onDismiss: () -> Unit
) {
    var newWord by remember { mutableStateOf("") }
    var newPhonetic by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Personalizado") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = AndeanGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Diccionario de Pronunciación Andina", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Asegura la dicción perfecta de nombres quechuas, pueblos andinos y apodos radiales en el sintetizador.",
                    color = Color(0xFFA8A3B4),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Agregar regla personalizada
                Text(text = "Agregar Nueva Pronunciación:", color = AndeanGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = newWord,
                        onValueChange = { newWord = it },
                        label = { Text("Palabra o Nombre") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AndeanGold,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = newPhonetic,
                        onValueChange = { newPhonetic = it },
                        label = { Text("Fonética / Reemplazo") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AndeanGold,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        if (newWord.isNotBlank() && newPhonetic.isNotBlank()) {
                            onAddRule(newWord.trim(), newPhonetic.trim(), selectedCategory)
                            newWord = ""
                            newPhonetic = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceVariant, contentColor = AndeanGold)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar en Diccionario", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Lista de reglas
                Text(text = "Vocabulario Andino Integrado:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                val allRules = customRules + PronunciationDictionary.defaultRules
                allRules.forEach { rule ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = rule.originalWord, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "➔ ${rule.phoneticReplacement}", color = AndeanTurquoise, fontSize = 12.sp)
                            }
                            Text(text = "${rule.category} • ${rule.description}", color = Color(0xFFA8A3B4), fontSize = 10.sp)
                        }

                        if (rule.category == "Personalizado") {
                            IconButton(onClick = { onDeleteRule(rule.id) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = AndeanRed, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onApplyToScript,
                colors = ButtonDefaults.buttonColors(containerColor = AndeanGold, contentColor = Color.Black)
            ) {
                Text("Aplicar al Guion", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = Color(0xFFA8A3B4))
            }
        },
        containerColor = StudioSurface
    )
}
