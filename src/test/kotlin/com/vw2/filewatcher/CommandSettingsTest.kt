package com.vw2.filewatcher

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CommandSettingsTest {
    private lateinit var settings: CommandSettings

    @BeforeEach
    fun setUp() {
        settings = CommandSettings()
    }

    @Test
    fun `should return default commands for known file types`() {
        val mavenCommand = settings.getCommand("pom.xml")
        assertEquals("mvn clean install", mavenCommand)
    }

    @Test
    fun `should return custom command when set`() {
        settings.setCommand("pom.xml", "mvn clean compile")
        val command = settings.getCommand("pom.xml")
        assertEquals("mvn clean compile", command)
    }

    @Test
    fun `should return null for unknown file type`() {
        val command = settings.getCommand("unknown.txt")
        assertNull(command)
    }

    @Test
    fun `should return command for configured file path`() {
        settings.setCommandForFile("C:\\project\\pom.xml", "mvn clean install")

        val command = settings.getCommandForFile("C:\\project\\pom.xml")

        assertEquals("mvn clean install", command)
    }

    @Test
    fun `should return workDir for configured file path`() {
        settings.setWorkDirForFile("C:\\project\\pom.xml", "C:\\project")

        val workDir = settings.getWorkDirForFile("C:\\project\\pom.xml")

        assertEquals("C:\\project", workDir)
    }

    @Test
    fun `should match windows file path regardless of case`() {
        settings.setCommandForFile("C:\\Project\\Pom.xml", "mvn clean install")

        val command = settings.getCommandForFile("c:/project/pom.xml")

        assertEquals("mvn clean install", command)
    }

    @Test
    fun `should match file path with surrounding whitespace`() {
        settings.setCommandForFile(" C:\\project\\pom.xml ", "mvn clean install")

        val command = settings.getCommandForFile("C:/project/pom.xml")

        assertEquals("mvn clean install", command)
    }

    @Test
    fun `should remove command and workDir using normalized matching path`() {
        settings.addFileCommand("C:\\Project\\pom.xml", "mvn clean install", "C:\\Project")

        settings.removeFileCommand("c:/project/pom.xml")

        assertNull(settings.getCommandForFile("C:\\Project\\pom.xml"))
        assertNull(settings.getWorkDirForFile("C:\\Project\\pom.xml"))
    }
}
