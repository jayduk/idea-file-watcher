package com.vw2.filewatcher

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

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
    var fileCommands: MutableMap<String, String> = mutableMapOf()
    var workDirs: MutableMap<String, String> = mutableMapOf()

    override fun getState(): CommandSettings = this

    override fun loadState(state: CommandSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun getCommand(fileName: String): String? = commands[fileName]

    fun setCommand(fileName: String, command: String) {
        commands[fileName] = command
    }

    fun getCommandForFile(filePath: String): String? {
        val normalized = normalizePath(filePath)
        for ((key, value) in fileCommands) {
            if (normalizePath(key) == normalized) {
                return value
            }
        }
        return null
    }

    fun getWorkDirForFile(filePath: String): String? {
        val normalized = normalizePath(filePath)
        for ((key, value) in workDirs) {
            if (normalizePath(key) == normalized) {
                return value
            }
        }
        return null
    }

    fun setCommandForFile(filePath: String, command: String) {
        fileCommands[filePath] = command
    }

    fun setWorkDirForFile(filePath: String, workDir: String) {
        workDirs[filePath] = workDir
    }

    fun addFileCommand(filePath: String, command: String, workDir: String = "") {
        fileCommands[filePath] = command
        if (workDir.isNotBlank()) {
            workDirs[filePath] = workDir
        }
    }

    fun removeFileCommand(filePath: String) {
        val normalized = normalizePath(filePath)
        fileCommands.keys.firstOrNull { normalizePath(it) == normalized }?.let { fileCommands.remove(it) }
        workDirs.keys.firstOrNull { normalizePath(it) == normalized }?.let { workDirs.remove(it) }
    }

    private fun normalizePath(path: String): String {
        val slashNormalized = path.trim().replace('\\', '/')
        val withoutTrailingSlash =
            if (slashNormalized.length > 3) slashNormalized.trimEnd('/') else slashNormalized

        return if (withoutTrailingSlash.matches(Regex("^[A-Za-z]:/.*"))) {
            withoutTrailingSlash.lowercase()
        } else {
            withoutTrailingSlash
        }
    }

    companion object {
        fun getInstance(): CommandSettings =
            ApplicationManager.getApplication().getService(CommandSettings::class.java)
    }
}
