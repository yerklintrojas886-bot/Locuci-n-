package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AndeanGold
import com.example.ui.theme.AndeanRed
import com.example.ui.theme.AndeanTurquoise
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface

data class TutorialStep(
    val stepNumber: Int,
    val title: String,
    val moduleTag: String,
    val icon: ImageVector,
    val color: Color,
    val summary: String,
    val bullets: List<String>
)

@Composable
fun TutorialDialog(
    onDismiss: () -> Unit
) {
    val steps = remember {
        listOf(
            TutorialStep(
                stepNumber = 1,
                title = "Generador de Spots con IA",
                moduleTag = "MÓDULO A",
                icon = Icons.Default.AutoAwesome,
                color = AndeanGold,
                summary = "Crea guiones radiales persuasivos para fiestas patronales, aniversarios y conciertos andinos en segundos con Gemini.",
                bullets = listOf(
                    "Diccionario Fonético Quechua: Optimiza palabras como 'Vilcashuamán', 'Huancayo' o 'Kachkaniraqmi'.",
                    "Voces Sintéticas Base: Ajuste de velocidad (0.8x a 1.4x) y emociones.",
                    "Mezcla con Ducking Automático a -14 dB: La música baja sola cuando entra la voz."
                )
            ),
            TutorialStep(
                stepNumber = 2,
                title = "Separador de Voz y Pistas Stems",
                moduleTag = "MÓDULO B",
                icon = Icons.Default.CallSplit,
                color = AndeanRed,
                summary = "Aísla la voz del cantante o extrae la pista instrumental limpia para crear spots sobre temas populares.",
                bullets = listOf(
                    "Separación 2 y 4 Pistas: Voz, Arpa/Melodía, Bajo/Bordoneo y Percusión.",
                    "Procesamiento Seguro: Cumple con políticas de propiedad intelectual con declaración jurada.",
                    "Integración al Editor: Envía los stems separados directamente a pistas independientes."
                )
            ),
            TutorialStep(
                stepNumber = 3,
                title = "Estudio Multipista & Metrónomo Huayno",
                moduleTag = "MÓDULO C",
                icon = Icons.Default.GraphicEq,
                color = AndeanTurquoise,
                summary = "Consola de grabación y edición multipista con soporte para compases típicos del huayno.",
                bullets = listOf(
                    "Metrónomo Huayno 2/4 y 4/4: Tiempos típicos con timbres de Wankar, Cencerro Huaylash y Beep.",
                    "Pre-cuenta (Count-In): Claves de 1 o 2 compases previos para que el cantante entre a tiempo.",
                    "Edición Profesional: Corte de clips, fundidos (fade in/out), paneo y ecualización."
                )
            ),
            TutorialStep(
                stepNumber = 4,
                title = "Voces Autorizadas y Ética",
                moduleTag = "MÓDULO D",
                icon = Icons.Default.RecordVoiceOver,
                color = Color(0xFFA06CD5),
                summary = "Utiliza voces andinas autorizadas con verificación de consentimiento y licencia biométrica.",
                bullets = listOf(
                    "Catálogo de Locutores Oficiales: Don Floro, Rosita de Huaraz y animadores del Centro.",
                    "Contrato y Consentimiento: Registro legal del artista con DNI y WhatsApp.",
                    "Seguridad de Servidor: Claves encriptadas y protegidas contra clonación no autorizada."
                )
            ),
            TutorialStep(
                stepNumber = 5,
                title = "Biblioteca Andina & Plantillas Listas",
                moduleTag = "MÓDULO E",
                icon = Icons.Default.LibraryMusic,
                color = AndeanGold,
                summary = "Plantillas listas para usar y más de 16 recursos sonoros auténticos para dar vida a tus spots.",
                bullets = listOf(
                    "Plantillas Completas: Santiago de Huancayo, Concierto con Arpa, Zapateo y Comerciales.",
                    "Recursos de Fiesta: Cohetes andinos, clarines, aplausos de coliseo y bordoneos de arpa.",
                    "Exportación Broadcast: Descarga o comparte tus mezclas en WAV estéreo 44.1 kHz sin pérdida."
                )
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentStepIndex]

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AndeanGold.copy(alpha = 0.7f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("tutorial_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Indicator Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(currentStep.color.copy(alpha = 0.2f))
                            .border(1.dp, currentStep.color, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = currentStep.moduleTag,
                            color = currentStep.color,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Paso ${currentStepIndex + 1} de ${steps.size}",
                        color = Color(0xFFA8A3B4),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tutorial_content"
                ) { step ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(step.color.copy(alpha = 0.15f))
                                .border(1.5.dp, step.color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = step.icon,
                                contentDescription = null,
                                tint = step.color,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = step.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = step.summary,
                            color = Color(0xFFD4CEE0),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF16131F)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                step.bullets.forEach { bullet ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = step.color,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = bullet,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Dots indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    steps.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (index == currentStepIndex) 20.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (index == currentStepIndex) AndeanGold else Color(0xFF383247))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStepIndex > 0) {
                        OutlinedButton(
                            onClick = { currentStepIndex-- },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Anterior", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Anterior", fontSize = 12.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (currentStepIndex < steps.size - 1) {
                        Button(
                            onClick = { currentStepIndex++ },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AndeanGold,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Siguiente", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Siguiente", modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AndeanTurquoise,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.testTag("tutorial_finish_btn")
                        ) {
                            Text("¡Comenzar a Crear Spots!", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
