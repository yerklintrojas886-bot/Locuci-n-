package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserRole
import com.example.ui.theme.AndeanGold
import com.example.ui.theme.StudioBlack
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface
import com.example.viewmodel.EditorViewModel

enum class StudioTab(val title: String, val icon: ImageVector) {
    HOME("Inicio", Icons.Default.Home),
    SPOTS("Spots", Icons.Default.Radio),
    SEPARATOR("Separar", Icons.Default.CallSplit),
    EDITOR("Editor", Icons.Default.GraphicEq),
    RESOURCES("Sonoteca", Icons.Default.LibraryMusic),
    VOICES("Voces", Icons.Default.Mic),
    ADMIN("Admin", Icons.Default.AdminPanelSettings)
}

@Composable
fun MainScreen(
    editorViewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by editorViewModel.currentUser.collectAsState()

    // Si no hay usuario logueado, mostrar pantalla de inicio de sesión obligatoria
    if (currentUser == null) {
        LoginScreen(
            onLogin = { user, pass, forceDisconnect, onError ->
                editorViewModel.login(user, pass, forceDisconnect) { success, err ->
                    if (!success && err != null) {
                        onError(err)
                    }
                }
            },
            modifier = modifier
        )
        return
    }

    var currentTab by rememberSaveable { mutableStateOf(StudioTab.HOME) }

    // Filtrar pestañas: la pestaña Admin solo se muestra si el rol es ADMIN
    val visibleTabs = if (currentUser?.role == UserRole.ADMIN) {
        StudioTab.entries
    } else {
        StudioTab.entries.filter { it != StudioTab.ADMIN }
    }

    // Android back handler
    BackHandler(enabled = currentTab != StudioTab.HOME) {
        currentTab = StudioTab.HOME
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = StudioSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(0.5.dp, StudioBorder)
                    .height(68.dp)
            ) {
                visibleTabs.forEach { tab ->
                    val isSelected = currentTab == tab
                    val isEditor = tab == StudioTab.EDITOR

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(if (isEditor) 24.dp else 20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = if (isEditor) 11.sp else 9.sp,
                                fontWeight = if (isSelected || isEditor) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isEditor) Color.Black else AndeanGold,
                            selectedTextColor = AndeanGold,
                            indicatorColor = if (isEditor) AndeanGold else Color(0xFF382F19),
                            unselectedIconColor = if (isEditor) AndeanGold.copy(alpha = 0.8f) else Color(0xFF868194),
                            unselectedTextColor = if (isEditor) AndeanGold.copy(alpha = 0.8f) else Color(0xFF868194)
                        )
                    )
                }
            }
        },
        containerColor = StudioBlack,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                StudioTab.HOME -> HomeScreen(
                    viewModel = editorViewModel,
                    onNavigateToEditor = { currentTab = StudioTab.EDITOR },
                    onNavigateToSpots = { currentTab = StudioTab.SPOTS },
                    onNavigateToSeparator = { currentTab = StudioTab.SEPARATOR },
                    onNavigateToVoices = { currentTab = StudioTab.VOICES },
                    onNavigateToResources = { currentTab = StudioTab.RESOURCES }
                )
                StudioTab.SPOTS -> SpotsScreen(
                    viewModel = editorViewModel,
                    onNavigateToEditor = { currentTab = StudioTab.EDITOR }
                )
                StudioTab.SEPARATOR -> SeparatorScreen(
                    viewModel = editorViewModel,
                    onNavigateToEditor = { currentTab = StudioTab.EDITOR }
                )
                StudioTab.EDITOR -> EditorScreen(
                    viewModel = editorViewModel
                )
                StudioTab.RESOURCES -> ResourcesScreen(
                    viewModel = editorViewModel,
                    onNavigateToEditor = { currentTab = StudioTab.EDITOR }
                )
                StudioTab.VOICES -> VoicesScreen(
                    viewModel = editorViewModel,
                    onNavigateToSpots = { currentTab = StudioTab.SPOTS },
                    onNavigateToEditor = { currentTab = StudioTab.EDITOR }
                )
                StudioTab.ADMIN -> AdminScreen(
                    viewModel = editorViewModel,
                    onNavigateToEditor = { currentTab = StudioTab.EDITOR }
                )
            }
        }
    }
}
