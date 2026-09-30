package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.HuaynoSpotTemplate
import com.example.data.SpotTemplateCatalog
import com.example.ui.theme.AndeanGold
import com.example.ui.theme.AndeanTurquoise
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface
import com.example.viewmodel.EditorViewModel

@Composable
fun TemplatesDialog(
    viewModel: EditorViewModel,
    onDismiss: () -> Unit,
    onUseInSpotGenerator: (HuaynoSpotTemplate) -> Unit,
    onLoadInEditor: (HuaynoSpotTemplate) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Todos") }
    var playingTemplateId by remember { mutableStateOf<String?>(null) }

    val categories = remember {
        listOf("Todos", "Fiesta Patronal", "Aniversario", "Lanzamiento Primicia", "Zapateo / Huaylash", "Restaurante Campestre", "Comercial")
    }

    val filteredTemplates = remember(selectedCategory) {
        if (selectedCategory == "Todos") SpotTemplateCatalog.templates
        else SpotTemplateCatalog.templates.filter { it.category == selectedCategory }
    }

    Dialog(onDismissRequest = {
        viewModel.stopResourcePreview()
        onDismiss()
    }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AndeanGold.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(650.dp)
                .padding(4.dp)
                .testTag("templates_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Plantillas de Spots de Huayno",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Guiones y pistas listos para emitir por radio",
                            color = Color(0xFFA8A3B4),
                            fontSize = 11.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.stopResourcePreview()
                            onDismiss()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFFA8A3B4))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AndeanGold,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Templates List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredTemplates, key = { it.id }) { template ->
                        TemplateCardItem(
                            template = template,
                            isPlaying = playingTemplateId == template.id,
                            onTogglePlay = {
                                if (playingTemplateId == template.id) {
                                    playingTemplateId = null
                                    viewModel.stopResourcePreview()
                                } else {
                                    playingTemplateId = template.id
                                    viewModel.playFloatArrayPreview(template.generateMusicSamples()) {
                                        playingTemplateId = null
                                    }
                                }
                            },
                            onUseInGenerator = {
                                viewModel.stopResourcePreview()
                                onUseInSpotGenerator(template)
                                onDismiss()
                            },
                            onLoadInEditor = {
                                viewModel.stopResourcePreview()
                                onLoadInEditor(template)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateCardItem(
    template: HuaynoSpotTemplate,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onUseInGenerator: () -> Unit,
    onLoadInEditor: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16131F)),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Title & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${template.regionalStyle} • ${template.durationSec.toInt()}s • ${template.meter.label} (${template.recommendedBpm} BPM)",
                        color = AndeanGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AndeanTurquoise.copy(alpha = 0.2f))
                        .border(1.dp, AndeanTurquoise.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = template.tag,
                        color = AndeanTurquoise,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Script text preview
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1C2B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "«${template.scriptText}»",
                    color = Color(0xFFD4CEE0),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata: Voice & FX
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🎙️ Voz sugerida: ${template.recommendedVoice}",
                    color = Color(0xFFA8A3B4),
                    fontSize = 10.sp
                )
                Text(
                    text = "🎆 Intro: ${template.introFxDescription}",
                    color = Color(0xFFA8A3B4),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (isPlaying) AndeanGold else Color(0xFF282433))
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Escuchar Pista",
                        tint = if (isPlaying) Color.Black else AndeanGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                OutlinedButton(
                    onClick = onUseInGenerator,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AndeanGold),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AndeanGold.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Icon(Icons.Default.Radio, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Generador", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onLoadInEditor,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AndeanTurquoise,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Abrir en Editor", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
