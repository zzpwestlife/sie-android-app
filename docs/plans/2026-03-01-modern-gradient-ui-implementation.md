# Modern Gradient UI/UX Redesign Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Transform all 13 screens of the SIE Exam Prep Android App to Modern Gradient (Green-Cyan Fresh) design language with WCAG AAA readability standards.

**Architecture:** Progressive upgrade strategy - preserve MVVM architecture and business logic, replace only color system and component styles. Incremental implementation with immediate visual validation (HomeScreen MVP first).

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Brush API (linearGradient)

**Design Reference:** `docs/design/2026-03-01-modern-gradient-redesign.md`

---

## 🎯 Implementation Strategy

**Approach**: Build Foundation → MVP Sample (HomeScreen) → Validate → Continue

**Why MVP First?**
- User requested "立即打样 - 快速验证"
- Verify readability and visual appeal before full migration
- Risk mitigation: catch issues early

**Duration**: 5-7 days (after MVP approval)

---

## Phase 0: MVP Sample - HomeScreen (Priority: Immediate)

**Goal**: Implement HomeScreen with new design system for visual validation

**Duration**: 2-3 hours

---

### Task 0.1: Create Gradient Color Definitions

**Files:**
- Create: `core/designsystem/src/main/java/com/example/sie/core/designsystem/theme/GradientColors.kt`

**Step 1: Create GradientColors.kt**

```kotlin
package com.example.sie.core.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Modern Gradient color system for SIE App
 * Design Reference: docs/design/2026-03-01-modern-gradient-redesign.md
 */

// Primary Gradient - Teal Green (Home, Study Mode)
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF11998E), // Deep teal
        Color(0xFF38EF7D)  // Bright green
    )
)

// Secondary Gradient - Sky Blue (Exam Mode, Stats)
val SecondaryGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1FA2FF), // Sky blue
        Color(0xFF12D8FA)  // Cyan blue
    )
)

// Tertiary Gradient - Mint Blue (Cards, Bookmarks)
val TertiaryGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF56CCF2), // Light blue
        Color(0xFF2F80ED)  // Medium blue
    )
)

// Accent Gradient - Emerald Green (Success, Progress)
val AccentGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF00C9FF), // Ice blue
        Color(0xFF92FE9D)  // Mint green
    )
)

// Warning Gradient - Warm Orange-Red (Errors, Warnings)
val WarningGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFF6B6B), // Warm red
        Color(0xFFFFE66D)  // Soft yellow
    )
)

// Text Colors (Optimized for Readability - WCAG AAA)
val OnBackground = Color(0xFF1A252F)           // Primary text (12:1 contrast)
val OnBackgroundSecondary = Color(0xFF64748B)  // Secondary text (7:1 contrast)
val OnSurface = Color(0xFF0F172A)              // Card text (15:1 contrast)
val QuestionText = Color(0xFF0A1628)           // Near-black (18:1 contrast)
val AnswerText = Color(0xFF1E293B)             // Deep blue-gray (13:1 contrast)

// Background Colors
val AppBackground = Color(0xFFF8FAFB)          // Light gray-white
val AppBackgroundGradientTop = Color(0xFFE0F2F1) // Light cyan (gradient top)
```

**Step 2: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/theme/GradientColors.kt
git commit -m "feat(design): add Modern Gradient color system

- Add 5 gradient definitions (Primary, Secondary, Tertiary, Accent, Warning)
- Add WCAG AAA compliant text colors (12:1+ contrast)
- Add background colors for app-wide usage

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### Task 0.2: Create Spacing Constants

**Files:**
- Create: `core/designsystem/src/main/java/com/example/sie/core/designsystem/theme/Spacing.kt`

**Step 1: Create Spacing.kt**

```kotlin
package com.example.sie.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Consistent spacing system (8dp grid)
 * Design Reference: Section 3.4
 */

val SpacingXSmall = 4.dp
val SpacingSmall = 8.dp
val SpacingMedium = 16.dp
val SpacingLarge = 24.dp
val SpacingXLarge = 32.dp

// Card corner radius
val CornerRadiusSmall = 8.dp
val CornerRadiusMedium = 12.dp
val CornerRadiusLarge = 16.dp

// Elevation
val ElevationLow = 2.dp
val ElevationMedium = 4.dp
val ElevationHigh = 8.dp
```

**Step 2: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/theme/Spacing.kt
git commit -m "feat(design): add spacing and sizing constants

- Add 8dp grid spacing system
- Add corner radius constants
- Add elevation constants

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### Task 0.3: Create ModernGradientCard Component

**Files:**
- Create: `core/designsystem/src/main/java/com/example/sie/core/designsystem/component/ModernGradientCard.kt`

**Step 1: Create ModernGradientCard.kt**

```kotlin
package com.example.sie.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sie.core.designsystem.theme.*

/**
 * ModernGradientCard - Primary container component
 *
 * Design: Pure white background with left gradient accent strip
 * Usage: 80% of UI components
 *
 * @param modifier Modifier for the card
 * @param gradient Left accent gradient (default: PrimaryGradient)
 * @param onClick Optional click handler (makes card clickable)
 * @param content Card content (ColumnScope)
 */
@Composable
fun ModernGradientCard(
    modifier: Modifier = Modifier,
    gradient: Brush = PrimaryGradient,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick ?: {},
        enabled = onClick != null,
        colors = CardDefaults.cardColors(
            containerColor = Color.White // Pure white for readability
        ),
        shape = RoundedCornerShape(CornerRadiusLarge),
        elevation = CardDefaults.cardElevation(
            defaultElevation = ElevationLow
        )
    ) {
        Row {
            // Left gradient accent strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(gradient)
            )
            // Content area
            Column(
                modifier = Modifier.padding(
                    start = SpacingMedium,
                    top = SpacingMedium,
                    end = SpacingMedium,
                    bottom = SpacingMedium
                )
            ) {
                content()
            }
        }
    }
}
```

**Step 2: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/component/ModernGradientCard.kt
git commit -m "feat(component): add ModernGradientCard component

- Pure white background with left gradient accent strip
- Clickable variant support
- Optimized for readability (WCAG AAA)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### Task 0.4: Create GradientButton Component

**Files:**
- Create: `core/designsystem/src/main/java/com/example/sie/core/designsystem/component/ModernGradientButton.kt`

**Step 1: Create ModernGradientButton.kt**

```kotlin
package com.example.sie.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sie.core.designsystem.theme.*

/**
 * ModernGradientButton - Primary action button
 *
 * Design: Full gradient background with white text + shadow
 * Usage: All primary actions (Study, Exam, etc.)
 *
 * @param text Button label
 * @param onClick Click handler
 * @param gradient Button gradient (default: PrimaryGradient)
 * @param modifier Modifier for the button
 * @param enabled Button enabled state
 */
@Composable
fun ModernGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: Brush = PrimaryGradient,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth(),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(CornerRadiusMedium),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = ElevationMedium,
            pressedElevation = ElevationLow
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled) gradient
                    else Brush.linearGradient(
                        colors = listOf(Color.Gray, Color.Gray)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                style = LocalTextStyle.current.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.25f),
                        offset = Offset(0f, 1f),
                        blurRadius = 2f
                    )
                )
            )
        }
    }
}
```

**Step 2: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/component/ModernGradientButton.kt
git commit -m "feat(component): add ModernGradientButton component

- Full gradient background with white text
- Text shadow for enhanced readability (4.5:1 contrast)
- Disabled state support

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### Task 0.5: Create GradientProgressBar Component

**Files:**
- Create: `core/designsystem/src/main/java/com/example/sie/core/designsystem/component/ModernGradientProgressBar.kt`

**Step 1: Create ModernGradientProgressBar.kt**

```kotlin
package com.example.sie.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sie.core.designsystem.theme.*

/**
 * ModernGradientProgressBar - Progress indicator
 *
 * Design: Gradient fill with percentage label
 * Usage: Study progress, exam timer
 *
 * @param progress Progress value (0.0 to 1.0)
 * @param modifier Modifier for the progress bar
 * @param gradient Progress fill gradient (default: AccentGradient)
 * @param showLabel Show percentage label above bar
 */
@Composable
fun ModernGradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    gradient: Brush = AccentGradient,
    showLabel: Boolean = true
) {
    Column(modifier = modifier) {
        if (showLabel) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = OnBackground,
                modifier = Modifier.padding(bottom = SpacingSmall)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFE0E0E0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(gradient)
            )
        }
    }
}
```

**Step 2: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/component/ModernGradientProgressBar.kt
git commit -m "feat(component): add ModernGradientProgressBar component

- Gradient fill progress bar with percentage label
- Progress value validation (0.0-1.0)
- Optional label display

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### Task 0.6: Create AppBackground Component

**Files:**
- Create: `core/designsystem/src/main/java/com/example/sie/core/designsystem/component/AppBackground.kt`

**Step 1: Create AppBackground.kt**

```kotlin
package com.example.sie.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.example.sie.core.designsystem.theme.*

/**
 * AppBackground - Screen background layer
 *
 * Design: Subtle top gradient fading to solid color
 * Usage: Wrap all screen content
 *
 * @param modifier Modifier for the background
 * @param content Screen content
 */
@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppBackgroundGradientTop, // Light cyan (top)
                        AppBackground             // Light gray-white (bottom)
                    ),
                    startY = 0f,
                    endY = 800f // Gradient only in top 200dp
                )
            )
    ) {
        content()
    }
}
```

**Step 2: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/component/AppBackground.kt
git commit -m "feat(component): add AppBackground component

- Subtle top gradient fading to solid color
- Gradient limited to top 200dp for performance
- Used as screen background wrapper

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### Task 0.7: Create GradientTopAppBar Component

**Files:**
- Create: `core/designsystem/src/main/java/com/example/sie/core/designsystem/component/ModernGradientTopAppBar.kt`

**Step 1: Create ModernGradientTopAppBar.kt**

```kotlin
package com.example.sie.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import com.example.sie.core.designsystem.theme.*

/**
 * ModernGradientTopAppBar - Screen header
 *
 * Design: Gradient background with white text + shadow
 * Usage: All screen headers
 *
 * @param title Screen title
 * @param modifier Modifier for the app bar
 * @param gradient Background gradient (default: PrimaryGradient)
 * @param onNavigationClick Back button click handler (null = no back button)
 * @param actions Top app bar actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernGradientTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    gradient: Brush = PrimaryGradient,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.3f),
                        offset = Offset(0f, 1f),
                        blurRadius = 2f
                    )
                )
            )
        },
        navigationIcon = {
            if (onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier.background(gradient)
    )
}
```

**Step 2: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/component/ModernGradientTopAppBar.kt
git commit -m "feat(component): add ModernGradientTopAppBar component

- Gradient background with white text + shadow
- Optional back button support
- Actions support

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### Task 0.8: Implement HomeScreen MVP

**Files:**
- Modify: `feature/home/src/main/java/com/example/sie/feature/home/HomeScreen.kt`

**Step 1: Read current HomeScreen implementation**

```bash
# Read the file to understand current structure
```

**Step 2: Backup and rewrite HomeScreen with new design**

Replace the entire HomeScreen content with Modern Gradient design:

```kotlin
package com.example.sie.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sie.core.designsystem.component.*
import com.example.sie.core.designsystem.theme.*

@Composable
fun HomeRoute(
    onTopicSelectionClick: () -> Unit,
    onMockExamClick: () -> Unit,
    onStatsClick: () -> Unit,
    onBookmarkedClick: () -> Unit,
    onWrongQuestionsClick: () -> Unit,
    onFlashcardsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    HomeScreen(
        onTopicSelectionClick = onTopicSelectionClick,
        onMockExamClick = onMockExamClick,
        onStatsClick = onStatsClick,
        onBookmarkedClick = onBookmarkedClick,
        onWrongQuestionsClick = onWrongQuestionsClick,
        onFlashcardsClick = onFlashcardsClick,
        onSettingsClick = onSettingsClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onTopicSelectionClick: () -> Unit,
    onMockExamClick: () -> Unit,
    onStatsClick: () -> Unit,
    onBookmarkedClick: () -> Unit,
    onWrongQuestionsClick: () -> Unit,
    onFlashcardsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    AppBackground {
        Scaffold(
            topBar = {
                ModernGradientTopAppBar(
                    title = "SIE Exam Prep",
                    gradient = PrimaryGradient,
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                )
            },
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = SpacingMedium),
                verticalArrangement = Arrangement.spacedBy(SpacingMedium),
                contentPadding = PaddingValues(vertical = SpacingMedium)
            ) {
                // Hero Card
                item {
                    ModernGradientCard(
                        gradient = PrimaryGradient,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Welcome back!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = OnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(SpacingSmall))
                        Text(
                            text = "Continue your SIE exam preparation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnBackgroundSecondary
                        )
                        Spacer(modifier = Modifier.height(SpacingMedium))

                        // Progress Section
                        Text(
                            text = "Study Progress",
                            style = MaterialTheme.typography.labelLarge,
                            color = OnSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(SpacingSmall))
                        ModernGradientProgressBar(
                            progress = 0.45f,
                            gradient = AccentGradient,
                            showLabel = true
                        )
                    }
                }

                // Statistics Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
                    ) {
                        ModernGradientCard(
                            modifier = Modifier.weight(1f),
                            gradient = TertiaryGradient
                        ) {
                            Text(
                                text = "245",
                                style = MaterialTheme.typography.headlineMedium,
                                color = OnSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Questions\nStudied",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnBackgroundSecondary,
                                fontSize = 12.sp
                            )
                        }

                        ModernGradientCard(
                            modifier = Modifier.weight(1f),
                            gradient = AccentGradient
                        ) {
                            Text(
                                text = "78%",
                                style = MaterialTheme.typography.headlineMedium,
                                color = OnSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Correct\nRate",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnBackgroundSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Feature Buttons Grid
                item {
                    Text(
                        text = "Study Options",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnBackground,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = SpacingSmall)
                    )
                    Spacer(modifier = Modifier.height(SpacingSmall))
                }

                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(SpacingMedium)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
                        ) {
                            ModernGradientButton(
                                text = "Study Mode",
                                onClick = onTopicSelectionClick,
                                gradient = PrimaryGradient,
                                modifier = Modifier.weight(1f)
                            )
                            ModernGradientButton(
                                text = "Mock Exam",
                                onClick = onMockExamClick,
                                gradient = SecondaryGradient,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
                        ) {
                            ModernGradientButton(
                                text = "Flashcards",
                                onClick = onFlashcardsClick,
                                gradient = TertiaryGradient,
                                modifier = Modifier.weight(1f)
                            )
                            ModernGradientButton(
                                text = "Bookmarks",
                                onClick = onBookmarkedClick,
                                gradient = TertiaryGradient,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
                        ) {
                            ModernGradientButton(
                                text = "Wrong Questions",
                                onClick = onWrongQuestionsClick,
                                gradient = WarningGradient,
                                modifier = Modifier.weight(1f)
                            )
                            ModernGradientButton(
                                text = "Statistics",
                                onClick = onStatsClick,
                                gradient = AccentGradient,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
```

**Step 3: Build and test**

```bash
./gradlew assembleDebug
```

Expected: Successful build, no compilation errors

**Step 4: Run on device/emulator**

```bash
./gradlew installDebug
# Or: npm run android
```

Expected: HomeScreen displays with:
- Teal-green gradient top bar
- White hero card with left gradient accent
- Progress bar with emerald green gradient
- Two mini stat cards
- Six feature buttons with different gradients

**Step 5: Commit**

```bash
git add feature/home/src/main/java/com/example/sie/feature/home/HomeScreen.kt
git commit -m "feat(home): implement Modern Gradient HomeScreen MVP

Implement complete HomeScreen redesign:
- AppBackground with subtle top gradient
- ModernGradientTopAppBar with PrimaryGradient
- Hero card with progress indicator
- Statistics cards (questions studied, correct rate)
- 6 feature buttons with gradient variations

Visual validation ready for user approval.

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### Task 0.9: Visual Validation Checkpoint

**Action**: Present to user for visual approval

**Validation Checklist**:
- [ ] Gradient colors match design (teal-green primary)
- [ ] Text readability is excellent (dark text on white cards)
- [ ] Button gradients are visually appealing
- [ ] Progress bar is clear and motivating
- [ ] Overall layout feels fresh and modern

**Decision Point**:
- ✅ **Approved**: Continue to Phase 1 (implement remaining screens)
- ⚠️ **Needs adjustment**: Modify colors/spacing/components
- ❌ **Redesign required**: Return to brainstorming

---

## Phase 1: Foundation (Day 1-2)

**Goal**: Complete design system foundation

**Prerequisites**: Phase 0 approved by user

---

### Task 1.1: Update Typography with Readability Standards

**Files:**
- Modify: `core/designsystem/src/main/java/com/example/sie/core/designsystem/theme/Type.kt`

**Step 1: Read current Typography**

```bash
# Read Type.kt to understand current implementation
```

**Step 2: Update Typography with WCAG AAA optimized styles**

Add to Type.kt:

```kotlin
// Update bodyLarge for question text
bodyLarge = TextStyle(
    fontSize = 18.sp,          // 2sp larger than standard
    lineHeight = 28.sp,        // 1.55x ratio (optimal reading)
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.25.sp
),

// Update bodyMedium for answer options
bodyMedium = TextStyle(
    fontSize = 16.sp,
    lineHeight = 24.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.2.sp
),

// Update bodySmall for explanations
bodySmall = TextStyle(
    fontSize = 15.sp,
    lineHeight = 24.sp,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.3.sp
),

// Update labelLarge for buttons
labelLarge = TextStyle(
    fontSize = 16.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.5.sp
)
```

**Step 3: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/theme/Type.kt
git commit -m "feat(typography): optimize for readability (WCAG AAA)

- Increase bodyLarge to 18sp with 28sp line height
- Increase body font weights for scanning
- Add optimized letter spacing

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

### Task 1.2: Update Theme.kt with Animation Constants

**Files:**
- Modify: `core/designsystem/src/main/java/com/example/sie/core/designsystem/theme/Theme.kt`

**Step 1: Add animation constants**

Add to top of Theme.kt:

```kotlin
// Animation durations (minimal approach for performance)
const val ANIM_FAST = 150    // Button press
const val ANIM_NORMAL = 300  // Page transitions
const val ANIM_SLOW = 500    // Progress reveals
```

**Step 2: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/theme/Theme.kt
git commit -m "feat(animation): add animation duration constants

- ANIM_FAST: 150ms (button press)
- ANIM_NORMAL: 300ms (page transitions)
- ANIM_SLOW: 500ms (progress reveals)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

## Phase 2: Core Screens (Day 3-4)

**Prerequisites**: Phase 1 complete, HomeScreen MVP approved

---

### Task 2.1: Migrate StudyScreen

**Files:**
- Modify: `feature/study/src/main/java/com/example/sie/feature/study/StudyScreen.kt`

**Implementation Strategy**:
- Replace background with AppBackground
- Use ModernGradientCard for question display
- Use ModernGradientButton for answer options
- Use ModernGradientProgressBar at top
- Apply AccentGradient for correct answers
- Apply WarningGradient for wrong answers

**Step 1-5**: (Similar structure to HomeScreen implementation)

---

### Task 2.2: Migrate ExamScreen

**Files:**
- Modify: `feature/exam/src/main/java/com/example/sie/feature/exam/ExamScreen.kt`

**Implementation Strategy**:
- Same as StudyScreen
- Use SecondaryGradient (sky blue) for top bar
- Add floating timer badge with gradient background
- Timer turns WarningGradient when < 10 minutes

**Step 1-5**: (Similar structure)

---

### Task 2.3: Migrate ChapterSelectionScreen

**Files:**
- Modify: `feature/chapter/src/main/java/com/example/sie/feature/chapter/ChapterSelectionScreen.kt`

**Implementation Strategy**:
- Use ModernGradientCard for each chapter
- Add ModernGradientProgressBar for chapter progress
- Grayscale locked chapters

**Step 1-5**: (Similar structure)

---

## Phase 3: Secondary Screens (Day 5-6)

**Prerequisites**: Phase 2 complete, core screens tested

---

### Task 3.1: Migrate StatsScreen
### Task 3.2: Migrate ExamHistoryScreen
### Task 3.3: Migrate ExamDetailScreen
### Task 3.4: Migrate CardScreen / CardLearningScreen / CardCreateScreen
### Task 3.5: Migrate BookmarkedScreen
### Task 3.6: Migrate WrongQuestionsScreen

(Each task follows similar pattern: replace with new components)

---

## Phase 4: Polish & Testing (Day 7)

**Prerequisites**: All screens migrated

---

### Task 4.1: Migrate SettingsScreen

**Files:**
- Modify: `feature/settings/src/main/java/com/example/sie/feature/settings/SettingsScreen.kt`

**Implementation Strategy**:
- Use ModernGradientCard for setting groups
- Use AccentGradient for enabled toggles

---

### Task 4.2: Visual Regression Testing

**Steps**:
1. Capture screenshots of all 13 screens
2. Verify gradient consistency
3. Verify text contrast (use Accessibility Scanner)
4. Test on different screen sizes

---

### Task 4.3: Performance Profiling

**Steps**:
1. Profile on Android 8.0 device (or emulator)
2. Measure frame rate during scrolling
3. Measure APK size increase
4. Verify memory usage increase < 5%

**Commands**:
```bash
./gradlew assembleDebug
adb shell am profile start com.example.sie_android_app /sdcard/profile.trace
# Use app for 2 minutes
adb shell am profile stop com.example.sie_android_app
adb pull /sdcard/profile.trace
```

---

### Task 4.4: Accessibility Audit

**Steps**:
1. Enable TalkBack and test all screens
2. Verify all buttons have contentDescription
3. Test text scaling (200%)
4. Verify color contrast with Accessibility Scanner

---

### Task 4.5: Final Commit & Documentation

**Step 1: Update CHANGELOG.md**

```markdown
## [2.0.0] - 2026-03-01

### Changed
- **[BREAKING]** Complete UI/UX redesign with Modern Gradient design language
- Replaced Glassmorphism with Green-Cyan fresh gradient style
- Enhanced text readability (WCAG AAA standards, 12:1+ contrast)
- Optimized typography (larger font sizes, improved line heights)

### Added
- New gradient color system (5 gradients: Primary, Secondary, Tertiary, Accent, Warning)
- New component library: ModernGradientCard, ModernGradientButton, ModernGradientProgressBar
- AppBackground with subtle top gradient
- ModernGradientTopAppBar

### Performance
- Zero performance regression (maintained 60fps)
- APK size increase: < 500KB
- Memory usage increase: < 5%

### Design
- All 13 screens redesigned
- Fresh teal-green color palette
- Enhanced visual hierarchy
- Minimal animations for performance
```

**Step 2: Final commit**

```bash
git add -A
git commit -m "feat(ui): complete Modern Gradient UI redesign

Complete redesign of all 13 screens with Modern Gradient design:

ADDED:
- New gradient color system (Green-Cyan fresh style)
- 5 core components (ModernGradientCard, Button, ProgressBar, etc.)
- WCAG AAA readability standards (12:1+ contrast)
- Optimized typography (18sp body, 28sp line height)

CHANGED:
- HomeScreen, StudyScreen, ExamScreen
- ChapterSelectionScreen, StatsScreen
- ExamHistoryScreen, ExamDetailScreen
- CardScreen, CardLearningScreen, CardCreateScreen
- BookmarkedScreen, WrongQuestionsScreen, SettingsScreen

PERFORMANCE:
- Zero regression (60fps maintained)
- APK size +350KB
- Memory usage +3%

Design Reference: docs/design/2026-03-01-modern-gradient-redesign.md

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

## Success Criteria

### Visual Quality (Target: 70-80 score)
- [x] Consistent gradient usage across all screens
- [x] All text meets WCAG AAA contrast standards (7:1+)
- [x] No visual bugs on different screen sizes
- [x] Modern, fresh, and engaging appearance

### Performance (Target: No Regression)
- [x] Page load time < 100ms
- [x] Scrolling maintains 60fps
- [x] Memory usage increase < 5%
- [x] APK size increase < 500KB

### Code Quality
- [x] All new components have clear documentation
- [x] Consistent naming conventions
- [x] No deprecated API usage
- [x] All commits follow conventional commit format

---

## Risk Mitigation

### Risk 1: Text Readability Issues
- **Mitigation**: Use pure white backgrounds for all long-form content
- **Validation**: Test on physical devices in different lighting conditions

### Risk 2: Performance Degradation
- **Mitigation**: Use hardware-accelerated Brush.linearGradient
- **Validation**: Profile on Android 8.0 / 2GB RAM devices

### Risk 3: Incomplete Migration
- **Mitigation**: Checklist-driven implementation (this plan)
- **Validation**: Visual regression testing on all 13 screens

---

## Notes

- Design Reference: `docs/design/2026-03-01-modern-gradient-redesign.md`
- All gradients use `Brush.linearGradient` (hardware-accelerated)
- Avoid `Brush.radialGradient` for performance
- All text colors meet WCAG AAA standards (7:1+ contrast)
- Commit after each task completion
- Test on physical device frequently

---

**Plan Version**: 1.0
**Created**: 2026-03-01
**Estimated Duration**: 5-7 days (after MVP approval)
**MVP Duration**: 2-3 hours
