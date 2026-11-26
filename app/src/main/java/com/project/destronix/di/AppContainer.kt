package com.project.destronix.di

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.room.Room
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.project.destronix.core.DefaultDispatcherProvider
import com.project.destronix.core.DispatcherProvider
import com.project.destronix.data.catalog.DemoEpisodeCatalog
import com.project.destronix.data.config.AnimeConfigLoader
import com.project.destronix.data.config.AnimeSourceConfig
import com.project.destronix.data.config.AnimeSourcesConfig
import com.project.destronix.data.local.DestronixDatabase
import com.project.destronix.data.remote.JikanApiService
import com.project.destronix.data.remote.JikanRateLimitInterceptor
import com.project.destronix.data.repository.AnimeRepositoryImpl
import com.project.destronix.data.repository.LibraryRepositoryImpl
import com.project.destronix.data.repository.SearchHistoryRepositoryImpl
import com.project.destronix.data.repository.SettingsRepositoryImpl
import com.project.destronix.domain.repository.AnimeRepository
import com.project.destronix.domain.repository.LibraryRepository
import com.project.destronix.domain.repository.SearchHistoryRepository
import com.project.destronix.domain.repository.SettingsRepository
import com.project.destronix.domain.usecase.ClearSearchHistoryUseCase
import com.project.destronix.domain.usecase.GetAnimeDetailsUseCase
import com.project.destronix.domain.usecase.GetHomeFeedUseCase
import com.project.destronix.domain.usecase.ObserveLibraryEntryUseCase
import com.project.destronix.domain.usecase.ObserveLibraryUseCase
import com.project.destronix.domain.usecase.ObserveSearchHistoryUseCase
import com.project.destronix.domain.usecase.RemoveFromLibraryUseCase
import com.project.destronix.domain.usecase.SaveLibraryEntryUseCase
import com.project.destronix.domain.usecase.SaveSearchQueryUseCase
import com.project.destronix.domain.usecase.ObserveThemeSettingsUseCase
import com.project.destronix.domain.usecase.SearchAnimeUseCase
import com.project.destronix.domain.usecase.SetDynamicColorUseCase
import com.project.destronix.domain.usecase.SetLibraryStatusUseCase
import com.project.destronix.domain.usecase.SetThemeModeUseCase
import com.project.destronix.domain.usecase.SetWatchProgressUseCase
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

/** Grafo de dependencias de la aplicación (inyección manual, sin frameworks). */
interface AppContainer {
    val dispatchers: DispatcherProvider
    val animeSource: AnimeSourceConfig
    val episodeCatalog: DemoEpisodeCatalog
    val animeRepository: AnimeRepository
    val libraryRepository: LibraryRepository
    val searchHistoryRepository: SearchHistoryRepository
    val settingsRepository: SettingsRepository

    val getHomeFeed: GetHomeFeedUseCase
    val searchAnime: SearchAnimeUseCase
    val getAnimeDetails: GetAnimeDetailsUseCase
    val observeLibrary: ObserveLibraryUseCase
    val observeLibraryEntry: ObserveLibraryEntryUseCase
    val saveLibraryEntry: SaveLibraryEntryUseCase
    val setLibraryStatus: SetLibraryStatusUseCase
    val setWatchProgress: SetWatchProgressUseCase
    val removeFromLibrary: RemoveFromLibraryUseCase
    val observeSearchHistory: ObserveSearchHistoryUseCase
    val saveSearchQuery: SaveSearchQueryUseCase
    val clearSearchHistory: ClearSearchHistoryUseCase
    val observeThemeSettings: ObserveThemeSettingsUseCase
    val setThemeMode: SetThemeModeUseCase
    val setDynamicColor: SetDynamicColorUseCase
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val dispatchers: DispatcherProvider = DefaultDispatcherProvider()

    private val database: DestronixDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            DestronixDatabase::class.java,
            "destronix.db",
        ).build()
    }

    private val animeConfig: AnimeSourcesConfig by lazy {
        AnimeConfigLoader.load(context.applicationContext)
    }

    override val animeSource: AnimeSourceConfig get() = animeConfig.activeSource

    override val episodeCatalog: DemoEpisodeCatalog by lazy {
        DemoEpisodeCatalog(animeSource.streamBaseUrl)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(JikanRateLimitInterceptor())
            .cache(Cache(File(context.cacheDir, "anime_http_cache"), 10L * 1024 * 1024))
            .build()
    }

    private val gson by lazy {
        GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
    }

    private val jikanApi: JikanApiService by lazy {
        Retrofit.Builder()
            .baseUrl(animeSource.apiBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(JikanApiService::class.java)
    }

    override val animeRepository: AnimeRepository by lazy {
        AnimeRepositoryImpl(jikanApi, dispatchers)
    }

    override val libraryRepository: LibraryRepository by lazy {
        LibraryRepositoryImpl(database.libraryDao())
    }

    override val searchHistoryRepository: SearchHistoryRepository by lazy {
        SearchHistoryRepositoryImpl(database.searchHistoryDao())
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(context.applicationContext)
    }

    override val getHomeFeed = GetHomeFeedUseCase(animeRepository)
    override val searchAnime = SearchAnimeUseCase(animeRepository)
    override val getAnimeDetails = GetAnimeDetailsUseCase(animeRepository)
    override val observeLibrary = ObserveLibraryUseCase(libraryRepository)
    override val observeLibraryEntry = ObserveLibraryEntryUseCase(libraryRepository)
    override val saveLibraryEntry = SaveLibraryEntryUseCase(libraryRepository)
    override val setLibraryStatus = SetLibraryStatusUseCase(libraryRepository)
    override val setWatchProgress = SetWatchProgressUseCase(libraryRepository)
    override val removeFromLibrary = RemoveFromLibraryUseCase(libraryRepository)
    override val observeSearchHistory = ObserveSearchHistoryUseCase(searchHistoryRepository)
    override val saveSearchQuery = SaveSearchQueryUseCase(searchHistoryRepository)
    override val clearSearchHistory = ClearSearchHistoryUseCase(searchHistoryRepository)
    override val observeThemeSettings = ObserveThemeSettingsUseCase(settingsRepository)
    override val setThemeMode = SetThemeModeUseCase(settingsRepository)
    override val setDynamicColor = SetDynamicColorUseCase(settingsRepository)
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer no disponible. Envuelve la UI en CompositionLocalProvider.")
}
