package com.project.destronix.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.project.destronix.R
import com.project.destronix.domain.model.Anime
import com.project.destronix.domain.model.LibraryEntry
import com.project.destronix.presentation.LocalViewModelFactory
import com.project.destronix.presentation.common.AnimeCard
import com.project.destronix.presentation.common.ErrorState
import com.project.destronix.presentation.common.ImageFallback
import com.project.destronix.presentation.common.SectionHeader
import com.project.destronix.presentation.common.ShimmerBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAnimeClick: (Int) -> Unit,
    onGenreClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onPlayClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: HomeViewModel = viewModel(factory = LocalViewModelFactory.current)
    val state = viewModel.uiState

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.load(refresh = true) },
        modifier = modifier.fillMaxSize(),
    ) {
        when {
            state.isLoading -> HomeLoading()

            state.error != null && state.feed == null -> ErrorState(
                error = state.error,
                onRetry = { viewModel.load() },
            )

            state.feed != null -> HomeContent(
                feed = state.feed,
                continueWatching = state.continueWatching,
                onAnimeClick = onAnimeClick,
                onGenreClick = onGenreClick,
                onSearchClick = onSearchClick,
                onPlayClick = onPlayClick,
            )
        }
    }
}

@Composable
private fun HomeContent(
    feed: com.project.destronix.domain.model.HomeFeed,
    continueWatching: List<LibraryEntry>,
    onAnimeClick: (Int) -> Unit,
    onGenreClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onPlayClick: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
    ) {
        feed.topAiring.firstOrNull()?.let { hero ->
            HeroBanner(
                anime = hero,
                onClick = { onAnimeClick(hero.id) },
                modifier = Modifier.padding(16.dp),
            )
        }
        if (continueWatching.isNotEmpty()) {
            SectionHeader(title = stringResource(R.string.home_continue_watching))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(continueWatching, key = { it.animeId }) { entry ->
                    ContinueWatchingCard(
                        entry = entry,
                        onClick = { onPlayClick(entry.animeId) },
                        modifier = Modifier.width(180.dp),
                    )
                }
            }
        }
        SectionHeader(title = stringResource(R.string.home_section_genres))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(feed.genres, key = { it.id }) { genre ->
                FilterChip(
                    selected = false,
                    onClick = { onGenreClick(genre.id) },
                    label = { Text(genre.name) },
                )
            }
        }
        AnimeRow(
            title = stringResource(R.string.home_section_airing),
            items = feed.topAiring,
            onAnimeClick = onAnimeClick,
            onSeeAll = onSearchClick,
        )
        AnimeRow(
            title = stringResource(R.string.home_section_season),
            items = feed.seasonal,
            onAnimeClick = onAnimeClick,
            onSeeAll = onSearchClick,
        )
        AnimeRow(
            title = stringResource(R.string.home_section_upcoming),
            items = feed.topUpcoming,
            onAnimeClick = onAnimeClick,
            onSeeAll = onSearchClick,
        )
        AnimeRow(
            title = stringResource(R.string.home_section_movies),
            items = feed.movies,
            onAnimeClick = onAnimeClick,
            onSeeAll = onSearchClick,
        )
    }
}

@Composable
private fun ContinueWatchingCard(
    entry: LibraryEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            SubcomposeAsyncImage(
                model = entry.imageUrl,
                contentDescription = entry.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { ShimmerBox(Modifier.fillMaxSize()) },
                error = { ImageFallback(Modifier.fillMaxSize()) },
            )
        }
        Text(
            text = entry.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = stringResource(R.string.home_continue_episode, entry.progressEpisodes + 1),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp),
        )
        LinearProgressIndicator(
            progress = { entry.progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .height(6.dp),
        )
    }
}

@Composable
private fun AnimeRow(
    title: String,
    items: List<Anime>,
    onAnimeClick: (Int) -> Unit,
    onSeeAll: () -> Unit,
) {
    if (items.isEmpty()) return
    SectionHeader(title = title, onSeeAll = onSeeAll)
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items, key = { it.id }) { anime ->
            AnimeCard(
                anime = anime,
                onClick = { onAnimeClick(anime.id) },
                modifier = Modifier.width(112.dp),
            )
        }
    }
}

@Composable
private fun HeroBanner(
    anime: Anime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clickable(onClick = onClick),
    ) {
        Box {
            SubcomposeAsyncImage(
                model = anime.largeImageUrl ?: anime.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = { ImageFallback(Modifier.fillMaxSize()) },
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xFF05070C).copy(alpha = 0.92f)),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp),
            ) {
                Text(
                    text = anime.displayTitle,
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val meta = listOfNotNull(
                    anime.score?.let { "★ ${String.format("%.1f", it)}" },
                    anime.year?.toString(),
                    anime.type,
                ).joinToString("  ·  ")
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                Button(
                    onClick = onClick,
                    modifier = Modifier.padding(top = 14.dp),
                ) {
                    Text(stringResource(R.string.home_hero_cta))
                }
            }
        }
    }
}

@Composable
private fun HomeLoading() {
    Column(modifier = Modifier.fillMaxSize()) {
        ShimmerBox(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(260.dp),
            shape = RoundedCornerShape(20.dp),
        )
        repeat(3) { row ->
            ShimmerBox(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .width(if (row == 0) 180.dp else 140.dp)
                    .height(22.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(4) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(112.dp)
                            .aspectRatio(3f / 4f),
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
