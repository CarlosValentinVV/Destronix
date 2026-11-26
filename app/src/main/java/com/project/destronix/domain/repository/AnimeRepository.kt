package com.project.destronix.domain.repository

import com.project.destronix.core.AppResult
import com.project.destronix.domain.model.Anime
import com.project.destronix.domain.model.AnimeDetails
import com.project.destronix.domain.model.HomeFeed
import com.project.destronix.domain.model.Paged
import com.project.destronix.domain.model.SearchFilters

/** Contrato de datos de catálogo. La UI nunca conoce la API real. */
interface AnimeRepository {
    suspend fun getHomeFeed(): AppResult<HomeFeed>
    suspend fun searchAnime(
        query: String,
        filters: SearchFilters = SearchFilters(),
        page: Int = 1,
    ): AppResult<Paged<Anime>>
    suspend fun getAnimeDetails(id: Int): AppResult<AnimeDetails>
}
