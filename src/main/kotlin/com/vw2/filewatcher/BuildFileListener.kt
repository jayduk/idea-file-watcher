package com.vw2.filewatcher

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

data class MatchedCommand(
    val command: String,
    val workDir: String?
)

class WatchedFileMatcher(
    private val settingsProvider: () -> CommandSettings
) {
    fun match(filePath: String): MatchedCommand? {
        val settings = settingsProvider()
        val command = settings.getCommandForFile(filePath)

        return if (command.isNullOrBlank()) {
            null
        } else {
            MatchedCommand(
                command = command,
                workDir = settings.getWorkDirForFile(filePath)
            )
        }
    }
}

class BuildFileListener(
    private val project: Project?,
    private val matcher: WatchedFileMatcher = WatchedFileMatcher { CommandSettings.getInstance() },
    private val notifier: ((Project, String, String?) -> Unit)? = null
) : BulkFileListener {
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

    override fun before(events: List<VFileEvent>) {
        FileWatcherLogger.info("Before events: ${events.size}")
    }

    override fun after(events: List<VFileEvent>) {
        FileWatcherLogger.info("After events: ${events.size}")

        for (event in events) {
            FileWatcherLogger.info("Event: ${event.javaClass.simpleName} - ${event.path}")

            val isContentChange = event is VFileContentChangeEvent
            val isCreate = event is VFileCreateEvent

            if (isContentChange || isCreate) {
                handleChangedPath(event.path)
            }
        }
    }

    private fun handleChangedPath(filePath: String) {
        if (project == null) {
            FileWatcherLogger.info("No project available for: $filePath")
            return
        }

        val match = matcher.match(filePath)
        if (match != null) {
            FileWatcherLogger.info("Matched! Command: ${match.command}, WorkDir: ${match.workDir}")
            val notificationHandler = notifier ?: ::showNotificationWithRetry
            notificationHandler(project, match.command, match.workDir)
        } else {
            FileWatcherLogger.info("No match for: $filePath")
        }
    }

    private fun showNotificationWithRetry(project: Project, command: String, workDir: String?) {
        try {
            CommandNotification.showNotification(
                project = project,
                title = "Execute Command",
                command = command
            ) {
                FileWatcherLogger.info("Executing command: $command, WorkDir: $workDir")
                val executor = CommandExecutor()
                executor.execute(command, workDir) { output, error ->
                    if (output != null && output.isNotBlank()) {
                        FileWatcherLogger.info("Command output: $output")
                    }
                    if (error != null && error.isNotBlank()) {
                        FileWatcherLogger.warn("Command stderr: $error")
                    }
                    FileWatcherLogger.info("Command completed")
                }
            }
            FileWatcherLogger.info("Notification shown successfully")
        } catch (e: Exception) {
            FileWatcherLogger.error("Failed to show notification", e)
        }
    }
}
