package com.project.destronix.data.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AnimeConfigLoaderTest {

    @Test
    fun `blank config falls back to Jikan`() {
        val config = AnimeConfigLoader.parse("")

        assertEquals("jikan", config.activeSource.id)
        assertEquals("https://api.jikan.moe/v4/", config.activeSource.apiBaseUrl)
    }

    @Test
    fun `invalid json falls back to Jikan`() {
        val config = AnimeConfigLoader.parse("{ esto no es json")

        assertEquals("jikan", config.activeSource.id)
        assertEquals("https://api.jikan.moe/v4/", config.activeSource.apiBaseUrl)
    }

    @Test
    fun `urls are normalized and active source is selected`() {
        val json = """
            {
              "activeSourceId": "alternativa",
              "sources": [
                {
                  "id": "jikan",
                  "name": "Jikan",
                  "apiBaseUrl": "https://api.jikan.moe/v4",
                  "streamBaseUrl": "https://streams.test",
                  "websiteUrl": "https://jikan.moe",
                  "enabled": true
                },
                {
                  "id": "alternativa",
                  "name": "Alternativa",
                  "apiBaseUrl": "https://otro.test/api",
                  "enabled": true
                }
              ]
            }
        """.trimIndent()

        val config = AnimeConfigLoader.parse(json)

        assertEquals("alternativa", config.activeSource.id)
        assertEquals("https://otro.test/api/", config.activeSource.apiBaseUrl)
        assertEquals("https://streams.test/", config.sources.first().streamBaseUrl)
        assertEquals("https://jikan.moe/", config.sources.first().websiteUrl)
    }

    @Test
    fun `disabled and invalid sources are ignored`() {
        val json = """
            {
              "sources": [
                { "id": "mala", "apiBaseUrl": "ftp://no-valido", "enabled": true },
                { "id": "desactivada", "apiBaseUrl": "https://off.test/api", "enabled": false },
                { "id": "buena", "apiBaseUrl": "https://buena.test/api" }
              ]
            }
        """.trimIndent()

        val config = AnimeConfigLoader.parse(json)

        assertEquals(1, config.sources.size)
        assertEquals("buena", config.activeSource.id)
        assertNull(config.activeSource.websiteUrl)
    }

    @Test
    fun `unknown active source falls back to the first enabled source`() {
        val json = """
            {
              "activeSourceId": "inexistente",
              "sources": [
                { "id": "primera", "apiBaseUrl": "https://primera.test/api" },
                { "id": "segunda", "apiBaseUrl": "https://segunda.test/api" }
              ]
            }
        """.trimIndent()

        val config = AnimeConfigLoader.parse(json)

        assertEquals("primera", config.activeSource.id)
    }
}
