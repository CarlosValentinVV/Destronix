package com.project.destronix.data.repository

import com.project.destronix.core.AppError
import com.project.destronix.core.AppResult
import com.project.destronix.core.DataSource
import com.project.destronix.data.catalog.DemoAnimeCatalog
import com.project.destronix.domain.model.SearchFilters
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoAnimeRepositoryTest {

    private val repository = DemoAnimeRepository(DemoAnimeCatalog.parse(CATALOG_JSON))

    @Test
    fun `search matches title english title and synopsis`() = runTest {
        val byTitle = repository.searchAnime("neon")
        val byEnglishTitle = repository.searchAnime("awakening")
        val bySynopsis = repository.searchAnime("marea")

        assertEquals(listOf(9001), byTitle.ids())
        assertEquals(listOf(9001), byEnglishTitle.ids())
        assertEquals(listOf(9005), bySynopsis.ids())
    }

    @Test
    fun `search applies status score year and genre filters`() = runTest {
        assertEquals(listOf(9001, 9005), repository.searchAnime("", SearchFilters(status = "airing")).ids())
        assertEquals(listOf(9004), repository.searchAnime("", SearchFilters(minScore = 9.0)).ids())
        assertEquals(
            listOf(9001, 9005, 9007),
            repository.searchAnime("", SearchFilters(year = 2026)).ids(),
        )
        assertEquals(
            listOf(9001, 9004),
            repository.searchAnime("", SearchFilters(genres = listOf(1))).ids(),
        )
    }

    @Test
    fun `search ignores accents and orders by title`() = runTest {
        val result = repository.searchAnime(
            query = "fantasia",
            filters = SearchFilters(),
        )
        assertEquals(listOf(9004, 9005, 9008), result.ids())

        val ordered = repository.searchAnime("", SearchFilters(orderBy = "title"))
        val titles = (ordered as AppResult.Success).data.items.map { it.displayTitle }
        assertEquals(titles.sorted(), titles)
    }

    @Test
    fun `home feed groups the demo animes`() = runTest {
        val result = repository.getHomeFeed() as AppResult.Success

        assertEquals(DataSource.LOCAL, result.source)
        assertEquals(listOf(9001, 9005), result.data.topAiring.map { it.id })
        assertEquals(listOf(9007), result.data.topUpcoming.map { it.id })
        assertEquals(listOf(9004), result.data.movies.map { it.id })
        assertTrue(result.data.genres.any { it.name == "Action" })
    }

    @Test
    fun `details include recommendations and missing ids return not found`() = runTest {
        val details = repository.getAnimeDetails(9001) as AppResult.Success
        assertEquals(9001, details.data.anime.id)
        assertTrue(details.data.recommendations.none { it.id == 9001 })

        assertEquals(AppResult.Error(AppError.NotFound), repository.getAnimeDetails(1234))
    }

    private fun AppResult<com.project.destronix.domain.model.Paged<com.project.destronix.domain.model.Anime>>.ids(): List<Int> =
        ((this as AppResult.Success).data.items.map { it.id })

    private companion object {
        val CATALOG_JSON = """
            [
              {
                "id": 9001,
                "title": "Neon Rider",
                "titleEnglish": "Neon Rider: Awakening",
                "score": 8.7,
                "episodes": 12,
                "status": "Currently Airing",
                "type": "TV",
                "year": 2026,
                "genres": ["Action", "Ciencia ficcion"],
                "synopsis": "Un repartidor nocturno."
              },
              {
                "id": 9004,
                "title": "Espada y eco",
                "score": 9.1,
                "episodes": 1,
                "status": "Finished Airing",
                "type": "Movie",
                "year": 2025,
                "genres": ["Action", "Fantasia"],
                "synopsis": "La ultima espadachin."
              },
              {
                "id": 9005,
                "title": "Mareas de cristal",
                "score": 8.5,
                "status": "Currently Airing",
                "type": "TV",
                "year": 2026,
                "genres": ["Fantasia", "Supernatural"],
                "synopsis": "Cada marea devuelve un recuerdo."
              },
              {
                "id": 9007,
                "title": "Cuarto oscuro",
                "score": 8.0,
                "status": "Not yet aired",
                "type": "TV",
                "year": 2026,
                "genres": ["Horror", "Misterio"],
                "synopsis": "Una puerta cambia cada noche."
              },
              {
                "id": 9008,
                "title": "Bosque de tinta",
                "score": 8.9,
                "status": "Finished Airing",
                "type": "TV",
                "year": 2025,
                "genres": ["Aventura", "Fantasia"],
                "synopsis": "Un bosque de fantasia."
              }
            ]
        """.trimIndent()
    }
}
