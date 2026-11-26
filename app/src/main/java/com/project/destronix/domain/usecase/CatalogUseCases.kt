package com.project.destronix.domain.usecase

import com.project.destronix.core.AppResult
import com.project.destronix.domain.model.Anime
import com.project.destronix.domain.model.AnimeDetails
import com.project.destronix.domain.model.HomeFeed
import com.project.destronix.domain.model.Paged
import com.project.destronix.domain.model.SearchFilters
import com.project.destronix.domain.repository.AnimeRepository

class GetHomeFeedUseCase(private val repository: AnimeRepository) {
    suspend operator fun invoke(): AppResult<HomeFeed> = repository.getHomeFeed()
}

class SearchAnimeUseCase(private val repository: AnimeRepository) {
    suspend operator fun invoke(
        query: String,
        filters: SearchFilters = SearchFilters(),
        page: Int = 1,
    ): AppResult<Paged<Anime>> = repository.searchAnime(query.trim(), filters, page)
}

class GetAnimeDetailsUseCase(private val repository: AnimeRepository) {
    suspend operator fun invoke(id: Int): AppResult<AnimeDetails> = repository.getAnimeDetails(id)
}
