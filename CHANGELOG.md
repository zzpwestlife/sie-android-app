# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **Chapter-Based Learning**: New feature allowing users to:
  - Select multiple chapters for targeted study
  - View detailed progress per chapter (total questions, studied count, accuracy)
  - Smart question sorting (wrong questions first, then unstudied, then by error count)
  - Real-time progress tracking using lastStudiedAt timestamps
- Database migration 12→13 adding `lastStudiedAt` field to questions table
- New `feature/chapter` module with ChapterSelectionScreen and ChapterSelectionViewModel
- Localization support for chapter names (English/Chinese)
- Unit tests for ChapterSelectionViewModel
- Unit tests for smart sorting algorithm

### Changed

- Home screen: "Start Practice" now navigates to Chapter Selection
- StudyViewModel: Added chapter filtering and smart sorting capabilities
- QuestionRepository: Added methods for category-based queries

### Technical Details

- **Database**: Added `lastStudiedAt` column (nullable Long) and composite index for efficient chapter-based queries
- **Architecture**: Lightweight chapter filter approach extending existing StudyViewModel
- **Smart Sorting Algorithm**:
  1. Wrong questions first (isWrong = true)
  2. Higher wrongCount among wrong questions
  3. Unstudied questions (lastStudiedAt = null) among correct answers
  4. Oldest studied questions first
  5. Stable sort by ID as tiebreaker

## [1.0.0] - Initial Release

### Added
- Basic question study functionality
- Exam mode
- Bookmark feature
- Statistics tracking
- Multi-language support (English/Chinese)
- Dark mode support
