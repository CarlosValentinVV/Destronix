package com.project.destronix.domain.model

/** Episodio reproducible. El catálogo de Jikan no facilita streams legales. */
data class VideoEpisode(
    val number: Int,
    val title: String,
    val streamUrl: String,
    val thumbnailUrl: String? = null,
)
