package com.project.destronix.data.catalog

import com.project.destronix.data.config.VideoServerConfig
import com.project.destronix.domain.model.Anime
import com.project.destronix.domain.model.VideoEpisode
import com.project.destronix.domain.model.VideoServer
import com.project.destronix.domain.model.VideoServerType
import kotlin.math.abs

/**
 * Catálogo de demostración: Jikan ofrece metadatos y tráilers, pero no streams de episodios.
 * Los vídeos y sus servidores se toman de la configuración externa de fuentes.
 */
class DemoEpisodeCatalog(
    private val streamBaseUrl: String,
    private val servers: List<VideoServerConfig> = emptyList(),
) {

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
        val playableServers = servers.filter { it.type != VideoServerType.EMBED }
            .takeIf { it.isNotEmpty() }
            ?: listOf(fallbackServer)

        return (1..count).map { number ->
            val clip = clips[(number - 1 + offset) % clips.size]
            val videoServers = playableServers.map { config ->
                VideoServer(
                    id = config.id,
                    label = config.name,
                    url = buildUrl(config.baseUrl, clip, number),
                    type = config.type,
                    language = config.language,
                )
            }
            VideoEpisode(
                number = number,
                title = "Episodio $number",
                streamUrl = videoServers.first().url,
                servers = videoServers,
                thumbnailUrl = anime.imageUrl,
            )
        }
    }

    /** Media3 no puede reproducir páginas web incrustadas, así que EMBED nunca se ofrece. */
    private val fallbackServer: VideoServerConfig
        get() = VideoServerConfig(
            id = "demo",
            name = "Demo",
            baseUrl = streamBaseUrl.trimEnd('/'),
        )

    /**
     * Las bases pueden ser una carpeta (se añade el clip) o una plantilla con
     * `{episode}` y `{clip}` para generar la URL completa.
     */
    private fun buildUrl(baseUrl: String, clip: String, episode: Int): String {
        var url = baseUrl.trimEnd('/')
        val hasTemplate = "{episode}" in url || "{clip}" in url
        if ("{episode}" in url) url = url.replace("{episode}", episode.toString())
        if ("{clip}" in url) url = url.replace("{clip}", clip)
        return when {
            hasTemplate -> url
            url.endsWith(clip) -> url
            else -> "$url/$clip"
        }
    }

    companion object {
        const val DEFAULT_STREAM_BASE_URL = "https://storage.googleapis.com/gtv-videos-bucket/sample/"
    }
}
