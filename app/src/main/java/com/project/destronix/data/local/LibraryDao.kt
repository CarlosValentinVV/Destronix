package com.project.destronix.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "library_entries")
data class LibraryEntryEntity(
    @PrimaryKey val animeId: Int,
    val title: String,
    val imageUrl: String?,
    val status: String,
    val score: Double?,
    val progressEpisodes: Int,
    val totalEpisodes: Int?,
    val addedAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val createdAt: Long,
)

@Dao
interface LibraryDao {
    @Query("SELECT * FROM library_entries ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<LibraryEntryEntity>>

    @Query("SELECT * FROM library_entries WHERE animeId = :animeId")
    fun observe(animeId: Int): Flow<LibraryEntryEntity?>

    @Upsert
    suspend fun upsert(entry: LibraryEntryEntity)

    @Query("UPDATE library_entries SET status = :status, updatedAt = :updatedAt WHERE animeId = :animeId")
    suspend fun updateStatus(animeId: Int, status: String, updatedAt: Long)

    @Query("UPDATE library_entries SET progressEpisodes = :progress, updatedAt = :updatedAt WHERE animeId = :animeId")
    suspend fun updateProgress(animeId: Int, progress: Int, updatedAt: Long)

    @Query("DELETE FROM library_entries WHERE animeId = :animeId")
    suspend fun delete(animeId: Int)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT query FROM search_history ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<String>>

    @Upsert
    suspend fun upsert(entry: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clear()
}
