package com.project.destronix.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.project.destronix.R
import com.project.destronix.domain.model.Anime
import com.project.destronix.domain.model.AnimeCharacter
import com.project.destronix.domain.model.AnimeDetails
import com.project.destronix.domain.model.AnimeRelation
import com.project.destronix.domain.model.AnimeVideo
import com.project.destronix.domain.model.LibraryStatus
import com.project.destronix.presentation.LocalViewModelFactory
import com.project.destronix.presentation.common.AnimeCard
import com.project.destronix.presentation.common.ErrorState
import com.project.destronix.presentation.common.ImageFallback
import com.project.destronix.presentation.common.ShimmerBox
import com.project.destronix.presentation.common.SectionHeader
import com.project.destronix.presentation.common.openExternalUrl

@Composable
fun DetailScreen(
    animeId: Int,
    onAnimeClick: (Int) -> Unit,
    onPlayClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AnimeDetailViewModel = viewModel(factory = LocalViewModelFactory.current)
    val state = viewModel.uiState

    LaunchedEffect(animeId) { viewModel.load(animeId) }

    when {
        state.isLoading && state.details == null -> DetailLoading(modifier)

        state.error != null && state.details == null -> ErrorState(
            error = state.error,
            onRetry = { viewModel.load(animeId) },
            modifier = modifier,
        )

        state.details != null -> DetailContent(
            details = state.details,
            inLibrary = state.libraryEntry != null,
            currentStatus = state.libraryEntry?.status,
            onToggleLibrary = viewModel::toggleLibrary,
            onStatusChange = viewModel::changeStatus,
            onAnimeClick = onAnimeClick,
            onPlayClick = onPlayClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun DetailContent(
    details: AnimeDetails,
    inLibrary: Boolean,
    currentStatus: LibraryStatus?,
    onToggleLibrary: () -> Unit,
    onStatusChange: (LibraryStatus) -> Unit,
    onAnimeClick: (Int) -> Unit,
    onPlayClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val anime = details.anime
    val trailerUrl = details.videos.firstOrNull()?.youtubeUrl ?: anime.trailerUrl

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        DetailHero(anime = anime)
        Text(
            text = anime.displayTitle,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = onToggleLibrary) {
                Text(
                    if (inLibrary) stringResource(R.string.detail_remove_library)
                    else stringResource(R.string.detail_add_library),
                )
            }
            if (inLibrary) StatusMenu(currentStatus = currentStatus, onChange = onStatusChange)
            Button(onClick = { onPlayClick(anime.id) }) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.player_watch))
            }
            trailerUrl?.let { url ->
                Button(onClick = { context.openExternalUrl(url) }) {
                    Icon(Icons.Rounded.PlayCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.detail_watch_trailer))
                }
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 12.dp),
        ) {
            items(anime.genres) { genre ->
                AssistChip(onClick = {}, label = { Text(genre) })
            }
        }

        FactsRow(details = details)
        SynopsisCard(synopsis = anime.synopsis.orEmpty())

        if (details.characters.isNotEmpty()) {
            SectionHeader(title = stringResource(R.string.detail_characters))
            CharacterRow(characters = details.characters)
        }
        if (details.videos.isNotEmpty()) {
            SectionHeader(title = stringResource(R.string.detail_videos))
            VideoRow(videos = details.videos)
        }
        if (details.recommendations.isNotEmpty()) {
            SectionHeader(title = stringResource(R.string.detail_recommendations))
            AnimeRow(
                items = details.recommendations,
                onAnimeClick = onAnimeClick,
            )
        }
        if (details.related.isNotEmpty()) {
            SectionHeader(title = stringResource(R.string.detail_related))
            details.related.forEach { relation -> RelatedBlock(relation = relation, onClick = onAnimeClick) }
        }
    }
}

@Composable
private fun DetailHero(anime: Anime) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
    ) {
        SubcomposeAsyncImage(
            model = anime.largeImageUrl ?: anime.imageUrl,
            contentDescription = anime.displayTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = { ShimmerBox(Modifier.fillMaxSize()) },
            error = { ImageFallback(Modifier.fillMaxSize()) },
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xFF05070C).copy(alpha = 0.85f)),
                    ),
                ),
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOfNotNull(
                anime.score?.let { "★ ${String.format("%.1f", it)}" },
                anime.episodes?.let { "$it ep." },
                anime.type,
                anime.year?.toString(),
                anime.status,
            ).take(4).forEach { fact ->
                AssistChip(
                    onClick = {},
                    label = { Text(fact, color = Color.White) },
                )
            }
        }
    }
}

@Composable
private fun StatusMenu(
    currentStatus: LibraryStatus?,
    onChange: (LibraryStatus) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(stringResource(R.string.detail_change_status))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            LibraryStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.label + if (status == currentStatus) " ✓" else "") },
                    onClick = {
                        onChange(status)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun FactsRow(details: AnimeDetails) {
    val facts = listOfNotNull(
        details.duration?.let { "Duración" to it },
        details.rating?.let { "Clasificación" to it },
        details.source?.let { "Origen" to it },
        details.broadcast?.let { "Emisión" to it },
        details.anime.rank?.let { "Ranking" to "#$it" },
        details.anime.popularity?.let { "Popularidad" to "#$it" },
    )
    if (facts.isEmpty()) return
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(facts) { (label, value) ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(value, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                }
            }
        }
    }
}

@Composable
private fun SynopsisCard(synopsis: String) {
    if (synopsis.isBlank()) return
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.detail_synopsis),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = synopsis,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 5,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp),
            )
            TextButton(onClick = { expanded = !expanded }) {
                Text(
                    stringResource(
                        if (expanded) R.string.detail_collapse else R.string.detail_expand,
                    ),
                )
            }
        }
    }
}

@Composable
private fun CharacterRow(characters: List<AnimeCharacter>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(characters) { character ->
            Column(
                modifier = Modifier.width(72.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SubcomposeAsyncImage(
                    model = character.imageUrl,
                    contentDescription = character.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    error = { ImageFallback(Modifier.fillMaxSize()) },
                )
                Text(
                    character.name,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun VideoRow(videos: List<AnimeVideo>) {
    val context = LocalContext.current
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(videos) { video ->
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .aspectRatio(16f / 9f)
                    .clickable { context.openExternalUrl(video.youtubeUrl) },
            ) {
                SubcomposeAsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = { ImageFallback(Modifier.fillMaxSize()) },
                )
                Icon(
                    Icons.Rounded.PlayCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.Center).size(36.dp),
                )
            }
        }
    }
}

@Composable
private fun AnimeRow(items: List<Anime>, onAnimeClick: (Int) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items) { anime ->
            AnimeCard(
                anime = anime,
                onClick = { onAnimeClick(anime.id) },
                modifier = Modifier.width(112.dp),
            )
        }
    }
}

@Composable
private fun RelatedBlock(relation: AnimeRelation, onClick: (Int) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(relation.relation, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(relation.entries) { anime ->
                AssistChip(
                    onClick = { onClick(anime.id) },
                    label = { Text(anime.displayTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                )
            }
        }
    }
}

@Composable
private fun DetailLoading(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(0.dp),
        )
        repeat(3) {
            ShimmerBox(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .height(if (it == 0) 32.dp else 20.dp),
            )
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
            items(4) { ShimmerBox(Modifier.size(width = 112.dp, height = 150.dp)) }
        }
        Spacer(Modifier.height(16.dp))
    }
}
