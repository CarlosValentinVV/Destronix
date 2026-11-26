package com.project.destronix.presentation.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.project.destronix.R
import com.project.destronix.domain.model.LibraryEntry
import com.project.destronix.domain.model.LibraryStatus
import com.project.destronix.presentation.LocalViewModelFactory
import com.project.destronix.presentation.common.ImageFallback

@Composable
fun LibraryScreen(
    onAnimeClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: LibraryViewModel = viewModel(factory = LocalViewModelFactory.current)
    val state = viewModel.uiState

    Column(modifier = modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = state.filter == null,
                    onClick = { viewModel.setFilter(null) },
                    label = { Text(stringResource(R.string.library_all)) },
                )
            }
            items(LibraryStatus.entries) { status ->
                FilterChip(
                    selected = state.filter == status,
                    onClick = { viewModel.setFilter(status) },
                    label = { Text("${status.label} (${state.count(status)})") },
                )
            }
        }

        val visible = state.visibleEntries
        if (visible.isEmpty()) {
            LibraryEmpty(hasEntries = state.entries.isNotEmpty())
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(visible, key = { it.animeId }) { entry ->
                    LibraryItem(
                        entry = entry,
                        onClick = { onAnimeClick(entry.animeId) },
                        onIncrease = { viewModel.changeProgress(entry, 1) },
                        onDecrease = { viewModel.changeProgress(entry, -1) },
                        onStatusChange = { viewModel.changeStatus(entry, it) },
                        onRemove = { viewModel.remove(entry) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryItem(
    entry: LibraryEntry,
    onClick: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onStatusChange: (LibraryStatus) -> Unit,
    onRemove: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            SubcomposeAsyncImage(
                model = entry.imageUrl,
                contentDescription = entry.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(60.dp)
                    .height(84.dp),
                error = { ImageFallback(Modifier.fillMaxSize()) },
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Box {
                    AssistChip(
                        onClick = { menuExpanded = true },
                        label = { Text(entry.status.label) },
                    )
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        LibraryStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.label) },
                                onClick = {
                                    onStatusChange(status)
                                    menuExpanded = false
                                },
                            )
                        }
                    }
                }
                if (entry.totalEpisodes != null && entry.totalEpisodes > 0) {
                    LinearProgressIndicator(
                        progress = { entry.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .height(6.dp),
                    )
                    Text(
                        text = stringResource(
                            R.string.library_episodes,
                            entry.progressEpisodes,
                            entry.totalEpisodes,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.library_episodes_unknown, entry.progressEpisodes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onIncrease) {
                    Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.library_increase))
                }
                IconButton(onClick = onDecrease) {
                    Icon(Icons.Rounded.Remove, contentDescription = stringResource(R.string.library_decrease))
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(R.string.library_remove),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryEmpty(hasEntries: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(
                if (hasEntries) R.string.library_empty_filter else R.string.library_empty,
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
