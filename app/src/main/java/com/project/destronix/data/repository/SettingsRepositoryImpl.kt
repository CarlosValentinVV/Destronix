package com.project.destronix.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.project.destronix.domain.model.ThemeMode
import com.project.destronix.domain.model.ThemeSettings
import com.project.destronix.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "destronix_settings")

class SettingsRepositoryImpl(
    private val context: Context,
) : SettingsRepository {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }

    override val settings: Flow<ThemeSettings> = context.settingsDataStore.data.map { preferences ->
        ThemeSettings(
            mode = ThemeMode.entries.firstOrNull { it.name == preferences[Keys.THEME_MODE] }
                ?: ThemeMode.SYSTEM,
            dynamicColor = preferences[Keys.DYNAMIC_COLOR] ?: false,
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }
}
