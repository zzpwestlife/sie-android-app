# SIE Exam Preparation App - Implementation Plan

## Overview
- **Objective**: Build a native Android application for Securities Industry Essentials (SIE) exam preparation, replacing the previous React Native implementation.
- **Scope**: Complete app lifecycle from study materials to exam simulation, analytics, and settings.
- **Success Criteria**:
    - Functional Question Bank (Chapter, Random, Timed modes).
    - Full Mock Exam engine (75 questions, 1hr 45min timer).
    - Data persistence with Room (Offline first).
    - >80% Unit Test Coverage.
    - Material Design 3 compliance.

## Architecture Overview
- **Pattern**: MVVM + Clean Architecture (Presentation, Domain, Data).
- **Modularization**: Multi-module Gradle project.
    - `:app`: Application entry point.
    - `:core:common`: Utility classes, extensions.
    - `:core:model`: Shared data models.
    - `:core:database`: Room database, DAOs.
    - `:core:datastore`: Preferences (Settings).
    - `:core:designsystem`: Compose Theme, Typography, Components.
    - `:feature:home`: Dashboard.
    - `:feature:study`: Question practice, Chapter selection.
    - `:feature:exam`: Mock exam engine, Results.
    - `:feature:stats`: Analytics, Charts.
    - `:feature:settings`: User preferences, About.
- **Tech Stack**:
    - **Language**: Kotlin.
    - **UI**: Jetpack Compose (Material 3).
    - **DI**: Hilt.
    - **Async**: Coroutines & Flow.
    - **Local Data**: Room, Proto DataStore.
    - **Build**: Gradle Kotlin DSL, Version Catalog (`libs.versions.toml`).

## Architectural Decisions

### Decision 1: Multi-Module Strategy
- **Context**: Large application with distinct features (Study vs Exam vs Stats).
- **Decision**: Feature-based modularization with a shared `core` layer.
- **Rationale**: Improves build times, enforces separation of concerns, allows feature toggling/isolation.

### Decision 2: Single Activity Architecture
- **Context**: Navigation between screens.
- **Decision**: One `MainActivity` hosting a Compose `NavHost`.
- **Rationale**: Standard for Compose apps, simplifies lifecycle management and shared element transitions.

### Decision 3: Offline-First
- **Context**: Users need to study on the go.
- **Decision**: Room Database as the single source of truth.
- **Rationale**: Ensures app works without internet; sync can be added later if needed.

## Simplified Scope
Complex features such as **Leaderboards**, **Certificates**, and **Social Sharing** have been intentionally omitted from the initial release based on user request. The focus is strictly on the core exam preparation experience: study modes, mock exams, and personal progress tracking.

## Implementation Phases

### Phase 0: Cleanup & Infrastructure
**Objective**: Establish a clean slate and set up the build system.

#### Tasks
1. **[SETUP-001]**: Clean Legacy Code
   - **Description**: Remove existing `app/src`, `android`, and React Native configuration files. Keep `.git` and `README.md`.
   - **Complexity**: Low
   - **Dependencies**: None
   - **Files**: `app/`, `android/`, `package.json`, `node_modules/`

2. **[SETUP-002]**: Gradle Setup & Version Catalog
   - **Description**: Initialize `gradle/libs.versions.toml` with dependencies (Compose, Hilt, Room, Coroutines). Convert `build.gradle` to Kotlin DSL (`.kts`).
   - **Complexity**: Medium
   - **Dependencies**: SETUP-001
   - **Files**: `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`

3. **[SETUP-003]**: Module Structure Creation
   - **Description**: Create the directory structure and `build.gradle.kts` for `core` and `feature` modules.
   - **Complexity**: Medium
   - **Dependencies**: SETUP-002
   - **Files**: `:core:*`, `:feature:*`

### Phase 1: Core Layers (Data & Domain)
**Objective**: Implement the data foundation.

#### Tasks
4. **[CORE-001] (DONE)**: Core Model & Database
   - **Description**: Define `Question`, `Answer`, `ExamResult` entities. Setup Room Database in `:core:database`.
   - **Complexity**: High
   - **Dependencies**: SETUP-003
   - **Files**: `QuestionEntity.kt`, `AppDatabase.kt`

5. **[CORE-002] (DONE)**: Data Seeding Logic
   - **Description**: Implement logic to pre-populate the Room database from a JSON file (Question Bank).
   - **Complexity**: Medium
   - **Dependencies**: CORE-001
   - **Files**: `DatabaseCallback.kt`, `assets/questions.json`

6. **[CORE-003] (DONE)**: Repository Layer
   - **Description**: Create `QuestionRepository`, `ExamRepository` interfaces and implementations.
   - **Complexity**: Medium
   - **Dependencies**: CORE-001
   - **Files**: `QuestionRepository.kt`, `OfflineQuestionRepository.kt`

7. **[CORE-004] (DONE)**: User Preferences (DataStore)
   - **Description**: Implement `SettingsRepository` using DataStore for Theme (Dark/Light) and Font Size.
   - **Complexity**: Low
   - **Dependencies**: SETUP-003
   - **Files**: `UserPreferencesSerializer.kt`

### Phase 2: Core UI & Navigation
**Objective**: Set up the visual framework.

#### Tasks
8. **[UI-001] (DONE)**: Design System
   - **Description**: Implement Material 3 Theme, Typography, Shapes, and common components (Buttons, TopBar, LoadingState) in `:core:designsystem`.
   - **Complexity**: Medium
   - **Dependencies**: SETUP-003
   - **Files**: `Theme.kt`, `Type.kt`, `SieButton.kt`

9. **[UI-002] (DONE)**: Navigation Setup
   - **Description**: Set up Type-Safe Navigation (Compose Navigation) and the main `NavHost` in `:app`.
   - **Complexity**: Medium
   - **Dependencies**: UI-001
   - **Files**: `SieNavHost.kt`, `Screen.kt`

### Phase 3: Feature Implementation - Study & Practice
**Objective**: Enable the primary study loop.

#### Tasks
10. **[FEAT-001] (DONE)**: Home Screen
    - **Description**: Dashboard showing progress summary and navigation to modes.
    - **Complexity**: Low
    - **Dependencies**: UI-002
    - **Files**: `:feature:home`

11. **[FEAT-002] (DONE)**: Question Component & Logic
    - **Description**: Create a reusable Compose component for rendering a question and multiple-choice answers with selection state.
    - **Complexity**: High
    - **Dependencies**: UI-001
    - **Files**: `QuestionCard.kt`

12. **[FEAT-003]**: Study Mode (Chapter/Random)
    - **Description**: Implement the flow for practicing questions. Immediate feedback on answer selection.
    - **Complexity**: High
    - **Dependencies**: FEAT-002, CORE-003
    - **Files**: `:feature:study`

### Phase 4: Feature Implementation - Exam Engine
**Objective**: Simulate the real exam environment.

#### Tasks
13. **[EXAM-001] (DONE)**: Exam Session Manager
    - **Description**: ViewModel logic to handle a 75-question session, timer (1h 45m), and state (no immediate feedback).
    - **Complexity**: Very High
    - **Dependencies**: CORE-003
    - **Files**: `ExamViewModel.kt`

14. **[EXAM-002] (DONE)**: Exam UI & Answer Sheet
    - **Description**: Exam screen with Timer, Question Pager, and "Answer Sheet" grid view for quick navigation.
    - **Complexity**: High
    - **Dependencies**: EXAM-001
    - **Files**: `ExamScreen.kt`, `AnswerSheet.kt`

15. **[EXAM-003] (DONE)**: Scoring & Result Report
    - **Description**: Calculate score, determine Pass/Fail (70%), and generate a detailed report.
    - **Complexity**: Medium
    - **Dependencies**: EXAM-001
    - **Files**: `ExamResultScreen.kt`

### Phase 5: Stats & Settings
**Objective**: User retention and personalization.

#### Tasks
16. **[STATS-001]**: Statistics Dashboard
    - **Description**: Visual charts (MPAndroidChart or Compose canvas) showing progress over time and weak areas.
    - **Complexity**: High
    - **Dependencies**: CORE-001
    - **Files**: `:feature:stats`

17. **[SETT-001]**: Settings Screen
    - **Description**: Toggle Night Mode, Font Size, Reset Progress, Language Selection.
    - **Complexity**: Low
    - **Dependencies**: CORE-004
    - **Files**: `:feature:settings`

### Phase 6: QA & Polish
**Objective**: Ensure quality and readiness for release.

#### Tasks
18. **[TEST-001] (IN_PROGRESS)**: Unit Tests
    - **Description**: Write JUnit tests for Repositories and ViewModels. Target 80% coverage.
    - **Complexity**: Medium
    - **Dependencies**: All Features
    - **Files**: `src/test/`

19. **[TEST-002] (SKIPPED)**: UI Automation Tests (Simplified scope)
    - **Description**: Write Espresso/Compose tests for critical flows (Start Exam -> Submit).
    - **Complexity**: Medium
    - **Dependencies**: All Features
    - **Files**: `src/androidTest/`

20. **[OPS-001] (DONE)**: CI/CD & ProGuard
    - **Description**: GitHub Actions workflow for build/test. Configure `proguard-rules.pro` for release. (CI/CD done, ProGuard done)
    - **Complexity**: Medium
    - **Dependencies**: None
    - **Files**: `.github/workflows/android.yml`, `proguard-rules.pro`

## Risk Assessment
- **Content Accuracy**: Ensuring the SIE question bank is accurate. *Mitigation*: Use a verifiable JSON source and allow user reporting.
- **Performance**: Rendering complex charts or large lists. *Mitigation*: Use Compose `LazyColumn` and optimized queries.
- **Migration**: Old RN code might confuse the build. *Mitigation*: Phase 0 strict cleanup.
