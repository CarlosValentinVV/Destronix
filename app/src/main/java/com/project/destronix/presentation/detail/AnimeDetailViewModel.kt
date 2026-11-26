package com.project.destronix.presentation.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.destronix.core.AppError
import com.project.destronix.core.AppResult
import com.project.destronix.domain.model.AnimeDetails
import com.project.destronix.domain.model.LibraryEntry
import com.project.destronix.domain.model.LibraryStatus
import com.project.destronix.domain.usecase.GetAnimeDetailsUseCase
import com.project.destronix.domain.usecase.ObserveLibraryEntryUseCase
import com.project.destronix.domain.usecase.RemoveFromLibraryUseCase
import com.project.destronix.domain.usecase.SaveLibraryEntryUseCase
import com.project.destronix.domain.usecase.SetLibraryStatusUseCase
import kotlinx.coroutines.launch

data class AnimeDetailUiState(
    val details: AnimeDetails? = null,
    val libraryEntry: LibraryEntry? = null,
    val isLoading: Boolean = true,
    val error: AppError? = null,
)

class AnimeDetailViewModel(
    private val getAnimeDetails: GetAnimeDetailsUseCase,
    private val observeLibraryEntry: ObserveLibraryEntryUseCase,
    private val saveLibraryEntry: SaveLibraryEntryUseCase,
    private val setLibraryStatus: SetLibraryStatusUseCase,
    private val removeFromLibrary: RemoveFromLibraryUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(AnimeDetailUiState())
        private set

    private var animeId: Int = -1

    fun load(id: Int) {
        if (animeId == id && uiState.details != null) return
        animeId = id
        uiState = uiState.copy(isLoading = true, error = null)
        viewModelScope.launch {
            observeLibraryEntry(id).collect { entry ->
                uiState = uiState.copy(libraryEntry = entry)
            }
        }
        viewModelScope.launch {
            uiState = when (val result = getAnimeDetails(id)) {
                is AppResult.Success -> uiState.copy(details = result.data, isLoading = false)
                is AppResult.Error -> uiState.copy(error = result.error, isLoading = false)
                AppResult.Loading -> uiState.copy(isLoading = true)
            }
        }
    }

    fun toggleLibrary() {
        val entry = uiState.libraryEntry
        if (entry != null) {
            viewModelScope.launch { removeFromLibrary(entry.animeId) }
        } else {
            uiState.details?.let { details ->
                viewModelScope.launch {
                    saveLibraryEntry(
                        LibraryEntry(
                            animeId = details.anime.id,
                            title = details.anime.displayTitle,
                            imageUrl = details.anime.imageUrl,
                            status = LibraryStatus.PLANNING,
                            score = details.anime.score,
                            totalEpisodes = details.anime.episodes,
                        ),
                    )
                }
            }
        }
    }

    fun changeStatus(status: LibraryStatus) {
        uiState.details?.let { details ->
            viewModelScope.launch {
                if (uiState.libraryEntry == null) {
                    saveLibraryEntry(
                        LibraryEntry(
                            animeId = details.anime.id,
                            title = details.anime.displayTitle,
                            imageUrl = details.anime.imageUrl,
                            status = status,
                            score = details.anime.score,
                            totalEpisodes = details.anime.episodes,
                        ),
                    )
                } else {
                    setLibraryStatus(details.anime.id, status)
                }
            }
        }
    }
}
