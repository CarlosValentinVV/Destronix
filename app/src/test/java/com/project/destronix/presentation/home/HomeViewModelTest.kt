package com.project.destronix.presentation.home

import com.project.destronix.core.AppError
import com.project.destronix.core.AppResult
import com.project.destronix.domain.model.Anime
import com.project.destronix.domain.model.AnimeDetails
import com.project.destronix.domain.model.HomeFeed
import com.project.destronix.domain.model.Paged
import com.project.destronix.domain.model.SearchFilters
import com.project.destronix.domain.repository.AnimeRepository
import com.project.destronix.domain.usecase.GetHomeFeedUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.After
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val repository = FakeAnimeRepository()

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load publishes the feed and clears loading`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        repository.result = AppResult.Success(HomeFeed(topAiring = listOf(anime())))
        val viewModel = HomeViewModel(GetHomeFeedUseCase(repository))

        assertNotNull(viewModel.uiState.feed)
        assertEquals(1, viewModel.uiState.feed?.topAiring?.size)
        assertFalse(viewModel.uiState.isLoading)
    }

    @Test
    fun `failed load exposes a domain error`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        repository.result = AppResult.Error(AppError.Network)
        val viewModel = HomeViewModel(GetHomeFeedUseCase(repository))

        assertEquals(AppError.Network, viewModel.uiState.error)
        assertFalse(viewModel.uiState.isLoading)
    }

    private fun anime() = Anime(id = 1, title = "Naruto")

    private class FakeAnimeRepository : AnimeRepository {
        var result: AppResult<HomeFeed> = AppResult.Loading

        override suspend fun getHomeFeed(): AppResult<HomeFeed> = result

        override suspend fun searchAnime(
            query: String,
            filters: SearchFilters,
            page: Int,
        ): AppResult<Paged<Anime>> = error("No usado en esta prueba")

        override suspend fun getAnimeDetails(id: Int): AppResult<AnimeDetails> =
            error("No usado en esta prueba")
    }
}
