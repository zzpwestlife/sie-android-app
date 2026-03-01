# SIE Exam Prep Android App

This is a comprehensive Android application designed for Securities Industry Essentials (SIE) exam preparation. It is built using modern Android development practices and technologies.

## Project Status
The app is fully refactored to Native Android using Kotlin and Jetpack Compose.

## Simplified Scope
Based on user request, complex features such as Leaderboard, Certificates, and advanced social sharing are intentionally omitted to focus on core study and exam functionalities.

## Architecture
The project follows a modern multi-module architecture to ensure separation of concerns and scalability:
- **:app**: The application entry point.
- **:core:***: Fundamental components (e.g., `:core:data`, `:core:database`, `:core:designsystem`, `:core:model`, `:core:common`, `:core:datastore`).
- **:feature:***: Feature-specific modules (e.g., `:feature:home`, `:feature:study`, `:feature:exam`, `:feature:stats`, `:feature:settings`).

## Features Implemented
- **Study Mode**: Interactive study session.
- **Exam Engine**: 75 questions with a countdown timer simulating the real exam.
- **Exam Review Mode**: Review past exam attempts and detailed explanations.
- **Stats**: User performance statistics.
- **Settings**: App configuration including Dark Mode.

## Build Instructions
To build the application, run:
```bash
./gradlew assembleDebug
```

## Packaging & Distribution (打包与分发)

### 1. 快速分享 (Quick Share - Debug APK)
最简单的分发方式，适合内部测试或发给朋友体验。
- **优点**: 无需签名配置，开箱即用。
- **缺点**: 性能稍低，无法上架应用商店，安装时需开启“未知来源”权限。

**步骤**:
1. 运行构建命令:
   ```bash
   ./gradlew assembleDebug
   ```
2. 找到生成的 APK 文件:
   `app/build/outputs/apk/debug/app-debug.apk`
3. 将该文件发送给对方即可安装。

### 2. 正式发布 (Production Release - Signed APK)
适合正式发布给用户，性能优化，且经过签名验证。
- **优点**: 经过代码混淆和优化，体积更小，运行更快，更安全。
- **缺点**: 需要生成密钥库 (Keystore) 并配置签名。

**推荐步骤 (使用 Android Studio)**:
1. 打开菜单栏: `Build` -> `Generate Signed Bundle / APK...`
2. 选择 `APK` (如果直接发给用户) 或 `Android App Bundle` (如果上传 Google Play)。
3. 点击 `Create new...` 创建一个新的密钥库 (Keystore)。
   - 记住设置的路径、密码和别名 (Alias)。
4. 填写密码，选择 `release` 构建变体。
5. 点击 `Finish` 等待构建完成。
6. 构建完成后，右下角会有提示，点击 `locate` 即可找到生成的 APK/AAB 文件。

**命令行方式 (高级用户)**:
需要在 `app/build.gradle.kts` 中配置 `signingConfigs`，然后运行:
```bash
./gradlew assembleRelease
```

## Test Instructions
To run the unit tests, execute:
```bash
./gradlew testDebugUnitTest
```

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM / Clean Architecture / Multi-module
- **Dependency Injection**: Hilt
- **Database**: Room
- **Concurrency**: Kotlin Coroutines & Flow
- **Build System**: Gradle (Kotlin DSL)
- **Minimum API**: 21
- **Target API**: 34

## Development Requirements

- **Unit Test Coverage**: ≥ 80%
- **UI Automation Tests**: Skipped (Simplified)
- **CI/CD**: Yes
- **Security**: ProGuard, API signing, GDPR compliance

## Deliverables

- Production-ready APK
- Source Code
- Technical Documentation
- Test Reports
- Operations Manual
- Product Whitepaper

## Deadline

2026-04-30
