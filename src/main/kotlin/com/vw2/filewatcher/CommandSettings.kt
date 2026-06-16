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

    override fun getState(): CommandSettings = this

    override fun loadState(state: CommandSettings) {
        XmlSerializerUtil.copyBean(state, this)
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
