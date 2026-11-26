package com.project.destronix.domain.model

/** Modelo principal de anime, independiente de la API y de la base de datos. */
data class Anime(
    val id: Int,
    val title: String,
    val titleEnglish: String? = null,
    val imageUrl: String? = null,
    val largeImageUrl: String? = null,
    val score: Double? = null,
    val episodes: Int? = null,
    val status: String? = null,
    val type: String? = null,
    val year: Int? = null,
    val season: String? = null,
    val genres: List<String> = emptyList(),
    val synopsis: String? = null,
    val studios: List<String> = emptyList(),
    val popularity: Int? = null,
    val rank: Int? = null,
    val trailerUrl: String? = null,
) {
    val displayTitle: String get() = titleEnglish?.takeIf { it.isNotBlank() } ?: title
}

/** Información extendida que se muestra en la pantalla de detalle. */
data class AnimeDetails(
    val anime: Anime,
    val background: String? = null,
    val broadcast: String? = null,
    val duration: String? = null,
    val rating: String? = null,
    val source: String? = null,
    val characters: List<AnimeCharacter> = emptyList(),
    val videos: List<AnimeVideo> = emptyList(),
    val recommendations: List<Anime> = emptyList(),
    val related: List<AnimeRelation> = emptyList(),
)

data class AnimeCharacter(
    val id: Int,
    val name: String,
    val role: String? = null,
    val imageUrl: String? = null,
)

data class AnimeVideo(
    val title: String,
    val youtubeId: String,
    val thumbnailUrl: String? = null,
) {
    val youtubeUrl: String get() = "https://www.youtube.com/watch?v=$youtubeId"
}

data class AnimeRelation(
    val relation: String,
    val entries: List<Anime> = emptyList(),
)

data class AnimeGenre(
    val id: Int,
    val name: String,
    val count: Int? = null,
)

data class Paged<T>(
    val items: List<T>,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
) {
    val hasNextPage: Boolean get() = currentPage < totalPages
}

data class HomeFeed(
    val topAiring: List<Anime> = emptyList(),
    val topUpcoming: List<Anime> = emptyList(),
    val seasonal: List<Anime> = emptyList(),
    val movies: List<Anime> = emptyList(),
    val genres: List<AnimeGenre> = emptyList(),
)
