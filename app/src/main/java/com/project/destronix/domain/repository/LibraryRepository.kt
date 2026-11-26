package com.project.destronix.domain.repository

import com.project.destronix.domain.model.LibraryEntry
import com.project.destronix.domain.model.LibraryStatus
import kotlinx.coroutines.flow.Flow

/** Contrato de persistencia local de la biblioteca del usuario. */
interface LibraryRepository {
    fun observeAll(): Flow<List<LibraryEntry>>
    fun observe(animeId: Int): Flow<LibraryEntry?>
    suspend fun save(entry: LibraryEntry)
    suspend fun setStatus(animeId: Int, status: LibraryStatus)
    suspend fun setProgress(animeId: Int, episodesWatched: Int)
    suspend fun remove(animeId: Int)
}
