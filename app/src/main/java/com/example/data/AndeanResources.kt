package com.example.data

import com.example.audio.AndeanAudioSynthesizer

enum class ResourceCategory(val displayName: String, val iconTag: String) {
    MUSIC_STEMS("Música & Bordoneos", "MUSIC"),
    SOUND_EFFECTS("Efectos Andinos / FX", "FX"),
    ANIMATOR_CHEERS("Gritos & Animación Radial", "VOICE"),
    HUAYNO_PERCUSSION("Percusión & Ritmo", "PERC")
}

data class AndeanAudioResource(
    val id: String,
    val name: String,
    val category: ResourceCategory,
    val durationSec: Float,
    val regionalOrigin: String,
    val description: String,
    val generator: () -> FloatArray
)

object AndeanResourceCatalog {
    val resources: List<AndeanAudioResource> = listOf(
        // MÚSICA & BORDONEOS
        AndeanAudioResource(
            id = "res_arpa_ayacuchana",
            name = "Bordoneo de Arpa Ayacuchana",
            category = ResourceCategory.MUSIC_STEMS,
            durationSec = 10.0f,
            regionalOrigin = "Ayacucho",
            description = "Bordoneo grave en cuerdas de tripa y metal típico de las alturas de Huamanga (108 BPM)",
            generator = { AndeanAudioSynthesizer.generateHarpHuayno(10.0f) }
        ),
        AndeanAudioResource(
            id = "res_arpa_huanca",
            name = "Arpa Huancaína de Huaylash",
            category = ResourceCategory.MUSIC_STEMS,
            durationSec = 12.0f,
            regionalOrigin = "Huancayo / Junín",
            description = "Arpegio alegre y brillante con bordoneo rápido para zapateo festivo (128 BPM)",
            generator = { AndeanAudioSynthesizer.generateRegionalHuayno("Huayno Huanca / Huaylash", 12.0f) }
        ),
        AndeanAudioResource(
            id = "res_requinto_sureno",
            name = "Punteo de Requinto Andino",
            category = ResourceCategory.MUSIC_STEMS,
            durationSec = 10.0f,
            regionalOrigin = "Cusco / Puno",
            description = "Punteo agudo y melancólico característico del huayno sureño con pedal de chorus",
            generator = { AndeanAudioSynthesizer.generateRegionalHuayno("Huayno con Requinto / Sureño", 10.0f) }
        ),
        AndeanAudioResource(
            id = "res_quena_zampona",
            name = "Melodía de Quena & Zampoña Dulce",
            category = ResourceCategory.MUSIC_STEMS,
            durationSec = 8.0f,
            regionalOrigin = "Puno / Lago Titicaca",
            description = "Vientos andinos en escala pentatónica menor evocando las montañas andinas",
            generator = { AndeanAudioSynthesizer.generateQuenaMelody(8.0f) }
        ),

        // EFECTOS ANDINOS / FX
        AndeanAudioResource(
            id = "res_cohete_andino",
            name = "Cohete Andino de Fiesta Patronal",
            category = ResourceCategory.SOUND_EFFECTS,
            durationSec = 3.0f,
            regionalOrigin = "Sierra Central",
            description = "Disparo pirotécnico y detonación lejana tradicional de las fiestas patronales serranas",
            generator = { AndeanAudioSynthesizer.generateSpotIntroFx(3.0f) }
        ),
        AndeanAudioResource(
            id = "res_aplausos_ovacion",
            name = "Aplausos & Ovación de Coliseo",
            category = ResourceCategory.SOUND_EFFECTS,
            durationSec = 6.0f,
            regionalOrigin = "Nacional",
            description = "Ovación festiva de coliseo lleno con silbidos y gritos de entusiasmo popular",
            generator = { AndeanAudioSynthesizer.generateCheeringAndApplause(6.0f) }
        ),
        AndeanAudioResource(
            id = "res_clarin_cajamarca",
            name = "Clarín Cajamarquino Tradicional",
            category = ResourceCategory.SOUND_EFFECTS,
            durationSec = 4.0f,
            regionalOrigin = "Cajamarca",
            description = "Llamado ancestral de clarín de caña de cuatro metros para inicio de fiesta",
            generator = { AndeanAudioSynthesizer.generateSpotIntroFx(4.0f) }
        ),
        AndeanAudioResource(
            id = "res_zapateo_tabladillo",
            name = "Zapateo Fuerte en Tabladillo",
            category = ResourceCategory.SOUND_EFFECTS,
            durationSec = 5.0f,
            regionalOrigin = "Junín / Mantaro",
            description = "Golpe rítmico de zapatos y botines sobre madera de estrado en competencia de baile",
            generator = { AndeanAudioSynthesizer.generateSpotOutroFx(5.0f) }
        ),

        // GRITOS Y ANIMACIÓN RADIAL
        AndeanAudioResource(
            id = "res_grito_esooo",
            name = "Locución: «¡Esooo compadre, zapatea lindo!»",
            category = ResourceCategory.ANIMATOR_CHEERS,
            durationSec = 3.0f,
            regionalOrigin = "Huancavelica",
            description = "Grito eufórico de animador radial andino para arengar la pista de baile",
            generator = { AndeanAudioSynthesizer.generateAnnouncerVoice(3.0f) }
        ),
        AndeanAudioResource(
            id = "res_grito_sentimiento",
            name = "Locución: «¡Con sentimiento andino!»",
            category = ResourceCategory.ANIMATOR_CHEERS,
            durationSec = 3.0f,
            regionalOrigin = "Ayacucho",
            description = "Frase emotiva para pasajes sentimentales de arpa y guitarra",
            generator = { AndeanAudioSynthesizer.generateAnnouncerVoice(3.0f) }
        ),
        AndeanAudioResource(
            id = "res_grito_alturas",
            name = "Locución: «¡Desde las alturas del Perú!»",
            category = ResourceCategory.ANIMATOR_CHEERS,
            durationSec = 3.5f,
            regionalOrigin = "Nacional",
            description = "Identificador radial festivo con eco de estadio para spots publicitarios",
            generator = { AndeanAudioSynthesizer.generateAnnouncerVoice(3.5f) }
        ),
        AndeanAudioResource(
            id = "res_grito_cerveza",
            name = "Locución: «¡Salud con cerveza bien helada!»",
            category = ResourceCategory.ANIMATOR_CHEERS,
            durationSec = 3.0f,
            regionalOrigin = "Sierra Central",
            description = "Arenga cervecera infaltable en todo spot de concierto y baile social",
            generator = { AndeanAudioSynthesizer.generateAnnouncerVoice(3.0f) }
        ),

        // PERCUSIÓN & RITMO
        AndeanAudioResource(
            id = "res_wankar_24",
            name = "Wankar Tradicional 2/4",
            category = ResourceCategory.HUAYNO_PERCUSSION,
            durationSec = 8.0f,
            regionalOrigin = "Ayacucho / Cusco",
            description = "Base rítmica pura de caja andina en compás binario de 2 tiempos",
            generator = { AndeanAudioSynthesizer.generateHarpHuayno(8.0f) }
        ),
        AndeanAudioResource(
            id = "res_cencerro_huaylash",
            name = "Cencerro Metálico de Huaylash",
            category = ResourceCategory.HUAYNO_PERCUSSION,
            durationSec = 6.0f,
            regionalOrigin = "Huancayo",
            description = "Repique metálico agudo de cencerro para marcar el tiempo y contratiempo",
            generator = { AndeanAudioSynthesizer.generateSpotOutroFx(6.0f) }
        ),
        AndeanAudioResource(
            id = "res_bombo_leguero",
            name = "Bombo Andino de Fiesta",
            category = ResourceCategory.HUAYNO_PERCUSSION,
            durationSec = 7.0f,
            regionalOrigin = "Ancash",
            description = "Golpe profundo de mazo y aro para apertura de carnavales y huaynos festivos",
            generator = { AndeanAudioSynthesizer.generateCheeringAndApplause(7.0f) }
        )
    )
}
