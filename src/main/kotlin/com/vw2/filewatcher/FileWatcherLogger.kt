package com.vw2.filewatcher

import com.intellij.openapi.diagnostic.Logger

object FileWatcherLogger {
    private val logger = Logger.getInstance("FileWatcher")

    fun info(message: String) {
        logger.info(message)
    }

    fun warn(message: String) {
        logger.warn(message)
    }

    fun error(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            logger.error(message, throwable)
        } else {
            logger.error(message)
        }
    }

    fun debug(message: String) {
        logger.debug(message)
    }
}
