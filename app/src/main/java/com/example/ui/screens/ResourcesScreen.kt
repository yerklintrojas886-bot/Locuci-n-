package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.AndeanAudioResource
import com.example.data.AndeanResourceCatalog
import com.example.data.ResourceCategory
import com.example.ui.theme.AndeanGold
import com.example.ui.theme.AndeanRed
import com.example.ui.theme.AndeanTurquoise
import com.example.ui.theme.StudioBlack
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.viewmodel.EditorViewModel

@Composable
fun ResourcesScreen(
    viewModel: EditorViewModel,
    onNavigateToEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ResourceCategory?>(null) }
    var currentlyPlayingId by remember { mutableStateOf<String?>(null) }

    val allResources = remember { AndeanResourceCatalog.resources }

    val filteredResources = remember(searchQuery, selectedCategory) {
        allResources.filter { res ->
            val matchesCategory = selectedCategory == null || res.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    res.name.contains(searchQuery, ignoreCase = true) ||
                    res.regionalOrigin.contains(searchQuery, ignoreCase = true) ||
                    res.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBlack)
            .padding(16.dp)
            .testTag("resources_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Módulo E • Biblioteca de Recursos",
                    color = AndeanGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sonoteca Andina de Huayno",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Button(
                onClick = onNavigateToEditor,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AndeanGold,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ir al Editor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar arpa, cohete, zapateo, clarín o frase...", color = Color(0xFF7E798B), fontSize = 12.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = AndeanGold, modifier = Modifier.size(18.dp))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AndeanGold,
                unfocusedBorderColor = StudioBorder,
                focusedContainerColor = StudioSurface,
                unfocusedContainerColor = StudioSurface,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("Todos (${allResources.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AndeanGold,
                        selectedLabelColor = Color.Black
                    )
                )
            }
            items(ResourceCategory.entries) { cat ->
                val count = allResources.count { it.category == cat }
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                    label = { Text("${cat.displayName} ($count)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AndeanTurquoise,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Resources list
        Text(
            text = "Recursos Disponibles (${filteredResources.size})",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredResources, key = { it.id }) { resource ->
                ResourceItemCard(
                    resource = resource,
                    isPlaying = currentlyPlayingId == resource.id,
                    onTogglePlay = {
                        if (currentlyPlayingId == resource.id) {
                            currentlyPlayingId = null
                            viewModel.stopResourcePreview()
                        } else {
                            currentlyPlayingId = resource.id
                            viewModel.playResourcePreview(resource) {
                                currentlyPlayingId = null
                            }
                        }
                    },
                    onInsertInEditor = {
                        viewModel.insertResourceSample(resource)
                    }
                )
            }
        }
    }
}

@Composable
private fun ResourceItemCard(
    resource: AndeanAudioResource,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onInsertInEditor: () -> Unit
) {
    val categoryColor = when (resource.category) {
        ResourceCategory.MUSIC_STEMS -> AndeanTurquoise
        ResourceCategory.SOUND_EFFECTS -> AndeanRed
        ResourceCategory.ANIMATOR_CHEERS -> AndeanGold
        ResourceCategory.HUAYNO_PERCUSSION -> Color(0xFFA06CD5)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play/Pause button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) categoryColor else Color(0xFF282333))
                    .border(1.dp, categoryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Escuchar",
                        tint = if (isPlaying) Color.Black else categoryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Resource Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = resource.name,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(categoryColor.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = resource.regionalOrigin,
                            color = categoryColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = resource.description,
                    color = Color(0xFFA8A3B4),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )

                Text(
                    text = "Duración: ${resource.durationSec}s • 44.1 kHz Estéreo",
                    color = Color(0xFF7E798B),
                    fontSize = 9.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Insert in Editor Button
            OutlinedButton(
                onClick = onInsertInEditor,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AndeanGold),
                border = androidx.compose.foundation.BorderStroke(1.dp, AndeanGold.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("Insertar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
