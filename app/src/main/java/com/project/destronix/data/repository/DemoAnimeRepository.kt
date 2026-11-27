package com.project.destronix.data.repository

import com.project.destronix.core.AppError
import com.project.destronix.core.AppResult
import com.project.destronix.core.DataSource
import com.project.destronix.domain.model.Anime
import com.project.destronix.domain.model.AnimeDetails
import com.project.destronix.domain.model.AnimeGenre
import com.project.destronix.domain.model.HomeFeed
import com.project.destronix.domain.model.Paged
import com.project.destronix.domain.model.SearchFilters
import com.project.destronix.domain.repository.AnimeRepository
import java.text.Normalizer

/** Repositorio local para probar búsqueda, fichas y reproductor sin red. */
class DemoAnimeRepository(private val animes: List<Anime>) : AnimeRepository {

    private val byId: Map<Int, Anime> = animes.associateBy { it.id }

    override suspend fun getHomeFeed(): AppResult<HomeFeed> = local(
        HomeFeed(
            topAiring = matchStatus("airing").take(6),
            topUpcoming = matchStatus("upcoming").take(6),
            seasonal = animes.filter { it.year == 2026 }.take(8),
            movies = animes.filter { it.type.equals("movie", ignoreCase = true) }.take(6),
            genres = GENRES.map { (id, name) -> AnimeGenre(id = id, name = name) },
        ),
    )

    override suspend fun searchAnime(
        query: String,
        filters: SearchFilters,
        page: Int,
    ): AppResult<Paged<Anime>> {
        val normalizedQuery = query.normalize()
        val genreNames = filters.genres.mapNotNull { GENRES[it] }

        var results = animes.filter { anime ->
            val searchable = listOfNotNull(
                anime.title,
                anime.titleEnglish,
                anime.synopsis,
            ).plus(anime.genres).joinToString(" ").normalize()

            (normalizedQuery.isBlank() || normalizedQuery.split(" ").all { it in searchable }) &&
                filters.type?.let { anime.type.equals(it, ignoreCase = true) } != false &&
                filters.status?.let { anime.matchesStatus(it) } != false &&
                filters.minScore?.let { (anime.score ?: 0.0) >= it } != false &&
                filters.year?.let { anime.year == it } != false &&
                (genreNames.isEmpty() || genreNames.any { it in anime.genres })
        }

        results = when (filters.orderBy?.lowercase()) {
            "title" -> results.sortedBy { it.displayTitle.normalize() }
            "popularity" -> results.sortedBy { it.popularity ?: Int.MAX_VALUE }
            "score" -> results.sortedByDescending { it.score ?: 0.0 }
            else -> results
        }

        return local(
            Paged(
                items = if (page == 1) results else emptyList(),
                currentPage = page,
                totalPages = 1,
            ),
        )
    }

    override suspend fun getAnimeDetails(id: Int): AppResult<AnimeDetails> {
        val anime = byId[id] ?: return AppResult.Error(AppError.NotFound)
        return local(
            AnimeDetails(
                anime = anime,
                background = null,
                broadcast = null,
                duration = null,
                rating = null,
                source = null,
                recommendations = animes.filter { it.id != anime.id }.take(6),
            ),
        )
    }

    private fun matchStatus(status: String): List<Anime> = animes.filter { it.matchesStatus(status) }

    private fun Anime.matchesStatus(status: String): Boolean = when (status.lowercase()) {
        "airing" -> this.status.equals("Currently Airing", ignoreCase = true)
        "finished" -> this.status.equals("Finished Airing", ignoreCase = true)
        "upcoming" -> this.status.equals("Not yet aired", ignoreCase = true)
        else -> true
    }

    private fun String.normalize(): String =
        Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .lowercase()

    private fun <T> local(data: T): AppResult<T> = AppResult.Success(data, DataSource.LOCAL)

    companion object {
        /** IDs compatibles con los géneros de Jikan, usados por los chips de Inicio. */
        val GENRES: Map<Int, String> = linkedMapOf(
            1 to "Action",
            2 to "Aventura",
            4 to "Comedia",
            7 to "Misterio",
            8 to "Drama",
            10 to "Fantasia",
            14 to "Horror",
            22 to "Ciencia ficcion",
            25 to "Shounen",
            26 to "Deportes",
            36 to "Recuentos de la vida",
            37 to "Supernatural",
        )
    }
}
