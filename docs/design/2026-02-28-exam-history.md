# 考试历史功能设计文档

## 目标

实现完整的考试历史回顾功能：在考试提交时持久化每道题的答题详情，在统计页面提供历史入口，支持逐题回顾答题情况（正确/错误、答案对比、解析）。

## 架构决策

| 决策点 | 选择 | 理由 |
|--------|------|------|
| 数据粒度 | 完整答题记录 + 标记信息 | 支持逐题回顾 |
| 错题集 | 复用现有 WrongQuestionsScreen | 避免重复建设 |
| 入口位置 | StatsScreen 集成 | 与统计数据关联度高 |
| 分页策略 | 无需分页 | SIE 考试次数有限 |

## 数据库变更

### 新增 `exam_answers` 表（Migration 13→14）

```sql
CREATE TABLE IF NOT EXISTS `exam_answers` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `examResultId` INTEGER NOT NULL,
    `questionId` INTEGER NOT NULL,
    `selectedOptionIndex` INTEGER NOT NULL,
    `isCorrect` INTEGER NOT NULL DEFAULT 0,
    `isFlagged` INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY(`examResultId`) REFERENCES `exam_results`(`id`) ON DELETE CASCADE
)
CREATE INDEX IF NOT EXISTS `index_exam_answers_result` ON `exam_answers`(`examResultId`)
```

**字段说明：**
- `examResultId` — 关联到 exam_results.id，级联删除
- `questionId` — 关联到 questions.id（逻辑关联，不设物理外键，因为题库可能更新）
- `selectedOptionIndex` — 用户选择的选项索引
- `isCorrect` — 是否回答正确
- `isFlagged` — 考试中是否被标记

### 修改 `OfflineExamRepository.saveExamResult()`

当前实现仅保存 ExamResult。需要改为事务性操作：先插入 ExamResult 获取 ID，再批量插入 ExamAnswer 记录。

## 模型层

### ExamAnswer（新增）

```kotlin
// core/model/src/main/java/com/example/sie/core/model/ExamAnswer.kt
data class ExamAnswer(
    val id: Int = 0,
    val examResultId: Int,
    val questionId: Int,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean,
    val isFlagged: Boolean = false
)
```

### ExamResult（扩展）

```kotlin
// 在 ExamResult 中添加可选字段用于详情页
data class ExamResult(
    val id: Int,
    val date: Long,
    val score: Int,
    val totalQuestions: Int,
    val correctCount: Int
)
// 注意：不在 ExamResult 中嵌套 answers，详情页单独查询
```

### ExamAnswerEntity（新增）

```kotlin
// core/database/src/main/java/com/example/sie/core/database/model/ExamAnswerEntity.kt
@Entity(
    tableName = "exam_answers",
    foreignKeys = [ForeignKey(
        entity = ExamResultEntity::class,
        parentColumns = ["id"],
        childColumns = ["examResultId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["examResultId"])]
)
data class ExamAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examResultId: Int,
    val questionId: Int,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean,
    val isFlagged: Boolean = false
)
```

## DAO 层扩展

### ExamResultDao（扩展）

```kotlin
// 新增方法
@Insert
suspend fun insertExamResultAndGetId(examResult: ExamResultEntity): Long

@Insert
suspend fun insertExamAnswers(answers: List<ExamAnswerEntity>)

@Transaction
@Query("SELECT * FROM exam_answers WHERE examResultId = :examResultId")
fun getExamAnswers(examResultId: Int): Flow<List<ExamAnswerEntity>>

@Query("SELECT * FROM exam_results WHERE id = :id")
fun getExamResultById(id: Int): Flow<ExamResultEntity?>
```

## Repository 层扩展

### ExamRepository 接口新增

```kotlin
suspend fun saveExamResultWithAnswers(
    examResult: ExamResult,
    answers: List<ExamAnswer>
)
fun getExamAnswers(examResultId: Int): Flow<List<ExamAnswer>>
fun getExamResultById(id: Int): Flow<ExamResult?>
```

## UI 层

### 1. StatsScreen 修改

在现有 `StatsScreen` 的 `Success` 状态中，在平均分卡片和最近结果列表之间添加一个「查看全部考试历史」按钮。每个考试结果卡片也改为可点击，直接进入该考试的详情页。

### 2. ExamHistoryScreen（新增）

**路由**：`exam_history_route`

**UI 结构**：
- TopAppBar + 返回按钮
- LazyColumn 展示所有考试记录
- 每条记录：日期、分数（圆环）、正确题数/总题数、通过/未通过标签
- 点击进入 ExamDetailScreen

**UiState**：
```kotlin
sealed interface ExamHistoryUiState {
    data object Loading : ExamHistoryUiState
    data object Empty : ExamHistoryUiState
    data class Success(val results: List<ExamResult>) : ExamHistoryUiState
}
```

### 3. ExamDetailScreen（新增）

**路由**：`exam_detail_route/{examResultId}`

**UI 结构**：
- TopAppBar 显示考试日期 + 分数
- 筛选切换：全部 / 仅错题
- HorizontalPager 逐题展示，复用 QuestionCard 组件
- 每题显示：题号、用户答案标记（正确绿色/错误红色）、解析
- 底部：题目索引导航条

**UiState**：
```kotlin
sealed interface ExamDetailUiState {
    data object Loading : ExamDetailUiState
    data object Error : ExamDetailUiState
    data class Success(
        val examResult: ExamResult,
        val answers: List<ExamAnswer>,
        val questions: List<Question>,
        val filterWrongOnly: Boolean = false
    ) : ExamDetailUiState
}
```

**关键逻辑**：
- 通过 `examResultId` 查询 ExamAnswer 列表
- 通过 ExamAnswer 中的 `questionId` 列表批量查询 Question
- 合并展示：Question 内容 + ExamAnswer 选择结果

## 导航变更

### SieNavHost 新增路由

```kotlin
examHistoryScreen(
    onBackClick = navController::popBackStack,
    onExamClick = navController::navigateToExamDetail
)
examDetailScreen(
    onBackClick = navController::popBackStack
)
```

### StatsScreen 新增导航回调

```kotlin
fun NavGraphBuilder.statsScreen(
    onExamHistoryClick: () -> Unit,        // 新增
    onExamResultClick: (Int) -> Unit       // 新增：点击单条记录
)
```

## ExamViewModel 修改

### submitExam() 扩展

当前 `submitExam()` 调用 `examRepository.saveExamResult()`。需要改为调用 `saveExamResultWithAnswers()`，将 `userAnswers` Map 和 `flaggedQuestions` Set 转换为 `ExamAnswer` 列表一并保存。

```kotlin
// 伪代码
val answers = questions.map { question ->
    val selectedIndex = userAnswers[question.id] ?: -1
    ExamAnswer(
        examResultId = 0, // 由 repository 填充
        questionId = question.id,
        selectedOptionIndex = selectedIndex,
        isCorrect = selectedIndex == question.correctAnswerIndex,
        isFlagged = question.id in flaggedQuestions
    )
}
examRepository.saveExamResultWithAnswers(result, answers)
```

## 多语言支持

新增字符串资源：

| Key | English | 中文 |
|-----|---------|------|
| `exam_history_title` | Exam History | 考试历史 |
| `exam_history_empty` | No exam records yet | 暂无考试记录 |
| `exam_history_view_all` | View All History | 查看全部历史 |
| `exam_detail_title` | Exam Detail | 考试详情 |
| `exam_detail_filter_all` | All Questions | 全部题目 |
| `exam_detail_filter_wrong` | Wrong Only | 仅错题 |
| `exam_detail_question_index` | Question %d/%d | 第 %d/%d 题 |
| `exam_result_passed` | Passed | 通过 |
| `exam_result_failed` | Failed | 未通过 |
| `exam_detail_your_answer` | Your Answer | 你的答案 |
| `exam_detail_correct_answer` | Correct Answer | 正确答案 |
| `exam_detail_flagged` | Flagged | 已标记 |

## 测试策略

### 单元测试
1. **ExamAnswerEntity 映射测试** — Entity ↔ Model 转换
2. **ExamHistoryViewModel 测试** — 加载状态、空状态、成功状态
3. **ExamDetailViewModel 测试** — 加载详情、筛选错题
4. **ExamViewModel.submitExam 测试** — 验证答题记录正确保存

### 集成测试
1. **数据库迁移测试** — Migration 13→14 正确执行
2. **事务性保存测试** — ExamResult + ExamAnswers 原子性保存

## 文件变更清单

### 新增文件（8 个）
1. `core/model/src/main/java/com/example/sie/core/model/ExamAnswer.kt`
2. `core/database/src/main/java/com/example/sie/core/database/model/ExamAnswerEntity.kt`
3. `feature/exam/src/main/java/com/example/sie/feature/exam/ExamHistoryScreen.kt`
4. `feature/exam/src/main/java/com/example/sie/feature/exam/ExamDetailScreen.kt`
5. `feature/exam/src/main/java/com/example/sie/feature/exam/ExamHistoryViewModel.kt`
6. `feature/exam/src/main/java/com/example/sie/feature/exam/ExamDetailViewModel.kt`
7. `feature/exam/src/test/.../ExamHistoryViewModelTest.kt`
8. `feature/exam/src/test/.../ExamDetailViewModelTest.kt`

### 修改文件（10 个）
1. `core/database/src/main/java/com/example/sie/core/database/AppDatabase.kt` — Migration 13→14, 注册新 Entity
2. `core/database/src/main/java/com/example/sie/core/database/di/DatabaseModule.kt` — 注册新迁移
3. `core/database/src/main/java/com/example/sie/core/database/dao/ExamResultDao.kt` — 新增查询方法
4. `core/data/src/main/java/com/example/sie/core/data/repository/ExamRepository.kt` — 新增接口方法
5. `core/data/src/main/java/com/example/sie/core/data/repository/OfflineExamRepository.kt` — 实现新方法
6. `feature/exam/src/main/java/com/example/sie/feature/exam/ExamViewModel.kt` — 修改 submitExam()
7. `feature/exam/src/main/java/com/example/sie/feature/exam/navigation/ExamNavigation.kt` — 新增路由
8. `feature/stats/src/main/java/com/example/sie/feature/stats/StatsScreen.kt` — 添加入口
9. `feature/stats/src/main/java/com/example/sie/feature/stats/navigation/StatsNavigation.kt` — 添加导航回调
10. `app/src/main/java/com/example/sie_android_app/navigation/SieNavHost.kt` — 注册新路由
11. `core/common/src/main/res/values/strings.xml` — 英文字符串
12. `core/common/src/main/res/values-zh/strings.xml` — 中文字符串
