# 单元测试架构设计方案

**日期**: 2026-02-28
**目标**: 为 SIE Android App 补充单元测试，提升测试覆盖率至 ≥ 80%
**策略**: 渐进式 ViewModel 层单元测试 + JaCoCo 覆盖率验证

---

## 1. 项目现状分析

### 1.1 架构概览
- **多模块架构**: 6 个 core 模块 + 5 个 feature 模块
- **技术栈**: Kotlin, Jetpack Compose, Hilt, Room, Coroutines
- **目标覆盖率**: ≥ 80%（来自 README.md）

### 1.2 当前测试状态
- ✅ **已有测试**: `feature:exam/ExamViewModelTest.kt`（1 个测试用例）
- ❌ **缺失测试**: 其他 4 个 feature 模块、所有 core 模块
- ❌ **无 UI 测试**: README 明确 "UI Automation Tests: Skipped"

---

## 2. 测试策略

### 2.1 核心原则
1. **ViewModel 层优先**: 业务逻辑集中，性价比最高
2. **Repository 层不测**: 通过 ViewModel 测试间接验证
3. **无 UI 测试**: 遵循项目简化策略
4. **渐进式交付**: 分 3 个 Phase 迭代，每轮验证

### 2.2 测试覆盖范围

| 模块 | 待测试组件 | 优先级 | 预估测试数量 |
|------|-----------|--------|-------------|
| `feature:exam` | `ExamViewModel` | ✅ P0（已有） | 1 → 6 |
| `feature:study` | `StudyViewModel` | 🔥 P0 | 5 |
| `feature:stats` | `StatsViewModel` | 🔥 P0 | 4 |
| `feature:settings` | `SettingsViewModel` | 🔥 P0 | 3 |
| `feature:home` | `HomeViewModel` | ⚡ P1 | 3 |

**预估总测试用例数**: 21 个

### 2.3 依赖管理

**测试依赖** (统一添加至各 feature 模块):
```kotlin
dependencies {
    testImplementation(libs.junit)              // 4.13.2
    testImplementation(libs.mockk)              // 1.13.8
    testImplementation(libs.kotlinx.coroutines.test) // 1.7.3
}
```

**Mock 策略**:
- Repository 接口 → `mockk<Repository>()`
- 协程函数 → `coEvery { ... } returns flowOf(...)`
- Timer/Clock → `mockk(relaxed = true)`

---

## 3. 测试基础设施

### 3.1 协程测试规则

**`MainDispatcherRule.kt`** (各模块 `test/` 目录):
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

### 3.2 典型测试结构

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class StudyViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: StudyViewModel
    private val questionRepository: QuestionRepository = mockk()

    @Before
    fun setup() {
        clearAllMocks()
    }

    @Test
    fun `loadQuestions emits Loading then Success`() = runTest {
        // Arrange
        val questions = listOf(mockQuestion1, mockQuestion2)
        coEvery { questionRepository.getQuestionsByCategory("topic1") }
            returns flowOf(questions)

        // Act
        viewModel = StudyViewModel(questionRepository)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(StudyUiState.Success(questions), viewModel.uiState.value)
    }

    @Test
    fun `selectAnswer updates state correctly`() = runTest {
        // ... 测试答案选择逻辑
    }
}
```

---

## 4. 分阶段实施计划

### 🔄 Phase 1: 核心 ViewModel 测试（3-4 天）

**目标**: 为 4 个核心 feature 模块补充 ViewModel 测试

#### feature:exam - ExamViewModel
**现有测试**:
- ✅ `submitExam_calculatesScoreAndSavesResult`

**新增测试**:
1. `startExam_loads75Questions`
2. `onAnswerSelected_updatesUserAnswers`
3. `submitExam_withTimeout_showsWarning`
4. `pauseExam_savesProgress`
5. `handleError_whenRepositoryFails`

#### feature:study - StudyViewModel
**测试场景**:
1. `loadQuestions_emitsLoadingThenSuccess`
2. `selectAnswer_correctAnswer_updatesState`
3. `selectAnswer_wrongAnswer_showsExplanation`
4. `nextQuestion_incrementsIndex`
5. `handleError_whenNetworkFails`

#### feature:stats - StatsViewModel
**测试场景**:
1. `loadStats_emitsUserPerformanceData`
2. `filterByCategory_updatesFilteredStats`
3. `handleEmptyData_showsEmptyState`
4. `calculateTrend_returnsCorrectPercentage`

#### feature:settings - SettingsViewModel
**测试场景**:
1. `toggleTheme_updatesDarkThemeConfig`
2. `savePreferences_persistsToDataStore`
3. `clearData_resetsAllSettings`

**验收标准**:
- ✅ 所有 P0 模块测试通过
- ✅ 每个 ViewModel 至少 5 个测试用例
- ✅ 覆盖正常流程 + 异常流程

---

### 🔄 Phase 2: JaCoCo 集成与覆盖率验证（1 天）

**目标**: 配置测试覆盖率工具，生成报告

#### 2.1 根 `build.gradle.kts` 配置

```kotlin
plugins {
    id("jacoco")
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.register("jacocoTestReport", JacocoReport::class) {
    dependsOn(subprojects.map { it.tasks.named("testDebugUnitTest") })

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    sourceDirectories.setFrom(files(subprojects.flatMap {
        listOf("${it.projectDir}/src/main/java")
    }))

    classDirectories.setFrom(files(subprojects.flatMap {
        listOf("${it.buildDir}/tmp/kotlin-classes/debug")
    }))

    executionData.setFrom(files(subprojects.flatMap {
        listOf("${it.buildDir}/jacoco/testDebugUnitTest.exec")
    }))
}
```

#### 2.2 各 feature 模块 `build.gradle.kts`

```kotlin
android {
    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
    }
}
```

#### 2.3 执行覆盖率分析

```bash
# 运行测试并生成报告
./gradlew testDebugUnitTest jacocoTestReport

# 查看报告
open build/reports/jacoco/jacocoTestReport/html/index.html
```

**覆盖率目标**:
- ViewModel 层: **≥ 90%**
- 整体项目: **≥ 80%**

---

### 🔄 Phase 3: 补充测试与优化（1-2 天）

**目标**: 根据 JaCoCo 报告查漏补缺

#### 3.1 补充 feature:home 测试（P1）
```kotlin
class HomeViewModelTest {
    @Test
    fun `loadDashboard_showsRecentStats`() { ... }

    @Test
    fun `navigateToStudy_triggersNavigation`() { ... }

    @Test
    fun `loadProgress_calculatesCompletionRate`() { ... }
}
```

#### 3.2 增强边界条件测试
- 并发场景（多次快速点击）
- 大数据量（1000+ 题目）
- 异常恢复（网络断开后重连）

#### 3.3 文档编写
- `docs/testing/unit-testing-guide.md`（测试编写指南）
- `docs/testing/coverage-report.md`（覆盖率分析）

#### 3.4 CI/CD 集成
**`.github/workflows/test.yml`**:
```yaml
name: Unit Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '11'
      - name: Run Tests
        run: ./gradlew testDebugUnitTest
      - name: Generate Coverage Report
        run: ./gradlew jacocoTestReport
      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v3
```

---

## 5. 验证与交付标准

### 5.1 质量门禁

| 检查项 | 标准 | 工具 |
|--------|------|------|
| **测试通过率** | 100% | Gradle |
| **ViewModel 覆盖率** | ≥ 90% | JaCoCo |
| **整体覆盖率** | ≥ 80% | JaCoCo |
| **测试执行时间** | < 30s | Gradle |
| **无跳过测试** | 0 个 `@Ignore` | 代码审查 |

### 5.2 交付物清单

1. **测试代码** (21+ 个测试用例)
   - `feature/exam/src/test/java/.../ExamViewModelTest.kt`
   - `feature/study/src/test/java/.../StudyViewModelTest.kt`
   - `feature/stats/src/test/java/.../StatsViewModelTest.kt`
   - `feature/settings/src/test/java/.../SettingsViewModelTest.kt`
   - `feature/home/src/test/java/.../HomeViewModelTest.kt`

2. **配置文件**
   - 根 `build.gradle.kts` (JaCoCo 配置)
   - 各模块 `build.gradle.kts` (测试依赖)

3. **文档**
   - `docs/testing/unit-testing-guide.md`
   - `docs/testing/coverage-report.md`

4. **CI/CD 集成**
   - `.github/workflows/test.yml`

### 5.3 验证流程

每个 Phase 结束后必须执行:

```bash
# 1. 运行所有单元测试
./gradlew testDebugUnitTest

# 2. 生成覆盖率报告
./gradlew jacocoTestReport

# 3. 查看报告
open build/reports/jacoco/jacocoTestReport/html/index.html
```

---

## 6. 风险管理

### 6.1 风险识别与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| **Mock 场景不真实** | 测试通过但线上有 bug | 定期与实际 Repository 行为对比 |
| **协程测试不稳定** | 偶现失败 | 统一使用 `advanceUntilIdle()` |
| **覆盖率未达标** | 需返工 | Phase 2 提前验证，及时调整 |
| **测试执行缓慢** | 开发体验差 | 避免 Thread.sleep，使用测试调度器 |

### 6.2 时间估算

| Phase | 工作量 | 预计时间 |
|-------|-------|---------|
| Phase 1: 核心 ViewModel 测试 | 编写 18 个测试用例 | 3-4 天 |
| Phase 2: JaCoCo 集成与验证 | 配置 + 报告分析 | 1 天 |
| Phase 3: 补充测试与优化 | 查漏补缺 + 文档 | 1-2 天 |
| **总计** | - | **5-7 天** |

---

## 7. 测试架构总览

```
┌─────────────────────────────────────────────────────────────┐
│                    Android App (SIE)                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   feature:   │  │   feature:   │  │   feature:   │     │
│  │     exam     │  │    study     │  │    stats     │     │
│  │              │  │              │  │              │     │
│  │ ViewModel ✅ │  │ ViewModel 🔨 │  │ ViewModel 🔨 │     │
│  │   (已有测试) │  │  (待补充)    │  │  (待补充)    │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐                       │
│  │   feature:   │  │   feature:   │                       │
│  │  settings    │  │     home     │                       │
│  │              │  │              │                       │
│  │ ViewModel 🔨 │  │ ViewModel ⚡ │                       │
│  │  (待补充)    │  │  (P1优先级)  │                       │
│  └──────────────┘  └──────────────┘                       │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                      core:data                              │
│              ┌─────────────────────────┐                    │
│              │  Repository Interfaces  │                    │
│              │  (MockK 全部 mock)      │                    │
│              └─────────────────────────┘                    │
├─────────────────────────────────────────────────────────────┤
│                  测试基础设施 (Shared)                        │
│  • MainDispatcherRule (协程测试规则)                         │
│  • MockK (Mock 框架)                                        │
│  • JUnit 4 (断言框架)                                        │
│  • JaCoCo (覆盖率工具)                                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 8. 参考资料

- [Android Testing Guide](https://developer.android.com/training/testing)
- [MockK Documentation](https://mockk.io/)
- [Kotlin Coroutines Testing](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)
- [JaCoCo Plugin for Gradle](https://docs.gradle.org/current/userguide/jacoco_plugin.html)

---

**审批状态**: ✅ 已批准
**下一步**: 调用 `writing-plans` skill 创建详细实施计划
