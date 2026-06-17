# File Watcher

File Watcher 是一个 IntelliJ IDEA 插件，用于监听指定文件的创建或内容变更，并在 IDE 中弹出通知。通知中提供“执行命令”操作按钮，点击后会在配置的工作目录中执行对应的 shell 命令。

## 功能特性

- 监听 IntelliJ VFS 文件创建和内容变更事件。
- 按文件路径精确匹配配置的命令。
- 支持为每个文件单独配置命令和工作目录。
- 通过 IDE 气泡通知触发命令执行。
- 命令异步执行，stdout/stderr 输出到插件日志。

## 环境要求

- JDK 17
- Windows 环境（当前命令执行使用 `cmd /c`）
- IntelliJ IDEA Community 2023.3+
- Gradle Wrapper：仓库提供 `gradlew.bat`

## 快速开始

### 构建项目

```powershell
.\gradlew.bat build
```

### 运行测试

```powershell
.\gradlew.bat test
```

### 启动沙箱 IDE

```powershell
.\gradlew.bat runIde
```

### 构建插件 ZIP

```powershell
.\gradlew.bat buildPlugin
```

构建产物会生成在 `build/distributions/` 目录下。

## 使用方式

1. 安装并启动插件。
2. 在 IDE 中打开 `Settings/Preferences`。
3. 进入 `Tools` -> `File Watcher Commands`。
4. 添加需要监听的文件路径、执行命令和可选工作目录。
5. 当匹配的文件被创建或内容发生变化时，IDE 会显示通知。
6. 点击通知中的“执行命令”按钮运行配置的命令。

> 注意：文件路径匹配是精确匹配。插件会将反斜杠规范化为正斜杠，但不支持 glob、文件名模糊匹配或文件类型匹配。

## 项目结构

```text
src/main/kotlin/com/vw2/filewatcher/
  FileWatcherPlugin.kt        # 项目打开时注册 VFS 监听器
  BuildFileListener.kt        # 处理文件创建和内容变更事件
  CommandNotification.kt      # 显示 IDE 通知和执行按钮
  CommandExecutor.kt          # 异步执行命令
  CommandSettings.kt          # 持久化文件命令配置
  FileWatcherConfigurable.kt  # Settings/Preferences 配置页面
  FileWatcherLogger.kt        # 日志封装

src/main/resources/META-INF/plugin.xml
  # 插件 ID、服务、通知组和配置页面注册

src/test/kotlin/com/vw2/filewatcher/
  # JUnit 5 测试
```

## 常用命令

```powershell
# 编译、测试并构建插件
.\gradlew.bat build

# 运行全部测试
.\gradlew.bat test

# 运行单个测试类
.\gradlew.bat test --tests "com.vw2.filewatcher.CommandSettingsTest"

# 验证插件兼容性和元数据
.\gradlew.bat verifyPlugin
```

## 发布

签名和发布需要提前配置环境变量：

```powershell
$env:CERTIFICATE_CHAIN = "..."
$env:PRIVATE_KEY = "..."
$env:PRIVATE_KEY_PASSWORD = "..."
$env:PUBLISH_TOKEN = "..."
.\gradlew.bat signPlugin publishPlugin
```

## 技术说明

- Gradle project name: `idea-file-watcher`
- Plugin ID: `com.vw2.plugins.file-watcher`
- Kotlin: `1.9.22`
- JVM target: `17`
- IntelliJ Platform Gradle Plugin: `1.17.2`
- Target IDE: IntelliJ IDEA Community `2023.3`
- Compatibility: `233` 到 `261.*`

## 限制

- 命令执行当前固定使用 Windows `cmd /c`，暂不提供跨平台 shell 抽象。
- 文件匹配只支持精确路径匹配。
- 工作目录仅在配置路径存在时生效。
