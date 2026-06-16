# IDEA文件监听插件设计文档

## [S1] 问题描述

用户在修改构建文件（如pom.xml、build.gradle）后，需要手动执行相应的shell命令（如mvn install、gradle build）。这个过程重复且容易遗忘，影响开发效率。

## [S2] 解决方案概览

开发一个IntelliJ IDEA插件，监听构建文件的变化，当检测到修改时显示通知气泡，用户点击按钮即可执行预配置的shell命令。

## [S3] 核心组件

### 3.1 BuildFileListener

- 实现`VirtualFileListener`接口
- 监听文件的content和structure变化
- 过滤只监听构建文件（pom.xml、build.gradle、package.json等）
- 检测到变化时触发通知

### 3.2 CommandNotification

- 使用`NotificationGroup`显示通知
- 通知包含执行按钮
- 按钮点击后执行配置的shell命令

### 3.3 CommandSettings

- 实现`PersistentStateComponent`
- 存储文件类型到命令的映射
- 提供设置界面让用户配置

### 3.4 CommandExecutor

- 使用`ProcessBuilder`执行命令
- 在后台线程执行避免阻塞UI
- 执行结果输出到IDEA控制台

## [S4] 数据流

```
用户修改构建文件
    ↓
BuildFileListener检测到变化
    ↓
查找该文件类型对应的命令配置
    ↓
显示通知气泡（包含执行按钮）
    ↓
用户点击执行按钮
    ↓
CommandExecutor在后台执行命令
    ↓
执行结果输出到控制台
```

## [S5] 错误处理

### 5.1 文件监听错误

- 文件被删除时移除监听
- 文件重命名时重新绑定监听

### 5.2 命令执行错误

- 命令不存在时显示错误通知
- 执行超时时自动终止进程
- 执行失败时显示错误信息

### 5.3 配置错误

- 命令格式错误时提示用户
- 提供默认配置模板

## [S6] 测试策略

### 6.1 单元测试

- BuildFileListener的文件过滤逻辑
- CommandSettings的配置读写
- CommandExecutor的命令解析

### 6.2 集成测试

- 文件变化触发通知的完整流程
- 命令执行和结果输出

### 6.3 手动测试

- 在真实IDEA环境中测试
- 测试不同类型的构建文件
- 测试自定义命令配置

## [S7] 技术栈

- IntelliJ Platform SDK
- Kotlin（推荐）或Java
- Gradle（构建插件）
- JUnit 5（测试）

## [S8] 实现步骤

1. 创建插件项目结构
2. 实现BuildFileListener
3. 实现CommandNotification
4. 实现CommandSettings
5. 实现CommandExecutor
6. 编写单元测试
7. 编写集成测试
8. 手动测试
9. 打包发布
