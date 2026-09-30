package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AndeanAudioSynthesizer
import com.example.audio.AudioClipModel
import com.example.audio.AudioTrackModel
import com.example.audio.DuckingSettings
import com.example.audio.HuaynoMeter
import com.example.audio.HuaynoMetronomeEngine
import com.example.audio.MasterSettings
import com.example.audio.MetronomeConfig
import com.example.audio.MetronomePreset
import com.example.audio.MetronomeTimbre
import com.example.audio.MultiTrackAudioEngine
import com.example.audio.ProjectModel
import com.example.audio.TrackType
import com.example.audio.WavFileExporter
import com.example.data.AndeanAudioResource
import com.example.data.AuthAndStudioRepository
import com.example.data.AuthorizedVoiceEntity
import com.example.data.DeviceSessionEntity
import com.example.data.HuaynoSpotTemplate
import com.example.data.ProjectEntity
import com.example.data.ResourceCategory
import com.example.data.UsageRecordEntity
import com.example.data.UserEntity
import com.example.data.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.UUID
import kotlin.math.abs

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    val repository = AuthAndStudioRepository(application, viewModelScope)
    val currentUser: StateFlow<UserEntity?> = repository.currentUser
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val userProjects: StateFlow<List<ProjectEntity>> = repository.getProjectsForCurrentUser().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val authorizedVoices: StateFlow<List<AuthorizedVoiceEntity>> = repository.authorizedVoices.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allDeviceSessions: StateFlow<List<DeviceSessionEntity>> = repository.allDeviceSessions.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val allUsageRecords: StateFlow<List<UsageRecordEntity>> = repository.allUsage.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _selectedVoiceForSpots = MutableStateFlow("Don Floro (Voz Andina Tradicional)")
    val selectedVoiceForSpots: StateFlow<String> = _selectedVoiceForSpots.asStateFlow()

    fun setSelectedVoiceForSpots(voiceTag: String) {
        _selectedVoiceForSpots.value = voiceTag
    }

    private val _project = MutableStateFlow(createDefaultProject())
    val project: StateFlow<ProjectModel> = _project.asStateFlow()

    private val _currentPositionSec = MutableStateFlow(0f)
    val currentPositionSec: StateFlow<Float> = _currentPositionSec.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLooping = MutableStateFlow(false)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _peakLeft = MutableStateFlow(0f)
    val peakLeft: StateFlow<Float> = _peakLeft.asStateFlow()

    private val _peakRight = MutableStateFlow(0f)
    val peakRight: StateFlow<Float> = _peakRight.asStateFlow()

    private val _selectedClipId = MutableStateFlow<String?>(null)
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _selectedTrackId = MutableStateFlow<String?>(null)
    val selectedTrackId: StateFlow<String?> = _selectedTrackId.asStateFlow()

    // Timeline Zoom: pixels per second (e.g. 50dp to 200dp per second)
    private val _zoomPixelsPerSec = MutableStateFlow(75f)
    val zoomPixelsPerSec: StateFlow<Float> = _zoomPixelsPerSec.asStateFlow()

    // Magnetic snap (0 = disabled, 0.25f, 0.5f, 1.0f)
    private val _snapGridSec = MutableStateFlow(0.5f)
    val snapGridSec: StateFlow<Float> = _snapGridSec.asStateFlow()

    // Undo / Redo history
    private val undoStack = mutableListOf<ProjectModel>()
    private val redoStack = mutableListOf<ProjectModel>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Export status
    private val _lastExportedFile = MutableStateFlow<File?>(null)
    val lastExportedFile: StateFlow<File?> = _lastExportedFile.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Audio Engine
    private val audioEngine: MultiTrackAudioEngine = MultiTrackAudioEngine(
        sampleRate = 44100,
        onPositionUpdate = { pos -> _currentPositionSec.value = pos },
        onPeakUpdate = { l, r ->
            _peakLeft.value = l
            _peakRight.value = r
        },
        onPlaybackFinished = {
            _isPlaying.value = false
            if (!_isRecording.value && !metronomeEngine.isCountingIn.value) {
                metronomeEngine.stop()
            }
        }
    )

    // Metrónomo para ritmos Huayno (2/4 y 4/4) y grabación vocal
    val metronomeEngine = HuaynoMetronomeEngine(sampleRate = 44100)
    val isMetronomeActive: StateFlow<Boolean> = metronomeEngine.isActive
    val currentMetronomeBeat: StateFlow<Int> = metronomeEngine.currentBeat
    val isMetronomeAccent: StateFlow<Boolean> = metronomeEngine.isAccent
    val isCountInActive: StateFlow<Boolean> = metronomeEngine.isCountingIn
    val countInBeat: StateFlow<Int> = metronomeEngine.countInBeat
    val countInTotal: StateFlow<Int> = metronomeEngine.countInTotal

    // Presets auténticos de tempo y compás para Huayno
    val huaynoPresets = listOf(
        MetronomePreset("Huayno Ayacuchano Tradicional", 104, HuaynoMeter.TWO_FOUR, "Bordoneo, guitarra y arpa de 2 tiempos"),
        MetronomePreset("Huayno con Arpa Central", 116, HuaynoMeter.FOUR_FOUR, "Arpa huancaína, teclado y compás de 4 tiempos"),
        MetronomePreset("Huaylash / Zapateo Rápido", 132, HuaynoMeter.TWO_FOUR, "Compás enérgico 2/4 para zapateo"),
        MetronomePreset("Huayno Lento / Sentimental", 84, HuaynoMeter.TWO_FOUR, "Yaraví y huayno nostálgico"),
        MetronomePreset("Chicha / Huayno Moderno", 120, HuaynoMeter.FOUR_FOUR, "Guitarra eléctrica y timbales 4/4")
    )

    private val tapTimestamps = mutableListOf<Long>()

    // Recording state
    private var recordingJob: Job? = null
    private var audioRecord: AudioRecord? = null

    init {
        loadDefaultHuaynoStems()
    }

    private fun createDefaultProject(): ProjectModel {
        val musicTrackId = UUID.randomUUID().toString()
        val voiceTrackId = UUID.randomUUID().toString()
        val fxTrackId = UUID.randomUUID().toString()

        return ProjectModel(
            title = "Spot Huayno Gran Aniversario 30s",
            regionalStyle = "Huayno con Arpa - Estilo Andino",
            durationSec = 30.0f,
            tracks = listOf(
                AudioTrackModel(
                    id = musicTrackId,
                    name = "Arpa Huayno & Bordón",
                    type = TrackType.MUSIC,
                    volume = 0.95f,
                    colorHex = 0xFF06D6A0
                ),
                AudioTrackModel(
                    id = voiceTrackId,
                    name = "Locución Animador Radial",
                    type = TrackType.VOICE,
                    volume = 1.15f,
                    colorHex = 0xFFFFD166
                ),
                AudioTrackModel(
                    id = fxTrackId,
                    name = "Vientos & Aplausos Andinos",
                    type = TrackType.FX,
                    volume = 0.85f,
                    colorHex = 0xFFFF5C70
                )
            ),
            ducking = DuckingSettings(
                enabled = true,
                voiceTrackId = voiceTrackId,
                musicTrackId = musicTrackId,
                reductionDb = -13.0f,
                attackSec = 0.04f,
                releaseSec = 0.40f
            ),
            master = MasterSettings(
                volume = 1.0f,
                limiterEnabled = true,
                radioNormalize = true,
                reverbLevel = 0.15f,
                echoLevel = 0.10f
            )
        )
    }

    fun loadDefaultHuaynoStems() {
        viewModelScope.launch(Dispatchers.Default) {
            val harpHuayno = AndeanAudioSynthesizer.generateHarpHuayno(28.0f)
            val announcerVoice = AndeanAudioSynthesizer.generateAnnouncerVoice(22.0f)
            val quena = AndeanAudioSynthesizer.generateQuenaMelody(12.0f)
            val crowd = AndeanAudioSynthesizer.generateCheeringAndApplause(10.0f)

            val currentProj = _project.value
            val musicTrack = currentProj.tracks.getOrNull(0)
            val voiceTrack = currentProj.tracks.getOrNull(1)
            val fxTrack = currentProj.tracks.getOrNull(2)

            if (musicTrack != null && voiceTrack != null && fxTrack != null) {
                val updatedTracks = listOf(
                    musicTrack.copy(
                        clips = listOf(
                            AudioClipModel(
                                name = "Pista Arpa Huayno 108BPM",
                                startTimeSec = 0.0f,
                                durationSec = 28.0f,
                                fadeInSec = 0.5f,
                                fadeOutSec = 2.0f,
                                volume = 1.0f,
                                samples = harpHuayno
                            )
                        )
                    ),
                    voiceTrack.copy(
                        clips = listOf(
                            AudioClipModel(
                                name = "Locución: ¡Gran Concierto Andino!",
                                startTimeSec = 3.0f,
                                durationSec = 22.0f,
                                fadeInSec = 0.1f,
                                fadeOutSec = 0.2f,
                                volume = 1.15f,
                                samples = announcerVoice
                            )
                        )
                    ),
                    fxTrack.copy(
                        clips = listOf(
                            AudioClipModel(
                                name = "Quena & Zampoña Melodía",
                                startTimeSec = 0.0f,
                                durationSec = 10.0f,
                                fadeInSec = 0.2f,
                                fadeOutSec = 1.5f,
                                volume = 0.85f,
                                samples = quena
                            ),
                            AudioClipModel(
                                name = "Bombos y Aplausos Fiesta",
                                startTimeSec = 18.0f,
                                durationSec = 10.0f,
                                fadeInSec = 0.5f,
                                fadeOutSec = 2.0f,
                                volume = 0.9f,
                                samples = crowd
                            )
                        )
                    )
                )

                _project.value = currentProj.copy(tracks = updatedTracks)
                saveSnapshot()
            }
        }
    }

    /**
     * Carga el spot generado en el Módulo A directamente al Editor Multipista con mezcla automática y ducking
     */
    fun loadAutoMixedSpot(
        title: String,
        regionalStyle: String,
        durationSec: Float,
        musicSamples: FloatArray,
        voiceSamples: FloatArray,
        introFxSamples: FloatArray,
        outroFxSamples: FloatArray,
        scriptTitle: String
    ) {
        val musicTrackId = UUID.randomUUID().toString()
        val voiceTrackId = UUID.randomUUID().toString()
        val fxTrackId = UUID.randomUUID().toString()

        val voiceDurationSec = (voiceSamples.size.toFloat() / 44100f).coerceAtLeast(4.0f)
        val voiceStartSec = 1.8f // El animador entra tras 1.8s de intro musical con cohete

        val musicTrack = AudioTrackModel(
            id = musicTrackId,
            name = "Música Huayno ($regionalStyle)",
            type = TrackType.MUSIC,
            volume = 0.90f,
            colorHex = 0xFF06D6A0,
            clips = listOf(
                AudioClipModel(
                    name = "Pista Huayno Base $regionalStyle",
                    startTimeSec = 0.0f,
                    durationSec = durationSec,
                    fadeInSec = 0.4f,
                    fadeOutSec = 1.8f,
                    volume = 1.0f,
                    samples = musicSamples
                )
            )
        )

        val voiceTrack = AudioTrackModel(
            id = voiceTrackId,
            name = "Locución: $scriptTitle",
            type = TrackType.VOICE,
            volume = 1.25f,
            colorHex = 0xFFFFD166,
            clips = listOf(
                AudioClipModel(
                    name = "Locución Andina Spot",
                    startTimeSec = voiceStartSec,
                    durationSec = voiceDurationSec,
                    fadeInSec = 0.05f,
                    fadeOutSec = 0.15f,
                    volume = 1.25f,
                    samples = voiceSamples
                )
            )
        )

        val fxClips = mutableListOf<AudioClipModel>()
        if (introFxSamples.isNotEmpty()) {
            val introDur = (introFxSamples.size.toFloat() / 44100f).coerceAtMost(3.5f)
            fxClips.add(
                AudioClipModel(
                    name = "Cohete y Clarín Intro",
                    startTimeSec = 0.0f,
                    durationSec = introDur,
                    fadeInSec = 0.1f,
                    fadeOutSec = 0.5f,
                    volume = 0.95f,
                    samples = introFxSamples
                )
            )
        }

        if (outroFxSamples.isNotEmpty()) {
            val outroDur = (outroFxSamples.size.toFloat() / 44100f).coerceAtMost(4.5f)
            val outroStart = (durationSec - outroDur).coerceAtLeast(voiceStartSec + voiceDurationSec - 0.5f)
            fxClips.add(
                AudioClipModel(
                    name = "Ovación y Bombo Cierre",
                    startTimeSec = outroStart,
                    durationSec = outroDur,
                    fadeInSec = 0.3f,
                    fadeOutSec = 1.5f,
                    volume = 0.90f,
                    samples = outroFxSamples
                )
            )
        }

        val fxTrack = AudioTrackModel(
            id = fxTrackId,
            name = "Efectos Andinos (Intro/Cierre)",
            type = TrackType.FX,
            volume = 0.85f,
            colorHex = 0xFFFF5C70,
            clips = fxClips
        )

        val newProject = ProjectModel(
            id = UUID.randomUUID().toString(),
            title = title,
            regionalStyle = regionalStyle,
            durationSec = durationSec,
            tracks = listOf(musicTrack, voiceTrack, fxTrack),
            ducking = DuckingSettings(
                enabled = true,
                voiceTrackId = voiceTrackId,
                musicTrackId = musicTrackId,
                reductionDb = -14.0f,
                attackSec = 0.04f,
                releaseSec = 0.35f
            ),
            master = MasterSettings(
                volume = 1.0f,
                limiterEnabled = true,
                radioNormalize = true,
                reverbLevel = 0.16f,
                echoLevel = 0.10f
            )
        )

        stopPlayback()
        _project.value = newProject
        saveSnapshot()
        _userMessage.value = "¡Spot cargado en el Editor con Ducking automático!"
    }

    private fun saveSnapshot() {
        undoStack.add(_project.value)
        if (undoStack.size > 25) undoStack.removeAt(0)
        redoStack.clear()
        _canUndo.value = undoStack.size > 1
        _canRedo.value = false
    }

    fun undo() {
        if (undoStack.size > 1) {
            val current = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(current)
            _project.value = undoStack.last()
            _canUndo.value = undoStack.size > 1
            _canRedo.value = true
            _userMessage.value = "Deshecho"
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(next)
            _project.value = next
            _canUndo.value = true
            _canRedo.value = redoStack.isNotEmpty()
            _userMessage.value = "Rehecho"
        }
    }

    // Playback Controls
    fun togglePlayPause() {
        if (_isPlaying.value) {
            audioEngine.pause()
            _isPlaying.value = false
            if (!_isRecording.value) {
                metronomeEngine.stop()
            }
        } else {
            audioEngine.play(_project.value, _currentPositionSec.value, _isLooping.value)
            _isPlaying.value = true
            if (_project.value.metronome.playDuringPlayback) {
                metronomeEngine.startContinuous(_project.value.metronome)
            }
        }
    }

    fun stopPlayback() {
        audioEngine.stop()
        _isPlaying.value = false
        _currentPositionSec.value = 0f
        audioEngine.currentPositionSec = 0f
        if (!_isRecording.value) {
            metronomeEngine.stop()
        }
    }

    fun seekTo(seconds: Float) {
        val target = snapTime(seconds.coerceIn(0f, _project.value.durationSec))
        _currentPositionSec.value = target
        audioEngine.seekTo(target, if (_isPlaying.value) _project.value else null)
    }

    fun toggleLoop() {
        _isLooping.value = !_isLooping.value
    }

    fun setZoom(zoom: Float) {
        _zoomPixelsPerSec.value = zoom.coerceIn(30f, 250f)
    }

    fun setSnapGrid(snap: Float) {
        _snapGridSec.value = snap
    }

    private fun snapTime(time: Float): Float {
        val grid = _snapGridSec.value
        if (grid <= 0.01f) return time
        return (Math.round(time / grid) * grid)
    }

    // Selection
    fun selectClip(trackId: String, clipId: String?) {
        _selectedTrackId.value = trackId
        _selectedClipId.value = clipId
    }

    fun updateProjectTitle(title: String) {
        saveSnapshot()
        _project.value = _project.value.copy(title = title)
    }

    // Track Operations
    fun toggleMuteTrack(trackId: String) {
        saveSnapshot()
        _project.value = _project.value.copy(
            tracks = _project.value.tracks.map {
                if (it.id == trackId) it.copy(isMuted = !it.isMuted) else it
            }
        )
    }

    fun toggleSoloTrack(trackId: String) {
        saveSnapshot()
        _project.value = _project.value.copy(
            tracks = _project.value.tracks.map {
                if (it.id == trackId) it.copy(isSolo = !it.isSolo) else it
            }
        )
    }

    fun setTrackVolume(trackId: String, volume: Float) {
        _project.value = _project.value.copy(
            tracks = _project.value.tracks.map {
                if (it.id == trackId) it.copy(volume = volume.coerceIn(0f, 2f)) else it
            }
        )
    }

    fun setTrackPan(trackId: String, pan: Float) {
        _project.value = _project.value.copy(
            tracks = _project.value.tracks.map {
                if (it.id == trackId) it.copy(pan = pan.coerceIn(-1f, 1f)) else it
            }
        )
    }

    fun addTrack(type: TrackType = TrackType.AUX) {
        saveSnapshot()
        val count = _project.value.tracks.size + 1
        val color = when (type) {
            TrackType.MUSIC -> 0xFF06D6A0
            TrackType.VOICE -> 0xFFFFD166
            TrackType.FX -> 0xFFFF5C70
            TrackType.AUX -> 0xFFA06CD5
        }
        val newTrack = AudioTrackModel(
            name = "${type.displayName} $count",
            type = type,
            colorHex = color
        )
        _project.value = _project.value.copy(tracks = _project.value.tracks + newTrack)
        _userMessage.value = "Pista agregada"
    }

    fun removeTrack(trackId: String) {
        if (_project.value.tracks.size <= 1) {
            _userMessage.value = "Debe haber al menos una pista"
            return
        }
        saveSnapshot()
        _project.value = _project.value.copy(
            tracks = _project.value.tracks.filter { it.id != trackId }
        )
        _userMessage.value = "Pista eliminada"
    }

    // Clip Operations
    fun moveClip(trackId: String, clipId: String, newStartTimeSec: Float) {
        val snapped = snapTime(newStartTimeSec.coerceAtLeast(0f))
        _project.value = _project.value.copy(
            tracks = _project.value.tracks.map { track ->
                if (track.id == trackId) {
                    track.copy(
                        clips = track.clips.map { clip ->
                            if (clip.id == clipId) clip.copy(startTimeSec = snapped) else clip
                        }
                    )
                } else track
            }
        )
    }

    fun commitClipMove() {
        saveSnapshot()
    }

    fun splitClipAtPlayhead() {
        val currentPlayhead = _currentPositionSec.value
        val trackId = _selectedTrackId.value
        val clipId = _selectedClipId.value

        if (trackId == null || clipId == null) {
            _userMessage.value = "Selecciona un clip para cortar"
            return
        }

        saveSnapshot()
        var splitDone = false

        _project.value = _project.value.copy(
            tracks = _project.value.tracks.map { track ->
                if (track.id == trackId) {
                    val newClips = mutableListOf<AudioClipModel>()
                    for (clip in track.clips) {
                        if (clip.id == clipId && currentPlayhead > clip.startTimeSec && currentPlayhead < (clip.startTimeSec + clip.durationSec)) {
                            val firstPartDuration = currentPlayhead - clip.startTimeSec
                            val secondPartDuration = clip.durationSec - firstPartDuration

                            val clip1 = clip.copy(
                                id = UUID.randomUUID().toString(),
                                durationSec = firstPartDuration,
                                fadeOutSec = 0.05f
                            )
                            val clip2 = clip.copy(
                                id = UUID.randomUUID().toString(),
                                startTimeSec = currentPlayhead,
                                durationSec = secondPartDuration,
                                offsetSec = clip.offsetSec + firstPartDuration,
                                fadeInSec = 0.05f
                            )
                            newClips.add(clip1)
                            newClips.add(clip2)
                            splitDone = true
                        } else {
                            newClips.add(clip)
                        }
                    }
                    track.copy(clips = newClips)
                } else track
            }
        )

        if (splitDone) {
            _userMessage.value = "Clip dividido en ${String.format("%.2f", currentPlayhead)}s"
        } else {
            _userMessage.value = "El cabezal debe estar dentro del clip seleccionado"
        }
    }

    fun duplicateSelectedClip() {
        val trackId = _selectedTrackId.value
        val clipId = _selectedClipId.value
        if (trackId == null || clipId == null) return

        saveSnapshot()
        _project.value = _project.value.copy(
            tracks = _project.value.tracks.map { track ->
                if (track.id == trackId) {
                    val clip = track.clips.find { it.id == clipId }
                    if (clip != null) {
                        val duplicated = clip.copy(
                            id = UUID.randomUUID().toString(),
                            name = "${clip.name} (Copia)",
                            startTimeSec = clip.startTimeSec + clip.durationSec + 0.5f
                        )
                        track.copy(clips = track.clips + duplicated)
                    } else track
                } else track
            }
        )
        _userMessage.value = "Clip duplicado"
    }

    fun deleteSelectedClip() {
        val trackId = _selectedTrackId.value
        val clipId = _selectedClipId.value
        if (trackId == null || clipId == null) return

        saveSnapshot()
        _project.value = _project.value.copy(
            tracks = _project.value.tracks.map { track ->
                if (track.id == trackId) {
                    track.copy(clips = track.clips.filter { it.id != clipId })
                } else track
            }
        )
        _selectedClipId.value = null
        _userMessage.value = "Clip eliminado"
    }

    fun updateClipFades(trackId: String, clipId: String, fadeInSec: Float, fadeOutSec: Float) {
        saveSnapshot()
        _project.value = _project.value.copy(
            tracks = _project.value.tracks.map { track ->
                if (track.id == trackId) {
                    track.copy(
                        clips = track.clips.map { clip ->
                            if (clip.id == clipId) {
                                clip.copy(
                                    fadeInSec = fadeInSec.coerceIn(0f, clip.durationSec / 2f),
                                    fadeOutSec = fadeOutSec.coerceIn(0f, clip.durationSec / 2f)
                                )
                            } else clip
                        }
                    )
                } else track
            }
        )
    }

    fun updateClipVolume(trackId: String, clipId: String, volume: Float) {
        saveSnapshot()
        _project.value = _project.value.copy(
            tracks = _project.value.tracks.map { track ->
                if (track.id == trackId) {
                    track.copy(
                        clips = track.clips.map { clip ->
                            if (clip.id == clipId) clip.copy(volume = volume.coerceIn(0f, 2f)) else clip
                        }
                    )
                } else track
            }
        )
    }

    // Ducking Configuration
    fun updateDuckingSettings(
        enabled: Boolean,
        voiceTrackId: String?,
        musicTrackId: String?,
        reductionDb: Float,
        attackSec: Float,
        releaseSec: Float
    ) {
        saveSnapshot()
        _project.value = _project.value.copy(
            ducking = DuckingSettings(
                enabled = enabled,
                voiceTrackId = voiceTrackId,
                musicTrackId = musicTrackId,
                reductionDb = reductionDb,
                attackSec = attackSec,
                releaseSec = releaseSec
            )
        )
        _userMessage.value = "Ducking actualizado"
    }

    // Master Settings & Effects
    fun updateMasterSettings(
        volume: Float,
        limiter: Boolean,
        normalize: Boolean,
        reverb: Float,
        echo: Float
    ) {
        saveSnapshot()
        _project.value = _project.value.copy(
            master = MasterSettings(
                volume = volume,
                limiterEnabled = limiter,
                radioNormalize = normalize,
                reverbLevel = reverb,
                echoLevel = echo
            )
        )
    }

    // Metronome Operations
    fun toggleMetronome() {
        val currentCfg = _project.value.metronome
        if (metronomeEngine.isActive.value) {
            metronomeEngine.stop()
            _project.value = _project.value.copy(metronome = currentCfg.copy(isEnabled = false))
        } else {
            metronomeEngine.startContinuous(currentCfg)
            _project.value = _project.value.copy(metronome = currentCfg.copy(isEnabled = true))
        }
    }

    fun setMetronomeBpm(bpm: Int) {
        val clamped = bpm.coerceIn(40, 240)
        val updated = _project.value.metronome.copy(bpm = clamped)
        _project.value = _project.value.copy(metronome = updated)
        metronomeEngine.updateConfig(updated)
    }

    fun setMetronomeMeter(meter: HuaynoMeter) {
        val updated = _project.value.metronome.copy(meter = meter)
        _project.value = _project.value.copy(metronome = updated)
        metronomeEngine.updateConfig(updated)
        _userMessage.value = "Compás Huayno cambiado a ${meter.label} (${meter.subtitle})"
    }

    fun setMetronomeTimbre(timbre: MetronomeTimbre) {
        val updated = _project.value.metronome.copy(timbre = timbre)
        _project.value = _project.value.copy(metronome = updated)
        metronomeEngine.updateConfig(updated)
        metronomeEngine.previewClick(timbre, isAccent = true, vol = updated.volume)
    }

    fun setMetronomeVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        val updated = _project.value.metronome.copy(volume = clamped)
        _project.value = _project.value.copy(metronome = updated)
        metronomeEngine.updateConfig(updated)
    }

    fun setMetronomeCountInBars(bars: Int) {
        val clamped = bars.coerceIn(0, 4)
        val updated = _project.value.metronome.copy(countInBars = clamped)
        _project.value = _project.value.copy(metronome = updated)
        _userMessage.value = if (clamped == 0) "Pre-cuenta desactivada" else "Pre-cuenta configurada en $clamped compás(es)"
    }

    fun setMetronomeDuringRecording(enabled: Boolean) {
        val updated = _project.value.metronome.copy(playDuringRecording = enabled)
        _project.value = _project.value.copy(metronome = updated)
    }

    fun setMetronomeDuringPlayback(enabled: Boolean) {
        val updated = _project.value.metronome.copy(playDuringPlayback = enabled)
        _project.value = _project.value.copy(metronome = updated)
    }

    fun applyMetronomePreset(preset: MetronomePreset) {
        val updated = _project.value.metronome.copy(
            bpm = preset.bpm,
            meter = preset.meter
        )
        _project.value = _project.value.copy(metronome = updated)
        metronomeEngine.updateConfig(updated)
        _userMessage.value = "Preset aplicado: ${preset.name} (${preset.meter.label} • ${preset.bpm} BPM)"
    }

    fun previewMetronomeClick(timbre: MetronomeTimbre) {
        metronomeEngine.previewClick(timbre, isAccent = true, vol = _project.value.metronome.volume)
    }

    fun tapTempo() {
        val now = System.currentTimeMillis()
        tapTimestamps.removeAll { now - it > 2500 }
        tapTimestamps.add(now)
        if (tapTimestamps.size >= 2) {
            val intervals = (1 until tapTimestamps.size).map { tapTimestamps[it] - tapTimestamps[it - 1] }
            val avgIntervalMs = intervals.average()
            if (avgIntervalMs > 100) {
                val detectedBpm = (60_000.0 / avgIntervalMs).toInt().coerceIn(40, 240)
                setMetronomeBpm(detectedBpm)
                _userMessage.value = "Tap Tempo: $detectedBpm BPM (${_project.value.metronome.meter.label})"
            }
        }
    }

    // Mic recording directly into Voice track with Huayno Metronome & Pre-cuenta (Count-In)
    fun toggleRecording(context: Context) {
        if (_isRecording.value || metronomeEngine.isCountingIn.value) {
            stopRecording()
        } else {
            startRecordingWithMetronome(context)
        }
    }

    private fun startRecordingWithMetronome(context: Context) {
        val metronomeCfg = _project.value.metronome
        if (metronomeCfg.countInBars > 0) {
            _userMessage.value = "Pre-cuenta Huayno (${metronomeCfg.meter.label}): ¡Prepárate para cantar!"
            metronomeEngine.startCountIn(
                config = metronomeCfg,
                bars = metronomeCfg.countInBars,
                keepPlayingAfterCountIn = metronomeCfg.playDuringRecording,
                onCountInFinished = {
                    viewModelScope.launch(Dispatchers.Main) {
                        startActualRecording(context)
                    }
                }
            )
        } else {
            if (metronomeCfg.playDuringRecording) {
                metronomeEngine.startContinuous(metronomeCfg)
            }
            startActualRecording(context)
        }
    }

    private fun startActualRecording(context: Context) {
        try {
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufSize * 2
            )

            audioRecord?.startRecording()
            _isRecording.value = true
            _userMessage.value = "¡Grabando voz en vivo con compás ${_project.value.metronome.meter.label}!"

            // También reproducimos las pistas de acompañamiento para que el cantante escuche la música
            if (!_isPlaying.value && _project.value.tracks.any { it.clips.isNotEmpty() }) {
                audioEngine.play(_project.value, _currentPositionSec.value, isLooping = false)
                _isPlaying.value = true
            }

            val recordedSamples = mutableListOf<Float>()
            val recordStartSec = _currentPositionSec.value

            recordingJob = viewModelScope.launch(Dispatchers.IO) {
                val buffer = ShortArray(1024)
                while (isActive && _isRecording.value) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    for (i in 0 until read) {
                        recordedSamples.add(buffer[i] / 32767f)
                    }
                }

                // Add recorded clip to selected or voice track
                withContext(Dispatchers.Main) {
                    val durSec = recordedSamples.size.toFloat() / sampleRate
                    if (durSec > 0.4f) {
                        val targetTrack = _project.value.tracks.find { it.type == TrackType.VOICE }
                            ?: _project.value.tracks.firstOrNull()

                        if (targetTrack != null) {
                            val newClip = AudioClipModel(
                                name = "Voz Huayno Mic (${String.format("%.1f", durSec)}s • ${_project.value.metronome.meter.label})",
                                startTimeSec = recordStartSec,
                                durationSec = durSec,
                                fadeInSec = 0.05f,
                                fadeOutSec = 0.1f,
                                volume = 1.25f,
                                samples = recordedSamples.toFloatArray()
                            )
                            saveSnapshot()
                            _project.value = _project.value.copy(
                                tracks = _project.value.tracks.map {
                                    if (it.id == targetTrack.id) it.copy(clips = it.clips + newClip) else it
                                }
                            )
                            _userMessage.value = "Voz guardada en pista ${targetTrack.name}"
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            _userMessage.value = "Permiso de micrófono no otorgado"
            _isRecording.value = false
            metronomeEngine.stop()
        } catch (e: Exception) {
            _userMessage.value = "Error al iniciar grabación: ${e.message}"
            _isRecording.value = false
            metronomeEngine.stop()
        }
    }

    private fun stopRecording() {
        _isRecording.value = false
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        metronomeEngine.stop()
        if (_isPlaying.value) {
            audioEngine.pause()
            _isPlaying.value = false
        }
    }

    // Export final mix or stems to WAV file
    fun exportMix(context: Context, fileName: String = "HuaynoStudio_Spot_Final") {
        viewModelScope.launch {
            // Validar cuotas o créditos del usuario antes de exportar
            val check = repository.checkCanConsumeAction("EXPORT_WAV", cost = 1)
            if (check.isFailure) {
                _userMessage.value = check.exceptionOrNull()?.message ?: "Límite alcanzado"
                return@launch
            }

            _isExporting.value = true
            _userMessage.value = "Exportando mezcla a WAV..."

            val rendered = withContext(Dispatchers.Default) {
                audioEngine.renderFullMix(_project.value)
            }

            val file = withContext(Dispatchers.IO) {
                WavFileExporter.exportToWavFile(
                    context = context,
                    fileName = fileName,
                    leftChannel = rendered.first,
                    rightChannel = rendered.second,
                    sampleRate = 44100,
                    normalizeLoudness = _project.value.master.radioNormalize
                )
            }

            _lastExportedFile.value = file
            _isExporting.value = false
            _userMessage.value = "¡Audio exportado con éxito!"

            // Registrar consumo de uso
            repository.recordUsage(
                actionType = "EXPORT_WAV",
                detail = "Exportó mezcla ${file.name} (${String.format("%.1f", _project.value.durationSec)}s)",
                cost = 1
            )
        }
    }

    fun saveCurrentProject() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val proj = _project.value
            val projectEntity = ProjectEntity(
                id = proj.id,
                ownerUsername = user.username,
                title = proj.title,
                regionalStyle = proj.regionalStyle,
                durationSec = proj.durationSec,
                projectJson = JSONObject().apply {
                    put("id", proj.id)
                    put("title", proj.title)
                    put("durationSec", proj.durationSec.toDouble())
                    put("regionalStyle", proj.regionalStyle)
                    put("tracksCount", proj.tracks.size)
                }.toString(),
                updatedAt = System.currentTimeMillis()
            )
            repository.saveProject(projectEntity)
            _userMessage.value = "Proyecto guardado en la cuenta de ${user.username}"
        }
    }

    fun login(user: String, pass: String, forceDisconnect: Boolean = false, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = repository.login(user, pass, forceDisconnect)
            if (res.isSuccess) {
                _userMessage.value = "Bienvenido, ${res.getOrNull()?.fullName ?: user}"
                onResult(true, null)
            } else {
                val err = res.exceptionOrNull()?.message ?: "Error al iniciar sesión"
                _userMessage.value = err
                onResult(false, err)
            }
        }
    }

    fun login(user: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        login(user, pass, false, onResult)
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _userMessage.value = "Sesión cerrada"
        }
    }

    fun createUser(
        user: String,
        tempPass: String,
        fullName: String,
        whatsapp: String,
        role: UserRole,
        isUnlimited: Boolean,
        credits: Int,
        dailyLimit: Int,
        monthlyLimit: Int,
        maxDevices: Int = 1,
        onDone: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.createNewUser(
                username = user,
                tempPass = tempPass,
                fullName = fullName,
                whatsapp = whatsapp,
                role = role,
                isUnlimited = isUnlimited,
                credits = credits,
                dailyLimit = dailyLimit,
                monthlyLimit = monthlyLimit,
                maxDevices = maxDevices
            )
            if (res.isSuccess) {
                _userMessage.value = "Usuario $user creado exitosamente"
                onDone(true, null)
            } else {
                val msg = res.exceptionOrNull()?.message ?: "Error al crear usuario"
                _userMessage.value = msg
                onDone(false, msg)
            }
        }
    }

    fun createUser(
        user: String,
        tempPass: String,
        fullName: String,
        whatsapp: String,
        role: UserRole,
        isUnlimited: Boolean,
        credits: Int,
        dailyLimit: Int,
        monthlyLimit: Int,
        onDone: (Boolean, String?) -> Unit
    ) {
        createUser(user, tempPass, fullName, whatsapp, role, isUnlimited, credits, dailyLimit, monthlyLimit, 1, onDone)
    }

    fun createUserByAdmin(
        user: String,
        tempPass: String,
        fullName: String,
        whatsapp: String,
        role: UserRole,
        isUnlimited: Boolean,
        credits: Int,
        dailyLimit: Int,
        monthlyLimit: Int,
        onDone: (Boolean, String?) -> Unit
    ) {
        createUser(user, tempPass, fullName, whatsapp, role, isUnlimited, credits, dailyLimit, monthlyLimit, 1, onDone)
    }

    fun toggleUserStatus(username: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleUserActive(username, currentStatus)
            _userMessage.value = "Estado de $username actualizado"
        }
    }

    fun resetPassword(username: String, newPass: String) {
        viewModelScope.launch {
            repository.resetUserPassword(username, newPass)
            _userMessage.value = "Contraseña de $username restablecida"
        }
    }

    fun deleteUser(username: String) {
        viewModelScope.launch {
            repository.deleteUser(username)
            _userMessage.value = "Usuario $username eliminado"
        }
    }

    fun shareExportedAudio(context: Context) {
        val file = _lastExportedFile.value
        if (file != null && file.exists()) {
            WavFileExporter.shareAudioFile(context, file, _project.value.title)
        } else {
            _userMessage.value = "Primero exporta la mezcla"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // Audio preview helpers for resources, voices & stems
    private var previewAudioTrack: AudioTrack? = null
    private var previewJob: Job? = null

    fun playFloatArrayPreview(samples: FloatArray, onDone: () -> Unit = {}) {
        stopResourcePreview()
        previewJob = viewModelScope.launch(Dispatchers.Default) {
            val shortBuffer = ShortArray(samples.size)
            for (i in samples.indices) {
                shortBuffer[i] = (samples[i].coerceIn(-1f, 1f) * 32767f).toInt().toShort()
            }
            val minBuf = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(44100)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(Math.max(minBuf, shortBuffer.size * 2))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            previewAudioTrack = track
            try {
                track.play()
                track.write(shortBuffer, 0, shortBuffer.size)
                val durMs = ((samples.size.toFloat() / 44100f) * 1000).toLong()
                delay(durMs)
            } catch (_: Exception) {}
            withContext(Dispatchers.Main) {
                onDone()
            }
        }
    }

    fun playResourcePreview(resource: AndeanAudioResource, onDone: () -> Unit) {
        val samples = resource.generator()
        playFloatArrayPreview(samples, onDone)
    }

    fun previewAuthorizedVoice(voice: AuthorizedVoiceEntity, onDone: () -> Unit) {
        val samples = AndeanAudioSynthesizer.generateAnnouncerVoice(3.5f)
        playFloatArrayPreview(samples, onDone)
    }

    fun stopResourcePreview() {
        previewJob?.cancel()
        previewJob = null
        try {
            previewAudioTrack?.stop()
            previewAudioTrack?.release()
        } catch (_: Exception) {}
        previewAudioTrack = null
    }

    fun insertResourceSample(resource: AndeanAudioResource) {
        val samples = resource.generator()
        val currentProj = _project.value
        val targetType = when (resource.category) {
            ResourceCategory.MUSIC_STEMS -> TrackType.MUSIC
            ResourceCategory.SOUND_EFFECTS -> TrackType.FX
            ResourceCategory.ANIMATOR_CHEERS -> TrackType.VOICE
            ResourceCategory.HUAYNO_PERCUSSION -> TrackType.MUSIC
        }
        var targetTrack = currentProj.tracks.find { it.type == targetType }
        if (targetTrack == null) {
            addTrack(targetType)
            targetTrack = _project.value.tracks.lastOrNull()
        }
        if (targetTrack != null) {
            val playhead = _currentPositionSec.value
            val newClip = AudioClipModel(
                name = resource.name,
                startTimeSec = playhead,
                durationSec = resource.durationSec,
                fadeInSec = 0.05f,
                fadeOutSec = 0.2f,
                volume = 1.0f,
                samples = samples
            )
            saveSnapshot()
            _project.value = _project.value.copy(
                tracks = _project.value.tracks.map {
                    if (it.id == targetTrack.id) it.copy(clips = it.clips + newClip) else it
                }
            )
            _userMessage.value = "Muestra '${resource.name}' agregada al editor en ${String.format("%.1f", playhead)}s"
        }
    }

    fun loadSeparatedStemsIntoEditor(title: String, instrumentalSamples: FloatArray, vocalSamples: FloatArray) {
        val musicTrackId = UUID.randomUUID().toString()
        val voiceTrackId = UUID.randomUUID().toString()
        val durSec = (instrumentalSamples.size.toFloat() / 44100f).coerceAtLeast(15.0f)

        val musicTrack = AudioTrackModel(
            id = musicTrackId,
            name = "Pista Instrumental Separada",
            type = TrackType.MUSIC,
            volume = 0.95f,
            colorHex = 0xFF06D6A0,
            clips = listOf(
                AudioClipModel(
                    name = "Instrumental Stem (Demucs)",
                    startTimeSec = 0.0f,
                    durationSec = durSec,
                    fadeInSec = 0.2f,
                    fadeOutSec = 1.0f,
                    volume = 1.0f,
                    samples = instrumentalSamples
                )
            )
        )

        val voiceTrack = AudioTrackModel(
            id = voiceTrackId,
            name = "Voz Aislada (Demucs)",
            type = TrackType.VOICE,
            volume = 1.15f,
            colorHex = 0xFFFFD166,
            clips = listOf(
                AudioClipModel(
                    name = "Voz Acapella Stem",
                    startTimeSec = 0.0f,
                    durationSec = (vocalSamples.size.toFloat() / 44100f).coerceAtLeast(10.0f),
                    fadeInSec = 0.1f,
                    fadeOutSec = 0.5f,
                    volume = 1.15f,
                    samples = vocalSamples
                )
            )
        )

        val newProject = ProjectModel(
            id = UUID.randomUUID().toString(),
            title = title,
            regionalStyle = "Separación Stems Huayno",
            durationSec = durSec,
            tracks = listOf(musicTrack, voiceTrack),
            ducking = DuckingSettings(
                enabled = true,
                voiceTrackId = voiceTrackId,
                musicTrackId = musicTrackId,
                reductionDb = -12.0f
            )
        )

        stopPlayback()
        _project.value = newProject
        saveSnapshot()
        _userMessage.value = "Stems cargados en el Editor Multipista"
    }

    fun loadSpotTemplateIntoEditor(template: HuaynoSpotTemplate) {
        val musicSamples = template.generateMusicSamples()
        val introSamples = template.generateIntroFxSamples()
        val outroSamples = template.generateOutroFxSamples()
        val voiceDur = (template.durationSec - 5.0f).coerceAtLeast(8.0f)
        val voiceSamples = AndeanAudioSynthesizer.generateAnnouncerVoice(voiceDur)

        loadAutoMixedSpot(
            title = template.title,
            regionalStyle = template.regionalStyle,
            durationSec = template.durationSec,
            musicSamples = musicSamples,
            voiceSamples = voiceSamples,
            introFxSamples = introSamples,
            outroFxSamples = outroSamples,
            scriptTitle = template.eventName
        )
    }

    fun revokeDeviceSession(sessionId: String) {
        viewModelScope.launch {
            repository.revokeDeviceSession(sessionId)
            _userMessage.value = "Sesión de dispositivo revocada"
        }
    }

    fun revokeAllSessionsForUser(username: String) {
        viewModelScope.launch {
            repository.revokeAllSessionsForUser(username)
            _userMessage.value = "Todas las sesiones de $username han sido revocadas"
        }
    }

    fun updateUserMaxDevices(username: String, maxDevices: Int) {
        viewModelScope.launch {
            repository.updateUserMaxDevices(username, maxDevices)
            _userMessage.value = "Límite de $username actualizado a $maxDevices dispositivo(s)"
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
        metronomeEngine.release()
        stopResourcePreview()
        stopRecording()
    }
}
