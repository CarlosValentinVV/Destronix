package com.project.destronix.presentation.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.destronix.domain.model.ThemeMode
import com.project.destronix.domain.model.ThemeSettings
import com.project.destronix.domain.usecase.ClearSearchHistoryUseCase
import com.project.destronix.domain.usecase.ObserveThemeSettingsUseCase
import com.project.destronix.domain.usecase.SetDynamicColorUseCase
import com.project.destronix.domain.usecase.SetThemeModeUseCase
import kotlinx.coroutines.launch

data class SettingsUiState(
    val theme: ThemeSettings = ThemeSettings(),
    val historyCleared: Boolean = false,
)

class SettingsViewModel(
    private val observeTheme: ObserveThemeSettingsUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setDynamicColorUseCase: SetDynamicColorUseCase,
    private val clearSearchHistory: ClearSearchHistoryUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(SettingsUiState())
        private set

    init {
        viewModelScope.launch {
            observeTheme().collect { settings ->
                uiState = uiState.copy(theme = settings)
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { setThemeModeUseCase(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { setDynamicColorUseCase(enabled) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            clearSearchHistory()
            uiState = uiState.copy(historyCleared = true)
        }
    }

    fun consumeMessage() {
        uiState = uiState.copy(historyCleared = false)
    }
}
