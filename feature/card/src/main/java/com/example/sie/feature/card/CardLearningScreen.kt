package com.example.sie.feature.card

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sie.core.designsystem.component.GlassCard
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
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(CommonR.string.card_learning_title), color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(CommonR.string.common_back), tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else if (uiState.isFinished) {
                    FinishedView(onBackClick)
                } else {
                    val card = uiState.currentCard
                    if (card != null) {
        AnimatedContent(
            targetState = uiState.isFlipped,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "CardFlip"
        ) { isFlipped ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clickable { onFlip() },
                gradient = if (isFlipped) 
                    Brush.linearGradient(listOf(Color(0xFF2d3436), Color(0xFF636e72)))
                else 
                    Brush.linearGradient(listOf(Color(0xFF0984e3), Color(0xFF74b9ff)))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (isFlipped) stringResource(CommonR.string.card_learning_back) else stringResource(CommonR.string.card_learning_front),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = cleanText(if (isFlipped) card.back else card.front),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                        if (isFlipped) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(CommonR.string.card_learning_flip_back),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(CommonR.string.card_learning_show_answer_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

                        if (uiState.isFlipped) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = { onResult(false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFe17055)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).padding(end = 8.dp).height(56.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(CommonR.string.card_learning_dont_know))
                                }
                                Button(
                                    onClick = { onResult(true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00b894)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).padding(start = 8.dp).height(56.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(CommonR.string.card_learning_know))
                                }
                            }
                        } else {
                            Button(
                                onClick = onFlip,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text(stringResource(CommonR.string.card_learning_show_answer))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinishedView(onBackClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFF00b894),
            modifier = Modifier.height(64.dp).width(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(CommonR.string.card_learning_finished_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(CommonR.string.card_learning_finished_message),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0984e3))
        ) {
            Text(stringResource(CommonR.string.card_learning_back_to_dashboard))
        }
    }
}

private fun cleanText(text: String): String {
    return text.replace("**", "")
}
