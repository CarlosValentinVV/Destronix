package com.project.destronix.domain.model

/** Filtros disponibles en la pantalla de búsqueda (equivalen a parámetros de Jikan). */
data class SearchFilters(
    val type: String? = null,
    val status: String? = null,
    val minScore: Double? = null,
    val genres: List<Int> = emptyList(),
    val year: Int? = null,
    val orderBy: String? = null,
) {
    val isActive: Boolean
        get() = type != null || status != null || minScore != null ||
            genres.isNotEmpty() || year != null || orderBy != null
}
