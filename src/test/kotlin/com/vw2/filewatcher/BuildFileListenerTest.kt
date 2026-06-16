package com.vw2.filewatcher

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

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
}
