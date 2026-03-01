package com.example.sie.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.example.sie.core.common.R as CommonR
import com.example.sie.core.designsystem.component.*
import com.example.sie.core.designsystem.theme.*

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
    AppBackground {
        when (uiState) {
            SettingsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is SettingsUiState.Success -> {
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }
                var showClearDialog by remember { mutableStateOf<ClearDataType?>(null) }

                Box(Modifier.fillMaxSize()) {
                    Column(
                        Modifier
                            .padding(SpacingMedium)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Page Title
                        Text(
                            text = stringResource(CommonR.string.settings_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = OnBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(SpacingLarge))

                        // Language Section
                        Text(
                            text = stringResource(CommonR.string.settings_language),
                            style = MaterialTheme.typography.titleMedium,
                            color = OnBackground,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = SpacingSmall)
                        )
                        ModernGradientCard(
                            modifier = Modifier.fillMaxWidth(),
                            gradient = TertiaryGradient
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

                        Spacer(Modifier.height(SpacingLarge))

                        // Font Size Section
                        Text(
                            text = stringResource(CommonR.string.settings_font_size),
                            style = MaterialTheme.typography.titleMedium,
                            color = OnBackground,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = SpacingSmall)
                        )
                        ModernGradientCard(
                            modifier = Modifier.fillMaxWidth(),
                            gradient = AccentGradient
                        ) {
                            val currentScale = uiState.settings.fontSizeScale
                            Text(
                                text = stringResource(CommonR.string.settings_font_size_current, currentScale),
                                color = OnSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = SpacingSmall)
                            )

                            Slider(
                                value = currentScale.toFloat(),
                                onValueChange = { onChangeFontSizeScale(it.toInt()) },
                                valueRange = -2f..2f,
                                steps = 3,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF11998E),
                                    activeTrackColor = Color(0xFF38EF7D),
                                    inactiveTrackColor = OnBackgroundSecondary.copy(alpha = 0.3f)
                                )
                            )
                        }

                        Spacer(Modifier.height(SpacingLarge))

                        // Data Management Section
                        Text(
                            text = stringResource(CommonR.string.settings_data_management),
                            style = MaterialTheme.typography.titleMedium,
                            color = OnBackground,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = SpacingSmall)
                        )
                        ModernGradientCard(
                            modifier = Modifier.fillMaxWidth(),
                            gradient = WarningGradient
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(SpacingSmall)) {
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
                        title = {
                            Text(
                                text = stringResource(CommonR.string.settings_clear_confirm_title),
                                color = OnBackground
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(CommonR.string.settings_clear_confirm_message, dataName),
                                color = OnBackgroundSecondary
                            )
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
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = OnBackground
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, OnBackgroundSecondary.copy(alpha = 0.5f))
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
            .padding(SpacingSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF11998E),
                unselectedColor = OnBackgroundSecondary
            )
        )
        Spacer(Modifier.width(SpacingSmall))
        Text(
            text = text,
            color = OnSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
