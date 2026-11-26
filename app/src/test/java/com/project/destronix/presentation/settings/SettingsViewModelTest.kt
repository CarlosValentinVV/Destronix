package com.project.destronix.presentation.settings

import com.project.destronix.domain.model.ThemeMode
import com.project.destronix.domain.model.ThemeSettings
import com.project.destronix.domain.repository.SearchHistoryRepository
import com.project.destronix.domain.repository.SettingsRepository
import com.project.destronix.domain.usecase.ClearSearchHistoryUseCase
import com.project.destronix.domain.usecase.ObserveThemeSettingsUseCase
import com.project.destronix.domain.usecase.SetDynamicColorUseCase
import com.project.destronix.domain.usecase.SetThemeModeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val settings = FakeSettingsRepository()
    private val history = FakeSearchHistoryRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `theme changes are persisted and observed`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.setThemeMode(ThemeMode.DARK)
        viewModel.setDynamicColor(true)
        runCurrent()

        assertEquals(ThemeMode.DARK, viewModel.uiState.theme.mode)
        assertTrue(viewModel.uiState.theme.dynamicColor)
    }

    @Test
    fun `clearing history shows confirmation and can be consumed`() = runTest(dispatcher) {
        history.saved.value = listOf("naruto")
        val viewModel = createViewModel()
        runCurrent()

        viewModel.clearHistory()
        runCurrent()

        assertTrue(history.saved.value.isEmpty())
        assertTrue(viewModel.uiState.historyCleared)
        viewModel.consumeMessage()
        assertFalse(viewModel.uiState.historyCleared)
    }

    private fun createViewModel() = SettingsViewModel(
        ObserveThemeSettingsUseCase(settings),
        SetThemeModeUseCase(settings),
        SetDynamicColorUseCase(settings),
        ClearSearchHistoryUseCase(history),
    )

    private class FakeSettingsRepository : SettingsRepository {
        private val current = MutableStateFlow(ThemeSettings())

        override val settings: Flow<ThemeSettings> = current

        override suspend fun setThemeMode(mode: ThemeMode) {
            current.value = current.value.copy(mode = mode)
        }

        override suspend fun setDynamicColor(enabled: Boolean) {
            current.value = current.value.copy(dynamicColor = enabled)
        }
    }

    private class FakeSearchHistoryRepository : SearchHistoryRepository {
        val saved = MutableStateFlow<List<String>>(emptyList())

        override fun observeRecent(limit: Int): Flow<List<String>> = saved

        override suspend fun save(query: String) {
            saved.value = listOf(query) + saved.value.filter { it != query }
        }

        override suspend fun clear() {
            saved.value = emptyList()
        }
    }
}
