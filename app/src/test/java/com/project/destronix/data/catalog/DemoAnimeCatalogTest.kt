package com.project.destronix.data.catalog

import com.project.destronix.domain.model.Anime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoAnimeCatalogTest {

    @Test
    fun `parses test animes from json`() {
        val json = """
            [
              { "id": 9001, "title": "Neon Rider", "titleEnglish": "Neon Rider: Awakening", "score": 8.7 },
              { "id": 9002, "title": "Luna de papel" },
              { "id": 0, "title": "Sin id" }
            ]
        """.trimIndent()

        val animes = DemoAnimeCatalog.parse(json)

        assertEquals(2, animes.size)
        assertEquals("Neon Rider: Awakening", animes.first().displayTitle)
        assertTrue(animes.none { it.id == 0 })
    }

    @Test
    fun `invalid json falls back to bundled examples`() {
        val animes = DemoAnimeCatalog.parse("{ esto no es json")

        assertEquals(3, animes.size)
        assertTrue(animes.any { it.title == "Neon Rider" })
        assertTrue(animes.all { it.id >= 9000 })
    }

    @Test
    fun `empty list falls back to bundled examples`() {
        val animes: List<Anime> = DemoAnimeCatalog.parse("[]")

        assertEquals(3, animes.size)
    }
}
