package com.project.destronix.domain.model

/** Episodio reproducible. El catálogo de Jikan no facilita streams legales. */
data class VideoEpisode(
    val number: Int,
    val title: String,
    val streamUrl: String,
    val servers: List<VideoServer> = emptyList(),
    val thumbnailUrl: String? = null,
) {
    /** URL del servidor seleccionado, con compatibilidad con episodios de una sola fuente. */
    fun urlFor(index: Int): String = servers.getOrNull(index)?.url ?: streamUrl
}
