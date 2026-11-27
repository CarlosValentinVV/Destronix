package com.project.destronix.data.config

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.project.destronix.domain.model.VideoServerType
import java.io.IOException

/**
 * Lee la configuración de fuentes desde `assets/anime_sources.json` en tiempo de ejecución.
 * El archivo real no se versiona; si falta o es inválido se usa [AnimeSourcesConfig.DEFAULT].
 */
object AnimeConfigLoader {

    private const val ASSET_NAME = "anime_sources.json"
    private val gson = Gson()

    fun load(context: Context): AnimeSourcesConfig = try {
        context.assets.open(ASSET_NAME).bufferedReader().use { parse(it.readText()) }
    } catch (_: IOException) {
        AnimeSourcesConfig.DEFAULT
    }

    fun parse(json: String): AnimeSourcesConfig {
        if (json.isBlank()) return AnimeSourcesConfig.DEFAULT
        return try {
            val dto = gson.fromJson(json, AnimeSourcesConfigDto::class.java)
            val sources = dto?.sources.orEmpty().mapNotNull { it.toConfig() }
            if (sources.isEmpty()) {
                AnimeSourcesConfig.DEFAULT
            } else {
                AnimeSourcesConfig(activeSourceId = dto?.activeSourceId, sources = sources)
            }
        } catch (_: JsonSyntaxException) {
            AnimeSourcesConfig.DEFAULT
        }
    }

    private fun normalizeUrl(value: String?): String? {
        val url = value?.trim()?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
            ?: return null
        return if (url.endsWith("/")) url else "$url/"
    }

    /** Las URLs de servidor pueden contener plantillas, así que no se les añade una barra final. */
    private fun normalizeServerUrl(value: String?): String? =
        value?.trim()
            ?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
            ?.trimEnd('/')

    private fun parseServerType(value: String?): VideoServerType =
        runCatching { VideoServerType.valueOf(value?.trim()?.uppercase().orEmpty()) }
            .getOrDefault(VideoServerType.DIRECT)

    private data class AnimeSourcesConfigDto(
        val activeSourceId: String? = null,
        val sources: List<AnimeSourceDto>? = null,
    )

    private data class AnimeSourceDto(
        val id: String? = null,
        val name: String? = null,
        val apiBaseUrl: String? = null,
        val streamBaseUrl: String? = null,
        val websiteUrl: String? = null,
        val enabled: Boolean? = null,
        val servers: List<VideoServerDto>? = null,
    ) {
        fun toConfig(): AnimeSourceConfig? {
            if (enabled == false) return null
            val api = normalizeUrl(apiBaseUrl) ?: return null
            return AnimeSourceConfig(
                id = id?.trim()?.takeIf { it.isNotEmpty() } ?: api,
                name = name?.trim()?.takeIf { it.isNotEmpty() } ?: id ?: api,
                apiBaseUrl = api,
                streamBaseUrl = normalizeUrl(streamBaseUrl)
                    ?: AnimeSourcesConfig.DEFAULT.sources.first().streamBaseUrl,
                websiteUrl = normalizeUrl(websiteUrl),
                servers = servers.orEmpty().mapNotNull { it.toConfig() },
            )
        }
    }

    private data class VideoServerDto(
        val id: String? = null,
        val name: String? = null,
        val baseUrl: String? = null,
        val type: String? = null,
        val language: String? = null,
        val enabled: Boolean? = null,
    ) {
        fun toConfig(): VideoServerConfig? {
            if (enabled == false) return null
            val url = normalizeServerUrl(baseUrl) ?: return null
            return VideoServerConfig(
                id = id?.trim()?.takeIf { it.isNotEmpty() } ?: url,
                name = name?.trim()?.takeIf { it.isNotEmpty() } ?: id ?: url,
                baseUrl = url,
                type = parseServerType(type),
                language = language?.trim()?.takeIf { it.isNotEmpty() },
            )
        }
    }
}
