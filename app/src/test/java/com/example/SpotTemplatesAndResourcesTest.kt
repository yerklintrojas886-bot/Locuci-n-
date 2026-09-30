package com.example

import com.example.data.AndeanResourceCatalog
import com.example.data.HuaynoDatabase
import com.example.data.ResourceCategory
import com.example.data.SpotTemplateCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpotTemplatesAndResourcesTest {

    @Test
    fun testSpotTemplatesCatalogCompleteness() {
        val templates = SpotTemplateCatalog.templates
        assertTrue("Templates catalog should contain at least 5 templates", templates.size >= 5)

        templates.forEach { template ->
            assertNotNull("Template ID must not be null", template.id)
            assertTrue("Template title must not be empty", template.title.isNotBlank())
            assertTrue("Template category must not be empty", template.category.isNotBlank())
            assertTrue("Duration must be positive", template.durationSec > 0f)
            assertTrue("Recommended BPM must be reasonable for Huayno (60-180)", template.recommendedBpm in 60..180)
            assertTrue("Script text must not be empty", template.scriptText.isNotBlank())
            assertTrue("Regional style must not be empty", template.regionalStyle.isNotBlank())

            // Test audio synthesis for template music and FX
            val musicSamples = template.generateMusicSamples()
            assertTrue("Music samples must be generated", musicSamples.isNotEmpty())
            val introSamples = template.generateIntroFxSamples()
            assertTrue("Intro FX samples must be generated", introSamples.isNotEmpty())
            val outroSamples = template.generateOutroFxSamples()
            assertTrue("Outro FX samples must be generated", outroSamples.isNotEmpty())
        }
    }

    @Test
    fun testAndeanResourcesCatalog() {
        val resources = AndeanResourceCatalog.resources
        assertTrue("Resources catalog must have at least 10 items", resources.size >= 10)

        val categories = resources.map { it.category }.toSet()
        assertEquals("Must cover all 4 resource categories", 4, categories.size)
        assertTrue(categories.contains(ResourceCategory.MUSIC_STEMS))
        assertTrue(categories.contains(ResourceCategory.SOUND_EFFECTS))
        assertTrue(categories.contains(ResourceCategory.ANIMATOR_CHEERS))
        assertTrue(categories.contains(ResourceCategory.HUAYNO_PERCUSSION))

        resources.forEach { res ->
            assertTrue("Resource name must not be blank", res.name.isNotBlank())
            assertTrue("Resource duration must be positive", res.durationSec > 0f)
            val samples = res.generator()
            assertTrue("Resource generator must produce samples for ${res.name}", samples.isNotEmpty())
        }
    }

    @Test
    fun testSecurityPasswordHashing() {
        val pass = "radio2026"
        val hash1 = HuaynoDatabase.hashPassword(pass)
        val hash2 = HuaynoDatabase.hashPassword(pass)
        assertEquals("Hashing same password must be deterministic", hash1, hash2)
        assertEquals("SHA-256 hex string must be 64 characters", 64, hash1.length)
        assertFalse("Hash must not equal plaintext", hash1 == pass)

        val adminHash = HuaynoDatabase.hashPassword("admin123")
        assertTrue("Different passwords produce different hashes", hash1 != adminHash)
    }
}
