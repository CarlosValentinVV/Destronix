package com.project.destronix.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LibraryEntryEntity::class, SearchHistoryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class DestronixDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}
