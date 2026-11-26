package com.project.destronix.presentation.detail

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
import com.project.destronix.domain.usecase.GetAnimeDetailsUseCase
import com.project.destronix.domain.usecase.ObserveLibraryEntryUseCase
import com.project.destronix.domain.usecase.RemoveFromLibraryUseCase
import com.project.destronix.domain.usecase.SaveLibraryEntryUseCase
import com.project.destronix.domain.usecase.SetLibraryStatusUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeAnimeRepository()
    private val library = FakeLibraryRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load publishes details`() = runTest(dispatcher) {
        repository.details = AppResult.Success(AnimeDetails(anime = anime()))
        val viewModel = createViewModel()

        viewModel.load(10)
        runCurrent()

        assertEquals("Naruto", viewModel.uiState.details?.anime?.title)
        assertTrue(!viewModel.uiState.isLoading)
    }

    @Test
    fun `toggle library saves the current anime`() = runTest(dispatcher) {
        repository.details = AppResult.Success(AnimeDetails(anime = anime()))
        val viewModel = createViewModel()
        viewModel.load(10)
        runCurrent()

        viewModel.toggleLibrary()
        runCurrent()

        val entry = viewModel.uiState.libraryEntry
        assertNotNull(entry)
        assertEquals(LibraryStatus.PLANNING, entry?.status)
        assertEquals("Naruto", entry?.title)
    }

    @Test
    fun `changing status updates the saved entry`() = runTest(dispatcher) {
        repository.details = AppResult.Success(AnimeDetails(anime = anime()))
        val viewModel = createViewModel()
        viewModel.load(10)
        runCurrent()
        viewModel.toggleLibrary()
        runCurrent()

        viewModel.changeStatus(LibraryStatus.WATCHING)
        runCurrent()

        assertEquals(LibraryStatus.WATCHING, viewModel.uiState.libraryEntry?.status)
    }

    private fun createViewModel() = AnimeDetailViewModel(
        GetAnimeDetailsUseCase(repository),
        ObserveLibraryEntryUseCase(library),
        SaveLibraryEntryUseCase(library),
        SetLibraryStatusUseCase(library),
        RemoveFromLibraryUseCase(library),
    )

    private fun anime() = Anime(id = 10, title = "Naruto", episodes = 220, score = 8.7)

    private class FakeAnimeRepository : AnimeRepository {
        var details: AppResult<AnimeDetails> = AppResult.Loading

        override suspend fun getHomeFeed(): AppResult<HomeFeed> = error("No usado")

        override suspend fun searchAnime(
            query: String,
            filters: SearchFilters,
            page: Int,
        ): AppResult<Paged<Anime>> = error("No usado")

        override suspend fun getAnimeDetails(id: Int): AppResult<AnimeDetails> = details
    }

    private class FakeLibraryRepository : LibraryRepository {
        private val entry = MutableStateFlow<LibraryEntry?>(null)

        override fun observeAll(): Flow<List<LibraryEntry>> = flowOf(emptyList())

        override fun observe(animeId: Int): Flow<LibraryEntry?> = entry

        override suspend fun save(entry: LibraryEntry) {
            this.entry.value = entry
        }

        override suspend fun setStatus(animeId: Int, status: LibraryStatus) {
            entry.value = entry.value?.copy(status = status)
        }

        override suspend fun setProgress(animeId: Int, episodesWatched: Int) {
            entry.value = entry.value?.copy(progressEpisodes = episodesWatched)
        }

        override suspend fun remove(animeId: Int) {
            entry.value = null
        }
    }
}
