package com.project.destronix.presentation.home

import com.project.destronix.core.AppError
import com.project.destronix.core.AppResult
import com.project.destronix.domain.model.Anime
import com.project.destronix.domain.model.AnimeDetails
import com.project.destronix.domain.model.HomeFeed
import com.project.destronix.domain.model.LibraryEntry
import com.project.destronix.domain.model.LibraryStatus
import com.project.destronix.domain.model.Paged
import com.project.destronix.domain.model.SearchFilters
import com.project.destronix.domain.repository.AnimeRepository
import com.project.destronix.domain.repository.LibraryRepository
import com.project.destronix.domain.usecase.GetHomeFeedUseCase
import com.project.destronix.domain.usecase.ObserveContinueWatchingUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val repository = FakeAnimeRepository()
    private val library = FakeLibraryRepository()

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load publishes the feed and clears loading`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        repository.result = AppResult.Success(HomeFeed(topAiring = listOf(anime())))
        val viewModel = createViewModel()

        assertNotNull(viewModel.uiState.feed)
        assertEquals(1, viewModel.uiState.feed?.topAiring?.size)
        assertFalse(viewModel.uiState.isLoading)
    }

    @Test
    fun `failed load exposes a domain error`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        repository.result = AppResult.Error(AppError.Network)
        val viewModel = createViewModel()

        assertEquals(AppError.Network, viewModel.uiState.error)
        assertFalse(viewModel.uiState.isLoading)
    }

    @Test
    fun `continue watching exposes only active entries ordered by recency`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        repository.result = AppResult.Success(HomeFeed())
        library.entries.value = listOf(
            entry(id = 1, status = LibraryStatus.WATCHING, progress = 3, total = 12, updated = 300),
            entry(id = 2, status = LibraryStatus.PLANNING, progress = 2, total = 12, updated = 400),
            entry(id = 3, status = LibraryStatus.WATCHING, progress = 0, total = 12, updated = 500),
            entry(id = 4, status = LibraryStatus.WATCHING, progress = 12, total = 12, updated = 200),
            entry(id = 5, status = LibraryStatus.WATCHING, progress = 1, total = null, updated = 100),
        )
        val viewModel = createViewModel()

        assertEquals(listOf(1, 5), viewModel.uiState.continueWatching.map { it.animeId })
    }

    private fun createViewModel() = HomeViewModel(
        GetHomeFeedUseCase(repository),
        ObserveContinueWatchingUseCase(library),
    )

    private fun anime() = Anime(id = 1, title = "Naruto")

    private fun entry(
        id: Int,
        status: LibraryStatus,
        progress: Int,
        total: Int?,
        updated: Long,
    ) = LibraryEntry(
        animeId = id,
        title = "Anime $id",
        imageUrl = null,
        status = status,
        progressEpisodes = progress,
        totalEpisodes = total,
        updatedAt = updated,
    )

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

    private class FakeLibraryRepository : LibraryRepository {
        val entries = MutableStateFlow<List<LibraryEntry>>(emptyList())

        override fun observeAll(): Flow<List<LibraryEntry>> = entries

        override fun observe(animeId: Int): Flow<LibraryEntry?> =
            throw UnsupportedOperationException("No usado en esta prueba")

        override suspend fun save(entry: LibraryEntry) = Unit

        override suspend fun setStatus(animeId: Int, status: LibraryStatus) = Unit

        override suspend fun setProgress(animeId: Int, episodesWatched: Int) = Unit

        override suspend fun remove(animeId: Int) = Unit
    }
}
