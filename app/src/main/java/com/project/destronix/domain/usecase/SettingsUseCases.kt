package com.project.destronix.domain.usecase

import com.project.destronix.domain.model.ThemeMode
import com.project.destronix.domain.model.ThemeSettings
import com.project.destronix.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveThemeSettingsUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<ThemeSettings> = repository.settings
}

class SetThemeModeUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(mode: ThemeMode) = repository.setThemeMode(mode)
}

class SetDynamicColorUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setDynamicColor(enabled)
}
