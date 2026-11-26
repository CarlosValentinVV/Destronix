package com.project.destronix.presentation.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.destronix.core.AppError
import com.project.destronix.core.AppResult
import com.project.destronix.domain.model.Anime
import com.project.destronix.domain.model.SearchFilters
import com.project.destronix.domain.usecase.ClearSearchHistoryUseCase
import com.project.destronix.domain.usecase.ObserveSearchHistoryUseCase
import com.project.destronix.domain.usecase.SaveSearchQueryUseCase
import com.project.destronix.domain.usecase.SearchAnimeUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MS = 350L

data class SearchUiState(
    val query: String = "",
    val filters: SearchFilters = SearchFilters(),
    val results: List<Anime> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: AppError? = null,
    val hasSearched: Boolean = false,
    val history: List<String> = emptyList(),
) {
    val hasNextPage: Boolean get() = currentPage < totalPages
}

class SearchViewModel(
    private val searchAnime: SearchAnimeUseCase,
    private val observeHistory: ObserveSearchHistoryUseCase,
    private val saveQuery: SaveSearchQueryUseCase,
    private val clearHistory: ClearSearchHistoryUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(SearchUiState())
        private set

    private var debounceJob: Job? = null

    init {
        viewModelScope.launch {
            observeHistory().collectLatest { history ->
                uiState = uiState.copy(history = history)
            }
        }
    }

    /** Aplica el género procedente del deep link del carrusel de inicio. */
    fun applyGenre(genreId: Int) {
        updateFilters { copy(genres = listOf(genreId)) }
    }

    fun onQueryChange(query: String) {
        uiState = uiState.copy(query = query)
        debounceJob?.cancel()
        if (query.isBlank() && !uiState.filters.isActive) {
            uiState = uiState.copy(
                results = emptyList(),
                hasSearched = false,
                error = null,
            )
            return
        }
        debounceJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            performSearch(page = 1, saveHistory = false)
        }
    }

    fun submit() {
        debounceJob?.cancel()
        viewModelScope.launch {
            performSearch(page = 1, saveHistory = true)
        }
    }

    fun updateFilters(transform: SearchFilters.() -> SearchFilters) {
        uiState = uiState.copy(filters = uiState.filters.transform())
        debounceJob?.cancel()
        if (uiState.query.isBlank() && !uiState.filters.isActive) {
            uiState = uiState.copy(results = emptyList(), hasSearched = false)
            return
        }
        debounceJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            performSearch(page = 1, saveHistory = false)
        }
    }

    fun clearFilters() {
        uiState = uiState.copy(filters = SearchFilters())
        if (uiState.query.isBlank()) {
            uiState = uiState.copy(results = emptyList(), hasSearched = false)
        } else {
            submit()
        }
    }

    fun loadMore() {
        val state = uiState
        if (!state.hasNextPage || state.isLoading || state.isLoadingMore) return
        viewModelScope.launch { performSearch(page = state.currentPage + 1, saveHistory = false) }
    }

    fun clearHistory() {
        viewModelScope.launch { clearHistory() }
    }

    private suspend fun performSearch(page: Int, saveHistory: Boolean) {
        val state = uiState
        if (page == 1) {
            uiState = uiState.copy(isLoading = true, error = null)
        } else {
            uiState = uiState.copy(isLoadingMore = true)
        }
        if (saveHistory) saveQuery(state.query)

        when (val result = searchAnime(state.query, state.filters, page)) {
            is AppResult.Success -> {
                val paged = result.data
                uiState = uiState.copy(
                    results = if (page == 1) paged.items else uiState.results + paged.items,
                    currentPage = paged.currentPage,
                    totalPages = paged.totalPages,
                    isLoading = false,
                    isLoadingMore = false,
                    error = null,
                    hasSearched = true,
                )
            }

            is AppResult.Error -> uiState = uiState.copy(
                isLoading = false,
                isLoadingMore = false,
                error = result.error,
                hasSearched = true,
            )

            AppResult.Loading -> Unit
        }
    }
}
