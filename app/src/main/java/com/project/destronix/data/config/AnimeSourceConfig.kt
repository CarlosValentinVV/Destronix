package com.project.destronix.data.config

/** Fuente de catálogo y vídeos, leída desde `assets/anime_sources.json`. */
data class AnimeSourceConfig(
    val id: String,
    val name: String,
    val apiBaseUrl: String,
    val streamBaseUrl: String,
    val websiteUrl: String? = null,
)

/** Fuentes disponibles y fuente activa seleccionada por `activeSourceId`. */
data class AnimeSourcesConfig(
    val activeSourceId: String? = null,
    val sources: List<AnimeSourceConfig> = emptyList(),
) {
    val activeSource: AnimeSourceConfig
        get() = sources.firstOrNull { it.id == activeSourceId }
            ?: sources.firstOrNull()
            ?: DEFAULT.sources.first()

    companion object {
        /** Configuración de reserva cuando el archivo no existe o no es válido. */
        val DEFAULT = AnimeSourcesConfig(
            activeSourceId = "jikan",
            sources = listOf(
                AnimeSourceConfig(
                    id = "jikan",
                    name = "MyAnimeList (Jikan)",
                    apiBaseUrl = "https://api.jikan.moe/v4/",
                    streamBaseUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/",
                    websiteUrl = "https://jikan.moe/",
                ),
            ),
        )
    }
}
