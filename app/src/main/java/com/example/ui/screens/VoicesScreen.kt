package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import com.example.data.AuthorizedVoiceEntity
import com.example.ui.theme.AndeanGold
import com.example.ui.theme.AndeanTurquoise
import com.example.ui.theme.StudioBlack
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.viewmodel.EditorViewModel
import kotlinx.coroutines.launch

@Composable
fun VoicesScreen(
    viewModel: EditorViewModel,
    onNavigateToSpots: () -> Unit,
    onNavigateToEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val authorizedVoices by viewModel.authorizedVoices.collectAsState()

    var showConsentDialog by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var currentlyPlayingVoiceId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBlack)
            .padding(16.dp)
            .testTag("voices_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Módulo D • Voces Autorizadas",
                    color = Color(0xFFA06CD5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Biblioteca de Voces & Consentimiento",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Button(
                onClick = { showRegisterDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA06CD5),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Registrar Voz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Ethical Voice & License Policy Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showConsentDialog = true }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = AndeanTurquoise,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Marco de Consentimiento Ético y Licenciamiento",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Todas las voces cuentan con contrato de cesión vocal para spots andinos. Claves de servidor seguras (ELEVENLABS_API_KEY). Toca para ver el acuerdo legal.",
                        color = Color(0xFFA8A3B4),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Voces Andinas Verificadas (${authorizedVoices.size})",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(authorizedVoices, key = { it.id }) { voice ->
                VoiceItemCard(
                    voice = voice,
                    isPlaying = currentlyPlayingVoiceId == voice.id,
                    onTogglePlay = {
                        if (currentlyPlayingVoiceId == voice.id) {
                            currentlyPlayingVoiceId = null
                            viewModel.stopResourcePreview()
                        } else {
                            currentlyPlayingVoiceId = voice.id
                            viewModel.previewAuthorizedVoice(voice) {
                                currentlyPlayingVoiceId = null
                            }
                        }
                    },
                    onUseInSpots = {
                        viewModel.setSelectedVoiceForSpots(voice.voiceTag)
                        onNavigateToSpots()
                    }
                )
            }
        }
    }

    // Modal: Legal Voice Consent Terms
    if (showConsentDialog) {
        Dialog(onDismissRequest = { showConsentDialog = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AndeanGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Acuerdo de Licencia y Consentimiento Biométrico",
                            color = AndeanGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showConsentDialog = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13101A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState())
                                .height(220.dp)
                        ) {
                            Text(
                                text = "1. ALCANCE DE LA LICENCIA\n" +
                                        "Los locutores y artistas andinos registrados han otorgado autorización expresa e informada para que sus modelos de voz sean utilizados exclusivamente en cuñas radiales, spots promocionales y avisos comerciales andinos generados dentro de HuaynoStudio.\n\n" +
                                        "2. PROHIBICIÓN DE USOS NO AUTORIZADOS\n" +
                                        "Queda terminantemente prohibido generar declaraciones políticas falsas, estafas, difamación o contenidos denigrantes que vulneren el honor y la dignidad del artista (Ley N° 29733 de Protección de Datos Personales del Perú).\n\n" +
                                        "3. REGALÍAS Y REMUNERACIÓN\n" +
                                        "Cada locutor verificado recibe compensación económica periódica por cada lote de spots producidos con su identificador vocal.\n\n" +
                                        "4. SEGURIDAD DE SERVIDOR\n" +
                                        "Las llamadas a modelos de clonación neuronal se efectúan exclusivamente a través de endpoints de backend protegidos, sin exponer claves en el cliente.",
                                color = Color(0xFFD4CEE0),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showConsentDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = AndeanGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Entendido y Aceptado", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Modal: Register New Authorized Voice
    if (showRegisterDialog) {
        RegisterVoiceDialog(
            onDismiss = { showRegisterDialog = false },
            onRegister = { name, dni, whatsapp, tag, style ->
                coroutineScope.launch {
                    viewModel.repository.registerAuthorizedVoice(
                        artistName = name,
                        docIdentity = dni,
                        whatsapp = whatsapp,
                        voiceTag = tag,
                        regionalStyle = style
                    )
                    showRegisterDialog = false
                }
            }
        )
    }
}

@Composable
private fun VoiceItemCard(
    voice: AuthorizedVoiceEntity,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onUseInSpots: () -> Unit
) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2B1D3D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color(0xFFA06CD5),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = voice.voiceTag,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verificada",
                                tint = AndeanTurquoise,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "${voice.artistName} • ${voice.regionalStyle}",
                            color = Color(0xFFA8A3B4),
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF282433))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = voice.licenseCode,
                        color = AndeanGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onTogglePlay,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isPlaying) AndeanGold else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPlaying) "Pausar" else "Escuchar Muestra", fontSize = 11.sp)
                }

                Button(
                    onClick = onUseInSpots,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AndeanGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Radio, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Usar en Spots", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RegisterVoiceDialog(
    onDismiss: () -> Unit,
    onRegister: (artistName: String, docIdentity: String, whatsapp: String, voiceTag: String, regionalStyle: String) -> Unit
) {
    var artistName by remember { mutableStateOf("") }
    var docIdentity by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var voiceTag by remember { mutableStateOf("") }
    var regionalStyle by remember { mutableStateOf("Sierra Central / Huancayo") }
    var consentSigned by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA06CD5)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Registrar Nueva Voz Autorizada",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = artistName,
                    onValueChange = { artistName = it },
                    label = { Text("Nombre Completo del Artista / Locutor") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFA06CD5),
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = docIdentity,
                        onValueChange = { docIdentity = it },
                        label = { Text("DNI / Doc.") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFA06CD5),
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = { Text("WhatsApp") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFA06CD5),
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = voiceTag,
                    onValueChange = { voiceTag = it },
                    label = { Text("Nombre Artístico de la Voz (ej: Rosita del Mantaro)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFA06CD5),
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = regionalStyle,
                    onValueChange = { regionalStyle = it },
                    label = { Text("Región / Acento (ej: Ayacucho, Huancayo, Ancash)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFA06CD5),
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = consentSigned,
                        onCheckedChange = { consentSigned = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFA06CD5), checkmarkColor = Color.White)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Confirmo que el locutor firmó el acuerdo de cesión de derechos vocales para HuaynoStudio.",
                        color = Color(0xFFD4CEE0),
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (artistName.isNotBlank() && voiceTag.isNotBlank() && consentSigned) {
                            onRegister(artistName, docIdentity, whatsapp, voiceTag, regionalStyle)
                        }
                    },
                    enabled = artistName.isNotBlank() && voiceTag.isNotBlank() && consentSigned,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFA06CD5),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar y Generar Licencia", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
