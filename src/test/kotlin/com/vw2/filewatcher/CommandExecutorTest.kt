package com.vw2.filewatcher

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class CommandExecutorTest {

    @Test
    fun `should execute command and return output`() {
        val executor = CommandExecutor()
        val result = AtomicReference<String>()
        val latch = CountDownLatch(1)

        executor.execute("echo hello") { output, error ->
            result.set(output)
            latch.countDown()
        }

        latch.await(5, TimeUnit.SECONDS)
        assertEquals("hello", result.get()?.trim())
    }

    @Test
    fun `should execute command asynchronously`() {
        val executor = CommandExecutor()
        val latch = CountDownLatch(1)
        val threadName = AtomicReference<String>()

        executor.execute("echo test") { _, _ ->
            threadName.set(Thread.currentThread().name)
            latch.countDown()
        }

        latch.await(5, TimeUnit.SECONDS)
        assertNotEquals("main", threadName.get())
    }

    @Test
    fun `should capture error output`() {
        val executor = CommandExecutor()
        val errorOutput = AtomicReference<String>()
        val latch = CountDownLatch(1)

        executor.execute("ls nonexistent_directory_xyz") { _, error ->
            errorOutput.set(error)
            latch.countDown()
        }

        latch.await(5, TimeUnit.SECONDS)
        assertNotNull(errorOutput.get())
        assertTrue(errorOutput.get()?.isNotEmpty() == true)
    }

    @Test
    fun `should execute command with workDir`() {
        val executor = CommandExecutor()
        val result = AtomicReference<String>()
        val latch = CountDownLatch(1)

        executor.execute("echo hello", "C:\\") { output, _ ->
            result.set(output)
            latch.countDown()
        }

        latch.await(5, TimeUnit.SECONDS)
        assertEquals("hello", result.get()?.trim())
    }
}
