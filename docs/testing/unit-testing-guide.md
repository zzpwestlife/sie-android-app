# 单元测试编写指南

本指南用于 SIE Android App 的 ViewModel 层单元测试开发。

## 测试结构

所有 ViewModel 测试遵循 **AAA 模式**：

### Arrange（准备）
- 创建 Mock Repository
- 准备测试数据
- 初始化 ViewModel
- 配置 Stub 行为

### Act（执行）
- 调用 ViewModel 方法
- 推进协程时间（如需要）
- 触发状态变化

### Assert（断言）
- 验证 UI 状态
- 验证 Repository 调用
- 检查副作用

## 协程测试

### MainDispatcherRule 设置

在测试类中添加 `MainDispatcherRule` 以替换主线程调度器：

```kotlin
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description

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

@OptIn(ExperimentalCoroutinesApi::class)
class MyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: MyViewModel
    private val repository: MyRepository = mockk()
}
```

### 使用 runTest

所有测试方法使用 `runTest {}` 包装：

```kotlin
@Test
fun myTest() = runTest {
    // Arrange
    coEvery { repository.getData() } returns flowOf(data)
    viewModel = MyViewModel(repository)

    // 推进协程执行
    mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    assertEquals(expected, viewModel.uiState.value)
}
```

### 协程时间控制

根据场景选择合适的时间控制方法：

#### runCurrent() - 执行立即协程

用于执行当前调度的立即协程，**不包括** `delay()` 或计时器：

```kotlin
// 推荐用于有计时器的 ViewModel
viewModel.startExam()
mainDispatcherRule.testDispatcher.scheduler.runCurrent()
// 计时器不会被触发
```

**使用场景**:
- ExamViewModel 测试（有 30 分钟计时器）
- 避免触发后台计时器
- 仅验证初始状态

#### advanceUntilIdle() - 执行所有协程

执行**所有**挂起的协程，包括 `delay()` 和计时器：

```kotlin
viewModel.loadData()
mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
// 所有协程都会执行完成
```

**使用场景**:
- 无计时器的 ViewModel
- 需要等待所有协程完成
- 简单的异步操作测试

**注意**: 对于有循环 `delay()` 的代码（如 ExamViewModel 计时器），`advanceUntilIdle()` 会导致计时器立即归零并自动提交考试。

#### advanceTimeBy(millis) - 手动推进时间

手动推进虚拟时间，触发定时任务：

```kotlin
// 测试 5 秒超时
mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(5100L)
mainDispatcherRule.testDispatcher.scheduler.runCurrent()
```

**使用场景**:
- 测试超时行为
- 验证延迟执行的逻辑
- 时间相关的状态变化

## MockK 使用

### 创建 Mock

```kotlin
import io.mockk.mockk
import io.mockk.relaxed

// 标准 Mock（必须 stub 所有方法）
private val repository: MyRepository = mockk()

// Relaxed Mock（未 stub 的方法返回默认值）
private val repository: MyRepository = mockk(relaxed = true)
```

### Stub 协程方法

```kotlin
import io.mockk.coEvery

// Stub Flow 返回值
coEvery { repository.getData() } returns flowOf(data)

// Stub List 返回值
coEvery { repository.getList() } returns listOf(item1, item2)

// Stub 抛出异常
coEvery { repository.getData() } throws RuntimeException("Error")

// 条件 Stub
coEvery { repository.getById(1) } returns data1
coEvery { repository.getById(2) } returns data2
```

### Stub 普通方法

```kotlin
import io.mockk.every

// Stub Flow 属性
every { userDataRepository.userData } returns flowOf(userData)
```

### 验证方法调用

```kotlin
import io.mockk.coVerify

// 验证方法被调用
coVerify { repository.saveData(any()) }

// 验证方法被调用特定次数
coVerify(exactly = 1) { repository.saveData(any()) }

// 验证方法未被调用
coVerify(exactly = 0) { repository.saveData(any()) }

// 验证方法调用参数
coVerify { repository.saveData(match { it.id == 123 }) }
```

### 清理 Mock

```kotlin
import io.mockk.clearAllMocks
import org.junit.Before

@Before
fun setup() {
    clearAllMocks()
}
```

## 测试模式

### 测试 ViewModel 初始化

```kotlin
@Test
fun viewModelInitialization_setsCorrectInitialState() = runTest {
    // Arrange
    coEvery { repository.getData() } returns flowOf(data)

    // Act
    viewModel = MyViewModel(repository)
    mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.value
    assertTrue("State should be Success", state is UiState.Success)
}
```

### 测试状态更新

```kotlin
@Test
fun updateSetting_updatesState() = runTest {
    // Arrange
    every { repository.userData } returns flowOf(initialData)
    viewModel = MyViewModel(repository)
    mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

    // Act
    viewModel.updateSetting(newValue)
    mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.value
    assertEquals(newValue, state.setting)
    coVerify { repository.saveSetting(newValue) }
}
```

### 测试异常处理

```kotlin
@Test
fun loadData_handlesRepositoryException() = runTest {
    // Arrange
    coEvery { repository.getData() } throws RuntimeException("Database error")

    // Act
    viewModel = MyViewModel(repository)
    mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.value
    assertTrue("State should be Error", state is UiState.Error)
    assertTrue((state as UiState.Error).message.contains("Database error"))
}
```

### 测试空数据

```kotlin
@Test
fun loadData_withEmptyList_showsEmptyState() = runTest {
    // Arrange
    coEvery { repository.getList() } returns flowOf(emptyList())

    // Act
    viewModel = MyViewModel(repository)
    mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.value
    assertTrue("State should be Empty", state is UiState.Empty)
}
```

### 测试超时

```kotlin
@Test
fun loadData_withTimeout_showsError() = runTest {
    // Arrange
    coEvery { repository.getData() } returns flowOf(emptyList())

    // Act
    viewModel = MyViewModel(repository)
    // 推进时间超过 timeout 阈值
    mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(5100L)
    mainDispatcherRule.testDispatcher.scheduler.runCurrent()

    // Assert
    val state = viewModel.uiState.value
    assertTrue("State should be Error", state is UiState.Error)
}
```

## 运行测试

### 单个模块所有测试

```bash
./gradlew :feature:study:testDebugUnitTest
```

### 单个测试类

```bash
./gradlew :feature:study:testDebugUnitTest --tests StudyViewModelTest
```

### 单个测试方法（通配符）

```bash
./gradlew :feature:study:testDebugUnitTest --tests StudyViewModelTest.loadQuestions*
```

### 所有模块测试

```bash
./gradlew testDebugUnitTest
```

### 生成覆盖率报告

```bash
./gradlew clean testDebugUnitTest jacocoTestReport
open build/reports/jacoco/jacocoTestReport/html/index.html
```

## 质量标准

### 测试覆盖率目标

| 层级 | 目标覆盖率 |
|------|-----------|
| ViewModel 层 | ≥ 80% |
| 单个 ViewModel | ≥ 70% |
| Repository 层 | 暂不要求 |
| UI Composables | 暂不要求 |

### 测试数量要求

每个 ViewModel 至少包含：
- ✅ 初始化测试（1 个）
- ✅ 正常流程测试（2-3 个）
- ✅ 异常流程测试（1-2 个）
- ✅ 边界条件测试（1-2 个）

**总计**: 每个 ViewModel 5-8 个测试

### 测试命名规范

使用反引号支持自然语言描述：

```kotlin
@Test
fun `loadNewQuestion emits Loading then Success with question`() = runTest { }

@Test
fun `selectOption with correct answer updates state and reveals answer`() = runTest { }
```

或使用 Snake Case（推荐用于 CI/CD 报告）：

```kotlin
@Test
fun loadNewQuestion_emitsSuccessWithQuestion() = runTest { }

@Test
fun selectOption_withCorrectAnswer_updatesState() = runTest { }
```

## 常见问题

### 问题 1: Test timeout after 120 seconds

**原因**: 协程未完成，测试一直等待

**解决方案**:
1. 确保调用了 `advanceUntilIdle()` 或 `runCurrent()`
2. 检查是否有无限循环的 `delay()`
3. 对于计时器场景，使用 `runCurrent()` 而非 `advanceUntilIdle()`

### 问题 2: StateFlow 一直是初始值

**原因**: 协程未执行，状态未更新

**解决方案**:
```kotlin
viewModel = MyViewModel(repository)
// 必须推进协程
mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
// 然后才能断言状态
val state = viewModel.uiState.value
```

### 问题 3: MockK "no answer found" 错误

**原因**: Repository 方法未 stub

**解决方案**:
```kotlin
// 确保所有会被调用的方法都 stub 了
coEvery { repository.method1() } returns result1
coEvery { repository.method2() } returns result2

// 或使用 relaxed mock
private val repository = mockk<MyRepository>(relaxed = true)
```

### 问题 4: JVM target 不兼容错误

**原因**: 模块 JVM 版本不一致

**解决方案**:
在 `build.gradle.kts` 中统一设置为 Java 11：
```kotlin
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlinOptions {
    jvmTarget = "11"
}
```

### 问题 5: ExamViewModel 测试自动完成考试

**原因**: `advanceUntilIdle()` 触发了 30 分钟计时器

**解决方案**:
```kotlin
viewModel.startExam()
// 使用 runCurrent() 而非 advanceUntilIdle()
mainDispatcherRule.testDispatcher.scheduler.runCurrent()
```

## 参考资源

- [Kotlin Coroutines Test](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)
- [MockK Documentation](https://mockk.io/)
- [JUnit 4 Documentation](https://junit.org/junit4/)
- [Android Testing Codelab](https://developer.android.com/codelabs/advanced-android-kotlin-training-testing-basics)

## 团队约定

1. **测试优先**: 新 ViewModel 功能开发时，先写测试
2. **测试覆盖**: PR 前运行 `./gradlew jacocoTestReport` 检查覆盖率
3. **CI 集成**: 所有 PR 必须通过 CI 测试才能合并
4. **代码审查**: 测试代码也需要 Code Review
5. **持续改进**: 定期回顾测试质量，更新本指南
