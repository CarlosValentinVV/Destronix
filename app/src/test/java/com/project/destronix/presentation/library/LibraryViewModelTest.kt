package com.project.destronix.presentation.library

import com.project.destronix.domain.model.LibraryEntry
import com.project.destronix.domain.model.LibraryStatus
import com.project.destronix.domain.repository.LibraryRepository
import com.project.destronix.domain.usecase.ObserveLibraryUseCase
import com.project.destronix.domain.usecase.RemoveFromLibraryUseCase
import com.project.destronix.domain.usecase.SetLibraryStatusUseCase
import com.project.destronix.domain.usecase.SetWatchProgressUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeLibraryRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `filter shows only entries with chosen status`() = runTest(dispatcher) {
        repository.entries.value = listOf(
            entry(1, LibraryStatus.WATCHING),
            entry(2, LibraryStatus.COMPLETED),
        )
        val viewModel = createViewModel()
        runCurrent()

        viewModel.setFilter(LibraryStatus.WATCHING)

        assertEquals(listOf(1), viewModel.uiState.visibleEntries.map { it.animeId })
        assertEquals(2, viewModel.uiState.count(null))
    }

    @Test
    fun `progress is clamped to total episodes`() = runTest(dispatcher) {
        val entry = entry(1, LibraryStatus.WATCHING, progress = 11, total = 12)
        repository.entries.value = listOf(entry)
        val viewModel = createViewModel()
        runCurrent()

        viewModel.changeProgress(entry, 5)
        runCurrent()

        assertEquals(12, viewModel.uiState.entries.first().progressEpisodes)
    }

    @Test
    fun `remove deletes the entry`() = runTest(dispatcher) {
        val entry = entry(1, LibraryStatus.PLANNING)
        repository.entries.value = listOf(entry)
        val viewModel = createViewModel()
        runCurrent()

        viewModel.remove(entry)
        runCurrent()

        assertEquals(emptyList<LibraryEntry>(), viewModel.uiState.entries)
    }

    private fun createViewModel() = LibraryViewModel(
        ObserveLibraryUseCase(repository),
        SetWatchProgressUseCase(repository),
        SetLibraryStatusUseCase(repository),
        RemoveFromLibraryUseCase(repository),
    )

    private fun entry(
        id: Int,
        status: LibraryStatus,
        progress: Int = 0,
        total: Int? = 24,
    ) = LibraryEntry(
        animeId = id,
        title = "Anime $id",
        imageUrl = null,
        status = status,
        progressEpisodes = progress,
        totalEpisodes = total,
    )

    private class FakeLibraryRepository : LibraryRepository {
        val entries = MutableStateFlow<List<LibraryEntry>>(emptyList())

        override fun observeAll(): Flow<List<LibraryEntry>> = entries

        override fun observe(animeId: Int): Flow<LibraryEntry?> =
            MutableStateFlow(entries.value.firstOrNull { it.animeId == animeId })

        override suspend fun save(entry: LibraryEntry) {
            entries.value = listOf(entry) + entries.value.filter { it.animeId != entry.animeId }
        }

        override suspend fun setStatus(animeId: Int, status: LibraryStatus) {
            entries.value = entries.value.map {
                if (it.animeId == animeId) it.copy(status = status) else it
            }
        }

        override suspend fun setProgress(animeId: Int, episodesWatched: Int) {
            entries.value = entries.value.map {
                if (it.animeId == animeId) it.copy(progressEpisodes = episodesWatched) else it
            }
        }

        override suspend fun remove(animeId: Int) {
            entries.value = entries.value.filter { it.animeId != animeId }
        }
    }
}
