# Glassmorphism UI/UX Upgrade Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Transform the SIE Android app UI from functional to visually compelling using Glassmorphism Lite design with vibrant gradients and smooth animations.

**Architecture:** Build reusable glass-styled components (GlassCard, GradientButton, etc.) in the design system module, then progressively upgrade each feature screen to use these components. No blur effects—pure Compose primitives for performance.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Compose Animation APIs

**Design Doc:** `docs/design/2026-02-28-glassmorphism-ui-upgrade.md`

---

## Phase 1: Foundation - Design System Components

### Task 1: Create Glass Colors and Gradients

**Files:**
- Create: `core/designsystem/src/main/java/com/example/sie/core/designsystem/theme/GlassColors.kt`

**Step 1: Create GlassColors.kt with gradient definitions**

```kotlin
package com.example.sie.core.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Glass Surface Colors
val GlassSurface = Color.White.copy(alpha = 0.15f)
val GlassBorder = Color.White.copy(alpha = 0.3f)
val GlassShadow = Color.Black.copy(alpha = 0.1f)

// Gradient Brushes
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
)

val SecondaryGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFf093fb), Color(0xFFf5576c))
)

val TertiaryGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF4facfe), Color(0xFF00f2fe))
)

val AccentGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFfa709a), Color(0xFFfee140))
)

// Success/Error Gradients
val SuccessGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))
)

val ErrorGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF44336), Color(0xFFEF5350))
)
```

**Step 2: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/theme/GlassColors.kt
git commit -m "feat(design): add glass surface colors and gradient brushes"
```

---

### Task 2: Create GlassCard Component

**Files:**
- Create: `core/designsystem/src/main/java/com/example/sie/core/designsystem/component/GlassCard.kt`

**Step 1: Implement GlassCard composable**

```kotlin
package com.example.sie.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.sie.core.designsystem.theme.GlassBorder
import com.example.sie.core.designsystem.theme.GlassSurface

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    gradient: Brush? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.then(
            onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier
        ),
        colors = CardDefaults.cardColors(
            containerColor = GlassSurface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            brush = gradient ?: SolidColor(GlassBorder)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
```

**Step 2: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/component/GlassCard.kt
git commit -m "feat(design): add GlassCard component with gradient border support"
```

---

### Task 3: Create GradientButton Component

**Files:**
- Create: `core/designsystem/src/main/java/com/example/sie/core/designsystem/component/GradientButton.kt`

**Step 1: Implement GradientButton with scale animation**

```kotlin
package com.example.sie.core.designsystem.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GradientButton(
    text: String,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

    Box(
        modifier = modifier
            .height(56.dp)
            .scale(scale)
            .background(
                brush = if (enabled) gradient else Brush.linearGradient(listOf(Color.Gray, Color.DarkGray)),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
```

**Step 2: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/component/GradientButton.kt
git commit -m "feat(design): add GradientButton with press animation"
```

---

### Task 4: Create GlassTopAppBar Component

**Files:**
- Create: `core/designsystem/src/main/java/com/example/sie/core/designsystem/component/GlassTopAppBar.kt`

**Step 1: Implement GlassTopAppBar**

```kotlin
package com.example.sie.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sie.core.designsystem.theme.GlassBorder
import com.example.sie.core.designsystem.theme.GlassSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(GlassSurface)
            .border(width = 1.dp, color = GlassBorder)
    ) {
        TopAppBar(
            title = { Text(title, color = Color.White) },
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}
```

**Step 2: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/component/GlassTopAppBar.kt
git commit -m "feat(design): add GlassTopAppBar component"
```

---

### Task 5: Create StatChip Component

**Files:**
- Create: `core/designsystem/src/main/java/com/example/sie/core/designsystem/component/StatChip.kt`

**Step 1: Implement StatChip**

```kotlin
package com.example.sie.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sie.core.designsystem.theme.GlassBorder
import com.example.sie.core.designsystem.theme.GlassSurface

@Composable
fun StatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = GlassSurface,
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White.copy(0.7f)
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
```

**Step 2: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/component/StatChip.kt
git commit -m "feat(design): add StatChip component for stats display"
```

---

### Task 6: Create GradientProgressIndicator Component

**Files:**
- Create: `core/designsystem/src/main/java/com/example/sie/core/designsystem/component/GradientProgressIndicator.kt`

**Step 1: Implement GradientProgressIndicator**

```kotlin
package com.example.sie.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GradientProgressIndicator(
    progress: Float,
    gradient: Brush,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White.copy(alpha = 0.2f),
    animate: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (animate) progress else progress,
        animationSpec = tween(durationMillis = 700),
        label = "progress_animation"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        val width = size.width
        val height = size.height
        val cornerRadius = CornerRadius(height / 2, height / 2)

        // Background track
        drawRoundRect(
            color = backgroundColor,
            topLeft = Offset.Zero,
            size = Size(width, height),
            cornerRadius = cornerRadius
        )

        // Progress fill
        if (animatedProgress > 0f) {
            drawRoundRect(
                brush = gradient,
                topLeft = Offset.Zero,
                size = Size(width * animatedProgress.coerceIn(0f, 1f), height),
                cornerRadius = cornerRadius
            )
        }
    }
}
```

**Step 2: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/component/GradientProgressIndicator.kt
git commit -m "feat(design): add GradientProgressIndicator with animation"
```

---

### Task 7: Add Animation Constants to Theme

**Files:**
- Modify: `core/designsystem/src/main/java/com/example/sie/core/designsystem/theme/Theme.kt`

**Step 1: Add animation constants**

Add these constants at the top of `Theme.kt`:

```kotlin
// Animation durations
const val ANIM_FAST = 300 // Button clicks
const val ANIM_NORMAL = 500 // Page transitions
const val ANIM_SLOW = 700 // Score reveals
```

**Step 2: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/theme/Theme.kt
git commit -m "feat(design): add animation duration constants"
```

---

## Phase 2: Home Screen Upgrade

### Task 8: Add Gradient Background to HomeScreen

**Files:**
- Modify: `feature/home/src/main/java/com/example/sie/feature/home/HomeScreen.kt`

**Step 1: Import glass components and colors**

Add imports at the top:

```kotlin
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import com.example.sie.core.designsystem.component.GlassCard
import com.example.sie.core.designsystem.theme.*
```

**Step 2: Replace Scaffold background with gradient**

Modify the `HomeScreen` composable to wrap content in a Box with gradient background:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onTopicSelectionClick: () -> Unit,
    onMockExamClick: () -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit,
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
                SieTopAppBar(
                    title = "Dashboard",
                )
            }
        ) { paddingValues ->
            // ... rest of content
        }
    }
}
```

**Step 3: Commit**

```bash
git add feature/home/src/main/java/com/example/sie/feature/home/HomeScreen.kt
git commit -m "feat(home): add gradient background to HomeScreen"
```

---

### Task 9: Replace Buttons with GlassCards

**Files:**
- Modify: `feature/home/src/main/java/com/example/sie/feature/home/HomeScreen.kt`

**Step 1: Replace button content with GlassCards**

Replace the Column content inside Scaffold:

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
) {
    Text(
        text = "Welcome to Entry Test Prep",
        style = MaterialTheme.typography.headlineMedium,
        color = Color.White
    )

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        gradient = PrimaryGradient,
        onClick = onTopicSelectionClick
    ) {
        Text(
            text = "🎯 Start Practice",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Study questions by topic",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        gradient = SecondaryGradient,
        onClick = onMockExamClick
    ) {
        Text(
            text = "📝 Mock Exam",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Take a full practice test",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        gradient = TertiaryGradient,
        onClick = onStatsClick
    ) {
        Text(
            text = "📊 Statistics",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "View your performance",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        gradient = AccentGradient,
        onClick = onSettingsClick
    ) {
        Text(
            text = "⚙️ Settings",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Configure app preferences",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}
```

**Step 2: Commit**

```bash
git add feature/home/src/main/java/com/example/sie/feature/home/HomeScreen.kt
git commit -m "feat(home): replace buttons with GlassCards with gradients"
```

---

### Task 10: Add Stagger Animation to HomeScreen

**Files:**
- Modify: `feature/home/src/main/java/com/example/sie/feature/home/HomeScreen.kt`

**Step 1: Add animation imports and state**

```kotlin
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
```

**Step 2: Wrap each GlassCard with AnimatedVisibility**

Create a helper composable:

```kotlin
@Composable
private fun AnimatedGlassCard(
    visible: Boolean,
    delay: Int,
    gradient: Brush,
    onClick: () -> Unit,
    title: String,
    subtitle: String
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 500, delayMillis = delay)
        ) + slideInVertically(
            animationSpec = tween(durationMillis = 500, delayMillis = delay),
            initialOffsetY = { it / 4 }
        )
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            gradient = gradient,
            onClick = onClick
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
```

**Step 3: Use in HomeScreen**

```kotlin
var cardsVisible by remember { mutableStateOf(false) }

LaunchedEffect(Unit) {
    cardsVisible = true
}

Column(...) {
    Text("Welcome to Entry Test Prep", ...)

    AnimatedGlassCard(
        visible = cardsVisible,
        delay = 0,
        gradient = PrimaryGradient,
        onClick = onTopicSelectionClick,
        title = "🎯 Start Practice",
        subtitle = "Study questions by topic"
    )

    AnimatedGlassCard(
        visible = cardsVisible,
        delay = 100,
        gradient = SecondaryGradient,
        onClick = onMockExamClick,
        title = "📝 Mock Exam",
        subtitle = "Take a full practice test"
    )

    AnimatedGlassCard(
        visible = cardsVisible,
        delay = 200,
        gradient = TertiaryGradient,
        onClick = onStatsClick,
        title = "📊 Statistics",
        subtitle = "View your performance"
    )

    AnimatedGlassCard(
        visible = cardsVisible,
        delay = 300,
        gradient = AccentGradient,
        onClick = onSettingsClick,
        title = "⚙️ Settings",
        subtitle = "Configure app preferences"
    )
}
```

**Step 4: Commit**

```bash
git add feature/home/src/main/java/com/example/sie/feature/home/HomeScreen.kt
git commit -m "feat(home): add stagger animation to GlassCards"
```

---

## Phase 3: Study Screen Upgrade

### Task 11: Enhance QuestionCard with Glass Styling

**Files:**
- Modify: `core/designsystem/src/main/java/com/example/sie/core/designsystem/component/QuestionCard.kt`

**Step 1: Add glass styling imports**

```kotlin
import com.example.sie.core.designsystem.theme.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
```

**Step 2: Replace Card with GlassCard wrapper**

Modify the `QuestionCard` function:

```kotlin
@Composable
fun QuestionCard(
    question: Question,
    selectedOptionIndex: Int?,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showFeedback: Boolean = false,
    showExplanation: Boolean = true,
    language: String = "en"
) {
    val borderBrush = when {
        showFeedback && selectedOptionIndex != null && selectedOptionIndex == question.correctAnswerIndex -> SuccessGradient
        showFeedback && selectedOptionIndex != null && selectedOptionIndex != question.correctAnswerIndex -> ErrorGradient
        else -> null
    }

    GlassCard(
        modifier = modifier,
        gradient = borderBrush
    ) {
        Text(
            text = question.getLocalizedContent(language),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        question.options.forEachIndexed { index, _ ->
            // ... rest of option rendering with animated border colors
        }

        if (showFeedback && showExplanation) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Explanation:",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF4facfe)
            )
            Text(
                text = question.getExplanation(language),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}
```

**Step 3: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/component/QuestionCard.kt
git commit -m "feat(design): enhance QuestionCard with glass styling and gradient borders"
```

---

### Task 12: Update StudyScreen with StatChips and Progress Bar

**Files:**
- Modify: `feature/study/src/main/java/com/example/sie/feature/study/StudyScreen.kt`

**Step 1: Add glass component imports**

```kotlin
import com.example.sie.core.designsystem.component.GlassTopAppBar
import com.example.sie.core.designsystem.component.StatChip
import com.example.sie.core.designsystem.component.GradientProgressIndicator
import com.example.sie.core.designsystem.theme.*
```

**Step 2: Replace TopAppBar with GlassTopAppBar and add stats chips**

```kotlin
Scaffold(
    topBar = {
        Column(modifier = Modifier.background(GlassSurface)) {
            TopAppBar(
                title = { Text("Practice Mode", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(label = "Time", value = formatTime(elapsedTime))
                StatChip(label = "Count", value = "${state.stats.totalAnswered}")
                StatChip(label = "Accuracy", value = "$accuracy%")
            }
        }
    }
) { paddingValues ->
    // ... rest of content
}
```

**Step 3: Add progress bar at bottom of content**

Inside the main Column, after QuestionCard and before Navigation Buttons:

```kotlin
GradientProgressIndicator(
    progress = state.stats.totalAnswered.toFloat() / state.stats.totalQuestions,
    gradient = TertiaryGradient,
    modifier = Modifier.fillMaxWidth()
)

Text(
    text = "${state.stats.totalAnswered} / ${state.stats.totalQuestions}",
    style = MaterialTheme.typography.bodySmall,
    color = Color.White.copy(alpha = 0.7f),
    modifier = Modifier.align(Alignment.End)
)
```

**Step 4: Commit**

```bash
git add feature/study/src/main/java/com/example/sie/feature/study/StudyScreen.kt
git commit -m "feat(study): add StatChips and progress bar with glass styling"
```

---

## Phase 4: Exam Screen Upgrade

### Task 13: Add Countdown Pulse Animation

**Files:**
- Modify: `feature/exam/src/main/java/com/example/sie/feature/exam/ExamScreen.kt`

**Step 1: Add pulse animation to countdown**

Find the countdown timer section in `ExamInProgressContent` and add animation:

```kotlin
import androidx.compose.animation.core.*

val infiniteTransition = rememberInfiniteTransition(label = "pulse")
val scale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = if (state.timeLeftMillis < 5 * 60 * 1000) 1.05f else 1f,
    animationSpec = infiniteRepeatable(
        animation = tween(1000),
        repeatMode = RepeatMode.Reverse
    ),
    label = "timer_pulse"
)

Text(
    text = formatTime(state.timeLeftMillis),
    style = MaterialTheme.typography.titleMedium,
    color = timeColor,
    fontWeight = if (state.timeLeftMillis < 5 * 60 * 1000) FontWeight.Bold else FontWeight.Normal,
    modifier = Modifier.scale(scale)
)
```

**Step 2: Commit**

```bash
git add feature/exam/src/main/java/com/example/sie/feature/exam/ExamScreen.kt
git commit -m "feat(exam): add pulse animation to countdown timer"
```

---

### Task 14: Implement Animated Score Circle in ExamResultContent

**Files:**
- Modify: `feature/exam/src/main/java/com/example/sie/feature/exam/ExamScreen.kt`

**Step 1: Create animated score circle composable**

Add this composable before `ExamResultContent`:

```kotlin
@Composable
private fun AnimatedScoreCircle(
    score: Int,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(
            durationMillis = 700,
            easing = FastOutSlowInEasing
        ),
        label = "score_animation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(120.dp)
            .background(
                brush = if (score >= 70) SuccessGradient else ErrorGradient,
                shape = androidx.compose.foundation.shape.CircleShape
            )
    ) {
        Text(
            text = "$animatedScore%",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
```

**Step 2: Replace static score box with AnimatedScoreCircle**

In `ExamResultContent`, replace the score display Box with:

```kotlin
AnimatedScoreCircle(
    score = state.score,
    modifier = Modifier
)
```

**Step 3: Commit**

```bash
git add feature/exam/src/main/java/com/example/sie/feature/exam/ExamScreen.kt
git commit -m "feat(exam): add animated score circle to result screen"
```

---

### Task 15: Add Gradient Progress Bars to Category Breakdown

**Files:**
- Modify: `feature/exam/src/main/java/com/example/sie/feature/exam/ExamScreen.kt`

**Step 1: Replace LinearProgressIndicator with GradientProgressIndicator**

Import the component:

```kotlin
import com.example.sie.core.designsystem.component.GradientProgressIndicator
```

Find the category breakdown section and replace the progress indicator:

```kotlin
categoryStats.forEach { (category, stats) ->
    val (correct, total) = stats
    val percentage = (correct.toFloat() / total * 100).toInt()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category.substringBefore("/"),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            color = Color.White
        )
        Text(
            text = "$percentage% ($correct/$total)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (percentage >= 70) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
    GradientProgressIndicator(
        progress = correct.toFloat() / total,
        gradient = if (percentage >= 70) SuccessGradient else ErrorGradient,
        modifier = Modifier.fillMaxWidth()
    )
}
```

**Step 2: Commit**

```bash
git add feature/exam/src/main/java/com/example/sie/feature/exam/ExamScreen.kt
git commit -m "feat(exam): add gradient progress bars to category breakdown"
```

---

### Task 16: Replace Exam Result Action Buttons with GradientButton

**Files:**
- Modify: `feature/exam/src/main/java/com/example/sie/feature/exam/ExamScreen.kt`

**Step 1: Import GradientButton**

```kotlin
import com.example.sie.core.designsystem.component.GradientButton
```

**Step 2: Replace action buttons**

Find the action buttons section in `ExamResultContent`:

```kotlin
Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    GradientButton(
        text = if (language == "zh") "查看答案解析" else "Review Answers",
        gradient = PrimaryGradient,
        onClick = { isReviewing = true },
        modifier = Modifier.fillMaxWidth()
    )

    GradientButton(
        text = if (language == "zh") "重新考试" else "Retake Exam",
        gradient = SecondaryGradient,
        onClick = onResetExam,
        modifier = Modifier.fillMaxWidth()
    )

    TextButton(
        onClick = onBackClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (language == "zh") "返回首页" else "Back to Home",
            color = Color.White
        )
    }
}
```

**Step 3: Commit**

```bash
git add feature/exam/src/main/java/com/example/sie/feature/exam/ExamScreen.kt
git commit -m "feat(exam): replace action buttons with GradientButton"
```

---

## Phase 5: Stats and Settings Screens

### Task 17: Upgrade StatsScreen with GlassCards

**Files:**
- Modify: `feature/stats/src/main/java/com/example/sie/feature/stats/StatsScreen.kt`

**Step 1: Add glass component imports**

```kotlin
import com.example.sie.core.designsystem.component.GlassCard
import com.example.sie.core.designsystem.theme.*
```

**Step 2: Wrap stats sections in GlassCards**

Replace existing content layout with:

```kotlin
LazyColumn(
    modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
) {
    item {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            gradient = PrimaryGradient
        ) {
            Text(
                text = "Overall Performance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            // ... stats content
        }
    }

    item {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            gradient = TertiaryGradient
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            // ... activity list
        }
    }
}
```

**Step 3: Commit**

```bash
git add feature/stats/src/main/java/com/example/sie/feature/stats/StatsScreen.kt
git commit -m "feat(stats): add GlassCard styling to stats sections"
```

---

### Task 18: Upgrade SettingsScreen with GlassCards

**Files:**
- Modify: `feature/settings/src/main/java/com/example/sie/feature/settings/SettingsScreen.kt`

**Step 1: Add glass component imports**

```kotlin
import com.example.sie.core.designsystem.component.GlassCard
import com.example.sie.core.designsystem.theme.*
```

**Step 2: Group settings into GlassCards**

```kotlin
LazyColumn(
    modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
) {
    item {
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            // Dark mode toggle
            // Language selector
        }
    }

    item {
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Study Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            // Timer alerts
            // Auto-advance
        }
    }
}
```

**Step 3: Commit**

```bash
git add feature/settings/src/main/java/com/example/sie/feature/settings/SettingsScreen.kt
git commit -m "feat(settings): group settings into GlassCards"
```

---

## Phase 6: Testing and Polish

### Task 19: Add Gradient Backgrounds to All Screens

**Files:**
- Modify: `feature/study/src/main/java/com/example/sie/feature/study/StudyScreen.kt`
- Modify: `feature/exam/src/main/java/com/example/sie/feature/exam/ExamScreen.kt`
- Modify: `feature/stats/src/main/java/com/example/sie/feature/stats/StatsScreen.kt`
- Modify: `feature/settings/src/main/java/com/example/sie/feature/settings/SettingsScreen.kt`

**Step 1: Add gradient background wrapper to each screen**

For each screen, wrap the Scaffold in a Box with gradient background (same as HomeScreen):

```kotlin
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
        // ... rest of scaffold
    ) {
        // ... content
    }
}
```

**Step 2: Commit each file separately**

```bash
git add feature/study/src/main/java/com/example/sie/feature/study/StudyScreen.kt
git commit -m "feat(study): add gradient background"

git add feature/exam/src/main/java/com/example/sie/feature/exam/ExamScreen.kt
git commit -m "feat(exam): add gradient background"

git add feature/stats/src/main/java/com/example/sie/feature/stats/StatsScreen.kt
git commit -m "feat(stats): add gradient background"

git add feature/settings/src/main/java/com/example/sie/feature/settings/SettingsScreen.kt
git commit -m "feat(settings): add gradient background"
```

---

### Task 20: Manual Testing Checklist

**Step 1: Build and run the app**

```bash
./gradlew assembleDebug
./gradlew installDebug
```

**Step 2: Test each screen for:**

- [ ] HomeScreen: Cards appear with stagger animation, clicks work
- [ ] StudyScreen: Stats chips display correctly, progress bar animates
- [ ] ExamScreen: Countdown pulses when <5min, score circle animates
- [ ] StatsScreen: GlassCards render correctly
- [ ] SettingsScreen: Settings grouped in cards

**Step 3: Performance testing**

```bash
adb shell dumpsys gfxinfo com.example.sie_android_app
```

Check for:
- 60fps on navigation
- No dropped frames on animations
- Smooth scrolling

**Step 4: Accessibility testing**

- Enable TalkBack and test navigation
- Check color contrast in Settings > Accessibility
- Test touch targets (minimum 48dp)

**Step 5: Document results**

Create: `docs/test-reports/glassmorphism-ui-testing.md`

```markdown
# Glassmorphism UI Testing Report

## Device Tested
- Model: [Device name]
- Android Version: [Version]
- Screen Size: [Size]

## Functional Tests
- [ ] HomeScreen cards clickable
- [ ] Study mode progress tracking
- [ ] Exam timer animation
- [ ] Score reveal animation

## Performance Tests
- Average FPS: [XX]
- Frame drops: [X]
- Memory usage: [XX MB]

## Issues Found
1. [Issue description]
2. ...
```

**Step 6: Commit test report**

```bash
git add docs/test-reports/glassmorphism-ui-testing.md
git commit -m "docs: add UI testing report"
```

---

## Final Task: Update README with Screenshots

### Task 21: Update Project README

**Files:**
- Modify: `README.md`

**Step 1: Add screenshots section**

```markdown
## UI/UX Design

The app features a modern **Glassmorphism Lite** design with:
- Translucent glass-styled cards
- Vibrant gradient accents
- Smooth animations on key interactions
- 60fps performance across all devices

### Screenshots

[Add screenshots of HomeScreen, StudyScreen, ExamScreen]
```

**Step 2: Commit**

```bash
git add README.md
git commit -m "docs: update README with UI design description"
```

---

## Completion Checklist

### Phase 1: Foundation ✅
- [x] GlassColors.kt
- [x] GlassCard component
- [x] GradientButton component
- [x] GlassTopAppBar component
- [x] StatChip component
- [x] GradientProgressIndicator component
- [x] Animation constants

### Phase 2: Home Screen ✅
- [x] Gradient background
- [x] GlassCard buttons
- [x] Stagger animation

### Phase 3: Study Screen ✅
- [x] Enhanced QuestionCard
- [x] StatChips in TopBar
- [x] Progress bar

### Phase 4: Exam Screen ✅
- [x] Countdown pulse animation
- [x] Animated score circle
- [x] Gradient progress bars
- [x] GradientButton actions

### Phase 5: Stats & Settings ✅
- [x] StatsScreen GlassCards
- [x] SettingsScreen GlassCards

### Phase 6: Polish ✅
- [x] Gradient backgrounds on all screens
- [x] Manual testing
- [x] Update README

---

**End of Plan**

**Total Estimated Time:** 5-7 days
**Complexity:** Medium-High
**Risk Level:** Low (no blur effects, pure Compose)
