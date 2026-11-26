package com.project.destronix.data.catalog

import com.project.destronix.domain.model.Anime
import com.project.destronix.domain.model.VideoEpisode
import kotlin.math.abs

/**
 * Catálogo de demostración: Jikan ofrece metadatos y tráilers, pero no streams de episodios.
 * La base de los vídeos de prueba se toma de la configuración externa de fuentes.
 */
class DemoEpisodeCatalog(private val streamBaseUrl: String) {

    private val clips = listOf(
        "BigBuckBunny.mp4",
        "ElephantsDream.mp4",
        "ForBiggerBlazes.mp4",
        "ForBiggerEscapes.mp4",
        "ForBiggerFun.mp4",
        "ForBiggerJoyrides.mp4",
        "ForBiggerMeltdowns.mp4",
        "Sintel.mp4",
    )

    fun forAnime(anime: Anime, maxEpisodes: Int = 24): List<VideoEpisode> {
        val count = (anime.episodes ?: 12).coerceIn(1, maxEpisodes)
        val offset = abs(anime.id) % clips.size
        return (1..count).map { number ->
            VideoEpisode(
                number = number,
                title = "Episodio $number",
                streamUrl = streamBaseUrl + clips[(number - 1 + offset) % clips.size],
                thumbnailUrl = anime.imageUrl,
            )
        }
    }

    companion object {
        const val DEFAULT_STREAM_BASE_URL = "https://storage.googleapis.com/gtv-videos-bucket/sample/"
    }
}
