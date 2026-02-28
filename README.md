# SIE Exam Prep Android App

This is a comprehensive Android application designed for Securities Industry Essentials (SIE) exam preparation. It is built using modern Android development practices and technologies.

## Project Status
The app is fully refactored to Native Android using Kotlin and Jetpack Compose.

## Simplified Scope
Based on user request, complex features such as Leaderboard, Certificates, and advanced social sharing are intentionally omitted to focus on core study and exam functionalities.

## Architecture
The project follows a modern multi-module architecture to ensure separation of concerns and scalability:
- **:app**: The application entry point.
- **:core:***: Fundamental components (e.g., `:core:data`, `:core:database`, `:core:designsystem`, `:core:model`, `:core:common`, `:core:datastore`).
- **:feature:***: Feature-specific modules (e.g., `:feature:home`, `:feature:study`, `:feature:exam`, `:feature:stats`, `:feature:settings`).

## Features Implemented
- **Study Mode**: Interactive study session.
- **Exam Engine**: 75 questions with a countdown timer simulating the real exam.
- **Exam Review Mode**: Review past exam attempts and detailed explanations.
- **Stats**: User performance statistics.
- **Settings**: App configuration including Dark Mode.

## Build Instructions
To build the application, run:
```bash
./gradlew assembleDebug
```

## Test Instructions
To run the unit tests, execute:
```bash
./gradlew testDebugUnitTest
```

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM / Clean Architecture / Multi-module
- **Dependency Injection**: Hilt
- **Database**: Room
- **Concurrency**: Kotlin Coroutines & Flow
- **Build System**: Gradle (Kotlin DSL)
- **Minimum API**: 21
- **Target API**: 34

## Development Requirements

- **Unit Test Coverage**: ≥ 80%
- **UI Automation Tests**: Skipped (Simplified)
- **CI/CD**: Yes
- **Security**: ProGuard, API signing, GDPR compliance

## Deliverables

- Production-ready APK
- Source Code
- Technical Documentation
- Test Reports
- Operations Manual
- Product Whitepaper

## Deadline

2026-04-30
