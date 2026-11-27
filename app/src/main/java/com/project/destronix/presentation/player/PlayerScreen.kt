package com.project.destronix.presentation.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.project.destronix.R
import com.project.destronix.presentation.LocalViewModelFactory
import com.project.destronix.presentation.common.ErrorState

@Composable
fun PlayerScreen(
    animeId: Int,
    modifier: Modifier = Modifier,
) {
    val viewModel: PlayerViewModel = viewModel(factory = LocalViewModelFactory.current)
    val state = viewModel.uiState
    val context = LocalContext.current

    val player = remember {
        ExoPlayer.Builder(context).build().apply { playWhenReady = true }
    }

    LaunchedEffect(animeId) { viewModel.load(animeId) }

    val streamUrl = state.episodes
        .getOrNull(state.currentEpisodeIndex)
        ?.urlFor(state.selectedServerIndex)

    LaunchedEffect(streamUrl) {
        streamUrl?.let { url ->
            player.stop()
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    viewModel.onEpisodeCompleted()
                    viewModel.nextEpisode()
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                viewModel.onServerError()
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            state.error != null && state.details == null -> ErrorState(
                error = state.error,
                onRetry = { viewModel.load(animeId) },
            )

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black),
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                this.player = player
                                useController = true
                                controllerShowTimeoutMs = 3000
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                state.details?.anime?.displayTitle?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
                val servers = state.episodes
                    .getOrNull(state.currentEpisodeIndex)
                    ?.servers
                    .orEmpty()
                if (servers.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.player_servers),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(servers) { index, server ->
                            FilterChip(
                                selected = index == state.selectedServerIndex,
                                onClick = { viewModel.selectServer(index) },
                                label = {
                                    Text(
                                        listOfNotNull(server.label, server.language)
                                            .joinToString(" · "),
                                    )
                                },
                            )
                        }
                    }
                }
                state.serverError?.let {
                    Text(
                        text = stringResource(R.string.player_server_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.player_episodes),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(state.episodes) { index, episode ->
                        FilterChip(
                            selected = index == state.currentEpisodeIndex,
                            onClick = { viewModel.selectEpisode(index) },
                            label = { Text(episode.number.toString()) },
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.player_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}
