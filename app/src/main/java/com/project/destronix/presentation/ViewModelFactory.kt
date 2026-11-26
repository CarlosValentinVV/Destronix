package com.project.destronix.presentation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.destronix.di.AppContainer
import com.project.destronix.presentation.detail.AnimeDetailViewModel
import com.project.destronix.presentation.home.HomeViewModel
import com.project.destronix.presentation.search.SearchViewModel

/** Fábrica central de ViewModels: único punto donde se inyectan los casos de uso. */
class DestronixViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(HomeViewModel::class.java) ->
            HomeViewModel(container.getHomeFeed) as T

        modelClass.isAssignableFrom(SearchViewModel::class.java) ->
            SearchViewModel(
                container.searchAnime,
                container.observeSearchHistory,
                container.saveSearchQuery,
                container.clearSearchHistory,
            ) as T

        modelClass.isAssignableFrom(AnimeDetailViewModel::class.java) ->
            AnimeDetailViewModel(
                container.getAnimeDetails,
                container.observeLibraryEntry,
                container.saveLibraryEntry,
                container.setLibraryStatus,
                container.removeFromLibrary,
            ) as T

        else -> throw IllegalArgumentException("ViewModel no registrado: ${modelClass.name}")
    }
}

val LocalViewModelFactory = staticCompositionLocalOf<ViewModelProvider.Factory> {
    error("ViewModelFactory no disponible.")
}
