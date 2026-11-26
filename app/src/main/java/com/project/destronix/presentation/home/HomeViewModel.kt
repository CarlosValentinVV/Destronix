package com.project.destronix.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.destronix.core.AppError
import com.project.destronix.core.AppResult
import com.project.destronix.domain.model.HomeFeed
import com.project.destronix.domain.usecase.GetHomeFeedUseCase
import kotlinx.coroutines.launch

data class HomeUiState(
    val feed: HomeFeed? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: AppError? = null,
)

class HomeViewModel(
    private val getHomeFeed: GetHomeFeedUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(HomeUiState())
        private set

    init {
        load()
    }

    fun load(refresh: Boolean = false) {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = !refresh && uiState.feed == null,
                isRefreshing = refresh,
                error = null,
            )
            uiState = when (val result = getHomeFeed()) {
                is AppResult.Success -> uiState.copy(
                    feed = result.data,
                    isLoading = false,
                    isRefreshing = false,
                )

                is AppResult.Error -> uiState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = result.error,
                )

                AppResult.Loading -> uiState.copy(isLoading = true, isRefreshing = false)
            }
        }
    }
}
