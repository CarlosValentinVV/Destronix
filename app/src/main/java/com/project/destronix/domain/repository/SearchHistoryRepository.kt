package com.project.destronix.domain.repository

import kotlinx.coroutines.flow.Flow

/** Guarda las búsquedas recientes para mostrarlas como sugerencias. */
interface SearchHistoryRepository {
    fun observeRecent(limit: Int = 8): Flow<List<String>>
    suspend fun save(query: String)
    suspend fun clear()
}
