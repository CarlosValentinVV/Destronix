package com.project.destronix.domain.repository

import com.project.destronix.domain.model.ThemeMode
import com.project.destronix.domain.model.ThemeSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<ThemeSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDynamicColor(enabled: Boolean)
}
