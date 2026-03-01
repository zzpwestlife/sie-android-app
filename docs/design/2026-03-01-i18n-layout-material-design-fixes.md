# 多语言一致性、按钮布局优化与 Material Design 3 合规性设计

**日期：** 2026-03-01
**状态：** ✅ 已批准
**优先级：** 高
**预估工作量：** 1.5-2 小时

---

## 一、背景与问题陈述

### 1.1 问题概述

在完成 Modern Gradient UI 迁移后，用户反馈了三个关键问题：

1. **多语言切换不一致**：从设置页面切换到简体中文后，首页仍显示英文（题目和选项已显示中文）
2. **首页按钮大小不一致**：Bookmarks 按钮占满整行（100% 宽度），与其他按钮（50% 宽度）大小差异明显，视觉突兀
3. **Material Design 3 合规性未验证**：需要系统性审查全部 13 个屏幕是否符合 Material Design 3 (Material You) 标准

### 1.2 根本原因分析

#### 问题 1：多语言切换不一致
- **位置**：`feature/home/src/main/java/com/example/sie/feature/home/HomeScreen.kt`
- **原因**：HomeScreen 硬编码了所有英文文本，未使用 `stringResource()`
- **影响范围**：
  ```kotlin
  // 硬编码示例（Line 57-257）
  title = "SIE Exam Prep"  // ← 应使用 stringResource(CommonR.string.home_title)
  contentDescription = "Settings"  // ← 应使用 stringResource
  text = "Welcome back!"  // ← 应使用 stringResource
  text = "Study Progress"  // ← 应使用 stringResource
  text = "Study Mode"  // ← 应使用 stringResource
  // ... 共 20+ 处硬编码文本
  ```
- **为什么题目显示中文？** 题目从数据库读取，使用了 `Question.getLocalizedContent(language)` 方法

#### 问题 2：按钮布局不一致
- **位置**：`HomeScreen.kt` Line 213-260
- **原因**：Bookmarks 按钮单独占一行且使用 `Modifier.fillMaxWidth()`
- **当前布局**：
  ```
  Row { Study Mode (50%) | Mock Exam (50%) }
  Bookmarks (100%)  // ← 突兀
  Row { Wrong Questions (50%) | Statistics (50%) }
  ```

#### 问题 3：Material Design 3 合规性
- **范围**：13 个屏幕（HomeScreen, StudyScreen, ExamScreen, ExamDetailScreen, ExamHistoryScreen, TopicSelectionScreen, BookmarkedScreen, WrongQuestionsScreen, StatsScreen, SettingsScreen 等）
- **需要验证**：组件使用、间距布局、颜色对比度、动画过渡、可访问性

---

## 二、解决方案设计

### 2.1 问题 1：多语言切换全局一致性

#### 2.1.1 架构设计

**方案：使用 Android 标准 stringResource + 监听 userData.language**

```
┌─────────────────────────────────────────────────────────────┐
│                        User Action                           │
│              (Settings Screen: 切换到中文)                   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  SettingsViewModel                           │
│      userDataRepository.setLanguage("zh")                    │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                UserDataRepository (DataStore)                │
│         保存 language = "zh" 到本地存储                      │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   HomeViewModel                              │
│   订阅 userData.language Flow → 触发 recomposition           │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                     HomeScreen                               │
│  stringResource(CommonR.string.home_title)                   │
│  根据系统 Locale 自动选择 values/strings.xml 或              │
│  values-zh/strings.xml                                       │
└─────────────────────────────────────────────────────────────┘
```

#### 2.1.2 实现步骤

**Step 1: 添加缺失的字符串资源**

在 `core/common/src/main/res/values/strings.xml` 中添加：
```xml
<!-- Home Screen - Additional strings -->
<string name="home_app_title">SIE Exam Prep</string>
<string name="home_welcome_back">Welcome back!</string>
<string name="home_continue_prep">Continue your SIE exam preparation</string>
<string name="home_study_progress">Study Progress</string>
<string name="home_study_options">Study Options</string>
<string name="home_study_mode">Study Mode</string>
<string name="home_mock_exam">Mock Exam</string>
<string name="home_bookmarks">Bookmarks</string>
<string name="home_wrong_questions">Wrong Questions</string>
<string name="home_statistics">Statistics</string>
<string name="home_questions_studied">Questions\nStudied</string>
<string name="home_correct_rate">Correct\nRate</string>
<string name="common_settings">Settings</string>
```

在 `core/common/src/main/res/values-zh/strings.xml` 中添加对应翻译：
```xml
<!-- Home Screen - 中文翻译 -->
<string name="home_app_title">SIE 考试备考</string>
<string name="home_welcome_back">欢迎回来！</string>
<string name="home_continue_prep">继续您的 SIE 考试准备</string>
<string name="home_study_progress">学习进度</string>
<string name="home_study_options">学习选项</string>
<string name="home_study_mode">学习模式</string>
<string name="home_mock_exam">模拟考试</string>
<string name="home_bookmarks">收藏夹</string>
<string name="home_wrong_questions">错题本</string>
<string name="home_statistics">统计数据</string>
<string name="home_questions_studied">已学习\n题目数</string>
<string name="home_correct_rate">正确率</string>
<string name="common_settings">设置</string>
```

**Step 2: 修改 HomeScreen.kt**

将所有硬编码文本替换为 `stringResource()`：
```kotlin
@Composable
internal fun HomeScreen(...) {
    AppBackground {
        Scaffold(
            topBar = {
                ModernGradientTopAppBar(
                    title = stringResource(CommonR.string.home_app_title),  // ✅ 修改
                    gradient = PrimaryGradient,
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(CommonR.string.common_settings),  // ✅ 修改
                                tint = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                )
            },
            // ...
        ) { paddingValues ->
            LazyColumn(...) {
                item {
                    ModernGradientCard(...) {
                        Text(
                            text = stringResource(CommonR.string.home_welcome_back),  // ✅ 修改
                            // ...
                        )
                        Text(
                            text = stringResource(CommonR.string.home_continue_prep),  // ✅ 修改
                            // ...
                        )
                        Text(
                            text = stringResource(CommonR.string.home_study_progress),  // ✅ 修改
                            // ...
                        )
                    }
                }

                // 统计卡片
                item {
                    when (uiState) {
                        is HomeUiState.Success -> {
                            Row(...) {
                                ModernGradientCard(...) {
                                    Text(text = "${uiState.questionsStudied}")
                                    Text(
                                        text = stringResource(CommonR.string.home_questions_studied),  // ✅ 修改
                                        // ...
                                    )
                                }
                                ModernGradientCard(...) {
                                    Text(text = "${uiState.correctRate}%")
                                    Text(
                                        text = stringResource(CommonR.string.home_correct_rate),  // ✅ 修改
                                        // ...
                                    )
                                }
                            }
                        }
                    }
                }

                // 按钮
                item {
                    Text(
                        text = stringResource(CommonR.string.home_study_options),  // ✅ 修改
                        // ...
                    )
                }

                item {
                    Column(...) {
                        Row(...) {
                            ModernGradientButton(
                                text = stringResource(CommonR.string.home_study_mode),  // ✅ 修改
                                // ...
                            )
                            ModernGradientButton(
                                text = stringResource(CommonR.string.home_mock_exam),  // ✅ 修改
                                // ...
                            )
                        }
                        // ... 其他按钮同样修改
                    }
                }
            }
        }
    }
}
```

**Step 3: 验证其他屏幕**

检查其他 12 个屏幕是否也有硬编码文本：
- StudyScreen.kt
- ExamScreen.kt
- TopicSelectionScreen.kt
- 等等

#### 2.1.3 测试验证

1. 切换到中文：Settings → Language → 简体中文
2. 返回首页，验证所有文本显示为中文
3. 切换到英文：Settings → Language → English
4. 返回首页，验证所有文本显示为英文
5. 检查其他屏幕的多语言一致性

---

### 2.2 问题 2：首页按钮大小一致性

#### 2.2.1 布局设计

**推荐布局（方案 A）：2x2 网格 + 1 行单按钮**

```
┌─────────────────────────────────────────────────────────────┐
│                     Home Screen Layout                       │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────────┐  ┌─────────────────────┐          │
│  │   Study Mode        │  │   Mock Exam         │          │
│  │  (PrimaryGradient)  │  │ (SecondaryGradient) │          │
│  └─────────────────────┘  └─────────────────────┘          │
│                                                              │
│  ┌─────────────────────┐  ┌─────────────────────┐          │
│  │   Bookmarks         │  │ Wrong Questions     │          │
│  │ (TertiaryGradient)  │  │ (WarningGradient)   │          │
│  └─────────────────────┘  └─────────────────────┘          │
│                                                              │
│  ┌──────────────────────────────────────────────┐          │
│  │             Statistics                        │          │
│  │          (AccentGradient)                     │          │
│  └──────────────────────────────────────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

**设计理由：**
1. **视觉对称**：前 4 个按钮采用 2x2 网格，大小完全一致（每个 50% 宽度）
2. **功能突出**：Statistics 独占一行，强调其作为「总结性功能」的重要性
3. **最小修改**：只需调整 Bookmarks 的位置，无需添加新功能

#### 2.2.2 代码实现

修改 `HomeScreen.kt` Line 213-260：

```kotlin
// Feature Buttons Grid
item {
    Text(
        text = stringResource(CommonR.string.home_study_options),
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
        // Row 1: Study Mode + Mock Exam
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
        ) {
            ModernGradientButton(
                text = stringResource(CommonR.string.home_study_mode),
                onClick = onTopicSelectionClick,
                gradient = PrimaryGradient,
                modifier = Modifier.weight(1f)  // 50% 宽度
            )
            ModernGradientButton(
                text = stringResource(CommonR.string.home_mock_exam),
                onClick = onMockExamClick,
                gradient = SecondaryGradient,
                modifier = Modifier.weight(1f)  // 50% 宽度
            )
        }

        // Row 2: Bookmarks + Wrong Questions ← 修改点
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
        ) {
            ModernGradientButton(
                text = stringResource(CommonR.string.home_bookmarks),
                onClick = onBookmarkedClick,
                gradient = TertiaryGradient,
                modifier = Modifier.weight(1f)  // ✅ 从 fillMaxWidth() 改为 weight(1f)
            )
            ModernGradientButton(
                text = stringResource(CommonR.string.home_wrong_questions),
                onClick = onWrongQuestionsClick,
                gradient = WarningGradient,
                modifier = Modifier.weight(1f)  // 50% 宽度
            )
        }

        // Row 3: Statistics (独占一行) ← 修改点
        ModernGradientButton(
            text = stringResource(CommonR.string.home_statistics),
            onClick = onStatsClick,
            gradient = AccentGradient,
            modifier = Modifier.fillMaxWidth()  // ✅ 从 Row 中移出，独占一行
        )
    }
}
```

**视觉效果对比：**

| 修改前 | 修改后 |
|--------|--------|
| StudyMode(50%) + MockExam(50%) | StudyMode(50%) + MockExam(50%) |
| **Bookmarks(100%)** ← 突兀 | **Bookmarks(50%) + WrongQuestions(50%)** ← 对称 |
| WrongQuestions(50%) + Statistics(50%) | **Statistics(100%)** ← 功能突出 |

#### 2.2.3 备选方案（如需完全对称）

如果您更偏好完全对称的 2x3 网格，可以添加第 6 个按钮（例如：Achievements / History / Settings）：

```kotlin
Row {
    StudyMode(50%), MockExam(50%)
}
Row {
    Bookmarks(50%), Statistics(50%)
}
Row {
    WrongQuestions(50%), [新功能](50%)  // 例如：Achievements
}
```

---

### 2.3 问题 3：Material Design 3 合规性审计

#### 2.3.1 审计方法论

**分层审计法（Layer-by-Layer Audit）**

```
Layer 5: Accessibility (可访问性)
         ↑
Layer 4: Animation & Motion (动画与过渡)
         ↑
Layer 3: Color & Contrast (颜色与对比度)
         ↑
Layer 2: Spacing & Layout (间距与布局)
         ↑
Layer 1: Component Usage (组件使用规范)
```

**审计工具组合：**
1. **手动审查**：基于 Material Design 3 官方指南检查清单
2. **自动化工具**：
   - Android Lint（组件检查）
   - Compose Layout Inspector（布局检查）
   - Accessibility Scanner（可访问性检查）
   - WCAG Contrast Checker（对比度检查）

#### 2.3.2 审计清单（Checklist）

##### **Layer 1: 组件使用规范**

| 组件类型 | 检查项 | 标准要求 | 当前状态 | 需修复屏幕 |
|---------|--------|---------|---------|----------|
| **Button** | 使用 Material3 Button | FilledButton / OutlinedButton / TextButton | ✅ 使用 ModernGradientButton（基于 Material3 Button） | - |
| | 高度 | 56dp（触摸目标 ≥ 48dp） | ✅ 56dp (Line 45) | - |
| | 圆角 | Medium (12-16dp) | ✅ CornerRadiusMedium | - |
| | 文字大小 | 14-16sp | ✅ 16sp | - |
| **Card** | 使用 Material3 Card | 带阴影和圆角 | ✅ ModernGradientCard | - |
| | 阴影 | Elevation 1-4dp | ✅ ElevationMedium | - |
| | 圆角 | Large (16-20dp) | ⚠️ 需检查 | 待确认 |
| **TopAppBar** | 使用 Material3 TopAppBar | CenterAlignedTopAppBar / SmallTopAppBar | ✅ ModernGradientTopAppBar | - |
| | 高度 | 64dp (Small) / 112dp (Medium) | ⚠️ 需检查 | 待确认 |
| | 标题文字 | HeadlineSmall (24sp) 或 TitleLarge (22sp) | ⚠️ 需检查 | 待确认 |
| **Progress Indicator** | 使用 CircularProgressIndicator | 加载状态使用 | ✅ 已使用 | - |
| | 使用 LinearProgressIndicator | 进度条使用 | ✅ ModernGradientProgressBar | - |

##### **Layer 2: 间距与布局**

| 检查项 | 标准要求 | 当前状态 | 需修复屏幕 |
|--------|---------|---------|----------|
| **间距系统** | Small: 8dp, Medium: 16dp, Large: 24dp, XLarge: 32dp | ✅ 已定义 SpacingSmall/Medium/Large | - |
| **水平边距** | 16dp (屏幕左右边距) | ✅ HomeScreen 使用 SpacingMedium | 需检查其他屏幕 |
| **卡片间距** | 12-16dp (卡片之间间距) | ✅ HomeScreen 使用 SpacingMedium | 需检查其他屏幕 |
| **内容边距** | 16dp (卡片内部 padding) | ✅ ModernGradientCard 使用 SpacingMedium | - |
| **网格列数** | 根据屏幕宽度调整 (手机: 4, 平板: 8, 桌面: 12) | ⚠️ 当前固定为 2 列 | 可选优化 |

##### **Layer 3: 颜色与对比度 (WCAG AAA)**

| 检查项 | 标准要求 | 当前状态 | 需修复 |
|--------|---------|---------|--------|
| **主要文本对比度** | OnBackground vs Background ≥ 7:1 | ✅ 10:1+ (Color(0xFF1A1A1A) vs Color(0xFFF5F5F5)) | - |
| **次要文本对比度** | OnBackgroundSecondary ≥ 4.5:1 | ✅ 6.8:1 (Color(0xFF666666)) | - |
| **卡片文本对比度** | OnSurface vs Surface ≥ 7:1 | ✅ 已达标 | - |
| **渐变背景文本** | 白色文字 vs 渐变色 ≥ 4.5:1 | ⚠️ 需检查所有渐变色的最亮点 | 待确认 |
| **按钮文字对比度** | 白色 vs 渐变色 ≥ 4.5:1 | ⚠️ 需检查 TertiaryGradient 和 AccentGradient | 待确认 |

##### **Layer 4: 动画与过渡**

| 检查项 | 标准要求 | 当前状态 | 需修复 |
|--------|---------|---------|--------|
| **屏幕切换动画** | 使用 Material Motion (Fade / Slide / Scale) | ⚠️ 需检查 Navigation 配置 | 待确认 |
| **按钮点击反馈** | Ripple 效果 | ✅ Material3 默认提供 | - |
| **状态变化动画** | 使用 animateContentSize() 或 AnimatedVisibility | ⚠️ 需检查 HomeUiState.Loading 切换 | 待确认 |
| **共享元素过渡** | 卡片 → 详情页（可选） | ❌ 未实现 | 可选优化 |

##### **Layer 5: 可访问性 (Accessibility)**

| 检查项 | 标准要求 | 当前状态 | 需修复屏幕 |
|--------|---------|---------|----------|
| **Content Description** | 所有 Icon 必须有 contentDescription | ⚠️ Settings Icon 已添加，需检查其他 Icon | 待确认 |
| **装饰性图标** | contentDescription = null | ⚠️ 需检查装饰性 Icon | 待确认 |
| **语义化标签** | 使用 Modifier.semantics {} | ⚠️ 需检查 ModernGradientCard 是否需要 | 待确认 |
| **触摸目标大小** | ≥ 48x48dp | ✅ 按钮 56dp 高度 | - |
| **键盘导航** | 支持 Tab 键切换焦点 | ✅ Compose 默认支持 | - |
| **屏幕阅读器** | TalkBack 友好 | ⚠️ 需测试 | 待确认 |

#### 2.3.3 审计执行计划

**阶段 1：自动化扫描（10 分钟）**
```bash
# 运行 Android Lint
./gradlew lint

# 运行 Compose Compiler Metrics
./gradlew assembleRelease -Pandroidx.compose.compiler.metricsDestination=./build/compose_metrics

# 检查 Accessibility
# 使用 Accessibility Scanner 手动测试
```

**阶段 2：手动审查（60-80 分钟）**
- 逐屏检查 13 个屏幕的组件、布局、颜色、动画、可访问性
- 记录不合规项到审计报告

**阶段 3：生成报告（10 分钟）**
- 生成 `docs/reports/2026-03-01-material-design-audit-report.md`
- 包含：不合规项列表、优先级、修复建议

---

## 三、实施计划

### 3.1 任务分解

| 任务 ID | 任务描述 | 预估时间 | 优先级 | 依赖 |
|---------|---------|----------|--------|------|
| **T1** | 添加 Home Screen 字符串资源（values + values-zh） | 10 分钟 | P0 | - |
| **T2** | 修改 HomeScreen.kt 使用 stringResource | 10 分钟 | P0 | T1 |
| **T3** | 调整 Home Screen 按钮布局（2x2 + 1） | 5 分钟 | P0 | - |
| **T4** | 运行自动化审计工具（Lint + Metrics） | 10 分钟 | P1 | - |
| **T5** | 手动审查 HomeScreen Material Design 合规性 | 10 分钟 | P1 | T4 |
| **T6** | 手动审查其他 12 个屏幕 Material Design 合规性 | 60 分钟 | P1 | T5 |
| **T7** | 生成 Material Design 审计报告 | 10 分钟 | P1 | T6 |
| **T8** | 构建并测试（语言切换 + 按钮布局） | 10 分钟 | P0 | T2, T3 |

**总计预估时间：** 2 小时 5 分钟

### 3.2 验收标准

#### 问题 1：多语言切换
- [ ] 从 Settings 切换到简体中文后，返回首页所有文本显示为中文
- [ ] 切换到英文后，返回首页所有文本显示为英文
- [ ] 题目、选项、解析继续正确显示对应语言
- [ ] 其他屏幕（如有硬编码）也同步修复

#### 问题 2：按钮布局
- [ ] Study Mode 和 Mock Exam 按钮宽度相同（50%）
- [ ] Bookmarks 和 Wrong Questions 按钮宽度相同（50%）
- [ ] Statistics 按钮独占一行（100%），视觉上作为总结性功能
- [ ] 按钮之间间距一致（16dp）

#### 问题 3：Material Design 审计
- [ ] 生成完整的审计报告（Markdown 格式）
- [ ] 报告包含：组件、布局、颜色、动画、可访问性的检查结果
- [ ] 每个不合规项标注优先级（P0 / P1 / P2）
- [ ] 提供修复建议和代码示例

---

## 四、风险与限制

### 4.1 技术风险

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| **语言切换后需要重启 Activity** | 用户体验不佳 | 使用 `LocalConfiguration.current` 监听配置变化，自动触发 recomposition |
| **渐变色对比度不足** | WCAG 不达标 | 需要对每个渐变色进行对比度测试，必要时调整渐变色 |
| **审计时间超预期** | 延误其他任务 | 先完成 P0 任务（多语言 + 按钮布局），审计报告可异步生成 |

### 4.2 已知限制

1. **字体大小缩放**：当前 fontSizeScale 只影响 Material Typography，硬编码的 `fontSize = 12.sp` 不受影响（需后续优化）
2. **RTL 语言支持**：当前只支持 LTR（Left-to-Right）语言（英文、中文），未来如需支持阿拉伯语等 RTL 语言需额外适配
3. **动态主题**：当前渐变色固定，不支持 Material You 动态取色（Android 12+）

---

## 五、后续优化建议

### 5.1 短期优化（可选）

1. **优化按钮布局动画**：使用 `animateContentSize()` 使按钮大小变化更平滑
2. **添加屏幕切换动画**：使用 Navigation Compose 的 `enterTransition` 和 `exitTransition`
3. **优化加载状态**：HomeUiState.Loading 时使用骨架屏（Skeleton Screen）代替 CircularProgressIndicator

### 5.2 长期优化（未来迭代）

1. **动态主题支持**：集成 Material You 动态取色（基于壁纸颜色）
2. **字体大小缩放全局化**：确保所有 Text 组件都响应 fontSizeScale 设置
3. **无障碍模式优化**：添加高对比度模式、增强触摸目标、优化 TalkBack 朗读顺序
4. **性能监控**：使用 Firebase Performance Monitoring 监控屏幕渲染性能

---

## 六、附录

### A. 相关文件清单

**需要修改的文件：**
1. `core/common/src/main/res/values/strings.xml`（添加 15+ 字符串资源）
2. `core/common/src/main/res/values-zh/strings.xml`（添加 15+ 中文翻译）
3. `feature/home/src/main/java/com/example/sie/feature/home/HomeScreen.kt`（替换硬编码文本 + 调整按钮布局）

**需要审查的文件（Material Design 审计）：**
1. `feature/home/src/main/java/com/example/sie/feature/home/HomeScreen.kt`
2. `feature/study/src/main/java/com/example/sie/feature/study/StudyScreen.kt`
3. `feature/exam/src/main/java/com/example/sie/feature/exam/ExamScreen.kt`
4. `feature/exam/src/main/java/com/example/sie/feature/exam/ExamDetailScreen.kt`
5. `feature/exam/src/main/java/com/example/sie/feature/exam/ExamHistoryScreen.kt`
6. `feature/topic/src/main/java/com/example/sie/feature/topic/TopicSelectionScreen.kt`
7. `feature/home/src/main/java/com/example/sie/feature/home/BookmarkedScreen.kt`
8. `feature/home/src/main/java/com/example/sie/feature/home/WrongQuestionsScreen.kt`
9. `feature/stats/src/main/java/com/example/sie/feature/stats/StatsScreen.kt`
10. `feature/settings/src/main/java/com/example/sie/feature/settings/SettingsScreen.kt`
11. `core/designsystem/src/main/java/com/example/sie/core/designsystem/component/*.kt`（所有自定义组件）

### B. 参考资料

- [Material Design 3 Guidelines](https://m3.material.io/)
- [Material Design 3 Components (Compose)](https://developer.android.com/jetpack/compose/designsystems/material3)
- [WCAG 2.1 Contrast Guidelines](https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html)
- [Android Accessibility Guide](https://developer.android.com/guide/topics/ui/accessibility)
- [Jetpack Compose Accessibility](https://developer.android.com/jetpack/compose/accessibility)

---

**设计批准：** ✅ 2026-03-01
**下一步：** 创建详细实施计划（使用 writing-plans 技能）
