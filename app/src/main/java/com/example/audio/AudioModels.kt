package com.example.audio

import java.util.UUID

enum class TrackType(val displayName: String) {
    MUSIC("Música Huayno"),
    VOICE("Locución / Voz"),
    FX("Efectos Andinos"),
    AUX("Pista Extra")
}

data class AudioClipModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val startTimeSec: Float,
    val durationSec: Float,
    val offsetSec: Float = 0f,
    val fadeInSec: Float = 0f,
    val fadeOutSec: Float = 0f,
    val volume: Float = 1.0f,
    val samples: FloatArray = FloatArray(0)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioClipModel) return false
        return id == other.id &&
                name == other.name &&
                startTimeSec == other.startTimeSec &&
                durationSec == other.durationSec &&
                offsetSec == other.offsetSec &&
                fadeInSec == other.fadeInSec &&
                fadeOutSec == other.fadeOutSec &&
                volume == other.volume
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + startTimeSec.hashCode()
        result = 31 * result + durationSec.hashCode()
        return result
    }
}

data class AudioTrackModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: TrackType,
    val volume: Float = 1.0f,      // 0.0f a 2.0f
    val pan: Float = 0.0f,         // -1.0f (L) a +1.0f (R)
    val isMuted: Boolean = false,
    val isSolo: Boolean = false,
    val eqLow: Float = 0.0f,       // -12dB a +12dB
    val eqMid: Float = 0.0f,
    val eqHigh: Float = 0.0f,
    val clips: List<AudioClipModel> = emptyList(),
    val colorHex: Long = 0xFF06D6A0
)

data class DuckingSettings(
    val enabled: Boolean = true,
    val voiceTrackId: String? = null,
    val musicTrackId: String? = null,
    val reductionDb: Float = -12.0f, // Reducción en dB cuando habla la voz
    val attackSec: Float = 0.05f,   // Velocidad de bajada
    val releaseSec: Float = 0.40f   // Velocidad de recuperación
)

data class MasterSettings(
    val volume: Float = 1.0f,
    val limiterEnabled: Boolean = true,
    val radioNormalize: Boolean = true,
    val reverbLevel: Float = 0.15f,
    val echoLevel: Float = 0.10f
)

enum class HuaynoMeter(
    val beatsPerBar: Int,
    val label: String,
    val subtitle: String,
    val regionalDescription: String
) {
    TWO_FOUR(
        beatsPerBar = 2,
        label = "2/4",
        subtitle = "Huayno Tradicional / Huaylash",
        regionalDescription = "Compás andino fundamental de 2 tiempos. Golpe principal acentuado en el bombo/bajo y contratiempo bailable (Ayacucho, Junín, Cusco, Ancash)."
    ),
    FOUR_FOUR(
        beatsPerBar = 4,
        label = "4/4",
        subtitle = "Huayno Moderno con Arpa / Cumbia",
        regionalDescription = "Compás de 4 tiempos para huayno contemporáneo con arpa, teclado y fusión chicha / balada andina."
    ),
    THREE_FOUR(
        beatsPerBar = 3,
        label = "3/4",
        subtitle = "Pasacalle / Vals Serrano",
        regionalDescription = "Ritmo ternario utilizado en pasacalles de fiesta patronal y yaravíes solemnes."
    ),
    SIX_EIGHT(
        beatsPerBar = 6,
        label = "6/8",
        subtitle = "Carnaval / Qhaswa Andina",
        regionalDescription = "Compás compuesto festivo de comparsas y carnavales comunales andinos."
    )
}

enum class MetronomeTimbre(val displayName: String, val shortDesc: String) {
    WANKAR("Wankar / Madera Andina", "Golpe orgánico de caja andina y madera cálida"),
    COWBELL("Cencerro Huaylash", "Golpe metálico nítido para huaylash y arpa"),
    DIGITAL("Beep Digital de Estudio", "Acento agudo en 1 y pulso estándar de referencia")
}

data class MetronomePreset(
    val name: String,
    val bpm: Int,
    val meter: HuaynoMeter,
    val style: String
)

data class MetronomeConfig(
    val isEnabled: Boolean = false,
    val isPlaying: Boolean = false,
    val bpm: Int = 108,
    val meter: HuaynoMeter = HuaynoMeter.TWO_FOUR,
    val timbre: MetronomeTimbre = MetronomeTimbre.WANKAR,
    val volume: Float = 0.85f,
    val countInBars: Int = 1, // 0 = Sin pre-cuenta, 1 = 1 compás, 2 = 2 compases
    val playDuringRecording: Boolean = true,
    val playDuringPlayback: Boolean = false
)

data class ProjectModel(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Spot Huayno Fiesta Patronal 30s",
    val regionalStyle: String = "Huayno con Arpa - Ayacucho",
    val durationSec: Float = 30.0f,
    val sampleRate: Int = 44100,
    val tracks: List<AudioTrackModel> = emptyList(),
    val ducking: DuckingSettings = DuckingSettings(),
    val master: MasterSettings = MasterSettings(),
    val metronome: MetronomeConfig = MetronomeConfig()
)

