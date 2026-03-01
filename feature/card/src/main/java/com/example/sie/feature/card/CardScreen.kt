package com.example.sie.feature.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sie.core.designsystem.component.AppBackground
import com.example.sie.core.designsystem.component.ModernGradientButton
import com.example.sie.core.designsystem.component.ModernGradientCard
import com.example.sie.core.designsystem.component.ModernGradientProgressBar
import com.example.sie.core.designsystem.component.ModernGradientTopAppBar
import com.example.sie.core.designsystem.theme.*
import com.example.sie.core.common.R as CommonR

@Composable
fun CardRoute(
    viewModel: CardViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onStartLearning: () -> Unit,
    onCreateCard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    CardScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onStartLearning = onStartLearning,
        onCreateCard = onCreateCard
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CardScreen(
    uiState: CardUiState,
    onBackClick: () -> Unit,
    onStartLearning: () -> Unit,
    onCreateCard: () -> Unit
) {
    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ModernGradientTopAppBar(
                    title = stringResource(CommonR.string.card_title),
                    gradient = TertiaryGradient,
                    onNavigationClick = onBackClick
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(SpacingMedium),
                verticalArrangement = Arrangement.spacedBy(SpacingMedium)
            ) {
                when (uiState) {
                    CardUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is CardUiState.Success -> {
                        CardDashboard(
                            state = uiState,
                            onStartLearning = onStartLearning,
                            onCreateCard = onCreateCard
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardDashboard(
    state: CardUiState.Success,
    onStartLearning: () -> Unit,
    onCreateCard: () -> Unit
) {
    // Progress Section
    ModernGradientCard(
        modifier = Modifier.fillMaxWidth(),
        gradient = TertiaryGradient
    ) {
        Text(
            text = stringResource(CommonR.string.card_progress_title),
            style = MaterialTheme.typography.titleMedium,
            color = OnSurface,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(SpacingMedium))

        ModernGradientProgressBar(
            progress = if (state.totalCount > 0) state.learnedCount.toFloat() / state.totalCount else 0f,
            gradient = AccentGradient,
            showLabel = true
        )

        Spacer(modifier = Modifier.height(SpacingLarge))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem(
                value = state.totalCount.toString(),
                label = stringResource(CommonR.string.card_stat_total),
                gradient = TertiaryGradient
            )
            StatItem(
                value = state.learnedCount.toString(),
                label = stringResource(CommonR.string.card_stat_learned),
                gradient = AccentGradient
            )
            StatItem(
                value = state.masteredCount.toString(),
                label = stringResource(CommonR.string.card_stat_mastered),
                gradient = PrimaryGradient
            )
            StatItem(
                value = state.reviewPendingCount.toString(),
                label = stringResource(CommonR.string.card_stat_review),
                gradient = WarningGradient
            )
        }
    }

    // Actions Section
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
    ) {
        // Start Learning Button
        ModernGradientCard(
            modifier = Modifier
                .weight(1f)
                .height(140.dp),
            gradient = TertiaryGradient,
            onClick = onStartLearning
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.width(40.dp).height(40.dp),
                    tint = OnSurface
                )
                Spacer(modifier = Modifier.height(SpacingSmall))
                Text(
                    text = stringResource(CommonR.string.card_start_learning),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                if (state.reviewPendingCount > 0) {
                    Spacer(modifier = Modifier.height(SpacingXSmall))
                    Text(
                        text = stringResource(CommonR.string.card_cards_due, state.reviewPendingCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnBackgroundSecondary
                    )
                }
            }
        }

        // Create Card Button
        ModernGradientCard(
            modifier = Modifier
                .weight(1f)
                .height(140.dp),
            gradient = PrimaryGradient,
            onClick = onCreateCard
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.width(40.dp).height(40.dp),
                    tint = OnSurface
                )
                Spacer(modifier = Modifier.height(SpacingSmall))
                Text(
                    text = stringResource(CommonR.string.card_create_button),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    gradient: androidx.compose.ui.graphics.Brush
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnSurface
        )
        Spacer(modifier = Modifier.height(SpacingXSmall))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = OnBackgroundSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
