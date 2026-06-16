package com.vw2.filewatcher

import java.io.File
import java.util.concurrent.CompletableFuture

class CommandExecutor {

    fun execute(command: String, callback: (output: String?, error: String?) -> Unit) {
        CompletableFuture.runAsync {
            try {
                val isWindows = System.getProperty("os.name").lowercase().contains("win")
                val processBuilder = if (isWindows) {
                    ProcessBuilder("cmd", "/c", command)
                } else {
                    ProcessBuilder("sh", "-c", command)
                }
                processBuilder.redirectErrorStream(false)

                val process = processBuilder.start()
                val output = process.inputStream.bufferedReader().readText()
                val error = process.errorStream.bufferedReader().readText()
                process.waitFor()

                callback(output, error)
            } catch (e: Exception) {
                callback(null, e.message)
            }
        }
    }
}
