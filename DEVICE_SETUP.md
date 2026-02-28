# 设备安装与调试指南 (Redmi 12C / HyperOS)

本文档详细记录了在 **Redmi 12C** (运行 **HyperOS 1.0.8.0**) 上安装和调试 SIE Exam Prep 应用的步骤。小米/Redmi 设备具有特殊的安全机制，请务必按照以下步骤操作。

## 1. 开启开发者选项

1.  打开手机 **设置 (Settings)**。
2.  进入 **我的设备 (My Device)** -> **全部参数 (Detailed info and specs)**。
3.  连续点击 **“OS 版本”** (或 **MIUI 版本**) 7次，直到屏幕下方提示 “您已处于开发者模式” (You are now a developer)。

## 2. 开启 USB 调试与安全设置 (关键)

1.  回到 **设置** -> **更多设置 (Additional settings)** -> **开发者选项 (Developer options)**。
2.  找到并开启以下开关：
    *   **USB 调试 (USB debugging)**：允许通过 USB 进行调试。
    *   **USB 安装 (Install via USB)**：**必须开启**。允许通过 USB 安装应用。
        *   *注意：开启此选项通常需要插入 SIM 卡并登录小米账号。*
    *   **USB 调试（安全设置）(USB debugging (Security settings))**：建议开启。允许通过 USB 模拟点击（对 UI 测试有用）。

## 3. 连接电脑

1.  使用 USB 数据线将手机连接到 Mac。
2.  在手机通知栏下拉，点击 “正在通过 USB 充电”，选择 **“文件传输” (File Transfer / Android Auto)** 模式。**不要**选择“仅充电”。
3.  手机屏幕可能会弹出 **“允许 USB 调试吗？”** 的对话框。
    *   勾选 **“一律允许使用这台计算机进行调试”**。
    *   点击 **“允许” (OK)**。

## 4. 检查连接

在 Mac 的终端 (Terminal) 中运行以下命令：

```bash
adb devices
```

**预期输出：**
```
List of devices attached
<设备序列号>    device
```
如果显示 `unauthorized`，请在手机上确认授权弹窗。

## 5. 安装与调试

### 安装应用

在项目根目录下运行：

```bash
./gradlew installDebug
```

**⚠️ 重要提示：**
执行命令后，请**保持手机屏幕常亮并注视屏幕**。HyperOS 的安全中心可能会弹出一个全屏警告（如“正在通过 USB 安装未知应用”）。
*   您必须在倒计时结束前手动点击 **“允许”** 或 **“继续安装”**。
*   如果错过点击，安装将失败并报错 `INSTALL_FAILED_USER_RESTRICTED`。

### 查看日志

安装成功并打开应用后，使用以下命令查看实时日志：

```bash
# 过滤 SieApp 的日志、运行时错误和崩溃信息
adb logcat -s "SieApp" "AndroidRuntime" "*:E"
```

## 6. 常见问题排查

*   **报错 `INSTALL_FAILED_USER_RESTRICTED`**：
    *   原因：未开启“USB 安装”开关，或未在手机上点击“允许安装”。
    *   解决：检查开发者选项中的“USB 安装”是否已开启；重新运行安装命令并留意手机弹窗。
*   **报错 `INSTALL_FAILED_UPDATE_INCOMPATIBLE`**：
    *   原因：手机上已安装了签名不一致的旧版本。
    *   解决：在手机上手动卸载旧版 "SIE Exam Prep"，然后重新运行安装命令。
