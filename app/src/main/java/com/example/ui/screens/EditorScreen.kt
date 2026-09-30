package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.MetronomeConfig
import com.example.audio.TrackType
import com.example.ui.components.ClipEditorDialog
import com.example.ui.components.EffectsBottomSheet
import com.example.ui.components.ExportDialog
import com.example.ui.components.MetronomeBottomSheet
import com.example.ui.components.TimeRuler
import com.example.ui.components.TrackHeaderView
import com.example.ui.components.TrackLaneView
import com.example.ui.components.VUMeterView
import com.example.ui.theme.AndeanGold
import com.example.ui.theme.AndeanRed
import com.example.ui.theme.AndeanTurquoise
import com.example.ui.theme.StudioBlack
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.viewmodel.EditorViewModel
import kotlin.math.roundToInt

@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val project by viewModel.project.collectAsState()
    val currentPosition by viewModel.currentPositionSec.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isLooping by viewModel.isLooping.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val peakLeft by viewModel.peakLeft.collectAsState()
    val peakRight by viewModel.peakRight.collectAsState()
    val zoomPixelsPerSec by viewModel.zoomPixelsPerSec.collectAsState()
    val snapGridSec by viewModel.snapGridSec.collectAsState()
    val selectedClipId by viewModel.selectedClipId.collectAsState()
    val selectedTrackId by viewModel.selectedTrackId.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val lastExportedFile by viewModel.lastExportedFile.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val activeClip = project.tracks
        .flatMap { it.clips }
        .find { it.id == selectedClipId }

    val snackbarHostState = remember { SnackbarHostState() }

    var showEffectsSheet by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showClipEditorDialog by remember { mutableStateOf(false) }
    var showMetronomeSheet by remember { mutableStateOf(false) }

    val isMetronomeActive by viewModel.isMetronomeActive.collectAsState()
    val currentMetronomeBeat by viewModel.currentMetronomeBeat.collectAsState()
    val isMetronomeAccent by viewModel.isMetronomeAccent.collectAsState()
    val isCountInActive by viewModel.isCountInActive.collectAsState()
    val countInBeat by viewModel.countInBeat.collectAsState()
    val countInTotal by viewModel.countInTotal.collectAsState()

    // Audio recording permission launcher
    val recordPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleRecording(context)
        }
    }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    val timelineScrollState = rememberScrollState()
    val tracksVerticalScrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = StudioBlack,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // TOP STUDIO CONSOLE BAR
            TopConsoleBar(
                projectTitle = project.title,
                currentSec = currentPosition,
                totalSec = project.durationSec,
                peakLeft = peakLeft,
                peakRight = peakRight,
                canUndo = canUndo,
                canRedo = canRedo,
                isMetronomeActive = isMetronomeActive,
                meterLabel = project.metronome.meter.label,
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onSave = { viewModel.saveCurrentProject() },
                onOpenMetronome = { showMetronomeSheet = true },
                onOpenEffects = { showEffectsSheet = true },
                onOpenExport = { showExportDialog = true }
            )

            // TRANSPORT BAR
            TransportBar(
                isPlaying = isPlaying,
                isLooping = isLooping,
                isRecording = isRecording,
                snapGrid = snapGridSec,
                zoom = zoomPixelsPerSec,
                metronomeConfig = project.metronome,
                isMetronomeActive = isMetronomeActive,
                isMetronomeAccent = isMetronomeAccent,
                onPlayPause = { viewModel.togglePlayPause() },
                onStop = { viewModel.stopPlayback() },
                onLoopToggle = { viewModel.toggleLoop() },
                onRecordClick = {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) {
                        viewModel.toggleRecording(context)
                    } else {
                        recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onSplit = { viewModel.splitClipAtPlayhead() },
                onZoomChange = { viewModel.setZoom(it) },
                onSnapChange = { viewModel.setSnapGrid(it) },
                onAddTrack = { viewModel.addTrack() },
                onReloadHuaynoSamples = { viewModel.loadDefaultHuaynoStems() },
                onOpenMetronome = { showMetronomeSheet = true },
                onToggleMetronome = { viewModel.toggleMetronome() }
            )

            // COUNT-IN BANNER FOR VOCAL RECORDING
            AnimatedVisibility(visible = isCountInActive) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AndeanRed),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("count_in_active_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$countInBeat",
                                    color = AndeanRed,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column {
                                Text(
                                    text = "🎙️ PRE-CUENTA HUAYNO (${project.metronome.meter.label} • ${project.metronome.bpm} BPM)",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tiempo $countInBeat de $countInTotal • ¡Prepárate para cantar con el compás!",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Text(
                            text = "¡A CANTAR!",
                            color = AndeanGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // TIMELINE & TRACKS WORKSPACE
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(StudioBlack)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // LEFT COLUMN: Fixed Track Headers
                    Column(
                        modifier = Modifier
                            .width(170.dp)
                            .fillMaxHeight()
                            .background(StudioSurface)
                    ) {
                        // Corner spacer aligned with time ruler
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .background(StudioSurfaceVariant)
                                .border(0.5.dp, StudioBorder),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "PISTAS (${project.tracks.size})",
                                color = AndeanGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }

                        // Track Headers list
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(tracksVerticalScrollState)
                        ) {
                            project.tracks.forEach { track ->
                                TrackHeaderView(
                                    track = track,
                                    onMuteToggle = { viewModel.toggleMuteTrack(track.id) },
                                    onSoloToggle = { viewModel.toggleSoloTrack(track.id) },
                                    onVolumeChange = { viewModel.setTrackVolume(track.id, it) },
                                    onPanChange = { viewModel.setTrackPan(track.id, it) },
                                    onDeleteTrack = { viewModel.removeTrack(track.id) }
                                )
                            }
                        }
                    }

                    // RIGHT COLUMN: Horizontally Scrollable Timeline
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .horizontalScroll(timelineScrollState)
                    ) {
                        val timelineWidthDp = (project.durationSec * zoomPixelsPerSec).dp
                        val density = LocalDensity.current

                        Column(modifier = Modifier.width(timelineWidthDp)) {
                            // Time Ruler
                            TimeRuler(
                                totalDurationSec = project.durationSec,
                                pixelsPerSec = zoomPixelsPerSec,
                                currentPlayheadSec = currentPosition,
                                onSeek = { viewModel.seekTo(it) }
                            )

                            // Track Lanes
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(tracksVerticalScrollState)
                            ) {
                                project.tracks.forEach { track ->
                                    TrackLaneView(
                                        track = track,
                                        pixelsPerSec = zoomPixelsPerSec,
                                        totalDurationSec = project.durationSec,
                                        selectedClipId = selectedClipId,
                                        onClipSelected = { clipId ->
                                            viewModel.selectClip(track.id, clipId)
                                        },
                                        onClipMoved = { clipId, newSec ->
                                            viewModel.moveClip(track.id, clipId, newSec)
                                        },
                                        onMoveFinished = {
                                            viewModel.commitClipMove()
                                        }
                                    )
                                }
                            }
                        }

                        // Playhead Vertical Cursor Line
                        val playheadOffsetXDp = (currentPosition * zoomPixelsPerSec).dp
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = with(density) { playheadOffsetXDp.roundToPx() },
                                        y = 0
                                    )
                                }
                                .width(2.dp)
                                .fillMaxHeight()
                                .background(AndeanGold)
                        ) {
                            // Top playhead triangle / indicator
                            Box(
                                modifier = Modifier
                                    .offset(x = (-5).dp, y = 0.dp)
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(AndeanGold)
                            )
                        }
                    }
                }
            }

            // BOTTOM SELECTION TOOLBAR
            AnimatedVisibility(visible = activeClip != null) {
                if (activeClip != null) {
                    SelectedClipBar(
                        clipName = activeClip.name,
                        durationSec = activeClip.durationSec,
                        onOpenEditor = { showClipEditorDialog = true },
                        onDuplicate = { viewModel.duplicateSelectedClip() },
                        onDelete = { viewModel.deleteSelectedClip() },
                        onDeselect = { viewModel.selectClip("", null) }
                    )
                }
            }
        }

        // Clip Editor Dialog
        if (showClipEditorDialog && activeClip != null && selectedTrackId != null) {
            ClipEditorDialog(
                clip = activeClip,
                onDismiss = { showClipEditorDialog = false },
                onVolumeChange = { vol ->
                    viewModel.updateClipVolume(selectedTrackId!!, activeClip.id, vol)
                },
                onFadesChange = { fadeIn, fadeOut ->
                    viewModel.updateClipFades(selectedTrackId!!, activeClip.id, fadeIn, fadeOut)
                },
                onSplitAtPlayhead = { viewModel.splitClipAtPlayhead() },
                onDuplicate = { viewModel.duplicateSelectedClip() },
                onDelete = { viewModel.deleteSelectedClip() }
            )
        }

        // Effects & Ducking Bottom Sheet
        if (showEffectsSheet) {
            EffectsBottomSheet(
                tracks = project.tracks,
                ducking = project.ducking,
                master = project.master,
                onDismiss = { showEffectsSheet = false },
                onUpdateDucking = { enabled, voiceId, musicId, redDb, atk, rel ->
                    viewModel.updateDuckingSettings(enabled, voiceId, musicId, redDb, atk, rel)
                },
                onUpdateMaster = { vol, lim, norm, rev, echo ->
                    viewModel.updateMasterSettings(vol, lim, norm, rev, echo)
                }
            )
        }

        // Metronome Bottom Sheet
        if (showMetronomeSheet) {
            MetronomeBottomSheet(
                viewModel = viewModel,
                onDismiss = { showMetronomeSheet = false }
            )
        }

        // Export Dialog
        if (showExportDialog) {
            ExportDialog(
                isExporting = isExporting,
                lastExportedFile = lastExportedFile,
                defaultTitle = project.title,
                onDismiss = { showExportDialog = false },
                onExport = { name -> viewModel.exportMix(context, name) },
                onShare = { viewModel.shareExportedAudio(context) }
            )
        }
    }
}

@Composable
private fun TopConsoleBar(
    projectTitle: String,
    currentSec: Float,
    totalSec: Float,
    peakLeft: Float,
    peakRight: Float,
    canUndo: Boolean,
    canRedo: Boolean,
    isMetronomeActive: Boolean,
    meterLabel: String,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    onOpenMetronome: () -> Unit,
    onOpenEffects: () -> Unit,
    onOpenExport: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .border(0.5.dp, StudioBorder)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Project info & timecode
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = projectTitle,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val curMin = (currentSec / 60).toInt()
            val curS = (currentSec % 60).toInt()
            val curMs = ((currentSec - currentSec.toInt()) * 100).toInt()

            val totMin = (totalSec / 60).toInt()
            val totS = (totalSec % 60).toInt()

            Text(
                text = String.format("%02d:%02d.%02d / %02d:%02d", curMin, curS, curMs, totMin, totS),
                color = AndeanGold,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // VU Meter
        VUMeterView(
            leftPeak = peakLeft,
            rightPeak = peakRight,
            modifier = Modifier
                .width(68.dp)
                .height(30.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Undo & Redo buttons
        IconButton(
            onClick = onUndo,
            enabled = canUndo,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Undo,
                contentDescription = "Deshacer",
                tint = if (canUndo) Color.White else Color(0xFF555060),
                modifier = Modifier.size(18.dp)
            )
        }

        IconButton(
            onClick = onRedo,
            enabled = canRedo,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Redo,
                contentDescription = "Rehacer",
                tint = if (canRedo) Color.White else Color(0xFF555060),
                modifier = Modifier.size(18.dp)
            )
        }

        // Save project button
        IconButton(
            onClick = onSave,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Save,
                contentDescription = "Guardar Proyecto",
                tint = AndeanGold,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Huayno Metronome quick button
        OutlinedButton(
            onClick = onOpenMetronome,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (isMetronomeActive) AndeanGold else Color(0xFFD4CEE0)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isMetronomeActive) AndeanGold else StudioBorder
            ),
            modifier = Modifier.height(32.dp).testTag("open_metronome_console_btn")
        ) {
            Icon(Icons.Default.Speed, contentDescription = "Metrónomo", modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Metrónomo $meterLabel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Effects & Ducking
        OutlinedButton(
            onClick = onOpenEffects,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AndeanTurquoise
            ),
            modifier = Modifier.height(32.dp)
        ) {
            Icon(Icons.Default.Tune, contentDescription = "Efectos", modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Ducking", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Export button
        Button(
            onClick = onOpenExport,
            colors = ButtonDefaults.buttonColors(
                containerColor = AndeanGold,
                contentColor = Color.Black
            ),
            modifier = Modifier.height(32.dp)
        ) {
            Icon(Icons.Default.Download, contentDescription = "Exportar", modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Exportar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TransportBar(
    isPlaying: Boolean,
    isLooping: Boolean,
    isRecording: Boolean,
    snapGrid: Float,
    zoom: Float,
    metronomeConfig: MetronomeConfig,
    isMetronomeActive: Boolean,
    isMetronomeAccent: Boolean,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onLoopToggle: () -> Unit,
    onRecordClick: () -> Unit,
    onSplit: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onSnapChange: (Float) -> Unit,
    onAddTrack: () -> Unit,
    onReloadHuaynoSamples: () -> Unit,
    onOpenMetronome: () -> Unit,
    onToggleMetronome: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioSurfaceVariant)
            .border(0.5.dp, StudioBorder)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play / Stop / Record / Loop
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PLAY / PAUSE Main Big Button
            FilledIconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(42.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = AndeanGold,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                    modifier = Modifier.size(24.dp)
                )
            }

            // STOP Button
            IconButton(
                onClick = onStop,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Stop,
                    contentDescription = "Detener",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // RECORD Button (Microphone)
            FilledIconButton(
                onClick = onRecordClick,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isRecording) AndeanRed else Color(0xFF2E2633),
                    contentColor = if (isRecording) Color.White else AndeanRed
                )
            ) {
                Icon(
                    Icons.Default.FiberManualRecord,
                    contentDescription = "Grabar",
                    modifier = Modifier.size(18.dp)
                )
            }

            // LOOP Button
            IconButton(
                onClick = onLoopToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Repeat,
                    contentDescription = "Bucle",
                    tint = if (isLooping) AndeanTurquoise else Color(0xFF7E798B),
                    modifier = Modifier.size(18.dp)
                )
            }

            // SPLIT (CORTAR)
            IconButton(
                onClick = onSplit,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.ContentCut,
                    contentDescription = "Cortar clip en cabezal",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // METRÓNOMO HUAYNO TRANSPORT WIDGET
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isMetronomeActive) AndeanGold.copy(alpha = 0.22f) else Color(0xFF282433))
                    .border(
                        1.dp,
                        if (isMetronomeActive) AndeanGold else Color(0xFF3E394C),
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onOpenMetronome() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("metronome_transport_widget"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Pulsing LED dot indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (isMetronomeActive && isMetronomeAccent) AndeanGold
                            else if (isMetronomeActive) AndeanTurquoise
                            else Color(0xFF6B6678)
                        )
                )

                Text(
                    text = "${metronomeConfig.meter.label} • ${metronomeConfig.bpm}BPM",
                    color = if (isMetronomeActive) AndeanGold else Color(0xFFD4CEE0),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                IconButton(
                    onClick = onToggleMetronome,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = if (isMetronomeActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isMetronomeActive) "Pausar Metrónomo" else "Iniciar Metrónomo",
                        tint = if (isMetronomeActive) AndeanGold else Color(0xFFA8A3B4),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Tools: Zoom, Snap, +Pista
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Zoom Out
            IconButton(
                onClick = { onZoomChange(zoom - 15f) },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom -", tint = Color(0xFFA8A3B4), modifier = Modifier.size(16.dp))
            }

            // Zoom In
            IconButton(
                onClick = { onZoomChange(zoom + 15f) },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom +", tint = Color(0xFFA8A3B4), modifier = Modifier.size(16.dp))
            }

            // Snap Grid Selector
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF282433))
                    .border(1.dp, Color(0xFF3E394C), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (snapGrid > 0) "Imán: ${snapGrid}s" else "Imán: Off",
                    color = Color(0xFFD4CEE0),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Add track
            IconButton(
                onClick = onAddTrack,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Pista", tint = AndeanGold, modifier = Modifier.size(18.dp))
            }

            // Reload default stems button
            OutlinedButton(
                onClick = onReloadHuaynoSamples,
                modifier = Modifier.height(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD0CBD9))
            ) {
                Text("Muestras Huayno", fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun SelectedClipBar(
    clipName: String,
    durationSec: Float,
    onOpenEditor: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onDeselect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1B1922))
            .border(1.dp, AndeanGold.copy(alpha = 0.6f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Clip: $clipName",
                color = AndeanGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Duración: ${String.format("%.2f", durationSec)}s",
                color = Color(0xFFA8A3B4),
                fontSize = 10.sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onOpenEditor,
                colors = ButtonDefaults.buttonColors(containerColor = AndeanGold, contentColor = Color.Black),
                modifier = Modifier.height(30.dp)
            ) {
                Text("Fades / Vol", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onDuplicate,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Duplicar", modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Duplicar", fontSize = 11.sp)
            }

            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = AndeanRed),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Borrar", fontSize = 11.sp)
            }
        }
    }
}
