package com.vw2.filewatcher

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project

data class CommandResultMessage(
    val title: String,
    val content: String,
    val type: NotificationType
)

class CommandNotification {
    companion object {
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
            }

            showNotification(project, title, "检测到文件变化，点击执行命令：$command", NotificationType.INFORMATION, action)
        }

        fun showResultNotification(project: Project, command: String, result: CommandExecutionResult) {
            val message = buildResultMessage(command, result)
            showNotification(project, message.title, message.content, message.type)
        }

        fun buildResultMessage(command: String, result: CommandExecutionResult): CommandResultMessage {
            val success = result.exitCode == 0
            val title = if (success) "命令执行成功" else "命令执行失败"
            val type = if (success) NotificationType.INFORMATION else NotificationType.ERROR
            val details = listOfNotNull(
                result.output?.trim()?.takeIf { it.isNotBlank() },
                result.error?.trim()?.takeIf { it.isNotBlank() }
            ).joinToString("\n")
            val content = buildString {
                append("命令：")
                append(command)
                if (!success) {
                    append("\n退出码：")
                    append(result.exitCode)
                }
                if (details.isNotBlank()) {
                    append("\n输出：")
                    append(details)
                }
            }
            return CommandResultMessage(title, content, type)
        }

        private fun showNotification(
            project: Project,
            title: String,
            content: String,
            type: NotificationType,
            action: AnAction? = null
        ) {
            ApplicationManager.getApplication().invokeLater {
                val notification = NotificationGroupManager.getInstance()
                    .getNotificationGroup("File Watcher Commands")
                    .createNotification(title, content, type)

                if (action != null) {
                    notification.addAction(action)
                }
                notification.notify(project)
            }
        }
    }
}
