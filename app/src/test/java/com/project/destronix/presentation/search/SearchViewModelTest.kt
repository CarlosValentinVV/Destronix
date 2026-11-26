package com.project.destronix.presentation.search

import com.project.destronix.core.AppResult
import com.project.destronix.domain.model.Anime
import com.project.destronix.domain.model.AnimeDetails
import com.project.destronix.domain.model.HomeFeed
import com.project.destronix.domain.model.Paged
import com.project.destronix.domain.model.SearchFilters
import com.project.destronix.domain.repository.AnimeRepository
import com.project.destronix.domain.repository.SearchHistoryRepository
import com.project.destronix.domain.usecase.ClearSearchHistoryUseCase
import com.project.destronix.domain.usecase.ObserveSearchHistoryUseCase
import com.project.destronix.domain.usecase.SaveSearchQueryUseCase
import com.project.destronix.domain.usecase.SearchAnimeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class SearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeAnimeRepository()
    private val history = FakeSearchHistoryRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `typing triggers a debounced search with filters`() = runTest(dispatcher) {
        repository.result = AppResult.Success(Paged(listOf(anime(1)), currentPage = 1, totalPages = 3))
        val viewModel = createViewModel()

        viewModel.onQueryChange("naruto")
        advanceTimeBy(400)
        runCurrent()

        assertEquals("naruto", repository.lastQuery)
        assertEquals(1, viewModel.uiState.results.size)
        assertTrue(viewModel.uiState.hasNextPage)
    }

    @Test
    fun `submit saves the query in history`() = runTest(dispatcher) {
        repository.result = AppResult.Success(Paged(listOf(anime(2))))
        val viewModel = createViewModel()

        viewModel.onQueryChange("bleach")
        viewModel.submit()
        runCurrent()

        assertEquals(listOf("bleach"), viewModel.uiState.history)
        assertEquals(1, repository.calls)
    }

    @Test
    fun `load more appends the next page`() = runTest(dispatcher) {
        repository.result = AppResult.Success(Paged(listOf(anime(1)), 1, 2))
        val viewModel = createViewModel()
        viewModel.onQueryChange("one piece")
        advanceTimeBy(400)
        runCurrent()
        repository.result = AppResult.Success(Paged(listOf(anime(3)), 2, 2))

        viewModel.loadMore()
        runCurrent()

        assertEquals(listOf(1, 3), viewModel.uiState.results.map { it.id })
        assertEquals(2, viewModel.uiState.currentPage)
    }

    private fun createViewModel() = SearchViewModel(
        SearchAnimeUseCase(repository),
        ObserveSearchHistoryUseCase(history),
        SaveSearchQueryUseCase(history),
        ClearSearchHistoryUseCase(history),
    )

    private fun anime(id: Int) = Anime(id = id, title = "Anime $id")

    private class FakeAnimeRepository : AnimeRepository {
        var result: AppResult<Paged<Anime>> = AppResult.Loading
        var lastQuery: String = ""
        var lastFilters: SearchFilters = SearchFilters()
        var calls: Int = 0

        override suspend fun getHomeFeed(): AppResult<HomeFeed> = error("No usado")

        override suspend fun searchAnime(
            query: String,
            filters: SearchFilters,
            page: Int,
        ): AppResult<Paged<Anime>> {
            calls++
            lastQuery = query
            lastFilters = filters
            return result
        }

        override suspend fun getAnimeDetails(id: Int): AppResult<AnimeDetails> = error("No usado")
    }

    private class FakeSearchHistoryRepository : SearchHistoryRepository {
        private val queries = MutableStateFlow<List<String>>(emptyList())

        override fun observeRecent(limit: Int): Flow<List<String>> = queries.map { it.take(limit) }

        override suspend fun save(query: String) {
            queries.value = listOf(query) + queries.value.filter { it != query }
        }

        override suspend fun clear() {
            queries.value = emptyList()
        }
    }
}
