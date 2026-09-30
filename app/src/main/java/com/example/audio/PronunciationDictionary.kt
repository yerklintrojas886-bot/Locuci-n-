package com.example.audio

/**
 * Diccionario de pronunciación andino para nombres quechuas, localidades y apodos folclóricos.
 * Permite adaptar textos para que los motores de síntesis de voz (TTS) pronuncien adecuadamente
 * los fonemas andinos peruanos.
 */
data class PronunciationRule(
    val id: String,
    val originalWord: String,
    val phoneticReplacement: String,
    val category: String, // "Localidades", "Quechua / Tradición", "Apodos y Expresiones", "Personalizado"
    val description: String = ""
)

object PronunciationDictionary {

    val defaultRules = listOf(
        // Localidades y Regiones
        PronunciationRule("loc_1", "Huancavelica", "Huanca-velíca", "Localidades", "Acentuación clara en la penúltima sílaba"),
        PronunciationRule("loc_2", "Huamanga", "Wamánga", "Localidades", "Pronunciación w andina"),
        PronunciationRule("loc_3", "Vilcashuamán", "Vilcas-wamán", "Localidades", "Sonoridad ayacuchana"),
        PronunciationRule("loc_4", "Paucartambo", "Páucar-tambo", "Localidades", "Cusco tradicional"),
        PronunciationRule("loc_5", "Cajatambo", "Cája-tambo", "Localidades", "Sierra limeña"),
        PronunciationRule("loc_6", "Yauyos", "Yáuyos", "Localidades", "Énfasis en la primera sílaba"),
        PronunciationRule("loc_7", "Chincheros", "Chíncheros", "Localidades", "Apurímac"),
        PronunciationRule("loc_8", "Apurímac", "Apúrimac", "Localidades", "Pronunciación acentuada"),
        PronunciationRule("loc_9", "Tayacaja", "Taya-cája", "Localidades", "Huancavelica"),
        PronunciationRule("loc_10", "Paucarbamba", "Páucar-bamba", "Localidades", "Huánuco / Huancavelica"),
        PronunciationRule("loc_11", "Jauja", "Jáuja", "Localidades", "Valle del Mantaro"),

        // Quechua y Tradición
        PronunciationRule("q_1", "Qorilazo", "Cori-lázo", "Quechua / Tradición", "Hombre de oro, Chumbivilcas"),
        PronunciationRule("q_2", "Pukllay", "Púc-llay", "Quechua / Tradición", "Carnaval andino"),
        PronunciationRule("q_3", "Wanka", "Huánca", "Quechua / Tradición", "Pueblo y cultura del Mantaro"),
        PronunciationRule("q_4", "Chanka", "Chánca", "Quechua / Tradición", "Cultura andina guerrera"),
        PronunciationRule("q_5", "Urpicha", "Urpí-cha", "Quechua / Tradición", "Palomita / amada"),
        PronunciationRule("q_6", "Huaylash", "Huáylash", "Quechua / Tradición", "Danza del zapateo huanca"),
        PronunciationRule("q_7", "Chimayche", "Chimáy-che", "Quechua / Tradición", "Ritmo de Pomabamba, Ancash"),
        PronunciationRule("q_8", "Sikuri", "Sicúri", "Quechua / Tradición", "Zampoñistas del altiplano"),

        // Expresiones y Animación Radial
        PronunciationRule("exp_1", "¡Zapateo!", "¡Sapa-téo!", "Apodos y Expresiones", "Pronunciación festiva radial"),
        PronunciationRule("exp_2", "¡Gózalo!", "¡Gó-sa-lo!", "Apodos y Expresiones", "Grito clásico de animador"),
        PronunciationRule("exp_3", "¡Atención Perú!", "¡Atenssión Perú!", "Apodos y Expresiones", "Llamado enérgico"),
        PronunciationRule("exp_4", "Bordón", "Bordónn", "Apodos y Expresiones", "Bajo profundo del arpa")
    )

    /**
     * Aplica la lista de reglas al texto para optimizar la dicción del locutor sintético.
     */
    fun applyDictionary(text: String, customRules: List<PronunciationRule> = emptyList()): String {
        var processed = text
        val all = customRules + defaultRules

        for (rule in all) {
            val pattern = "\\b${Regex.escape(rule.originalWord)}\\b".toRegex(RegexOption.IGNORE_CASE)
            processed = processed.replace(pattern, rule.phoneticReplacement)
        }
        return processed
    }
}
