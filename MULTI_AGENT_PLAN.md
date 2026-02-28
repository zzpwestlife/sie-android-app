# Question Bank Expansion Plan

## Overview
- **Objective**: Expand the existing JSON question bank by at least 50 bilingual questions.
- **Scope**: Source material from `EntryTest/`, target file `core/database/src/main/assets/questions.json`.
- **Success Criteria**: 
  - 50+ new questions added.
  - Valid JSON format matching existing schema.
  - Bilingual support (English/Chinese).
  - <1% duplication rate.
  - Unit tests passing.

## Architecture Overview
- **Data Format**: JSON Array.
- **Schema**:
  ```json
  {
    "id": "Integer (unique)",
    "content": "String (Bilingual: En\\nCn)",
    "options": ["String (Bilingual)..."],
    "correctAnswerIndex": "Integer",
    "explanation": "String (Bilingual)",
    "category": "String"
  }
  ```

## Implementation Phases

### Phase 1: Analysis & Preparation
**Objective**: Understand existing data and source material.
#### Tasks
1. **[TASK-001]**: Read full `questions.json` to index existing IDs and Questions (for deduplication).
   - **Assignee**: System Architect
2. **[TASK-002]**: Read `EntryTest/Bi-入门考试学习资料_中英对照版.md` and `EntryTest/精炼知识清单.md` to extract knowledge points.
   - **Assignee**: System Architect

### Phase 2: Content Generation
**Objective**: Generate 50+ high-quality bilingual questions.
#### Tasks
1. **[TASK-003]**: Generate Batch 1 (Questions 1-15) - Focus on Macroeconomics/FX.
   - **Assignee**: System Architect
2. **[TASK-004]**: Generate Batch 2 (Questions 16-30) - Focus on Technical Analysis/Trading.
   - **Assignee**: System Architect
3. **[TASK-005]**: Generate Batch 3 (Questions 31-45) - Focus on Risk Management/Psychology.
   - **Assignee**: System Architect
4. **[TASK-006]**: Generate Batch 4 (Questions 46-60) - Focus on Platform/Tools.
   - **Assignee**: System Architect

### Phase 3: Integration & Verification
**Objective**: Merge data and verify integrity.
#### Tasks
1. **[TASK-007]**: Merge new questions into `questions.json` (Increment IDs correctly).
   - **Assignee**: System Architect
2. **[TASK-008]**: Generate Implementation Report (Markdown).
   - **Assignee**: System Architect
3. **[TASK-009]**: Create Unit Test `QuestionBankTest.kt` to validate JSON structure and uniqueness.
   - **Assignee**: System Architect

## Risk Assessment
- **Duplication**: High risk of overlapping with existing 74 questions. Mitigation: Create a content hash of existing questions before generating new ones.
- **JSON Validity**: Syntax errors in large JSON files. Mitigation: Use `jq` or unit tests to validate.

## Testing Strategy
- **Unit Testing**: Parse JSON, check for duplicate IDs, check for missing fields, check for bilingual format (`\n` separator).
