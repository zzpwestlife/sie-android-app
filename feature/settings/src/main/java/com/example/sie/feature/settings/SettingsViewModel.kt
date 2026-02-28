package com.example.sie.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sie.core.data.repository.UserDataRepository
import com.example.sie.core.model.DarkThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = userDataRepository.userData
        .map { userData ->
            SettingsUiState.Success(
                settings = UserEditableSettings(
                    darkThemeConfig = userData.darkThemeConfig,
                    fontSizeScale = userData.fontSizeScale,
                    language = userData.language
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState.Loading
        )

    fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        viewModelScope.launch {
            userDataRepository.setDarkThemeConfig(darkThemeConfig)
        }
    }

    fun updateFontSizeScale(scale: Int) {
        viewModelScope.launch {
            userDataRepository.setFontSizeScale(scale)
        }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            userDataRepository.setLanguage(language)
        }
    }
}

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val settings: UserEditableSettings) : SettingsUiState
}

data class UserEditableSettings(
    val darkThemeConfig: DarkThemeConfig,
    val fontSizeScale: Int,
    val language: String,
)
