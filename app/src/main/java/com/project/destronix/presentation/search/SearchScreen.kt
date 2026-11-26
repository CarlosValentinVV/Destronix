package com.project.destronix.presentation.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.destronix.R
import com.project.destronix.presentation.LocalViewModelFactory
import com.project.destronix.presentation.common.AnimeCard
import com.project.destronix.presentation.common.ErrorState
import com.project.destronix.presentation.common.messageRes

@Composable
fun SearchScreen(
    initialGenreId: Int?,
    onAnimeClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SearchViewModel = viewModel(factory = LocalViewModelFactory.current)
    val state = viewModel.uiState
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(initialGenreId) {
        initialGenreId?.let { viewModel.applyGenre(it) }
    }

    val gridState = rememberLazyGridState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val visible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            visible >= gridState.layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(shouldLoadMore, state.hasNextPage, state.results.size) {
        if (shouldLoadMore && state.hasNextPage && state.results.isNotEmpty()) viewModel.loadMore()
    }

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            leadingIcon = {
                Icon(Icons.Rounded.Search, contentDescription = null)
            },
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Rounded.Close, contentDescription = null)
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                viewModel.submit()
                keyboard?.hide()
            }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
        FilterRow(
            filters = state.filters,
            onFiltersChange = viewModel::updateFilters,
            onClear = viewModel::clearFilters,
        )

        when {
            state.isLoading && state.results.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            state.error != null && state.results.isEmpty() -> ErrorState(
                error = state.error,
                onRetry = viewModel::submit,
            )

            !state.hasSearched -> SearchSuggestions(
                history = state.history,
                onQuerySelected = { query ->
                    viewModel.onQueryChange(query)
                    viewModel.submit()
                },
                onClearHistory = viewModel::clearHistory,
            )

            state.results.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.search_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(32.dp),
                )
            }

            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 112.dp),
                state = gridState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.results, key = { it.id }) { anime ->
                    AnimeCard(anime = anime, onClick = { onAnimeClick(anime.id) })
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    when {
                        state.isLoadingMore -> LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        )

                        state.error != null -> Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Text(
                                text = stringResource(state.error.messageRes()),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                            )
                            Button(onClick = viewModel::loadMore) {
                                Text(stringResource(R.string.action_retry))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    filters: com.project.destronix.domain.model.SearchFilters,
    onFiltersChange: (com.project.destronix.domain.model.SearchFilters.() -> com.project.destronix.domain.model.SearchFilters) -> Unit,
    onClear: () -> Unit,
) {
    val typeOptions = listOf(
        FilterOption(null, stringResource(R.string.search_filter_type, "")),
        FilterOption("tv", stringResource(R.string.search_filter_type, "TV")),
        FilterOption("movie", stringResource(R.string.search_filter_type, "Película")),
        FilterOption("ova", stringResource(R.string.search_filter_type, "OVA")),
        FilterOption("special", stringResource(R.string.search_filter_type, "Especial")),
    )
    val statusOptions = listOf(
        FilterOption(null, stringResource(R.string.search_filter_status, "")),
        FilterOption("airing", stringResource(R.string.search_filter_status, "En emisión")),
        FilterOption("finished", stringResource(R.string.search_filter_status, "Finalizado")),
        FilterOption("upcoming", stringResource(R.string.search_filter_status, "Próximo")),
    )
    val scoreOptions = listOf(
        FilterOption(null, stringResource(R.string.search_filter_score, "")),
        FilterOption(7.0, stringResource(R.string.search_filter_score, "7+")),
        FilterOption(8.0, stringResource(R.string.search_filter_score, "8+")),
        FilterOption(9.0, stringResource(R.string.search_filter_score, "9+")),
    )
    val yearOptions = listOf(null, 2026, 2025, 2024, 2023, 2022).map {
        FilterOption(it, stringResource(R.string.search_filter_year, it?.toString().orEmpty()))
    }
    val orderOptions = listOf(
        FilterOption(null, stringResource(R.string.search_filter_order, "")),
        FilterOption("score", stringResource(R.string.search_filter_order, "Nota")),
        FilterOption("popularity", stringResource(R.string.search_filter_order, "Popularidad")),
        FilterOption("title", stringResource(R.string.search_filter_order, "Título")),
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            CycleChip("Tipo", typeOptions, filters.type) { value ->
                onFiltersChange { copy(type = value as? String) }
            }
        }
        item {
            CycleChip("Estado", statusOptions, filters.status) { value ->
                onFiltersChange { copy(status = value as? String) }
            }
        }
        item {
            CycleChip("Nota", scoreOptions, filters.minScore) { value ->
                onFiltersChange { copy(minScore = value as? Double) }
            }
        }
        item {
            CycleChip("Año", yearOptions, filters.year) { value ->
                onFiltersChange { copy(year = value as? Int) }
            }
        }
        item {
            CycleChip("Orden", orderOptions, filters.orderBy) { value ->
                onFiltersChange { copy(orderBy = value as? String) }
            }
        }
        item {
            AnimatedVisibility(filters.isActive) {
                TextButton(onClick = onClear) {
                    Text(stringResource(R.string.search_filters_clear))
                }
            }
        }
    }
}

private data class FilterOption(val value: Any?, val label: String)

@Composable
private fun CycleChip(
    name: String,
    options: List<FilterOption>,
    current: Any?,
    onSelect: (Any?) -> Unit,
) {
    val index = options.indexOfFirst { it.value == current }.let { if (it >= 0) it else 0 }
    val next = options[(index + 1) % options.size]
    FilterChip(
        selected = index > 0,
        onClick = { onSelect(next.value) },
        label = { Text(if (index == 0) name else options[index].label) },
    )
}

@Composable
private fun SearchSuggestions(
    history: List<String>,
    onQuerySelected: (String) -> Unit,
    onClearHistory: () -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        if (history.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.search_history),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onClearHistory) {
                    Text(stringResource(R.string.search_clear_history))
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history, key = { it }) { query ->
                    AssistChip(onClick = { onQuerySelected(query) }, label = { Text(query) })
                }
            }
        } else {
            Text(
                text = stringResource(R.string.search_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}
