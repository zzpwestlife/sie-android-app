package com.example.sie.feature.settings

import com.example.sie.core.data.repository.UserDataRepository
import com.example.sie.core.model.DarkThemeConfig
import com.example.sie.core.model.UserData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SettingsViewModel
    private val userDataRepository: UserDataRepository = mockk(relaxed = true)

    @Test
    fun `updateDarkThemeConfig updates repository`() = runTest {
        // Arrange
        val initialUserData = UserData(
            darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            useDynamicColor = false,
            fontSizeScale = 0,
            language = "zh"
        )
        coEvery { userDataRepository.userData } returns flowOf(initialUserData)

        // Act
        viewModel = SettingsViewModel(userDataRepository)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateDarkThemeConfig(DarkThemeConfig.DARK)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify { userDataRepository.setDarkThemeConfig(DarkThemeConfig.DARK) }
    }

    @Test
    fun `updateFontSizeScale updates repository`() = runTest {
        // Arrange
        val initialUserData = UserData(
            darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            useDynamicColor = false,
            fontSizeScale = 0,
            language = "zh"
        )
        coEvery { userDataRepository.userData } returns flowOf(initialUserData)

        // Act
        viewModel = SettingsViewModel(userDataRepository)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateFontSizeScale(2)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify { userDataRepository.setFontSizeScale(2) }
    }

    @Test
    fun `updateLanguage updates repository`() = runTest {
        // Arrange
        val initialUserData = UserData(
            darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            useDynamicColor = false,
            fontSizeScale = 0,
            language = "zh"
        )
        coEvery { userDataRepository.userData } returns flowOf(initialUserData)

        // Act
        viewModel = SettingsViewModel(userDataRepository)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateLanguage("en")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify { userDataRepository.setLanguage("en") }
    }
}
