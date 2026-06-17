package com.vw2.filewatcher

import com.intellij.notification.NotificationType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CommandNotificationTest {
    @Test
    fun `should build success notification for zero exit code`() {
        val result = CommandExecutionResult(
            output = "done\n",
            error = "",
            exitCode = 0
        )

        val message = CommandNotification.buildResultMessage("echo done", result)

        assertEquals("命令执行成功", message.title)
        assertEquals(NotificationType.INFORMATION, message.type)
        assertTrue(message.content.contains("echo done"))
        assertTrue(message.content.contains("done"))
    }

    @Test
    fun `should build failure notification for nonzero exit code`() {
        val result = CommandExecutionResult(
            output = "",
            error = "failed\n",
            exitCode = 7
        )

        val message = CommandNotification.buildResultMessage("exit /b 7", result)

        assertEquals("命令执行失败", message.title)
        assertEquals(NotificationType.ERROR, message.type)
        assertTrue(message.content.contains("exit /b 7"))
        assertTrue(message.content.contains("退出码：7"))
        assertTrue(message.content.contains("failed"))
    }
}
