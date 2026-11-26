package com.project.destronix.data.repository

import com.project.destronix.core.AppResult
import com.project.destronix.core.DispatcherProvider
import com.project.destronix.core.safeCall
import com.project.destronix.data.remote.JikanApiService
import com.project.destronix.data.remote.dto.AnimeDto
import com.project.destronix.data.remote.mapper.toDomain
import com.project.destronix.domain.model.Anime
import com.project.destronix.domain.model.AnimeDetails
import com.project.destronix.domain.model.HomeFeed
import com.project.destronix.domain.model.Paged
import com.project.destronix.domain.model.SearchFilters
import com.project.destronix.domain.repository.AnimeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class AnimeRepositoryImpl(
    private val api: JikanApiService,
    private val dispatchers: DispatcherProvider,
) : AnimeRepository {

    override suspend fun getHomeFeed(): AppResult<HomeFeed> = safeCall(dispatchers) {
        coroutineScope {
            val airing = async { api.getTopAnime(filter = "airing", limit = 12) }
            val movies = async { api.getTopAnime(filter = "movie", limit = 12) }
            val upcoming = async { api.getTopAnime(filter = "upcoming", limit = 12) }
            val seasonal = async { api.getCurrentSeason(limit = 16) }
            val genres = async { api.getGenres(limit = 24) }

            HomeFeed(
                topAiring = airing.await().data.map(AnimeDto::toDomain),
                movies = movies.await().data.map(AnimeDto::toDomain),
                topUpcoming = upcoming.await().data.map(AnimeDto::toDomain),
                seasonal = seasonal.await().data.map(AnimeDto::toDomain),
                genres = genres.await().data.map { it.toDomain() },
            )
        }
    }

    override suspend fun searchAnime(
        query: String,
        filters: SearchFilters,
        page: Int,
    ): AppResult<Paged<Anime>> = safeCall(dispatchers) {
        val response = api.searchAnime(
            query = query,
            page = page,
            type = filters.type,
            status = filters.status,
            minScore = filters.minScore,
            genres = filters.genres.takeIf { it.isNotEmpty() }?.joinToString(","),
            startDate = filters.year?.let { "$it-01-01" },
            orderBy = filters.orderBy,
        )
        val pagination = response.pagination
        Paged(
            items = response.data.map(AnimeDto::toDomain),
            currentPage = pagination?.currentPage ?: page,
            totalPages = pagination?.lastVisiblePage ?: 1,
        )
    }

    override suspend fun getAnimeDetails(id: Int): AppResult<AnimeDetails> = safeCall(dispatchers) {
        coroutineScope {
            val anime = async { api.getAnime(id).data }
            val characters = async { api.getCharacters(id).data }
            val videos = async { api.getVideos(id).data }
            val recommendations = async { api.getRecommendations(id).data }
            val relations = async { api.getRelations(id).data }

            val animeDto = anime.await()
            AnimeDetails(
                anime = animeDto.toDomain(),
                background = animeDto.background?.takeIf { it.isNotBlank() }?.removeHtml(),
                broadcast = animeDto.broadcast,
                duration = animeDto.duration,
                rating = animeDto.rating,
                source = animeDto.source,
                characters = characters.await().map { it.toDomain() },
                videos = videos.await().promo.mapNotNull { it.toDomain() },
                recommendations = recommendations.await().map { it.entry.toDomain() },
                related = relations.await().map { it.toDomain() },
            )
        }
    }
}

private fun String.removeHtml(): String = replace(Regex("<[^>]+>"), "").trim()
