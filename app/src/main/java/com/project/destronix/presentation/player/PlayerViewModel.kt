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

    fun load(animeId: Int) {
        if (loadedId == animeId && uiState.details != null) return
        loadedId = animeId
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
        if (index in uiState.episodes.indices) {
            uiState = uiState.copy(currentEpisodeIndex = index)
        }
    }

    fun nextEpisode() {
        selectEpisode(uiState.currentEpisodeIndex + 1)
    }

    /** Guarda el avance cuando termina un episodio, creando la entrada si no existía. */
    fun onEpisodeCompleted() {
        val anime = uiState.details?.anime ?: return
        val episodeNumber = uiState.episodes.getOrNull(uiState.currentEpisodeIndex)?.number ?: return
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
}
