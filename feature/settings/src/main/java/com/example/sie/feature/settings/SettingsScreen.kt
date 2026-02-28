package com.example.sie.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sie.core.model.DarkThemeConfig

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsScreen(
        uiState = uiState,
        onChangeDarkThemeConfig = viewModel::updateDarkThemeConfig,
        onChangeFontSizeScale = viewModel::updateFontSizeScale,
        onChangeLanguage = viewModel::updateLanguage
    )
}

@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    onChangeDarkThemeConfig: (DarkThemeConfig) -> Unit,
    onChangeFontSizeScale: (Int) -> Unit,
    onChangeLanguage: (String) -> Unit
) {
    when (uiState) {
        SettingsUiState.Loading -> {
             Column(Modifier.padding(16.dp)) {
                 CircularProgressIndicator()
             }
        }
        is SettingsUiState.Success -> {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(32.dp))
                
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.titleMedium
                )
                Column(Modifier.selectableGroup()) {
                    SettingsDialogThemeChooserRow(
                        text = "System Default",
                        selected = uiState.settings.darkThemeConfig == DarkThemeConfig.FOLLOW_SYSTEM,
                        onClick = { onChangeDarkThemeConfig(DarkThemeConfig.FOLLOW_SYSTEM) }
                    )
                    SettingsDialogThemeChooserRow(
                        text = "Light",
                        selected = uiState.settings.darkThemeConfig == DarkThemeConfig.LIGHT,
                        onClick = { onChangeDarkThemeConfig(DarkThemeConfig.LIGHT) }
                    )
                    SettingsDialogThemeChooserRow(
                        text = "Dark",
                        selected = uiState.settings.darkThemeConfig == DarkThemeConfig.DARK,
                        onClick = { onChangeDarkThemeConfig(DarkThemeConfig.DARK) }
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Language",
                    style = MaterialTheme.typography.titleMedium
                )
                Column(Modifier.selectableGroup()) {
                    SettingsDialogThemeChooserRow(
                        text = "English",
                        selected = uiState.settings.language == "en",
                        onClick = { onChangeLanguage("en") }
                    )
                    SettingsDialogThemeChooserRow(
                        text = "中文",
                        selected = uiState.settings.language == "zh",
                        onClick = { onChangeLanguage("zh") }
                    )
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Font Size Scale",
                    style = MaterialTheme.typography.titleMedium
                )
                
                val currentScale = uiState.settings.fontSizeScale
                Text("Current Scale: $currentScale")
                
                Slider(
                    value = currentScale.toFloat(),
                    onValueChange = { onChangeFontSizeScale(it.toInt()) },
                    valueRange = -2f..2f,
                    steps = 3
                )
            }
        }
    }
}

@Composable
fun SettingsDialogThemeChooserRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(Modifier.padding(8.dp))
        Text(text)
    }
}
