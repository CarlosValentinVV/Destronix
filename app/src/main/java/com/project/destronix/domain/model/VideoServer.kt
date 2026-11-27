package com.project.destronix.domain.model

/** Tipo de fuente de vídeo soportada por el reproductor. */
enum class VideoServerType {
    DIRECT,
    HLS,
    EMBED,
}

/** Servidor disponible para un episodio, procedente de la configuración externa. */
data class VideoServer(
    val id: String,
    val label: String,
    val url: String,
    val type: VideoServerType = VideoServerType.DIRECT,
    val language: String? = null,
)
