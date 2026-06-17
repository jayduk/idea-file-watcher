package com.vw2.filewatcher

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.vfs.VirtualFileManager

class FileWatcherPlugin : ProjectManagerListener {
    override fun projectOpened(project: Project) {
        FileWatcherLogger.info("Project opened: ${project.name}")
        val listener = BuildFileListener(project)
        val messageBus = project.messageBus.connect(project)
        messageBus.subscribe(VirtualFileManager.VFS_CHANGES, listener)
        FileWatcherLogger.info("BuildFileListener registered for project: ${project.name}")
    }
}
