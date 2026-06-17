package com.vw2.filewatcher

import java.io.File
import java.util.concurrent.CompletableFuture

class CommandExecutor {
    fun execute(command: String, workDir: String? = null, callback: (output: String?, error: String?) -> Unit) {
        CompletableFuture.runAsync {
            try {
                val builder = ProcessBuilder("cmd", "/c", command)
                    .redirectErrorStream(false)

                if (!workDir.isNullOrBlank()) {
                    val dir = File(workDir)
                    if (dir.exists() && dir.isDirectory) {
                        builder.directory(dir)
                    }
                }

                val process = builder.start()

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
