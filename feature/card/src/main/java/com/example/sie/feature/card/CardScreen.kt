package com.example.sie.feature.card

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sie.core.designsystem.component.GlassCard
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
                    title = {
                        Text(
                            text = stringResource(CommonR.string.card_title),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
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
                    .padding(16.dp)
            ) {

                when (uiState) {
                    CardUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
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
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF2d3436).copy(alpha = 0.5f),
                Color(0xFF2d3436).copy(alpha = 0.3f)
            )
        )
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(CommonR.string.card_progress_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                val progress = if (state.totalCount > 0) 
                    (state.learnedCount.toFloat() / state.totalCount) * 100 
                else 0f
                Text(
                    text = "${progress.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF0984e3)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { if (state.totalCount > 0) state.learnedCount.toFloat() / state.totalCount else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF0984e3),
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(value = state.totalCount.toString(), label = stringResource(CommonR.string.card_stat_total), color = Color(0xFF0984e3))
                StatItem(value = state.learnedCount.toString(), label = stringResource(CommonR.string.card_stat_learned), color = Color(0xFF00b894))
                StatItem(value = state.masteredCount.toString(), label = stringResource(CommonR.string.card_stat_mastered), color = Color(0xFFfdcb6e))
                StatItem(value = state.reviewPendingCount.toString(), label = stringResource(CommonR.string.card_stat_review), color = Color(0xFFe17055))
            }
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    // Actions Section
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Start Learning Button
        Button(
            onClick = onStartLearning,
            modifier = Modifier
                .weight(1f)
                .height(100.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0984e3)
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.width(32.dp).height(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(CommonR.string.card_start_learning),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                if (state.reviewPendingCount > 0) {
                    Text(
                        text = stringResource(CommonR.string.card_cards_due, state.reviewPendingCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        // Create Card Button
        Button(
            onClick = onCreateCard,
            modifier = Modifier
                .weight(1f)
                .height(100.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2d3436).copy(alpha = 0.5f) // Dark purple/black from screenshot looks custom
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.width(32.dp).height(32.dp),
                    tint = Color(0xFFe056fd) // Purple tint
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(CommonR.string.card_create_button),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFe056fd),
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
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
