package com.project.destronix.data.config

import com.project.destronix.domain.model.VideoServerType

/** Servidor declarado en la configuración externa para los episodios. */
data class VideoServerConfig(
    val id: String,
    val name: String,
    val baseUrl: String,
    val type: VideoServerType = VideoServerType.DIRECT,
    val language: String? = null,
)

/** Fuente de catálogo y vídeos, leída desde `assets/anime_sources.json`. */
data class AnimeSourceConfig(
    val id: String,
    val name: String,
    val apiBaseUrl: String,
    val streamBaseUrl: String,
    val websiteUrl: String? = null,
    val servers: List<VideoServerConfig> = emptyList(),
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
                    servers = listOf(
                        VideoServerConfig(
                            id = "demo",
                            name = "Demo",
                            baseUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/",
                        ),
                    ),
                ),
            ),
        )
    }
}
