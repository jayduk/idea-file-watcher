package com.vw2.filewatcher

import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

class CommandExecutor {
    fun execute(command: String, callback: (output: String?, error: String?) -> Unit) {
        CompletableFuture.runAsync {
            try {
                val process = ProcessBuilder("cmd", "/c", command)
                    .redirectErrorStream(false)
                    .start()

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
