package com.example.sie.feature.card

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sie.core.designsystem.component.AppBackground
import com.example.sie.core.designsystem.component.ModernGradientButton
import com.example.sie.core.designsystem.component.ModernGradientCard
import com.example.sie.core.designsystem.component.ModernGradientTopAppBar
import com.example.sie.core.designsystem.theme.*
import com.example.sie.core.common.R as CommonR

@Composable
fun CardLearningRoute(
    onBackClick: () -> Unit,
    viewModel: CardLearningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CardLearningScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onFlip = viewModel::flipCard,
        onResult = viewModel::markResult
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CardLearningScreen(
    uiState: CardLearningUiState,
    onBackClick: () -> Unit,
    onFlip: () -> Unit,
    onResult: (Boolean) -> Unit
) {
    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ModernGradientTopAppBar(
                    title = stringResource(CommonR.string.card_learning_title),
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else if (uiState.isFinished) {
                    FinishedView(onBackClick)
                } else {
                    val card = uiState.currentCard
                    if (card != null) {
                        // Simple 180° flip animation
                        val rotation by animateFloatAsState(
                            targetValue = if (uiState.isFlipped) 180f else 0f,
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            ),
                            label = "CardFlip"
                        )

                        ModernGradientCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp)
                                .graphicsLayer {
                                    rotationY = rotation
                                    cameraDistance = 12f * density
                                }
                                .clickable { onFlip() },
                            gradient = TertiaryGradient
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .padding(SpacingMedium)
                                        .verticalScroll(rememberScrollState())
                                        .graphicsLayer {
                                            rotationY = if (rotation > 90f) 180f else 0f
                                        }
                                ) {
                                    Text(
                                        text = if (uiState.isFlipped)
                                            stringResource(CommonR.string.card_learning_back)
                                        else
                                            stringResource(CommonR.string.card_learning_front),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnBackgroundSecondary
                                    )
                                    Spacer(modifier = Modifier.height(SpacingMedium))
                                    Text(
                                        text = cleanText(if (uiState.isFlipped) card.back else card.front),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = OnSurface,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(SpacingMedium))
                                    Text(
                                        text = if (uiState.isFlipped)
                                            stringResource(CommonR.string.card_learning_flip_back)
                                        else
                                            stringResource(CommonR.string.card_learning_show_answer_hint),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnBackgroundSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(SpacingLarge))

                        if (uiState.isFlipped) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
                            ) {
                                ModernGradientButton(
                                    text = stringResource(CommonR.string.card_learning_dont_know),
                                    onClick = { onResult(false) },
                                    gradient = WarningGradient,
                                    modifier = Modifier.weight(1f)
                                )
                                ModernGradientButton(
                                    text = stringResource(CommonR.string.card_learning_know),
                                    onClick = { onResult(true) },
                                    gradient = AccentGradient,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            ModernGradientButton(
                                text = stringResource(CommonR.string.card_learning_show_answer),
                                onClick = onFlip,
                                gradient = TertiaryGradient,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinishedView(onBackClick: () -> Unit) {
    ModernGradientCard(
        modifier = Modifier.fillMaxWidth(),
        gradient = AccentGradient
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = OnSurface,
                modifier = Modifier.height(64.dp).width(64.dp)
            )
            Spacer(modifier = Modifier.height(SpacingMedium))
            Text(
                text = stringResource(CommonR.string.card_learning_finished_title),
                style = MaterialTheme.typography.headlineMedium,
                color = OnSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(SpacingSmall))
            Text(
                text = stringResource(CommonR.string.card_learning_finished_message),
                style = MaterialTheme.typography.bodyLarge,
                color = OnBackgroundSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(SpacingLarge))
            ModernGradientButton(
                text = stringResource(CommonR.string.card_learning_back_to_dashboard),
                onClick = onBackClick,
                gradient = TertiaryGradient,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun cleanText(text: String): String {
    return text.replace("**", "")
}
