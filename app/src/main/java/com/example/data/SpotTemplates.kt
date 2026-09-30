package com.example.data

import com.example.audio.AndeanAudioSynthesizer
import com.example.audio.HuaynoMeter

data class HuaynoSpotTemplate(
    val id: String,
    val title: String,
    val category: String, // "Fiesta Patronal", "Aniversario", "Lanzamiento Primicia", "Zapateo / Huaylash", "Restaurante Campestre", "Comercial"
    val tag: String,
    val regionalStyle: String,
    val durationSec: Float,
    val recommendedBpm: Int,
    val meter: HuaynoMeter,
    val eventName: String,
    val dateTime: String,
    val venue: String,
    val artists: String,
    val contactWhatsapp: String,
    val ticketPrice: String,
    val scriptText: String,
    val recommendedVoice: String,
    val voiceSpeed: Float = 1.05f,
    val introFxDescription: String = "Cohete andino y clarín de fiesta",
    val outroFxDescription: String = "Aplausos de coliseo y bombo festivo",
    val duckingReductionDb: Float = -14.0f
) {
    fun generateMusicSamples(): FloatArray {
        return AndeanAudioSynthesizer.generateRegionalHuayno(regionalStyle, durationSec)
    }

    fun generateIntroFxSamples(): FloatArray {
        return AndeanAudioSynthesizer.generateSpotIntroFx(durationSec = 2.5f)
    }

    fun generateOutroFxSamples(): FloatArray {
        return AndeanAudioSynthesizer.generateSpotOutroFx(durationSec = 3.5f)
    }
}

object SpotTemplateCatalog {
    val templates: List<HuaynoSpotTemplate> = listOf(
        HuaynoSpotTemplate(
            id = "template_fiesta_patronal_huancayo",
            title = "Gran Fiesta Patronal Santiago Apóstol",
            category = "Fiesta Patronal",
            tag = "Tradicional • Más Usado",
            regionalStyle = "Huayno Huanca / Huaylash",
            durationSec = 30.0f,
            recommendedBpm = 128,
            meter = HuaynoMeter.TWO_FOUR,
            eventName = "Fiesta Patronal Santiago Apóstol 2026",
            dateTime = "Sábado 25 de Julio desde las 11:00 AM",
            venue = "Complejo San Juan, Chupaca - Huancayo",
            artists = "Amanda Portales, Pelo D'Ambrosio y orquesta Los Ases de Huancayo",
            contactWhatsapp = "+51 964 882 119",
            ticketPrice = "General S/ 30.00 (Incluye plato típico de lechón)",
            scriptText = "¡Atención Valle del Mantaro! ¡Llegó la auténtica fiesta de nuestras raíces! Este sábado veinticinco de julio, celebramos la tradicional Fiesta Patronal Santiago Apóstol en el Complejo San Juan de Chupaca. Zapatearemos con Amanda Portales y la Gran Orquesta Los Ases. ¡Habrá rica pachamanca, cerveza helada y el mejor contrapunto de Huaylash! Reservas al nueve seis cuatro, ocho ocho dos, uno uno nueve. ¡No te quedes sin tu entrada!",
            recommendedVoice = "Don Floro (Voz Andina Tradicional)",
            voiceSpeed = 1.05f
        ),

        HuaynoSpotTemplate(
            id = "template_aniversario_arpa",
            title = "Gran Aniversario: Los Reyes del Arpa",
            category = "Aniversario",
            tag = "Arpa Huancaína • Estelar",
            regionalStyle = "Huayno con Arpa Central",
            durationSec = 45.0f,
            recommendedBpm = 116,
            meter = HuaynoMeter.FOUR_FOUR,
            eventName = "Vigésimo Aniversario Agrupación Los Reyes del Arpa",
            dateTime = "Viernes 14 de Agosto, 8:00 PM",
            venue = "Coliseo Andino de Lima Norte (Óvalo Huandoy)",
            artists = "Dina Páucar, Sonia Morales, Anita Santivañez y Los Reyes del Arpa",
            contactWhatsapp = "+51 987 114 300",
            ticketPrice = "VIP S/ 60.00 • General S/ 35.00",
            scriptText = "¡El concierto del año en Lima Norte! Veinte años consagrados en el corazón del Perú. Este viernes catorce de agosto, tiramos la casa por la ventana con el Aniversario de Los Reyes del Arpa en el Coliseo Andino de Huandoy. En un solo escenario: la elegancia de Dina Páucar, Sonia Morales y Anita Santivañez. Pantallas gigantes, sonido de exportación y la fiesta andina más grande de la capital. Adquiere tus entradas al nueve ocho siete, uno uno cuatro, trescientos. ¡Los Reyes del Arpa te esperan!",
            recommendedVoice = "Locutor Radial del Centro (Potente)",
            voiceSpeed = 1.02f
        ),

        HuaynoSpotTemplate(
            id = "template_lanzamiento_requinto",
            title = "Lanzamiento Primicia Huayno con Requinto",
            category = "Lanzamiento Primicia",
            tag = "Requinto Sureño • Novedad",
            regionalStyle = "Huayno con Requinto / Sureño",
            durationSec = 30.0f,
            recommendedBpm = 108,
            meter = HuaynoMeter.TWO_FOUR,
            eventName = "Noche de Estreno: Primicias 2026",
            dateTime = "Domingo 9 de Mayo desde el mediodía",
            venue = "Local Los Sauces, Ate Vitarte - Lima",
            artists = "Chinito del Ande y Rosita de Huaraz",
            contactWhatsapp = "+51 951 440 228",
            ticketPrice = "S/ 25.00",
            scriptText = "¡Exclusivo para los amantes del requinto! Este domingo nueve de mayo, presentación oficial del nuevo videoclip y primicias dos mil veintiséis con el Chinito del Ande y Rosita de Huaraz en el Local Los Sauces de Ate. Canciones para llorar y zapatear con orgullo andino. ¡Informes al nueve cinco uno, cuatro cuatro cero, dos dos ocho! ¡Te esperamos!",
            recommendedVoice = "Rosita (Festiva y Dulce)",
            voiceSpeed = 1.06f
        ),

        HuaynoSpotTemplate(
            id = "template_duelo_zapateo",
            title = "Gran Duelo de Zapateo & Huaylash 15s",
            category = "Zapateo / Huaylash",
            tag = "Flash 15s • Enérgico",
            regionalStyle = "Huayno Huanca / Huaylash",
            durationSec = 15.0f,
            recommendedBpm = 134,
            meter = HuaynoMeter.TWO_FOUR,
            eventName = "Duelo de Campeones de Zapateo",
            dateTime = "Este Sábado 7:00 PM",
            venue = "Estadio Municipal de Sicaya",
            artists = "Cuadrillas Campeonas del Mantaro",
            contactWhatsapp = "+51 964 123 456",
            ticketPrice = "S/ 15.00",
            scriptText = "¡Pisa fuerte la tierra! Este sábado, duelo de zapateo en Sicaya con los mejores del Mantaro. ¡Premios en efectivo y orquesta en vivo! ¡No faltes!",
            recommendedVoice = "Don Floro (Voz Andina Tradicional)",
            voiceSpeed = 1.15f
        ),

        HuaynoSpotTemplate(
            id = "template_restaurante_campestre",
            title = "Restaurante Campestre El Rincón Andino",
            category = "Restaurante Campestre",
            tag = "Familiar • Ayacuchano",
            regionalStyle = "Huayno Ayacuchano Tradicional",
            durationSec = 30.0f,
            recommendedBpm = 96,
            meter = HuaynoMeter.TWO_FOUR,
            eventName = "Almuerzos Campestres & Música en Vivo",
            dateTime = "Todos los Sábados y Domingos",
            venue = "Valle de Cieneguilla, Km 18",
            artists = "Conjunto de Arpa y Guitarra Ayacuchana",
            contactWhatsapp = "+51 977 333 444",
            ticketPrice = "Ingreso libre con consumo",
            scriptText = "Disfruta el sabor auténtico de nuestra sierra en Restaurante Campestre El Rincón Andino en Cieneguilla. Exquisita trucha frita, chicharrón dorado y cuy chactado con la dulce melodía del arpa ayacuchana. Espacio para toda la familia, juegos y aire puro. Reservas al nueve siete siete, tres tres tres, cuatro cuatro cuatro.",
            recommendedVoice = "Rosita (Festiva y Dulce)",
            voiceSpeed = 0.98f
        ),

        HuaynoSpotTemplate(
            id = "template_baile_social_patrias",
            title = "Gran Baile Social: Fiestas Patrias del Perú",
            category = "Comercial",
            tag = "Nacional • 60s Completo",
            regionalStyle = "Huayno con Arpa Central",
            durationSec = 60.0f,
            recommendedBpm = 120,
            meter = HuaynoMeter.FOUR_FOUR,
            eventName = "Serenata a la Patria: Orgullo Huayno",
            dateTime = "Lunes 27 de Julio desde las 6:00 PM",
            venue = "Parque Zonal Huiracocha, San Juan de Lurigancho",
            artists = "Mac Salvador, Max Castro, William Luna y agrupación Los Andinos",
            contactWhatsapp = "+51 940 889 900",
            ticketPrice = "Preventa S/ 40.00",
            scriptText = "¡Celebremos el aniversario patrio como manda la tradición! Este veintisiete de julio, serenata a nuestra bandera en el Parque Zonal Huiracocha de San Juan de Lurigancho. Los mayores exponentes de la música andina contemporánea juntos: Mac Salvador, Max Castro y William Luna en una noche irrepetible. Bombos, vientos andinos, luces robóticas y la cerveza más helada para cantar a todo pulmón. Preventa con descuento al nueve cuatro cero, ocho ocho nueve, nueve cero cero. ¡Que viva el Perú y que viva nuestro Huayno!",
            recommendedVoice = "Locutor Radial del Centro (Potente)",
            voiceSpeed = 1.04f
        )
    )
}
