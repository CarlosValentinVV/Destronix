package com.project.destronix.domain.usecase

import com.project.destronix.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow

class ObserveSearchHistoryUseCase(private val repository: SearchHistoryRepository) {
    operator fun invoke(limit: Int = 8): Flow<List<String>> = repository.observeRecent(limit)
}

class SaveSearchQueryUseCase(private val repository: SearchHistoryRepository) {
    suspend operator fun invoke(query: String) {
        if (query.isNotBlank()) repository.save(query.trim())
    }
}

class ClearSearchHistoryUseCase(private val repository: SearchHistoryRepository) {
    suspend operator fun invoke() = repository.clear()
}
