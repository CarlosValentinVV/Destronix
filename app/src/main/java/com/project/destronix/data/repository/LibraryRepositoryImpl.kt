package com.project.destronix.data.repository

import com.project.destronix.data.local.LibraryDao
import com.project.destronix.data.local.LibraryEntryEntity
import com.project.destronix.data.local.SearchHistoryDao
import com.project.destronix.data.local.SearchHistoryEntity
import com.project.destronix.domain.model.LibraryEntry
import com.project.destronix.domain.model.LibraryStatus
import com.project.destronix.domain.repository.LibraryRepository
import com.project.destronix.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LibraryRepositoryImpl(
    private val dao: LibraryDao,
) : LibraryRepository {

    override fun observeAll(): Flow<List<LibraryEntry>> =
        dao.observeAll().map { entries -> entries.map(LibraryEntryEntity::toDomain) }

    override fun observe(animeId: Int): Flow<LibraryEntry?> =
        dao.observe(animeId).map { it?.toDomain() }

    override suspend fun save(entry: LibraryEntry) = dao.upsert(entry.toEntity())

    override suspend fun setStatus(animeId: Int, status: LibraryStatus) =
        dao.updateStatus(animeId, status.name, System.currentTimeMillis())

    override suspend fun setProgress(animeId: Int, episodesWatched: Int) =
        dao.updateProgress(animeId, episodesWatched.coerceAtLeast(0), System.currentTimeMillis())

    override suspend fun remove(animeId: Int) = dao.delete(animeId)
}

class SearchHistoryRepositoryImpl(
    private val dao: SearchHistoryDao,
) : SearchHistoryRepository {

    override fun observeRecent(limit: Int): Flow<List<String>> = dao.observeRecent(limit)

    override suspend fun save(query: String) =
        dao.upsert(SearchHistoryEntity(query = query.trim(), createdAt = System.currentTimeMillis()))

    override suspend fun clear() = dao.clear()
}

private fun LibraryEntryEntity.toDomain() = LibraryEntry(
    animeId = animeId,
    title = title,
    imageUrl = imageUrl,
    status = LibraryStatus.from(status),
    score = score,
    progressEpisodes = progressEpisodes,
    totalEpisodes = totalEpisodes,
    addedAt = addedAt,
    updatedAt = updatedAt,
)

private fun LibraryEntry.toEntity() = LibraryEntryEntity(
    animeId = animeId,
    title = title,
    imageUrl = imageUrl,
    status = status.name,
    score = score,
    progressEpisodes = progressEpisodes,
    totalEpisodes = totalEpisodes,
    addedAt = addedAt,
    updatedAt = System.currentTimeMillis(),
)
