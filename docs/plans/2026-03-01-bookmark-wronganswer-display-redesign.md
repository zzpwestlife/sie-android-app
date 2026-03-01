# 收藏夹 & 错题本 题目展示重构 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将收藏夹和错题本的题目/选项/解析展示统一为与学习模式相同的 `QuestionCard` 组件

**Architecture:** 扩展现有 `QuestionCard` 组件增加解析展示能力，然后在 BookmarkedScreen 和 WrongQuestionsScreen 中复用它，替换掉低质量的自定义按钮式选项渲染。错题本额外需要添加 i18n language 支持。

**Tech Stack:** Kotlin, Jetpack Compose, Hilt DI

---

### Task 1: 扩展 QuestionCard 组件 — 增加解析展示

**Files:**

- Modify: `core/designsystem/src/main/java/com/example/sie/core/designsystem/component/QuestionCard.kt:57-89`

**Step 1: 在 QuestionCard 的 Column 中，选项循环之后增加解析区域**

在第87行 `}` (options forEach 结束) 之后、第88行 `}` (Column 结束) 之前插入：

```kotlin
            // Explanation
            if (showExplanation && showFeedback && question.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(SpacingMedium))

                Text(
                    text = stringResource(CommonR.string.common_explanation),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(SpacingSmall))
                Text(
                    text = question.getExplanation(language),
                    style = MaterialTheme.typography.bodySmall,
                    color = QuestionText.copy(alpha = 0.8f),
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }
```

**说明：**
- 条件：`showExplanation && showFeedback` — 只有在反馈模式下才显示解析
- 使用 `question.getExplanation(language)` 支持 i18n
- 颜色用绿色 `0xFF4CAF50`，与正确答案的 OptionRow 绿色系一致
- `QuestionText.copy(alpha = 0.8f)` 保证解析文本可读但不抢主视觉

**Step 2: 验证现有调用不受影响**

现有的 `StudyScreen` 调用 `QuestionCard(showExplanation = false)`，所以不会触发新代码。`ExamScreen` 同理。

**Step 3: Commit**

```bash
git add core/designsystem/src/main/java/com/example/sie/core/designsystem/component/QuestionCard.kt
git commit -m "feat(designsystem): add explanation display to QuestionCard component"
```

---

### Task 2: 改造 BookmarkedQuestionCard — 复用 QuestionCard

**Files:**

- Modify: `feature/home/src/main/java/com/example/sie/feature/home/BookmarkedScreen.kt:289-386`

**Step 1: 添加 import**

在文件头部 import 区域添加：

```kotlin
import com.example.sie.core.designsystem.component.QuestionCard
```

可移除不再需要的 import：

```kotlin
// 删除：import com.example.sie.core.designsystem.component.ModernGradientButton
```

**Step 2: 重写 BookmarkedQuestionCard 函数**

将第289-386行的整个 `BookmarkedQuestionCard` 函数替换为：

```kotlin
@Composable
private fun BookmarkedQuestionCard(
    question: Question,
    language: String,
    onRemoveBookmark: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        // Header: Category + Remove button
        ModernGradientCard(
            modifier = Modifier.fillMaxWidth(),
            gradient = AccentGradient,
            onClick = { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = question.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF11998E),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = question.getLocalizedContent(language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurface,
                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onRemoveBookmark) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(CommonR.string.common_remove_bookmark),
                        tint = OnBackgroundSecondary
                    )
                }
            }
        }

        // Expanded: QuestionCard with options + explanation
        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))

            QuestionCard(
                question = question,
                selectedOptionIndex = question.correctAnswerIndex,
                onOptionSelected = {},
                showFeedback = true,
                showExplanation = true,
                language = language,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

**说明：**
- 保留 `ModernGradientCard` 作为收起态的头部卡片（显示 category + 题目摘要 + 删除按钮）
- 展开后在下方单独渲染 `QuestionCard`，不再嵌套在 `ModernGradientCard` 内
- `selectedOptionIndex = question.correctAnswerIndex` + `showFeedback = true` 实现只读展示正确答案
- `showExplanation = true` 显示解析
- `onOptionSelected = {}` 因为 `showFeedback = true` 时 OptionRow 内部已禁止点击

**Step 3: Commit**

```bash
git add feature/home/src/main/java/com/example/sie/feature/home/BookmarkedScreen.kt
git commit -m "refactor(home): use QuestionCard in BookmarkedScreen for consistent display"
```

---

### Task 3: 给 WrongQuestionsViewModel 添加 language 支持

**Files:**

- Modify: `feature/home/src/main/java/com/example/sie/feature/home/WrongQuestionsViewModel.kt:12-14`

**Step 1: 添加 UserDataRepository 依赖和 language StateFlow**

当前 WrongQuestionsViewModel 构造函数只注入了 `QuestionRepository`，需要添加 `UserDataRepository` 并暴露 `language`。

将第12-14行替换为：

```kotlin
@HiltViewModel
class WrongQuestionsViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val userDataRepository: com.example.sie.core.data.repository.UserDataRepository
) : ViewModel() {

    private val _language = MutableStateFlow("zh")
    val language: StateFlow<String> = _language.asStateFlow()

    init {
        viewModelScope.launch {
            userDataRepository.userData.collect { userData ->
                _language.value = userData.language
            }
        }
    }
```

**说明：**
- 与 `BookmarkedViewModel` 完全一致的 language 获取模式
- 默认 "zh"，与 BookmarkedViewModel 保持一致

**Step 2: Commit**

```bash
git add feature/home/src/main/java/com/example/sie/feature/home/WrongQuestionsViewModel.kt
git commit -m "feat(home): add i18n language support to WrongQuestionsViewModel"
```

---

### Task 4: 改造 WrongQuestionCard — 复用 QuestionCard

**Files:**

- Modify: `feature/home/src/main/java/com/example/sie/feature/home/WrongQuestionsScreen.kt:65-87,354-477`

**Step 1: 更新 WrongQuestionsRoute 获取 language**

将第65-87行的 `WrongQuestionsRoute` 替换为：

```kotlin
@Composable
fun WrongQuestionsRoute(
    onBackClick: () -> Unit,
    viewModel: WrongQuestionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val language by viewModel.language.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errorEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    WrongQuestionsScreen(
        uiState = uiState,
        language = language,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onCategorySelected = viewModel::selectCategory,
        onRemoveFromWrong = viewModel::removeFromWrong,
        onToggleBookmark = viewModel::toggleBookmark
    )
}
```

**Step 2: 将 language 参数透传到 WrongQuestionsScreen -> WrongQuestionsContent -> WrongQuestionCard**

更新 `WrongQuestionsScreen` 签名（第91行）：

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WrongQuestionsScreen(
    uiState: WrongQuestionsUiState,
    language: String,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onRemoveFromWrong: (Int) -> Unit,
    onToggleBookmark: (Int) -> Unit
)
```

在第148行传递给 `WrongQuestionsContent`：

```kotlin
WrongQuestionsContent(
    state = uiState,
    language = language,
    modifier = Modifier.padding(paddingValues),
    ...
)
```

更新 `WrongQuestionsContent` 签名（第162行）：

```kotlin
@Composable
private fun WrongQuestionsContent(
    state: WrongQuestionsUiState.Success,
    language: String,
    modifier: Modifier = Modifier,
    ...
)
```

在第192行传递给 `WrongQuestionCard`：

```kotlin
WrongQuestionCard(
    question = question,
    language = language,
    onRemoveClick = { onRemoveFromWrong(question.id) },
    onToggleBookmark = { onToggleBookmark(question.id) }
)
```

**Step 3: 添加 import**

```kotlin
import com.example.sie.core.designsystem.component.QuestionCard
```

可移除不再需要的 import：

```kotlin
// 删除：import androidx.compose.material3.OutlinedButton
// 删除：import androidx.compose.material3.ButtonDefaults
```

**Step 4: 重写 WrongQuestionCard 函数**

将第354-477行（含 AlertDialog）替换为：

```kotlin
@Composable
private fun WrongQuestionCard(
    question: Question,
    language: String,
    onRemoveClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRemoveDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        // Header card with question preview + actions
        ModernGradientCard(
            modifier = Modifier.fillMaxWidth(),
            gradient = WarningGradient,
            onClick = { expanded = !expanded }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = question.getLocalizedContent(language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnBackground,
                            maxLines = if (expanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(SpacingSmall))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = question.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = OnBackground.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(SpacingSmall))
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFFFF6B6B),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "\u00d7${question.wrongCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Row {
                        IconButton(onClick = onToggleBookmark) {
                            Icon(
                                imageVector = if (question.isBookmarked) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = stringResource(CommonR.string.common_bookmark),
                                tint = if (question.isBookmarked) Color(0xFFFFD700) else OnBackground.copy(alpha = 0.5f)
                            )
                        }
                        IconButton(onClick = { showRemoveDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(CommonR.string.wrong_questions_remove_from_list),
                                tint = OnBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // Expanded: QuestionCard with options + explanation
        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))

            QuestionCard(
                question = question,
                selectedOptionIndex = question.correctAnswerIndex,
                onOptionSelected = {},
                showFeedback = true,
                showExplanation = true,
                language = language,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = {
                Text(text = stringResource(CommonR.string.wrong_questions_remove_title))
            },
            text = {
                Text(text = stringResource(CommonR.string.wrong_questions_remove_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveDialog = false
                        onRemoveClick()
                    }
                ) {
                    Text(text = stringResource(CommonR.string.common_remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text(text = stringResource(CommonR.string.common_cancel))
                }
            }
        )
    }
}
```

**说明：**
- 头部保留 `ModernGradientCard` + WarningGradient 风格 + 所有操作按钮
- 题目文本改用 `question.getLocalizedContent(language)` 替代 `question.content`（修复 i18n bug）
- 展开后用 `QuestionCard` 统一展示选项和解析
- AlertDialog 保持不变

**Step 5: Commit**

```bash
git add feature/home/src/main/java/com/example/sie/feature/home/WrongQuestionsScreen.kt
git commit -m "refactor(home): use QuestionCard in WrongQuestionsScreen for consistent display"
```

---

### Task 5: 清理未使用的 import

**Files:**

- Modify: `feature/home/src/main/java/com/example/sie/feature/home/BookmarkedScreen.kt` (顶部 import)
- Modify: `feature/home/src/main/java/com/example/sie/feature/home/WrongQuestionsScreen.kt` (顶部 import)

**Step 1: BookmarkedScreen.kt 清理**

移除：
```kotlin
import com.example.sie.core.designsystem.component.ModernGradientButton
```

**Step 2: WrongQuestionsScreen.kt 清理**

移除：
```kotlin
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
```

**Step 3: Commit**

```bash
git add feature/home/src/main/java/com/example/sie/feature/home/BookmarkedScreen.kt feature/home/src/main/java/com/example/sie/feature/home/WrongQuestionsScreen.kt
git commit -m "chore(home): remove unused imports after QuestionCard refactor"
```

---

### Task 6: 构建验证

**Step 1: 编译项目确认无错误**

```bash
./gradlew :feature:home:compileDebugKotlin :core:designsystem:compileDebugKotlin 2>&1
```

预期：BUILD SUCCESSFUL

**Step 2: 如有编译错误，修复并重新提交**
