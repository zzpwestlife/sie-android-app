# 按章节学习功能设计文档

**日期**: 2026-02-28
**状态**: 已批准
**设计方案**: 轻量级章节筛选器（方案 A）
**预计开发周期**: 2-3 天

---

## 1. 功能概述

### 1.1 需求背景

用户希望能够按章节（基于题目的 `category` 字段）进行有针对性的学习，而不是随机或全量练习。需要支持：
- 查看每个章节的学习进度（总题数、已学、正确率）
- 多选章节进行混合学习
- 智能排序题目（优先显示错题和未学题目）
- 替换现有"开始练习"功能作为新的学习入口

### 1.2 核心特性

✅ **章节多选**: 支持用户勾选多个章节进行组合学习
✅ **进度追踪**: 显示每个章节的详细统计（总题数、已学、正确率、上次学习时间）
✅ **智能排序**: 错题优先 → 错误次数高 → 未学/很久未学 → ID 顺序
✅ **实时更新**: 基于 Flow 监听数据库变化，自动刷新进度

---

## 2. 架构设计

### 2.1 模块划分

```
┌─────────────────────────────────────┐
│      Feature Layer (特性层)          │
│  ┌─────────────────────────────┐   │
│  │  feature/chapter/           │   │
│  │  - ChapterSelectionScreen   │   │
│  │  - ChapterSelectionViewModel│   │
│  │  - ChapterSelectionUiState  │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
          ↓ 依赖
┌─────────────────────────────────────┐
│       Core Layer (核心层)            │
│  ┌─────────────────────────────┐   │
│  │  core/model/Chapter.kt      │   │
│  │  core/database/             │   │
│  │    - QuestionDao (扩展)     │   │
│  │    - QuestionEntity (扩展)  │   │
│  │  core/data/                 │   │
│  │    - QuestionRepository     │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

### 2.2 数据流设计

```
HomeScreen (点击"章节学习")
    ↓
ChapterSelectionScreen
    ↓ (加载所有章节)
ChapterSelectionViewModel.loadChapters()
    ↓ (聚合计算进度)
QuestionRepository.getAllCategories() + getAllQuestions()
    ↓ (展示章节列表)
UI: 显示章节卡片（进度、多选框）
    ↓ (用户多选章节)
toggleChapterSelection(chapterName)
    ↓ (点击"开始")
StudyScreen(selectedCategories = ["1. Macroeconomics", "3. ..."])
    ↓ (智能排序)
StudyViewModel.smartSortQuestions()
    ↓ (用户答题)
onAnswerSelected() → updateLastStudiedAt(questionId, timestamp)
    ↓ (返回)
ChapterSelectionScreen (进度自动刷新)
```

---

## 3. 数据模型设计

### 3.1 数据库 Schema 扩展

**修改 `QuestionEntity`**:

```kotlin
@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: Int,
    val content: String,
    val options: String,  // JSON array
    val correctAnswerIndex: Int,
    val explanation: String,
    val category: String,
    val isBookmarked: Boolean = false,
    val isWrong: Boolean = false,
    val wrongCount: Int = 0,
    val lastStudiedAt: Long? = null  // 新增：最后学习时间戳（毫秒）
)
```

**数据库迁移**:
- 从版本 12 升级到 13
- 迁移 SQL: `ALTER TABLE questions ADD COLUMN lastStudiedAt INTEGER`

### 3.2 Chapter 模型

```kotlin
data class Chapter(
    val name: String,              // 例如: "1. Macroeconomics"
    val displayName: String,        // 支持多语言，例如: "宏观经济学"
    val totalQuestions: Int,        // 该章节总题数
    val studiedQuestions: Int,      // 已学题数（lastStudiedAt != null）
    val correctCount: Int,          // 做对的题数
    val wrongCount: Int,            // 做错的题数
    val accuracyRate: Float,        // 正确率 (correctCount / studiedQuestions)
    val lastStudiedAt: Long?,       // 该章节最后学习时间
    val isSelected: Boolean = false // 用户是否选中该章节（UI 状态）
)
```

**计算逻辑**:
- `totalQuestions` = COUNT(*) WHERE category = name
- `studiedQuestions` = COUNT(*) WHERE category = name AND lastStudiedAt IS NOT NULL
- `correctCount` = studiedQuestions - wrongCount
- `accuracyRate` = correctCount / studiedQuestions × 100 (如果 studiedQuestions = 0，则为 0%)

### 3.3 DAO 扩展

**`QuestionDao` 新增方法**:

```kotlin
@Dao
interface QuestionDao {
    // 现有方法...

    // 新增：按章节查询所有题目
    @Query("SELECT * FROM questions WHERE category IN (:categories)")
    fun getQuestionsByCategories(categories: List<String>): Flow<List<QuestionEntity>>

    // 新增：获取所有不同的 category（用于章节列表）
    @Query("SELECT DISTINCT category FROM questions ORDER BY category")
    fun getAllCategories(): Flow<List<String>>

    // 新增：更新题目的最后学习时间
    @Query("UPDATE questions SET lastStudiedAt = :timestamp WHERE id = :questionId")
    suspend fun updateLastStudiedAt(questionId: Int, timestamp: Long)
}
```

---

## 4. ViewModel 逻辑

### 4.1 ChapterSelectionViewModel

**职责**:
1. 加载所有章节及其进度统计
2. 管理章节多选状态
3. 提供选中章节列表给 StudyScreen

**关键方法**:

```kotlin
@HiltViewModel
class ChapterSelectionViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChapterSelectionUiState>(ChapterSelectionUiState.Loading)
    val uiState: StateFlow<ChapterSelectionUiState> = _uiState.asStateFlow()

    init {
        loadChapters()
    }

    private fun loadChapters() {
        viewModelScope.launch {
            try {
                questionRepository.getAllCategories()
                    .combine(questionRepository.getAllQuestions()) { categories, questions ->
                        categories.map { category ->
                            val chapterQuestions = questions.filter { it.category == category }
                            Chapter(
                                name = category,
                                displayName = category.extractLocalizedName(),
                                totalQuestions = chapterQuestions.size,
                                studiedQuestions = chapterQuestions.count { it.lastStudiedAt != null },
                                correctCount = chapterQuestions.count {
                                    it.lastStudiedAt != null && !it.isWrong
                                },
                                wrongCount = chapterQuestions.sumOf { it.wrongCount },
                                accuracyRate = calculateAccuracy(chapterQuestions),
                                lastStudiedAt = chapterQuestions.mapNotNull { it.lastStudiedAt }.maxOrNull()
                            )
                        }
                    }
                    .collect { chapters ->
                        _uiState.value = ChapterSelectionUiState.Success(chapters)
                    }
            } catch (e: Exception) {
                _uiState.value = ChapterSelectionUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleChapterSelection(chapterName: String) {
        val currentState = _uiState.value
        if (currentState is ChapterSelectionUiState.Success) {
            val updatedChapters = currentState.chapters.map { chapter ->
                if (chapter.name == chapterName) {
                    chapter.copy(isSelected = !chapter.isSelected)
                } else {
                    chapter
                }
            }
            _uiState.value = currentState.copy(chapters = updatedChapters)
        }
    }

    fun getSelectedChapters(): List<String> {
        val currentState = _uiState.value
        return if (currentState is ChapterSelectionUiState.Success) {
            currentState.chapters.filter { it.isSelected }.map { it.name }
        } else {
            emptyList()
        }
    }

    private fun calculateAccuracy(questions: List<Question>): Float {
        val studied = questions.filter { it.lastStudiedAt != null }
        if (studied.isEmpty()) return 0f
        val correct = studied.count { !it.isWrong }
        return (correct.toFloat() / studied.size) * 100
    }
}

sealed class ChapterSelectionUiState {
    object Loading : ChapterSelectionUiState()
    data class Success(val chapters: List<Chapter>) : ChapterSelectionUiState()
    data class Error(val message: String) : ChapterSelectionUiState()
}
```

### 4.2 StudyViewModel 扩展

**新增智能排序方法**:

```kotlin
fun startChapterStudy(selectedCategories: List<String>) {
    viewModelScope.launch {
        questionRepository.getQuestionsByCategories(selectedCategories)
            .map { questions -> smartSortQuestions(questions) }
            .collect { sortedQuestions ->
                // 更新 UI State...
            }
    }
}

private fun smartSortQuestions(questions: List<Question>): List<Question> {
    return questions.sortedWith(
        compareByDescending<Question> { it.isWrong }           // 1. 错题优先
            .thenByDescending { it.wrongCount }                // 2. 错误次数多的优先
            .thenBy { it.lastStudiedAt ?: 0L }                 // 3. 未学或很久未学的优先
            .thenBy { it.id }                                   // 4. 最后按ID排序保持稳定
    )
}
```

**智能排序逻辑说明**:
1. **错题优先**: `isWrong = true` 的题目排在最前
2. **错误次数**: 多次做错的题目需要重点练习
3. **学习时间**: `lastStudiedAt` 为 null（未学）或时间戳很小（很久未学）的题目优先
4. **ID 排序**: 最后按题目 ID 保证顺序稳定性

**更新学习记录**:

```kotlin
fun onAnswerSelected(questionId: Int, answerIndex: Int) {
    viewModelScope.launch {
        // 现有逻辑：标记对错、更新 wrongCount...

        // 新增：更新最后学习时间
        questionRepository.markQuestionAsStudied(questionId)
    }
}

// QuestionRepository 新增方法
suspend fun markQuestionAsStudied(questionId: Int) {
    questionDao.updateLastStudiedAt(questionId, System.currentTimeMillis())
}
```

---

## 5. UI 设计

### 5.1 ChapterSelectionScreen 布局

**视觉风格**: Glassmorphism（延续应用现有设计语言）

**布局结构**:
```
┌────────────────────────────────┐
│  TopAppBar: "章节学习"         │
│  [返回按钮]              [开始] │ <- "开始"按钮在右上角
└────────────────────────────────┘
│                                │
│  ┌──────────────────────────┐ │
│  │ 🔖 1. Macroeconomics      │ │
│  │    宏观经济学             │ │
│  │    ────────────────────   │ │
│  │    32题 | 已学10 | 75%   │ │ <- 进度统计
│  │    [✓] 已选中             │ │ <- 多选状态
│  └──────────────────────────┘ │
│                                │
│  ┌──────────────────────────┐ │
│  │ 📊 2. Financial Markets   │ │
│  │    金融市场               │ │
│  │    ────────────────────   │ │
│  │    28题 | 已学0 | 0%     │ │
│  │    [  ] 未选              │ │
│  └──────────────────────────┘ │
│                                │
│  [更多章节...]                 │
└────────────────────────────────┘
```

**章节卡片组件**:
- 使用 `GlassCard` 组件保持视觉一致性
- 选中状态：应用 `PrimaryGradient` 边框高亮
- 显示进度 Chip：总题数、已学数、正确率
- Checkbox 多选支持

### 5.2 用户交互流程

**流程 1: 从主页进入章节学习**
1. 用户点击 HomeScreen 的"章节学习"卡片（替换原有"开始练习"）
2. 导航到 `ChapterSelectionScreen`
3. 屏幕加载所有章节及其进度（Loading → Success）
4. 用户看到每个章节的统计信息（总题数、已学、正确率）

**流程 2: 选择章节并开始学习**
1. 用户点击章节卡片或 Checkbox 进行多选
2. 选中的章节卡片高亮显示（`PrimaryGradient` 边框）
3. 顶部"开始"按钮变为可用状态（至少选中一个章节）
4. 点击"开始"后，导航到 `StudyScreen`，传递 `selectedCategories` 参数
5. StudyScreen 根据选中的章节筛选题目并智能排序
6. 用户开始答题，每道题回答后自动更新 `lastStudiedAt` 时间戳

**流程 3: 学习完成后返回**
1. 用户完成学习后点击"返回"或系统返回按钮
2. 回到 `ChapterSelectionScreen`
3. ViewModel 自动刷新章节进度数据（Flow 监听数据库变化）
4. 用户看到更新后的统计信息（已学题数、正确率变化）

### 5.3 错误处理

**数据库错误**:
- 如果 Room 查询失败，显示 `ChapterSelectionUiState.Error`
- 错误界面显示友好提示：「加载章节失败，请稍后重试」+ 重试按钮

**空数据处理**:
- 如果 `categories` 为空（数据库无题目），显示空状态提示：「暂无题目数据」

**用户未选择章节**:
- "开始"按钮保持禁用状态
- （可选）显示 Toast 提示：「请至少选择一个章节」

---

## 6. 性能优化

### 6.1 章节进度计算缓存

- 使用 `Flow.combine` + `stateIn(SharingStarted.WhileSubscribed(5000))` 缓存计算结果
- 避免每次重组都重新聚合数据

### 6.2 LazyColumn 优化

- 使用 `key = { it.name }` 确保列表项稳定性
- 章节卡片使用 `remember` 缓存状态

---

## 7. 测试策略

### 7.1 单元测试

- `ChapterSelectionViewModel` 的章节进度聚合逻辑
- `StudyViewModel` 的智能排序算法（验证排序顺序正确性）

### 7.2 UI 测试

- 章节多选交互（勾选/取消勾选）
- "开始"按钮的启用/禁用状态
- 章节卡片高亮显示

### 7.3 集成测试

- 数据库迁移验证（版本 12 → 13）
- QuestionDao 新增方法的正确性
- 学习记录更新后章节进度的刷新

---

## 8. 实施计划

### Phase 1: 数据层改造（1 天）

1. 数据库迁移：添加 `lastStudiedAt` 字段
2. 扩展 `QuestionDao`：新增按章节查询、统计方法
3. 扩展 `QuestionRepository`：添加 `markQuestionAsStudied()` 方法
4. 创建 `Chapter` 模型

### Phase 2: ViewModel 实现（0.5 天）

1. 创建 `ChapterSelectionViewModel` 和 `ChapterSelectionUiState`
2. 实现章节进度聚合逻辑
3. 实现章节多选状态管理
4. 扩展 `StudyViewModel`：添加智能排序和按章节筛选

### Phase 3: UI 开发（1 天）

1. 创建 `ChapterSelectionScreen` 和 `ChapterCard` 组件
2. 实现章节列表展示和多选交互
3. 集成进度统计 Chip 组件
4. 修改 `HomeScreen`：将"开始练习"改为"章节学习"入口
5. 添加导航路由

### Phase 4: 测试与优化（0.5 天）

1. 编写单元测试和 UI 测试
2. 性能优化：缓存策略、LazyColumn 优化
3. 错误处理和边界情况验证

**总计**: 2-3 天开发周期

---

## 9. 未来扩展可能性

1. **章节笔记**: 支持用户为每个章节添加学习笔记
2. **章节成就**: 完成章节后解锁成就徽章
3. **章节推荐**: 基于学习数据智能推荐下一个应学习的章节
4. **章节锁定**: 按学习顺序解锁章节（游戏化）
5. **章节目标**: 设置每个章节的学习目标和截止时间

---

## 10. 设计批准

✅ **批准时间**: 2026-02-28
✅ **批准人**: 用户
✅ **下一步**: 创建实施计划（使用 writing-plans skill）
