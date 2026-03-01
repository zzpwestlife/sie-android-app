# Code Review -- feature/unit-testing 分支未提交变更

**审查日期**: 2026-03-01
**审查分支**: `feature/unit-testing` (未提交变更, 对比已提交的 HEAD)
**变更规模**: 22 个文件, +185 / -905 行 (净删除 720 行)

---

## 一、变更概览

本次变更包含四大主题:

1. **QuestionCard 组件复用重构** -- 将 `BookmarkedScreen` 和 `WrongQuestionsScreen` 中手写的选项/解析展示逻辑替换为统一的 `QuestionCard` 组件
2. **Stats 模块完整移除** -- 删除 `feature/stats` 模块及所有关联引用 (导航、字符串资源、build 依赖)
3. **Exam 标记 (Flag) 功能移除** -- 移除考试中的题目标记功能，简化 ExamViewModel 和 ExamScreen
4. **WrongQuestionsViewModel 添加多语言支持** -- 注入 `UserDataRepository` 以获取用户语言偏好

---

## 二、严重问题 (Critical Issues)

### 2.1 ExamDetailScreen 遗留空 if 分支

**文件**: `feature/exam/src/main/java/com/example/sie/feature/exam/ExamDetailScreen.kt` (第 371-373 行)

```kotlin
if (answer.isFlagged) {
    // Flag removed
}
```

这是一个空的 `if` 分支，仅留了一行注释。虽然 `isFlagged` 在 `ExamViewModel` 中现在始终被设为 `false`，但历史数据中 `isFlagged` 可能为 `true`，此时进入该分支后什么都不做，且构成 Dead Code。

**建议**: 完全删除此 `if` 块。

### 2.2 isFlagged 字段半移除导致层级不一致

**文件**:
- `feature/exam/src/main/java/com/example/sie/feature/exam/ExamViewModel.kt` (第 293 行): `isFlagged = false` 硬编码
- `core/model/src/main/java/com/example/sie/core/model/ExamAnswer.kt`: `isFlagged` 字段仍存在
- `core/database/src/main/java/com/example/sie/core/database/model/ExamAnswerEntity.kt`: `isFlagged` 列仍存在
- `core/database/src/main/java/com/example/sie/core/database/AppDatabase.kt`: `isFlagged` DDL 仍存在

UI 层已完全移除标记功能，但模型层和数据库层仍保留了 `isFlagged` 字段，而 ViewModel 中强制写入 `false`。这种半移除状态会让后续维护者困惑。

**建议**: 若确认永久移除标记功能，应在 `ExamAnswer` model 上标注 `@Deprecated`，并在后续版本通过数据库迁移清理该字段。当前至少应在代码中添加明确的 TODO 注释说明意图。

---

## 三、改进建议 (Improvement Suggestions)

### 3.1 WrongQuestionsViewModel 和 BookmarkedViewModel 使用 FQN 而非 import

**文件**:
- `feature/home/src/main/java/com/example/sie/feature/home/WrongQuestionsViewModel.kt` (第 14 行)
- `feature/home/src/main/java/com/example/sie/feature/home/BookmarkedViewModel.kt` (第 14 行)

```kotlin
private val userDataRepository: com.example.sie.core.data.repository.UserDataRepository
```

两处均使用完全限定名而非顶部 import 声明，降低了可读性，且不符合 Kotlin 惯用风格。

**建议**: 添加 `import com.example.sie.core.data.repository.UserDataRepository` 并使用简称。

### 3.2 语言收集可用 stateIn 替代手动 collect

**文件**:
- `feature/home/src/main/java/com/example/sie/feature/home/WrongQuestionsViewModel.kt` (第 20-26 行)
- `feature/home/src/main/java/com/example/sie/feature/home/BookmarkedViewModel.kt` (第 25-31 行)

```kotlin
init {
    viewModelScope.launch {
        userDataRepository.userData.collect { userData ->
            _language.value = userData.language
        }
    }
}
```

通过 `launch` + `collect` 手动收集 Flow 并更新 `MutableStateFlow`。更惯用的写法:

```kotlin
val language: StateFlow<String> = userDataRepository.userData
    .map { it.language }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "zh")
```

这样更简洁、声明式，且与同文件中 `uiState` 的写法风格一致。

### 3.3 ExamScreen 中残留未使用的 import

**文件**: `feature/exam/src/main/java/com/example/sie/feature/exam/ExamScreen.kt`

移除标记 (Flag) 相关 UI 后，以下 import 不再被使用:

- 第 30 行: `import androidx.compose.material.icons.outlined.CheckCircle` -- Flag 图标的 outlined 版本已无引用点

**建议**: 删除该行 import。

### 3.4 展开后题目文本重复显示

**文件**:
- `feature/home/src/main/java/com/example/sie/feature/home/BookmarkedScreen.kt` (第 322 行 + 展开区)
- `feature/home/src/main/java/com/example/sie/feature/home/WrongQuestionsScreen.kt` (第 389 行 + 展开区)

在 `BookmarkedQuestionCard` 和 `WrongQuestionCard` 中，收起状态的 `ModernGradientCard` 已显示题目文本 (`question.getLocalizedContent(language)`)，展开后 `QuestionCard` 内部会再次显示完整的题目文本。用户会看到同一道题的内容出现两次。

**建议**: 考虑在 `QuestionCard` 中添加 `showQuestionText: Boolean = true` 参数，在此场景中设为 `false`；或在展开时隐藏头部卡片中的题目预览。

### 3.5 错误信息硬编码英文未国际化

**文件**:
- `feature/home/src/main/java/com/example/sie/feature/home/WrongQuestionsViewModel.kt` (第 80、92 行)
- `feature/home/src/main/java/com/example/sie/feature/home/BookmarkedViewModel.kt` (第 69 行)

```kotlin
_errorEvents.emit("Failed to remove from wrong list: ${e.message}")
_errorEvents.emit("Failed to toggle bookmark: ${e.message}")
```

在支持中英双语的应用中，这些 Snackbar 消息始终显示英文。

**建议**: 改用字符串资源 ID 或错误代码，在 UI 层通过 `stringResource` 转换为对应语言。

---

## 四、代码风格与规范 (Code Style)

### 4.1 import 语句插入位置不当

**文件**: `feature/home/src/main/java/com/example/sie/feature/home/WrongQuestionsScreen.kt` (第 26 行)

```kotlin
import androidx.compose.material3.AlertDialog
import com.example.sie.core.designsystem.component.QuestionCard  // <-- 项目 import 混入 material3 组中间
import androidx.compose.material3.CircularProgressIndicator
```

**建议**: 将 `QuestionCard` import 移至文件中其他 `com.example.sie` import 所在的区域。

### 4.2 Spacing 值混用

展开区域的间距使用了硬编码 `8.dp`:

```kotlin
// BookmarkedScreen.kt 第 344 行, WrongQuestionsScreen.kt 第 442 行
Spacer(modifier = Modifier.height(8.dp))
```

而代码库中已定义了 `SpacingSmall` 等主题常量，其他位置也在使用。应统一使用主题常量以保持一致性。

---

## 五、积极亮点 (Positive Highlights)

1. **组件复用方向正确**: 将 `BookmarkedScreen` 和 `WrongQuestionsScreen` 中各自手写的选项展示逻辑统一到 `QuestionCard` 组件，消除了约 80 行重复代码。后续只需维护一处选项渲染逻辑。

2. **Stats 模块清理彻底**: `feature/stats` 的移除非常干净 -- `settings.gradle.kts`、`app/build.gradle.kts`、`SieNavHost.kt`、`HomeNavigation.kt`、`HomeScreen.kt`、英文/中文字符串资源全部同步清理，grep 确认无残留引用。

3. **QuestionCard 解析展示条件严谨**: 新增的解析区域条件 `showExplanation && showFeedback && question.explanation.isNotBlank()` 覆盖了三重校验，且正确使用 `question.getExplanation(language)` 适配多语言。

4. **HomeViewModel 职责精简**: 移除了 `ExamRepository` 依赖和多余的统计计算，从 `combine(两个Flow)` 简化为 `单个Flow.map`，ViewModel 更加聚焦。

5. **i18n 资源同步清理**: 英文 (`values/strings.xml`) 和中文 (`values-zh/strings.xml`) 的字符串删除完全对称，共移除 stats 6条 + flag 4条 + home 4条 = 14 条已废弃的字符串资源。

---

## 六、审查总结

| 类别 | 数量 |
|------|------|
| 严重问题 | 2 |
| 改进建议 | 5 |
| 风格规范 | 2 |

**整体评估**: 这是一次合理的代码精简和组件复用重构。核心方向 -- 用统一的 `QuestionCard` 替代各处手写的选项展示 -- 是正确且有效的。主要风险点在于: (1) Flag 功能的半移除状态造成了模型层与 UI 层的不一致; (2) 展开卡片后题目文本重复显示影响用户体验。建议优先处理严重问题后再提交。

---

**审查人员**: Claude (claude-opus-4-6)
**代码库**: sie-android-app
**分支**: feature/unit-testing
