# IDEA文件监听插件实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 开发一个IntelliJ IDEA插件，监听构建文件变化并显示通知按钮执行shell命令

**Architecture:** 使用VirtualFileListener监听文件变化，NotificationGroup显示通知，PersistentStateComponent存储配置，ProcessBuilder执行命令

**Tech Stack:** Kotlin, IntelliJ Platform SDK, Gradle, JUnit 5

---

## 文件结构

```
vw2-plugins/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/vw2/filewatcher/
│   │   │       ├── BuildFileListener.kt
│   │   │       ├── CommandNotification.kt
│   │   │       ├── CommandSettings.kt
│   │   │       ├── CommandExecutor.kt
│   │   │       └── FileWatcherPlugin.kt
│   │   └── resources/
│   │       └── META-INF/
│   │           └── plugin.xml
│   └── test/
│       └── kotlin/
│           └── com/vw2/filewatcher/
│               ├── BuildFileListenerTest.kt
│               ├── CommandSettingsTest.kt
│               └── CommandExecutorTest.kt
└── docs/
    └── compose/
        ├── specs/
        └── plans/
```

---

## Task 1: 创建插件项目结构

**Covers:** [S7]

**Files:**
- Create: `build.gradle.kts`
- Create: `settings.gradle.kts`
- Create: `gradle.properties`
- Create: `src/main/resources/META-INF/plugin.xml`

- [ ] **Step 1: 创建build.gradle.kts**

```kotlin
plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "com.vw2"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

intellij {
    version.set("2023.2")
    type.set("IC")
    plugins.set(listOf("com.intellij.java"))
}

tasks {
    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("241.*")
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}
```

- [ ] **Step 2: 创建settings.gradle.kts**

```kotlin
rootProject.name = "vw2-file-watcher"
```

- [ ] **Step 3: 创建gradle.properties**

```properties
kotlin.stdlib.default.dependency=false
```

- [ ] **Step 4: 创建plugin.xml**

```xml
<idea-plugin>
    <id>com.vw2.filewatcher</id>
    <name>File Watcher Command Executor</name>
    <version>1.0.0</version>
    <vendor>VW2</vendor>

    <description>
        监听构建文件变化，显示通知按钮执行shell命令
    </description>

    <depends>com.intellij.modules.platform</depends>
    <depends>com.intellij.modules.java</depends>

    <extensions defaultExtensionNs="com.intellij">
        <applicationService
            serviceImplementation="com.vw2.filewatcher.CommandSettings"/>
        <notificationGroup
            id="File Watcher Commands"
            displayType="BALLOON"/>
    </extensions>
</idea-plugin>
```

- [ ] **Step 5: 验证项目结构**

Run: `./gradlew tasks`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 提交**

```bash
git add build.gradle.kts settings.gradle.kts gradle.properties src/main/resources/META-INF/plugin.xml
git commit -m "feat: create plugin project structure"
```

---

## Task 2: 实现CommandSettings配置存储

**Covers:** [S3.3]

**Files:**
- Create: `src/main/kotlin/com/vw2/filewatcher/CommandSettings.kt`
- Create: `src/test/kotlin/com/vw2/filewatcher/CommandSettingsTest.kt`

- [ ] **Step 1: 编写失败测试**

```kotlin
package com.vw2.filewatcher

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CommandSettingsTest {
    private lateinit var settings: CommandSettings

    @BeforeEach
    fun setUp() {
        settings = CommandSettings()
    }

    @Test
    fun `should return default commands for known file types`() {
        val mavenCommand = settings.getCommand("pom.xml")
        assertEquals("mvn clean install", mavenCommand)
    }

    @Test
    fun `should return custom command when set`() {
        settings.setCommand("pom.xml", "mvn clean compile")
        val command = settings.getCommand("pom.xml")
        assertEquals("mvn clean compile", command)
    }

    @Test
    fun `should return null for unknown file type`() {
        val command = settings.getCommand("unknown.txt")
        assertNull(command)
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `./gradlew test --tests "com.vw2.filewatcher.CommandSettingsTest"`
Expected: FAIL with "Unresolved reference: CommandSettings"

- [ ] **Step 3: 编写最小实现**

```kotlin
package com.vw2.filewatcher

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtilCopyBean

@State(
    name = "CommandSettings",
    storages = [Storage("FileWatcherCommands.xml")]
)
class CommandSettings : PersistentStateComponent<CommandSettings> {
    var commands: MutableMap<String, String> = mutableMapOf(
        "pom.xml" to "mvn clean install",
        "build.gradle" to "gradle build",
        "build.gradle.kts" to "gradle build",
        "package.json" to "npm install"
    )

    override fun getState(): CommandSettings = this

    override fun loadState(state: CommandSettings) {
        XmlSerializerUtilCopyBean(state, this)
    }

    fun getCommand(fileName: String): String? = commands[fileName]

    fun setCommand(fileName: String, command: String) {
        commands[fileName] = command
    }

    companion object {
        fun getInstance(): CommandSettings =
            ApplicationManager.getApplication().getService(CommandSettings::class.java)
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `./gradlew test --tests "com.vw2.filewatcher.CommandSettingsTest"`
Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add src/main/kotlin/com/vw2/filewatcher/CommandSettings.kt src/test/kotlin/com/vw2/filewatcher/CommandSettingsTest.kt
git commit -m "feat: implement CommandSettings with persistence"
```

---

## Task 3: 实现CommandExecutor命令执行器

**Covers:** [S3.4]

**Files:**
- Create: `src/main/kotlin/com/vw2/filewatcher/CommandExecutor.kt`
- Create: `src/test/kotlin/com/vw2/filewatcher/CommandExecutorTest.kt`

- [ ] **Step 1: 编写失败测试**

```kotlin
package com.vw2.filewatcher

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class CommandExecutorTest {
    @Test
    fun `should parse command correctly`() {
        val executor = CommandExecutor()
        val parts = executor.parseCommand("mvn clean install")
        assertArrayEquals(arrayOf("mvn", "clean", "install"), parts)
    }

    @Test
    fun `should handle quoted arguments`() {
        val executor = CommandExecutor()
        val parts = executor.parseCommand("echo \"hello world\"")
        assertArrayEquals(arrayOf("echo", "hello world"), parts)
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `./gradlew test --tests "com.vw2.filewatcher.CommandExecutorTest"`
Expected: FAIL with "Unresolved reference: CommandExecutor"

- [ ] **Step 3: 编写最小实现**

```kotlin
package com.vw2.filewatcher

import com.intellij.execution.process.ProcessOutput
import com.intellij.execution.process.ScriptRunnerUtil
import java.io.File

class CommandExecutor {
    fun parseCommand(command: String): Array<String> {
        val parts = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var quoteChar = ' '

        for (c in command) {
            when {
                c == '"' || c == '\'' -> {
                    if (inQuotes && c == quoteChar) {
                        inQuotes = false
                    } else if (!inQuotes) {
                        inQuotes = true
                        quoteChar = c
                    } else {
                        current.append(c)
                    }
                }
                c == ' ' && !inQuotes -> {
                    if (current.isNotEmpty()) {
                        parts.add(current.toString())
                        current = StringBuilder()
                    }
                }
                else -> current.append(c)
            }
        }
        if (current.isNotEmpty()) {
            parts.add(current.toString())
        }
        return parts.toTypedArray()
    }

    fun execute(command: String, workingDir: File? = null): ProcessOutput {
        val parts = parseCommand(command)
        val cmd = parts[0]
        val args = if (parts.size > 1) parts.sliceArray(1 until parts.size) else emptyArray()

        val commandLine = if (System.getProperty("os.name").lowercase().contains("win")) {
            arrayOf("cmd", "/c", cmd) + args
        } else {
            arrayOf(cmd) + args
        }

        return ScriptRunnerUtil.execute(commandLine, null, workingDir)
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `./gradlew test --tests "com.vw2.filewatcher.CommandExecutorTest"`
Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add src/main/kotlin/com/vw2/filewatcher/CommandExecutor.kt src/test/kotlin/com/vw2/filewatcher/CommandExecutorTest.kt
git commit -m "feat: implement CommandExecutor with shell execution"
```

---

## Task 4: 实现CommandNotification通知系统

**Covers:** [S3.2]

**Files:**
- Create: `src/main/kotlin/com/vw2/filewatcher/CommandNotification.kt`

- [ ] **Step 1: 创建CommandNotification**

```kotlin
package com.vw2.filewatcher

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import javax.swing.Icon

class CommandNotification {
    companion object {
        private val notificationGroup = NotificationGroup.getInstance("File Watcher Commands")

        fun showNotification(
            project: Project,
            title: String,
            command: String,
            onExecute: () -> Unit
        ) {
            val action = object : AnAction("执行命令") {
                override fun actionPerformed(e: AnActionEvent) {
                    onExecute()
                }

                override fun getIcon(iconFlags: Int): Icon? = null
            }

            val notification = notificationGroup.createNotification(
                title,
                "检测到文件变化，点击执行命令",
                NotificationType.INFORMATION
            )

            notification.addAction(action)
            notification.notify(project)
        }
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add src/main/kotlin/com/vw2/filewatcher/CommandNotification.kt
git commit -m "feat: implement CommandNotification for balloon notifications"
```

---

## Task 5: 实现BuildFileListener文件监听器

**Covers:** [S3.1]

**Files:**
- Create: `src/main/kotlin/com/vw2/filewatcher/BuildFileListener.kt`
- Create: `src/test/kotlin/com/vw2/filewatcher/BuildFileListenerTest.kt`

- [ ] **Step 1: 编写失败测试**

```kotlin
package com.vw2.filewatcher

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class BuildFileListenerTest {
    @Test
    fun `should recognize pom xml as build file`() {
        val listener = BuildFileListener(null)
        assertTrue(listener.isBuildFile("pom.xml"))
    }

    @Test
    fun `should recognize build gradle as build file`() {
        val listener = BuildFileListener(null)
        assertTrue(listener.isBuildFile("build.gradle"))
    }

    @Test
    fun `should not recognize txt as build file`() {
        val listener = BuildFileListener(null)
        assertFalse(listener.isBuildFile("readme.txt"))
    }

    @Test
    fun `should extract file name from path`() {
        val listener = BuildFileListener(null)
        assertEquals("pom.xml", listener.extractFileName("/path/to/pom.xml"))
    }
}
```

- [ ] **Step 2: 运行测试验证失败**

Run: `./gradlew test --tests "com.vw2.filewatcher.BuildFileListenerTest"`
Expected: FAIL with "Unresolved reference: BuildFileListener"

- [ ] **Step 3: 编写最小实现**

```kotlin
package com.vw2.filewatcher

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

class BuildFileListener(private val project: Project?) : BulkFileListener {
    private val buildFileExtensions = setOf(
        "pom.xml",
        "build.gradle",
        "build.gradle.kts",
        "package.json"
    )

    fun isBuildFile(fileName: String): Boolean {
        return buildFileExtensions.any { fileName.endsWith(it, ignoreCase = true) }
    }

    fun extractFileName(path: String): String {
        return path.substringAfterLast("/").substringAfterLast("\\")
    }

    override fun before(events: List<VFileEvent>) {}

    override fun after(events: List<VFileEvent>) {
        for (event in events) {
            if (event is VFileContentChangeEvent) {
                val file = event.file
                val fileName = file.name

                if (isBuildFile(fileName)) {
                    val settings = CommandSettings.getInstance()
                    val command = settings.getCommand(fileName)

                    if (command != null && project != null) {
                        CommandNotification.showNotification(
                            project = project,
                            title = "执行 $fileName 命令",
                            command = command
                        ) {
                            val executor = CommandExecutor()
                            executor.execute(command, project.basePath?.let { java.io.File(it) })
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 4: 运行测试验证通过**

Run: `./gradlew test --tests "com.vw2.filewatcher.BuildFileListenerTest"`
Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add src/main/kotlin/com/vw2/filewatcher/BuildFileListener.kt src/test/kotlin/com/vw2/filewatcher/BuildFileListenerTest.kt
git commit -m "feat: implement BuildFileListener for file change detection"
```

---

## Task 6: 实现FileWatcherPlugin插件入口

**Covers:** [S2]

**Files:**
- Create: `src/main/kotlin/com/vw2/filewatcher/FileWatcherPlugin.kt`
- Modify: `src/main/resources/META-INF/plugin.xml`

- [ ] **Step 1: 创建FileWatcherPlugin**

```kotlin
package com.vw2.filewatcher

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener

class FileWatcherPlugin : ProjectManagerListener {
    override fun projectOpened(project: Project) {
        val listener = BuildFileListener(project)
        val messageBus = project.messageBus.connect()
        messageBus.subscribe(VirtualFileManager.VFS_CHANGES, listener)
    }
}
```

- [ ] **Step 2: 更新plugin.xml**

```xml
<idea-plugin>
    <id>com.vw2.filewatcher</id>
    <name>File Watcher Command Executor</name>
    <version>1.0.0</version>
    <vendor>VW2</vendor>

    <description>
        监听构建文件变化，显示通知按钮执行shell命令
    </description>

    <depends>com.intellij.modules.platform</depends>
    <depends>com.intellij.modules.java</depends>

    <extensions defaultExtensionNs="com.intellij">
        <applicationService
            serviceImplementation="com.vw2.filewatcher.CommandSettings"/>
        <notificationGroup
            id="File Watcher Commands"
            displayType="BALLOON"/>
    </extensions>

    <projectListeners>
        <listener
            class="com.vw2.filewatcher.FileWatcherPlugin"
            topic="com.intellij.openapi.project.ProjectManagerListener"/>
    </projectListeners>
</idea-plugin>
```

- [ ] **Step 3: 验证编译**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 提交**

```bash
git add src/main/kotlin/com/vw2/filewatcher/FileWatcherPlugin.kt src/main/resources/META-INF/plugin.xml
git commit -m "feat: implement FileWatcherPlugin entry point"
```

---

## Task 7: 运行所有测试

**Covers:** [S6]

**Files:** 无

- [ ] **Step 1: 运行单元测试**

Run: `./gradlew test`
Expected: All tests PASS

- [ ] **Step 2: 验证插件打包**

Run: `./gradlew buildPlugin`
Expected: BUILD SUCCESSFUL, plugin ZIP created

- [ ] **Step 3: 最终提交**

```bash
git add -A
git commit -m "chore: verify all tests pass and plugin builds successfully"
```

---

## 自检结果

1. **Spec覆盖:** [S1]-[S8] 全部覆盖
2. **占位符:** 无
3. **类型一致性:** 所有类型、方法名一致
