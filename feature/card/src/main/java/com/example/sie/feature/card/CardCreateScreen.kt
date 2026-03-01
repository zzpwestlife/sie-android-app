package com.example.sie.feature.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sie.core.designsystem.component.AppBackground
import com.example.sie.core.designsystem.component.ModernGradientButton
import com.example.sie.core.designsystem.component.ModernGradientTopAppBar
import com.example.sie.core.designsystem.theme.*
import com.example.sie.core.common.R as CommonR

@Composable
fun CardCreateRoute(
    onBackClick: () -> Unit,
    viewModel: CardCreateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val successText = stringResource(CommonR.string.card_create_success)
    val errorText = stringResource(CommonR.string.common_error)

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar(successText)
            viewModel.resetSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(uiState.errorMessage ?: errorText)
        }
    }

    CardCreateScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onFrontChange = viewModel::updateFront,
        onBackChange = viewModel::updateBack,
        onCategoryChange = viewModel::updateCategory,
        onSave = viewModel::saveCard,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CardCreateScreen(
    uiState: CardCreateUiState,
    onBackClick: () -> Unit,
    onFrontChange: (String) -> Unit,
    onBackChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSave: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                ModernGradientTopAppBar(
                    title = stringResource(CommonR.string.card_create_title),
                    gradient = TertiaryGradient,
                    onNavigationClick = onBackClick
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(SpacingMedium)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = uiState.category,
                    onValueChange = onCategoryChange,
                    label = { Text(stringResource(CommonR.string.card_create_category), color = OnBackgroundSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface,
                        focusedBorderColor = Color(0xFF56CCF2),
                        unfocusedBorderColor = OnBackgroundSecondary
                    )
                )

                Spacer(modifier = Modifier.height(SpacingMedium))

                OutlinedTextField(
                    value = uiState.front,
                    onValueChange = onFrontChange,
                    label = { Text(stringResource(CommonR.string.card_create_front), color = OnBackgroundSecondary) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface,
                        focusedBorderColor = Color(0xFF56CCF2),
                        unfocusedBorderColor = OnBackgroundSecondary
                    ),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(SpacingMedium))

                OutlinedTextField(
                    value = uiState.back,
                    onValueChange = onBackChange,
                    label = { Text(stringResource(CommonR.string.card_create_back), color = OnBackgroundSecondary) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface,
                        focusedBorderColor = Color(0xFF56CCF2),
                        unfocusedBorderColor = OnBackgroundSecondary
                    ),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(SpacingLarge))

                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.height(56.dp))
                } else {
                    ModernGradientButton(
                        text = stringResource(CommonR.string.card_create_save),
                        onClick = onSave,
                        gradient = TertiaryGradient,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.front.isNotBlank() && uiState.back.isNotBlank()
                    )
                }
            }
        }
    }
}
