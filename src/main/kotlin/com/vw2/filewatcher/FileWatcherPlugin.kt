package com.vw2.filewatcher

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.vfs.VirtualFileManager

class FileWatcherPlugin : ProjectManagerListener {
    override fun projectOpened(project: Project) {
        val listener = BuildFileListener(project)
        val messageBus = project.messageBus.connect()
        messageBus.subscribe(VirtualFileManager.VFS_CHANGES, listener)
    }
}
