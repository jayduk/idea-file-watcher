package com.vw2.filewatcher

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.swing.JPanel
import javax.swing.JScrollPane

class FileWatcherConfigurableTest {
    @Test
    fun `configured rows should not be vertically clipped`() {
        val settings = CommandSettings()
        settings.addFileCommand(
            "C:\\project\\very-long-folder\\pom.xml",
            "mvn clean install -DskipTests",
            "C:\\project"
        )

        val configurable = FileWatcherConfigurable { settings }

        val component = configurable.createComponent()
        val scrollPane = component.getComponent(0) as JScrollPane
        val scrollPanel = scrollPane.viewport.view as JPanel
        val row = scrollPanel.components.first() as JPanel

        assertTrue(
            row.maximumSize.height >= row.preferredSize.height,
            "Row maximum height must be at least preferred height so BoxLayout does not clip controls"
        )
    }
}
