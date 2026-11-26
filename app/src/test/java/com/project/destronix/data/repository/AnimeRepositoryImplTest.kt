package com.project.destronix.data.repository

import com.project.destronix.core.AppError
import com.project.destronix.core.AppResult
import com.project.destronix.core.TestDispatcherProvider
import com.project.destronix.data.remote.JikanApiService
import com.project.destronix.data.remote.dto.AnimeDto
import com.project.destronix.data.remote.dto.CharacterWrapperDto
import com.project.destronix.data.remote.dto.GenreDto
import com.project.destronix.data.remote.dto.JikanDataResponse
import com.project.destronix.data.remote.dto.JikanListResponse
import com.project.destronix.data.remote.dto.JikanPagination
import com.project.destronix.data.remote.dto.JikanVideosDto
import com.project.destronix.data.remote.dto.RecommendationDto
import com.project.destronix.data.remote.dto.RelationDto
import com.project.destronix.domain.model.SearchFilters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeRepositoryImplTest {

    private val fakeApi = FakeJikanApiService()
    private val repository = AnimeRepositoryImpl(
        fakeApi,
        TestDispatcherProvider(UnconfinedTestDispatcher()),
    )

    @Test
    fun `home feed combines every section`() = runTest {
        val result = repository.getHomeFeed()

        assertTrue(result is AppResult.Success)
        val feed = (result as AppResult.Success).data
        assertEquals(listOf("airing"), feed.topAiring.map { it.title })
        assertEquals(listOf("movie"), feed.movies.map { it.title })
        assertEquals(listOf("Season"), feed.seasonal.map { it.title })
        assertEquals(listOf("Acción"), feed.genres.map { it.name })
    }

    @Test
    fun `search maps pagination into domain`() = runTest {
        fakeApi.searchResponse = JikanListResponse(
            data = listOf(animeDto(7, "Found")),
            pagination = JikanPagination(currentPage = 2, lastVisiblePage = 5),
        )

        val result = repository.searchAnime("found", SearchFilters(type = "tv"), page = 2)

        assertTrue(result is AppResult.Success)
        val paged = (result as AppResult.Success).data
        assertEquals(listOf("Found"), paged.items.map { it.title })
        assertEquals(2, paged.currentPage)
        assertTrue(paged.hasNextPage)
        assertEquals("tv", fakeApi.lastType)
    }

    @Test
    fun `network failures become domain errors`() = runTest {
        fakeApi.failWith = IOException("offline")

        val result = repository.getHomeFeed()

        assertEquals(AppResult.Error(AppError.Network), result)
    }

    private fun animeDto(id: Int, title: String) = AnimeDto(malId = id, title = title)

    private class FakeJikanApiService : JikanApiService {
        var failWith: Throwable? = null
        var searchResponse = JikanListResponse<AnimeDto>()
        var lastType: String? = null

        private fun <T> checkFailure(): T {
            failWith?.let { throw it }
            error("Respuesta no configurada en el fake")
        }

        override suspend fun getTopAnime(filter: String?, limit: Int, page: Int): JikanListResponse<AnimeDto> {
            failWith?.let { throw it }
            return JikanListResponse(data = listOf(AnimeDto(malId = 1, title = filter ?: "Top")))
        }

        override suspend fun getCurrentSeason(limit: Int, page: Int): JikanListResponse<AnimeDto> {
            failWith?.let { throw it }
            return JikanListResponse(data = listOf(AnimeDto(malId = 2, title = "Season")))
        }

        override suspend fun getGenres(limit: Int): JikanListResponse<GenreDto> {
            failWith?.let { throw it }
            return JikanListResponse(data = listOf(GenreDto(malId = 1, name = "Acción")))
        }

        override suspend fun getAnime(id: Int): JikanDataResponse<AnimeDto> = checkFailure()

        override suspend fun getCharacters(id: Int): JikanListResponse<CharacterWrapperDto> =
            checkFailure()

        override suspend fun getVideos(id: Int): JikanDataResponse<JikanVideosDto> = checkFailure()

        override suspend fun getRecommendations(id: Int): JikanListResponse<RecommendationDto> =
            checkFailure()

        override suspend fun getRelations(id: Int): JikanListResponse<RelationDto> = checkFailure()

        override suspend fun searchAnime(
            query: String,
            page: Int,
            limit: Int,
            type: String?,
            status: String?,
            minScore: Double?,
            genres: String?,
            startDate: String?,
            orderBy: String?,
            sort: String?,
            sfw: Boolean,
        ): JikanListResponse<AnimeDto> {
            lastType = type
            return searchResponse
        }
    }
}
