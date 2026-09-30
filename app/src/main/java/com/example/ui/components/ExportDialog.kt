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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AndeanGold
import com.example.ui.theme.AndeanTurquoise
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface
import java.io.File

@Composable
fun ExportDialog(
    isExporting: Boolean,
    lastExportedFile: File?,
    defaultTitle: String,
    onDismiss: () -> Unit,
    onExport: (fileName: String) -> Unit,
    onShare: () -> Unit
) {
    var fileName by remember {
        mutableStateOf(
            defaultTitle.replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Radio,
                    contentDescription = null,
                    tint = AndeanGold,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Exportar Mezcla Final",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Audio listo para emisión en Radio FM, parlantes y WhatsApp",
                    color = Color(0xFFA8A3B4),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (lastExportedFile != null && !isExporting) {
                    // Success View
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AndeanTurquoise.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .border(1.dp, AndeanTurquoise.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AndeanTurquoise,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "¡Mezcla renderizada con éxito!",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${lastExportedFile.name} • ${(lastExportedFile.length() / 1024)} KB",
                                    color = Color(0xFFD4CEE0),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onShare,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AndeanGold, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compartir Audio (WhatsApp / Guardar)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2836))
                    ) {
                        Text("Cerrar", color = Color.White)
                    }
                } else {
                    // Input & Export Form
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("Nombre del archivo") },
                        suffix = { Text(".wav", color = Color(0xFFA8A3B4)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AndeanGold,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Audio Specs Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Formato: WAV Broadcast",
                            color = Color(0xFFA8A3B4),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "44.1 kHz • 16-Bit Stereo",
                            color = AndeanTurquoise,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (isExporting) {
                        CircularProgressIndicator(color = AndeanGold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Renderizando pistas y aplicando ducking...",
                            color = Color(0xFFD4CEE0),
                            fontSize = 12.sp
                        )
                    } else {
                        Button(
                            onClick = { onExport(fileName) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AndeanGold,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Exportar", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Renderizar y Exportar WAV", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF232029))
                        ) {
                            Text("Cancelar", color = Color(0xFFA8A3B4))
                        }
                    }
                }
            }
        }
    }
}
