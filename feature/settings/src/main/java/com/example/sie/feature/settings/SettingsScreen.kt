package com.example.sie.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.example.sie.core.common.R as CommonR
import com.example.sie.core.designsystem.component.GlassCard
import com.example.sie.core.designsystem.theme.PrimaryGradient
import com.example.sie.core.designsystem.theme.SecondaryGradient
import com.example.sie.core.designsystem.theme.SuccessGradient
import com.example.sie.core.model.DarkThemeConfig

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsScreen(
        uiState = uiState,
        onChangeFontSizeScale = viewModel::updateFontSizeScale,
        onChangeLanguage = viewModel::updateLanguage,
        onClearStudyHistory = viewModel::clearStudyHistory,
        onClearWrongQuestions = viewModel::clearWrongQuestions,
        onClearBookmarks = viewModel::clearBookmarks,
        onClearExamHistory = viewModel::clearExamHistory,
        onClearAllData = viewModel::clearAllUserData
    )
}

@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    onChangeFontSizeScale: (Int) -> Unit,
    onChangeLanguage: (String) -> Unit,
    onClearStudyHistory: () -> Unit,
    onClearWrongQuestions: () -> Unit,
    onClearBookmarks: () -> Unit,
    onClearExamHistory: () -> Unit,
    onClearAllData: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460)
                    )
                )
            )
    ) {
        when (uiState) {
            SettingsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            is SettingsUiState.Success -> {
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }
                var showClearDialog by remember { mutableStateOf<ClearDataType?>(null) }

                Box(Modifier.fillMaxSize()) {
                    Column(
                        Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                    Text(
                        text = stringResource(CommonR.string.settings_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = stringResource(CommonR.string.settings_language),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        gradient = SecondaryGradient
                    ) {
                        Column(Modifier.selectableGroup()) {
                            SettingsDialogThemeChooserRow(
                                text = stringResource(CommonR.string.settings_language_en),
                                selected = uiState.settings.language == "en",
                                onClick = { onChangeLanguage("en") }
                            )
                            SettingsDialogThemeChooserRow(
                                text = stringResource(CommonR.string.settings_language_zh),
                                selected = uiState.settings.language == "zh",
                                onClick = { onChangeLanguage("zh") }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = stringResource(CommonR.string.settings_font_size),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        gradient = SuccessGradient
                    ) {
                        val currentScale = uiState.settings.fontSizeScale
                        Text(
                            text = stringResource(CommonR.string.settings_font_size_current, currentScale),
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Slider(
                            value = currentScale.toFloat(),
                            onValueChange = { onChangeFontSizeScale(it.toInt()) },
                            valueRange = -2f..2f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF667eea),
                                activeTrackColor = Color(0xFF667eea),
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Data Management Section
                    Text(
                        text = stringResource(CommonR.string.settings_data_management),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        gradient = com.example.sie.core.designsystem.theme.ErrorGradient
                    ) {
                        Column {
                            DataManagementButton(
                                text = stringResource(CommonR.string.settings_clear_study_history),
                                onClick = { showClearDialog = ClearDataType.StudyHistory }
                            )
                            DataManagementButton(
                                text = stringResource(CommonR.string.settings_clear_wrong_questions),
                                onClick = { showClearDialog = ClearDataType.WrongQuestions }
                            )
                            DataManagementButton(
                                text = stringResource(CommonR.string.settings_clear_bookmarks),
                                onClick = { showClearDialog = ClearDataType.Bookmarks }
                            )
                            DataManagementButton(
                                text = stringResource(CommonR.string.settings_clear_exam_history),
                                onClick = { showClearDialog = ClearDataType.ExamHistory }
                            )
                            DataManagementButton(
                                text = stringResource(CommonR.string.settings_clear_all_data),
                                onClick = { showClearDialog = ClearDataType.AllData }
                            )
                        }
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

                // Clear Confirmation Dialog
                showClearDialog?.let { dataType ->
                    val dataName = stringResource(dataType.getNameResId())
                    val successMessage = stringResource(CommonR.string.settings_clear_success, dataName)
                    AlertDialog(
                        onDismissRequest = { showClearDialog = null },
                        title = { Text(stringResource(CommonR.string.settings_clear_confirm_title)) },
                        text = {
                            Text(stringResource(CommonR.string.settings_clear_confirm_message, dataName))
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    when (dataType) {
                                        ClearDataType.StudyHistory -> onClearStudyHistory()
                                        ClearDataType.WrongQuestions -> onClearWrongQuestions()
                                        ClearDataType.Bookmarks -> onClearBookmarks()
                                        ClearDataType.ExamHistory -> onClearExamHistory()
                                        ClearDataType.AllData -> onClearAllData()
                                    }
                                    showClearDialog = null
                                    scope.launch {
                                        snackbarHostState.showSnackbar(successMessage)
                                    }
                                }
                            ) {
                                Text(stringResource(CommonR.string.common_submit))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearDialog = null }) {
                                Text(stringResource(CommonR.string.common_cancel))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DataManagementButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Text(text)
    }
}

private enum class ClearDataType {
    StudyHistory,
    WrongQuestions,
    Bookmarks,
    ExamHistory,
    AllData;

    fun getNameResId() = when (this) {
        StudyHistory -> CommonR.string.settings_data_study_history
        WrongQuestions -> CommonR.string.settings_data_wrong_questions
        Bookmarks -> CommonR.string.settings_data_bookmarks
        ExamHistory -> CommonR.string.settings_data_exam_history
        AllData -> CommonR.string.settings_data_all_data
    }
}

@Composable
private fun SettingsDialogThemeChooserRow(
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
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF667eea),
                unselectedColor = Color.White.copy(alpha = 0.6f)
            )
        )
        Spacer(Modifier.padding(8.dp))
        Text(
            text = text,
            color = Color.White
        )
    }
}
