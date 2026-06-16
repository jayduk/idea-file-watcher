package com.vw2.filewatcher

import com.intellij.openapi.project.Project
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
                            executor.execute(command) { _, _ -> }
                        }
                    }
                }
            }
        }
    }
}
