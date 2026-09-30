package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.UserRole
import com.example.ui.components.TemplatesDialog
import com.example.ui.components.TutorialDialog
import com.example.ui.theme.AndeanGold
import com.example.ui.theme.AndeanRed
import com.example.ui.theme.AndeanTurquoise
import com.example.ui.theme.StudioBlack
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.viewmodel.EditorViewModel

@Composable
fun HomeScreen(
    viewModel: EditorViewModel,
    onNavigateToEditor: () -> Unit,
    onNavigateToSpots: () -> Unit,
    onNavigateToSeparator: () -> Unit,
    onNavigateToVoices: () -> Unit,
    onNavigateToResources: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val userProjects by viewModel.userProjects.collectAsState()

    var showTutorialDialog by remember { mutableStateOf(false) }
    var showTemplatesDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBlack)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag("home_screen")
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AndeanTurquoise)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentUser?.role == UserRole.ADMIN) "ROL: ADMINISTRADOR" else "ESTUDIO ACTIVO",
                        color = AndeanTurquoise,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = currentUser?.fullName ?: "HuaynoStudio",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "@${currentUser?.username ?: "usuario"}",
                    color = Color(0xFFA8A3B4),
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Tutorial / Help shortcut
                IconButton(onClick = { showTutorialDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Tutorial",
                        tint = AndeanGold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Credits Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AndeanGold.copy(alpha = 0.15f))
                        .border(1.dp, AndeanGold, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Créditos: ",
                            color = Color(0xFFD4CEE0),
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (currentUser?.isUnlimited == true) "∞" else "${currentUser?.credits ?: 0}",
                            color = AndeanGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Logout button
                IconButton(onClick = { viewModel.logout() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Cerrar sesión",
                        tint = Color(0xFFA8A3B4)
                    )
                }
            }
        }

        // HERO BANNER
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_huayno_banner),
                    contentDescription = "HuaynoStudio Hero Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    StudioBlack.copy(alpha = 0.90f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(AndeanGold, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ESTUDIO MULTIPISTA & GENERADOR DE SPOTS",
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "HuaynoStudio Profesional 2026",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Spots con Gemini, Separador Demucs, Voces Autorizadas y Sonoteca Andina",
                        color = Color(0xFFD4CEE0),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // PRIMARY ACTION BUTTON: OPEN MULTITRACK EDITOR
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            Button(
                onClick = onNavigateToEditor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AndeanGold,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ABRIR ESTUDIO MULTIPISTA",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // QUICK ACTIONS: PLANTILLAS & TUTORIAL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { showTemplatesDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AndeanTurquoise),
                border = androidx.compose.foundation.BorderStroke(1.dp, AndeanTurquoise.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("open_templates_btn")
            ) {
                Icon(Icons.Default.Radio, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Plantillas de Spots", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { showTutorialDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AndeanGold),
                border = androidx.compose.foundation.BorderStroke(1.dp, AndeanGold.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("open_tutorial_btn")
            ) {
                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tutorial de Uso", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // USER'S SAVED PROJECTS
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = AndeanGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mis Proyectos Guardados (${userProjects.size})",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.saveCurrentProject() },
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AndeanGold)
                    ) {
                        Text("Guardar Actual", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (userProjects.isEmpty()) {
                    Text(
                        text = "Aún no tienes proyectos guardados. Abre el editor y pulsa 'Guardar Actual'.",
                        color = Color(0xFFA8A3B4),
                        fontSize = 11.sp
                    )
                } else {
                    userProjects.forEach { proj ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = proj.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "${proj.regionalStyle} • ${proj.durationSec}s", color = Color(0xFFA8A3B4), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MODULES SHORTCUTS SECTION
        Text(
            text = "Módulos de HuaynoStudio",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ModuleCard(
                title = "Editor Multipista (Módulo C)",
                subtitle = "Mezclador, metrónomo Huayno (2/4 y 4/4), pre-cuenta vocal, ducking y exportación WAV",
                badge = "100% OPERATIVO",
                badgeColor = AndeanTurquoise,
                icon = Icons.Default.GraphicEq,
                iconTint = AndeanTurquoise,
                onClick = onNavigateToEditor
            )

            ModuleCard(
                title = "Generador de Spots (Módulo A)",
                subtitle = "Guiones con Gemini, fonética quechua, locución andina y ducking",
                badge = "OPERATIVO • IA",
                badgeColor = AndeanGold,
                icon = Icons.Default.Radio,
                iconTint = AndeanGold,
                onClick = onNavigateToSpots
            )

            ModuleCard(
                title = "Separador de Voz y Pistas (Módulo B)",
                subtitle = "Aísla la voz o extrae la pista instrumental limpia con IA Demucs",
                badge = "ACTIVO • IA",
                badgeColor = AndeanRed,
                icon = Icons.Default.CallSplit,
                iconTint = AndeanRed,
                onClick = onNavigateToSeparator
            )

            ModuleCard(
                title = "Voces Autorizadas & Ética (Módulo D)",
                subtitle = "Locutores de huayno oficiales con licencia, contrato y consentimiento",
                badge = "ACTIVO • VERIFICADO",
                badgeColor = Color(0xFFA06CD5),
                icon = Icons.Default.Mic,
                iconTint = Color(0xFFA06CD5),
                onClick = onNavigateToVoices
            )

            ModuleCard(
                title = "Biblioteca de Recursos Andinos (Módulo E)",
                subtitle = "Sonoteca: arpas, cohetes, aplausos, clarines, zapateos y gritos de locución",
                badge = "ACTIVO • 16+ MUESTRAS",
                badgeColor = AndeanTurquoise,
                icon = Icons.Default.LibraryMusic,
                iconTint = AndeanTurquoise,
                onClick = onNavigateToResources
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // CONSUMPTION & LIMITS
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Consumo & Límites de la Cuenta",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (currentUser?.role == UserRole.ADMIN) "Admin (Ilimitado)" else "Usuario Estándar",
                        color = AndeanGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val dailyLimit = currentUser?.dailyLimit ?: 50
                QuotaRow(label = "Procesos y renders diarios permitidos", current = 1, max = dailyLimit)

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Límite de dispositivos contratados: ${currentUser?.maxDevices ?: 1} activo(s)",
                    color = AndeanTurquoise,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // Modal: Tutorial de Primer Uso
    if (showTutorialDialog) {
        TutorialDialog(onDismiss = { showTutorialDialog = false })
    }

    // Modal: Plantillas de Spots
    if (showTemplatesDialog) {
        TemplatesDialog(
            viewModel = viewModel,
            onDismiss = { showTemplatesDialog = false },
            onUseInSpotGenerator = { template ->
                onNavigateToSpots()
            },
            onLoadInEditor = { template ->
                viewModel.loadSpotTemplateIntoEditor(template)
                onNavigateToEditor()
            }
        )
    }
}

@Composable
private fun ModuleCard(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f))
                    .border(1.dp, iconTint.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .border(0.5.dp, badgeColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            color = badgeColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFFA8A3B4),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun QuotaRow(label: String, current: Int, max: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color(0xFFD4CEE0), fontSize = 11.sp)
            Text(
                text = "$current / $max hoy",
                color = AndeanGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (current.toFloat() / max.toFloat()).coerceIn(0f, 1f) },
            color = AndeanGold,
            trackColor = Color(0xFF282333),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}
