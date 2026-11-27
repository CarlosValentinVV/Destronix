package com.project.destronix.data.config

import com.project.destronix.data.config.CatalogMode
import com.project.destronix.domain.model.VideoServerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun `servers are parsed from the active source`() {
        val json = """
            {
              "activeSourceId": "principal",
              "sources": [
                {
                  "id": "principal",
                  "apiBaseUrl": "https://api.test/v4",
                  "servers": [
                    { "id": "desu", "name": "Desu", "baseUrl": "https://cdn.test/videos", "type": "HLS", "language": "SUB" },
                    { "name": "Directo", "baseUrl": "https://otro.test/videos/" },
                    { "id": "roto", "baseUrl": "ftp://no-valido" },
                    { "id": "off", "baseUrl": "https://off.test", "enabled": false }
                  ]
                }
              ]
            }
        """.trimIndent()

        val servers = AnimeConfigLoader.parse(json).activeSource.servers

        assertEquals(2, servers.size)
        assertEquals("desu", servers[0].id)
        assertEquals("Desu", servers[0].name)
        assertEquals("https://cdn.test/videos", servers[0].baseUrl)
        assertEquals(VideoServerType.HLS, servers[0].type)
        assertEquals("SUB", servers[0].language)
        assertEquals(VideoServerType.DIRECT, servers[1].type)
        assertNull(servers[1].language)
        assertTrue(servers.none { it.id == "roto" || it.id == "off" })
    }

    @Test
    fun `demo mode is parsed and does not require an api url`() {
        val json = """
            {
              "activeSourceId": "demo",
              "sources": [
                {
                  "id": "demo",
                  "name": "Pruebas",
                  "catalogMode": "DEMO",
                  "servers": []
                }
              ]
            }
        """.trimIndent()

        val config = AnimeConfigLoader.parse(json)

        assertEquals(CatalogMode.DEMO, config.activeSource.catalogMode)
        assertEquals("https://api.jikan.moe/v4/", config.activeSource.apiBaseUrl)
        assertEquals(CatalogMode.JIKAN, AnimeSourcesConfig.DEFAULT.sources.first().catalogMode)
    }
}
