package com.project.destronix.domain.usecase

import com.project.destronix.domain.model.LibraryEntry
import com.project.destronix.domain.model.LibraryStatus
import com.project.destronix.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow

class ObserveLibraryUseCase(private val repository: LibraryRepository) {
    operator fun invoke(): Flow<List<LibraryEntry>> = repository.observeAll()
}

class ObserveLibraryEntryUseCase(private val repository: LibraryRepository) {
    operator fun invoke(animeId: Int): Flow<LibraryEntry?> = repository.observe(animeId)
}

class SaveLibraryEntryUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(entry: LibraryEntry) = repository.save(entry)
}

class SetLibraryStatusUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(animeId: Int, status: LibraryStatus) =
        repository.setStatus(animeId, status)
}

class SetWatchProgressUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(animeId: Int, episodesWatched: Int) =
        repository.setProgress(animeId, episodesWatched)
}

class RemoveFromLibraryUseCase(private val repository: LibraryRepository) {
    suspend operator fun invoke(animeId: Int) = repository.remove(animeId)
}
