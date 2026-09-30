package com.example

import com.example.audio.HuaynoMeter
import com.example.audio.MetronomeConfig
import com.example.audio.MetronomePreset
import com.example.audio.MetronomeTimbre
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HuaynoMetronomeTest {

    @Test
    fun testDefaultHuaynoMetronomeConfig() {
        val config = MetronomeConfig()
        assertEquals(HuaynoMeter.TWO_FOUR, config.meter)
        assertEquals(2, config.meter.beatsPerBar)
        assertEquals("2/4", config.meter.label)
        assertEquals(108, config.bpm)
        assertEquals(MetronomeTimbre.WANKAR, config.timbre)
        assertEquals(1, config.countInBars)
        assertTrue(config.playDuringRecording)
    }

    @Test
    fun testHuaynoTimeSignatures() {
        // 2/4 compás tradicional del Huayno
        assertEquals(2, HuaynoMeter.TWO_FOUR.beatsPerBar)
        assertEquals("2/4", HuaynoMeter.TWO_FOUR.label)

        // 4/4 compás del Huayno Moderno con Arpa
        assertEquals(4, HuaynoMeter.FOUR_FOUR.beatsPerBar)
        assertEquals("4/4", HuaynoMeter.FOUR_FOUR.label)
    }

    @Test
    fun testHuaynoPresets() {
        val presetAyacuchano = MetronomePreset(
            name = "Huayno Ayacuchano Tradicional",
            bpm = 104,
            meter = HuaynoMeter.TWO_FOUR,
            style = "Bordoneo y arpa"
        )
        assertEquals(104, presetAyacuchano.bpm)
        assertEquals(HuaynoMeter.TWO_FOUR, presetAyacuchano.meter)

        val presetArpa = MetronomePreset(
            name = "Huayno con Arpa Central",
            bpm = 116,
            meter = HuaynoMeter.FOUR_FOUR,
            style = "Arpa huancaína y teclado"
        )
        assertEquals(116, presetArpa.bpm)
        assertEquals(HuaynoMeter.FOUR_FOUR, presetArpa.meter)
    }

    @Test
    fun testMetronomeTimbreChoices() {
        val wankar = MetronomeTimbre.WANKAR
        val cowbell = MetronomeTimbre.COWBELL
        val digital = MetronomeTimbre.DIGITAL

        assertEquals("Wankar / Madera Andina", wankar.displayName)
        assertEquals("Cencerro Huaylash", cowbell.displayName)
        assertEquals("Beep Digital de Estudio", digital.displayName)
    }
}
