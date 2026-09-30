package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DeviceSessionEntity
import com.example.data.UsageRecordEntity
import com.example.data.UserEntity
import com.example.data.UserRole
import com.example.ui.theme.AndeanGold
import com.example.ui.theme.AndeanRed
import com.example.ui.theme.AndeanTurquoise
import com.example.ui.theme.StudioBlack
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.viewmodel.EditorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminScreen(
    viewModel: EditorViewModel,
    onNavigateToEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allUsageRecords by viewModel.allUsageRecords.collectAsState()
    val allDeviceSessions by viewModel.allDeviceSessions.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Usuarios & Cuotas", "Costos por Usuario", "Dispositivos & Sesiones", "Claves & Seguridad")

    var showCreateDialog by remember { mutableStateOf(false) }
    var userToResetPassword by remember { mutableStateOf<String?>(null) }
    var newPasswordInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBlack)
            .padding(16.dp)
            .testTag("admin_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Panel de Administrador HuaynoStudio",
                    color = AndeanGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Control General, Costos & Dispositivos",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            if (selectedTabIndex == 0) {
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AndeanGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Crear Usuario", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = StudioSurfaceVariant,
            contentColor = AndeanGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = AndeanGold
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTabIndex == index) AndeanGold else Color(0xFFA8A3B4),
                            fontSize = 11.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // TAB CONTENT
        when (selectedTabIndex) {
            0 -> UsersTabContent(
                users = allUsers,
                onToggleActive = { username, status -> viewModel.toggleUserStatus(username, status) },
                onResetPassword = { username ->
                    userToResetPassword = username
                    newPasswordInput = ""
                },
                onDeleteUser = { username -> viewModel.deleteUser(username) },
                onUpdateMaxDevices = { username, maxDev -> viewModel.updateUserMaxDevices(username, maxDev) }
            )

            1 -> CostsTabContent(
                users = allUsers,
                usageRecords = allUsageRecords
            )

            2 -> DevicesTabContent(
                sessions = allDeviceSessions,
                onRevokeSession = { sessionId -> viewModel.revokeDeviceSession(sessionId) },
                onRevokeAllUserSessions = { username -> viewModel.revokeAllSessionsForUser(username) }
            )

            3 -> SecurityAndKeysTabContent()
        }
    }

    // Modal: Reset Password
    if (userToResetPassword != null) {
        Dialog(onDismissRequest = { userToResetPassword = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AndeanGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Restablecer Contraseña",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Para el usuario: ${userToResetPassword}",
                        color = AndeanGold,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        label = { Text("Nueva Contraseña") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AndeanGold,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { userToResetPassword = null }) {
                            Text("Cancelar", color = Color(0xFFA8A3B4))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newPasswordInput.length >= 4) {
                                    viewModel.resetPassword(userToResetPassword!!, newPasswordInput)
                                    userToResetPassword = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AndeanGold, contentColor = Color.Black)
                        ) {
                            Text("Guardar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal: Create User
    if (showCreateDialog) {
        CreateUserModal(
            onDismiss = { showCreateDialog = false },
            onCreate = { user, pass, name, wpp, role, unlim, creds, daily, month, maxDev ->
                viewModel.createUser(user, pass, name, wpp, role, unlim, creds, daily, month, maxDev) { success, _ ->
                    if (success) {
                        showCreateDialog = false
                    }
                }
            }
        )
    }
}

@Composable
private fun UsersTabContent(
    users: List<UserEntity>,
    onToggleActive: (String, Boolean) -> Unit,
    onResetPassword: (String) -> Unit,
    onDeleteUser: (String) -> Unit,
    onUpdateMaxDevices: (String, Int) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(users, key = { it.username }) { user ->
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (user.role == UserRole.ADMIN) AndeanGold.copy(alpha = 0.2f) else Color(0xFF282333)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (user.role == UserRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (user.role == UserRole.ADMIN) AndeanGold else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = user.fullName.ifEmpty { user.username },
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "@${user.username} • ${user.whatsapp.ifEmpty { "Sin WhatsApp" }}",
                                    color = Color(0xFFA8A3B4),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (user.isActive) AndeanTurquoise.copy(alpha = 0.2f) else AndeanRed.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (user.isActive) "ACTIVO" else "SUSPENDIDO",
                                color = if (user.isActive) AndeanTurquoise else AndeanRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Stats row: Credits, Daily limit, Max devices
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (user.isUnlimited) "Créditos: ∞ (Ilimitado)" else "Créditos: ${user.credits}",
                            color = AndeanGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tope diario: ${user.dailyLimit} ops",
                            color = Color(0xFFD4CEE0),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Máx Dispositivos: ${user.maxDevices}",
                            color = AndeanTurquoise,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Max devices toggle button (1 or 2)
                        OutlinedButton(
                            onClick = { onUpdateMaxDevices(user.username, if (user.maxDevices == 1) 2 else 1) },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Límite: ${user.maxDevices} disp.", fontSize = 10.sp)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(onClick = { onResetPassword(user.username) }, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.LockReset, contentDescription = "Reset Contraseña", tint = Color.White, modifier = Modifier.size(16.dp))
                        }

                        if (user.role != UserRole.ADMIN) {
                            IconButton(onClick = { onToggleActive(user.username, user.isActive) }, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    imageVector = if (user.isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                                    contentDescription = if (user.isActive) "Suspender" else "Reactivar",
                                    tint = if (user.isActive) AndeanRed else AndeanTurquoise,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(onClick = { onDeleteUser(user.username) }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFA8A3B4), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CostsTabContent(
    users: List<UserEntity>,
    usageRecords: List<UsageRecordEntity>
) {
    val totalCredits = usageRecords.sumOf { it.costCredits }
    val totalCostUsd = usageRecords.sumOf { it.estimatedCostUsd }
    val totalCostPen = usageRecords.sumOf { it.estimatedCostPen }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Gasto Total Estimado", color = Color(0xFFA8A3B4), fontSize = 10.sp)
                    Text("$${String.format("%.3f", totalCostUsd)} USD", color = AndeanGold, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("~S/ ${String.format("%.2f", totalCostPen)} Soles", color = AndeanTurquoise, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Créditos Consumidos", color = Color(0xFFA8A3B4), fontSize = 10.sp)
                    Text("$totalCredits créditos", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("${usageRecords.size} operaciones registradas", color = Color(0xFFA8A3B4), fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tariff matrix per service
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13101A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Tarifario y Costos por Servicio",
                    color = AndeanGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Guiones Gemini IA:", color = Color.White, fontSize = 11.sp)
                    Text("1 crédito (~$0.002 USD / S/ 0.008)", color = Color(0xFFA8A3B4), fontSize = 11.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Locución Sintética Andina:", color = Color.White, fontSize = 11.sp)
                    Text("2 créditos (~$0.010 USD / S/ 0.038)", color = Color(0xFFA8A3B4), fontSize = 11.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Separador Stems Demucs IA:", color = Color.White, fontSize = 11.sp)
                    Text("3 créditos (~$0.025 USD / S/ 0.095)", color = Color(0xFFA8A3B4), fontSize = 11.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Exportación WAV Broadcast:", color = Color.White, fontSize = 11.sp)
                    Text("1 crédito (~$0.005 USD / S/ 0.019)", color = Color(0xFFA8A3B4), fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Desglose Financiero por Usuario",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Table per user
        users.forEach { user ->
            val userRecords = usageRecords.filter { it.username == user.username }
            val userCredits = userRecords.sumOf { it.costCredits }
            val userUsd = userRecords.sumOf { it.estimatedCostUsd }
            val userPen = userRecords.sumOf { it.estimatedCostPen }

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = StudioSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.fullName.ifEmpty { user.username },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@${user.username} • ${userRecords.size} procesos ejecutados",
                            color = Color(0xFFA8A3B4),
                            fontSize = 10.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$userCredits créditos",
                            color = AndeanGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$${String.format("%.3f", userUsd)} USD • S/ ${String.format("%.2f", userPen)}",
                            color = AndeanTurquoise,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DevicesTabContent(
    sessions: List<DeviceSessionEntity>,
    onRevokeSession: (String) -> Unit,
    onRevokeAllUserSessions: (String) -> Unit
) {
    val activeSessions = sessions.filter { it.isActive }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = AndeanGold, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Control Estricto de Dispositivos por Cuenta",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Impide el uso simultáneo en más dispositivos de los contratados. Los usuarios excedentes son bloqueados al iniciar sesión.",
                        color = Color(0xFFA8A3B4),
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Sesiones de Dispositivos Activas (${activeSessions.size})",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (activeSessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay sesiones de dispositivos registradas.", color = Color(0xFFA8A3B4), fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(activeSessions, key = { it.id }) { session ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16131F)),
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
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(AndeanTurquoise.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = AndeanTurquoise, modifier = Modifier.size(18.dp))
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "${session.deviceName} (@${session.username})",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Conectado: ${dateFormat.format(Date(session.loginTimestamp))} • ${session.ipOrLocation}",
                                        color = Color(0xFFA8A3B4),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { onRevokeSession(session.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = AndeanRed, contentColor = Color.White),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Revocar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityAndKeysTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, AndeanGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = AndeanGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Variables Secretas de Servidor Requeridas",
                        color = AndeanGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Para garantizar la seguridad y que las claves no sean extraídas de ningún APK, todas las llamadas a IA deben efectuarse vía backend o Secrets Panel en AI Studio.",
                    color = Color(0xFFD4CEE0),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Checklist of services
        val services = listOf(
            Triple(
                "GEMINI_API_KEY",
                "Google AI Studio (aistudio.google.com)",
                "Necesaria para la generación de guiones de spots con fonética Quechua (Módulo A). Se inyecta de forma segura a través de Secrets Panel."
            ),
            Triple(
                "REPLICATE_API_TOKEN",
                "Replicate Cloud (replicate.com)",
                "Necesaria para el procesamiento del modelo Demucs de separación de voz y pistas instrumentales en GPU (Módulo B)."
            ),
            Triple(
                "ELEVENLABS_API_KEY",
                "ElevenLabs (elevenlabs.io)",
                "Necesaria para la clonación y síntesis de voces andinas autorizadas de alta fidelidad (Módulo D)."
            ),
            Triple(
                "FIREBASE_PROJECT_ID",
                "Firebase Console (firebase.google.com)",
                "Autenticación con usuario/contraseña, Firestore para guardado de proyectos y Storage para audios."
            )
        )

        services.forEach { (keyName, provider, desc) ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF15121E)),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = keyName,
                            color = AndeanTurquoise,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF282433))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(provider, color = Color(0xFFA8A3B4), fontSize = 9.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = desc,
                        color = Color(0xFFC0BACB),
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateUserModal(
    onDismiss: () -> Unit,
    onCreate: (username: String, tempPass: String, fullName: String, whatsapp: String, role: UserRole, isUnlimited: Boolean, credits: Int, dailyLimit: Int, monthlyLimit: Int, maxDevices: Int) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var tempPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var isUnlimited by remember { mutableStateOf(false) }
    var credits by remember { mutableIntStateOf(50) }
    var dailyLimit by remember { mutableIntStateOf(20) }
    var monthlyLimit by remember { mutableIntStateOf(300) }
    var maxDevices by remember { mutableIntStateOf(1) }
    var role by remember { mutableStateOf(UserRole.USER) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
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
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Crear Nuevo Usuario",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Usuario (ej. radio_andina)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AndeanGold,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tempPassword,
                    onValueChange = { tempPassword = it },
                    label = { Text("Contraseña temporal") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AndeanGold,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nombre Completo o Agrupación") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AndeanGold,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("WhatsApp de Contacto") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AndeanGold,
                        unfocusedBorderColor = StudioBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Max devices selection (1, 2, 3)
                Text(
                    text = "Límite de Dispositivos Permitidos: $maxDevices",
                    color = AndeanTurquoise,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = maxDevices.toFloat(),
                    onValueChange = { maxDevices = it.toInt() },
                    valueRange = 1f..3f,
                    steps = 1,
                    colors = SliderDefaults.colors(thumbColor = AndeanTurquoise, activeTrackColor = AndeanTurquoise)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Unlimited toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Créditos Ilimitados (∞)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Respeta tope diario de seguridad", color = Color(0xFFA8A3B4), fontSize = 10.sp)
                    }
                    Switch(
                        checked = isUnlimited,
                        onCheckedChange = { isUnlimited = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AndeanGold, checkedTrackColor = Color(0xFF5A4400))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Tope diario: $dailyLimit procesos/día", color = Color(0xFFD4CEE0), fontSize = 11.sp)
                Slider(
                    value = dailyLimit.toFloat(),
                    onValueChange = { dailyLimit = it.toInt() },
                    valueRange = 5f..100f,
                    colors = SliderDefaults.colors(thumbColor = AndeanGold, activeTrackColor = AndeanGold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color(0xFFA8A3B4))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onCreate(username, tempPassword, fullName, whatsapp, role, isUnlimited, credits, dailyLimit, monthlyLimit, maxDevices)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AndeanGold, contentColor = Color.Black)
                    ) {
                        Text("Crear Usuario", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
