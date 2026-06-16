package com.vw2.filewatcher

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project

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

            val notification = NotificationGroupManager.getInstance()
                .getNotificationGroup("File Watcher Commands")
                .createNotification(
                    title,
                    "检测到文件变化，点击执行命令",
                    NotificationType.INFORMATION
                )

            notification.addAction(action)
            notification.notify(project)
        }
    }
}
