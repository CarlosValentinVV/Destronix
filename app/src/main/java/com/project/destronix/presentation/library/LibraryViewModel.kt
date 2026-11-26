package com.project.destronix.presentation.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.destronix.domain.model.LibraryEntry
import com.project.destronix.domain.model.LibraryStatus
import com.project.destronix.domain.usecase.ObserveLibraryUseCase
import com.project.destronix.domain.usecase.RemoveFromLibraryUseCase
import com.project.destronix.domain.usecase.SetLibraryStatusUseCase
import com.project.destronix.domain.usecase.SetWatchProgressUseCase
import kotlinx.coroutines.launch

data class LibraryUiState(
    val entries: List<LibraryEntry> = emptyList(),
    val filter: LibraryStatus? = null,
) {
    val visibleEntries: List<LibraryEntry>
        get() = if (filter == null) entries else entries.filter { it.status == filter }

    fun count(status: LibraryStatus?): Int =
        if (status == null) entries.size else entries.count { it.status == status }
}

class LibraryViewModel(
    private val observeLibrary: ObserveLibraryUseCase,
    private val setProgress: SetWatchProgressUseCase,
    private val setStatus: SetLibraryStatusUseCase,
    private val remove: RemoveFromLibraryUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(LibraryUiState())
        private set

    init {
        viewModelScope.launch {
            observeLibrary().collect { entries ->
                uiState = uiState.copy(entries = entries)
            }
        }
    }

    fun setFilter(filter: LibraryStatus?) {
        uiState = uiState.copy(filter = filter)
    }

    fun changeProgress(entry: LibraryEntry, delta: Int) {
        val max = entry.totalEpisodes ?: Int.MAX_VALUE
        val next = (entry.progressEpisodes + delta).coerceIn(0, max)
        viewModelScope.launch { setProgress(entry.animeId, next) }
    }

    fun changeStatus(entry: LibraryEntry, status: LibraryStatus) {
        viewModelScope.launch { setStatus(entry.animeId, status) }
    }

    fun remove(entry: LibraryEntry) {
        viewModelScope.launch { remove(entry.animeId) }
    }
}
