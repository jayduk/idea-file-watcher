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
}
