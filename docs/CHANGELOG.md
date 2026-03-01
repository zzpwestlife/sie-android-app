# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial project structure for native Android implementation.
- Core modules: `common`, `data`, `database`, `datastore`, `designsystem`, `model`.
- Feature modules: `exam`, `home`, `settings`, `stats`, `study`.
- Database pre-population from `assets/questions.json`.
- UI implementation using Jetpack Compose and Material Design 3.
- Dark mode support.
- Exam engine with countdown timer.
- Study mode for interactive learning.
- Statistics tracking for user performance.

### Changed
- Refactored entire application from React Native to Native Android (Kotlin).
- Simplified scope to focus on core study and exam functionalities.

### Removed
- Leaderboard feature (temporarily).
- Certificate generation.
- Advanced social sharing features.
