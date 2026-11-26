package com.project.destronix.presentation.player

import com.project.destronix.core.AppResult
import com.project.destronix.data.catalog.DemoEpisodeCatalog
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
import com.project.destronix.domain.usecase.SaveLibraryEntryUseCase
import com.project.destronix.domain.usecase.SetLibraryStatusUseCase
import com.project.destronix.domain.usecase.SetWatchProgressUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeAnimeRepository()
    private val library = FakeLibraryRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository.details = AppResult.Success(AnimeDetails(anime = anime()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loading an anime creates its demo episodes`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.load(5)
        runCurrent()

        assertEquals(3, viewModel.uiState.episodes.size)
        assertTrue(viewModel.uiState.episodes.first().streamUrl.endsWith(".mp4"))
        assertEquals(0, viewModel.uiState.currentEpisodeIndex)
    }

    @Test
    fun `completing an episode creates a watching entry`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.load(5)
        runCurrent()

        viewModel.onEpisodeCompleted()
        runCurrent()

        val entry = library.entry.value
        assertEquals(LibraryStatus.WATCHING, entry?.status)
        assertEquals(1, entry?.progressEpisodes)
        assertEquals(3, entry?.totalEpisodes)
    }

    @Test
    fun `completing an episode updates an existing entry`() = runTest(dispatcher) {
        library.entry.value = LibraryEntry(
            animeId = 5,
            title = "Naruto",
            imageUrl = null,
            status = LibraryStatus.PLANNING,
            progressEpisodes = 1,
            totalEpisodes = 3,
        )
        val viewModel = createViewModel()
        viewModel.load(5)
        runCurrent()
        viewModel.selectEpisode(1)

        viewModel.onEpisodeCompleted()
        runCurrent()

        assertEquals(2, library.entry.value?.progressEpisodes)
        assertEquals(LibraryStatus.WATCHING, library.entry.value?.status)
    }

    @Test
    fun `next episode advances the selection`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        viewModel.load(5)
        runCurrent()

        viewModel.nextEpisode()

        assertEquals(1, viewModel.uiState.currentEpisodeIndex)
        assertEquals(2, viewModel.uiState.episodes[1].number)
        assertEquals(3, DemoEpisodeCatalog.forAnime(anime()).size)
    }

    private fun createViewModel() = PlayerViewModel(
        GetAnimeDetailsUseCase(repository),
        ObserveLibraryEntryUseCase(library),
        SaveLibraryEntryUseCase(library),
        SetWatchProgressUseCase(library),
        SetLibraryStatusUseCase(library),
    )

    private fun anime() = Anime(id = 5, title = "Naruto", episodes = 3, imageUrl = "cover.jpg")

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
        val entry = MutableStateFlow<LibraryEntry?>(null)

        override fun observeAll(): Flow<List<LibraryEntry>> =
            entry.value?.let { flowOf(listOf(it)) } ?: flowOf(emptyList())

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
