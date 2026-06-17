package com.vw2.filewatcher

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BuildFileListenerTest {
    @Test
    fun `should recognize pom xml as build file`() {
        val listener = BuildFileListener(null)
        assertTrue(listener.isBuildFile("pom.xml"))
    }

    @Test
    fun `should recognize build gradle as build file`() {
        val listener = BuildFileListener(null)
        assertTrue(listener.isBuildFile("build.gradle"))
    }

    @Test
    fun `should not recognize txt as build file`() {
        val listener = BuildFileListener(null)
        assertFalse(listener.isBuildFile("readme.txt"))
    }

    @Test
    fun `should extract file name from path`() {
        val listener = BuildFileListener(null)
        assertEquals("pom.xml", listener.extractFileName("/path/to/pom.xml"))
    }

    @Test
    fun `matcher should return command for changed file path using normalized path`() {
        val settings = CommandSettings()
        settings.addFileCommand(
            "C:\\Project\\pom.xml",
            "mvn clean install",
            "C:\\Project"
        )
        val matcher = WatchedFileMatcher { settings }

        val match = matcher.match("c:/project/pom.xml")

        assertNotNull(match)
        assertEquals("mvn clean install", match?.command)
        assertEquals("C:\\Project", match?.workDir)
    }

    @Test
    fun `matcher should ignore blank configured command`() {
        val settings = CommandSettings()
        settings.addFileCommand("C:\\Project\\pom.xml", "   ", "C:\\Project")
        val matcher = WatchedFileMatcher { settings }

        val match = matcher.match("C:/Project/pom.xml")

        assertNull(match)
    }

    @Test
    fun `matcher should return null for unconfigured file`() {
        val settings = CommandSettings()
        settings.addFileCommand("C:\\Project\\pom.xml", "mvn clean install", "C:\\Project")
        val matcher = WatchedFileMatcher { settings }

        val match = matcher.match("C:/Project/build.gradle.kts")

        assertNull(match)
    }
}
