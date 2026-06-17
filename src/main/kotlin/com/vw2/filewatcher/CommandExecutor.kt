package com.vw2.filewatcher

import java.io.File
import java.util.concurrent.CompletableFuture

data class CommandExecutionResult(
    val output: String?,
    val error: String?,
    val exitCode: Int
)

class CommandExecutor {
    fun execute(command: String, workDir: String? = null, callback: (CommandExecutionResult) -> Unit) {
        CompletableFuture.runAsync {
            callback(runCommand(command, workDir))
        }
    }

    fun execute(command: String, workDir: String? = null, callback: (output: String?, error: String?) -> Unit) {
        execute(command, workDir) { result ->
            callback(result.output, result.error)
        }
    }

    private fun runCommand(command: String, workDir: String?): CommandExecutionResult {
        return try {
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
            val exitCode = process.waitFor()

            CommandExecutionResult(output, error, exitCode)
        } catch (e: Exception) {
            CommandExecutionResult(null, e.message, -1)
        }
    }
}
