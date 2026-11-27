package com.project.destronix.presentation.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.destronix.core.AppError
import com.project.destronix.core.AppResult
import com.project.destronix.data.catalog.DemoEpisodeCatalog
import com.project.destronix.domain.model.AnimeDetails
import com.project.destronix.domain.model.LibraryEntry
import com.project.destronix.domain.model.LibraryStatus
import com.project.destronix.domain.model.VideoEpisode
import com.project.destronix.domain.usecase.GetAnimeDetailsUseCase
import com.project.destronix.domain.usecase.ObserveLibraryEntryUseCase
import com.project.destronix.domain.usecase.SaveLibraryEntryUseCase
import com.project.destronix.domain.usecase.SetLibraryStatusUseCase
import com.project.destronix.domain.usecase.SetWatchProgressUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PlayerUiState(
    val details: AnimeDetails? = null,
    val episodes: List<VideoEpisode> = emptyList(),
    val currentEpisodeIndex: Int = 0,
    val selectedServerIndex: Int = 0,
    val serverError: AppError? = null,
    val isLoading: Boolean = true,
    val error: AppError? = null,
)

class PlayerViewModel(
    private val episodeCatalog: DemoEpisodeCatalog,
    private val getAnimeDetails: GetAnimeDetailsUseCase,
    private val observeLibraryEntry: ObserveLibraryEntryUseCase,
    private val saveLibraryEntry: SaveLibraryEntryUseCase,
    private val setWatchProgress: SetWatchProgressUseCase,
    private val setLibraryStatus: SetLibraryStatusUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(PlayerUiState())
        private set

    private var loadedId = -1
    private val failedServers = mutableSetOf<Int>()

    fun load(animeId: Int) {
        if (loadedId == animeId && uiState.details != null) return
        loadedId = animeId
        failedServers.clear()
        uiState = PlayerUiState()
        viewModelScope.launch {
            when (val result = getAnimeDetails(animeId)) {
                is AppResult.Success -> {
                    val anime = result.data.anime
                    uiState = uiState.copy(
                        details = result.data,
                        episodes = episodeCatalog.forAnime(anime),
                        isLoading = false,
                    )
                }

                is AppResult.Error -> uiState = uiState.copy(error = result.error, isLoading = false)
                AppResult.Loading -> uiState = uiState.copy(isLoading = true)
            }
        }
    }

    fun selectEpisode(index: Int) {
        if (index !in uiState.episodes.indices) return
        failedServers.clear()
        uiState = uiState.copy(
            currentEpisodeIndex = index,
            selectedServerIndex = uiState.selectedServerIndex
                .takeIf { it in uiState.episodes[index].servers.indices }
                ?: 0,
            serverError = null,
        )
    }

    fun selectServer(index: Int) {
        val servers = currentEpisode()?.servers ?: return
        if (index !in servers.indices) return
        failedServers.clear()
        uiState = uiState.copy(selectedServerIndex = index, serverError = null)
    }

    /** URL que debe reproducir la UI, teniendo en cuenta el servidor elegido. */
    fun currentStreamUrl(): String? =
        currentEpisode()?.urlFor(uiState.selectedServerIndex)

    /**
     * Ante un error de reproducción prueba el siguiente servidor no fallado.
     * Cuando se agotan todos, expone un error en lugar de repetir infinitamente.
     */
    fun onServerError() {
        val servers = currentEpisode()?.servers.orEmpty()
        if (servers.size <= 1) {
            uiState = uiState.copy(serverError = serverFailure())
            return
        }

        val current = uiState.selectedServerIndex
        failedServers.add(current)
        val next = ((current + 1) until servers.size).firstOrNull { it !in failedServers }
            ?: (0 until current).firstOrNull { it !in failedServers }

        uiState = if (next == null) {
            uiState.copy(serverError = serverFailure())
        } else {
            uiState.copy(selectedServerIndex = next, serverError = null)
        }
    }

    fun nextEpisode() {
        selectEpisode(uiState.currentEpisodeIndex + 1)
    }

    /** Guarda el avance cuando termina un episodio, creando la entrada si no existía. */
    fun onEpisodeCompleted() {
        val anime = uiState.details?.anime ?: return
        val episodeNumber = currentEpisode()?.number ?: return
        viewModelScope.launch {
            val existing = observeLibraryEntry(anime.id).first()
            if (existing == null) {
                saveLibraryEntry(
                    LibraryEntry(
                        animeId = anime.id,
                        title = anime.displayTitle,
                        imageUrl = anime.imageUrl,
                        status = LibraryStatus.WATCHING,
                        progressEpisodes = episodeNumber,
                        totalEpisodes = anime.episodes,
                    ),
                )
            } else {
                setWatchProgress(
                    anime.id,
                    maxOf(existing.progressEpisodes, episodeNumber),
                )
                setLibraryStatus(anime.id, LibraryStatus.WATCHING)
            }
        }
    }

    private fun currentEpisode(): VideoEpisode? =
        uiState.episodes.getOrNull(uiState.currentEpisodeIndex)

    private fun serverFailure() = AppError.Server(0, "No hay servidores disponibles para este episodio.")
}
