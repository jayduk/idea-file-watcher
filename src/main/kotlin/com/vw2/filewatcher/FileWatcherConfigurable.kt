package com.vw2.filewatcher

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.io.File
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.SwingUtilities

class FileWatcherConfigurable() : Configurable {
    private var settingsProvider: () -> CommandSettings = { CommandSettings.getInstance() }
    private var mainPanel: JPanel? = null
    private val rows = mutableListOf<JPanel>()
    private val fileFields = mutableListOf<JTextField>()
    private val commandFields = mutableListOf<JTextField>()
    private val workDirFields = mutableListOf<JTextField>()

    internal constructor(settingsProvider: () -> CommandSettings) : this() {
        this.settingsProvider = settingsProvider
    }

    override fun getDisplayName(): String = "File Watcher Commands"

    override fun getHelpTopic(): String? = null

    override fun createComponent(): JComponent {
        val panel = JPanel(BorderLayout())

        val scrollPanel = JPanel()
        scrollPanel.layout = BoxLayout(scrollPanel, BoxLayout.Y_AXIS)

        val settings = settingsProvider()
        for ((filePath, command) in settings.fileCommands) {
            val workDir = settings.getWorkDirForFile(filePath) ?: ""
            addRow(scrollPanel, filePath, command, workDir)
        }

        val scrollPane = JBScrollPane(scrollPanel)
        panel.add(scrollPane, BorderLayout.CENTER)

        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        val addButton = JButton("Add File")
        addButton.addActionListener { addNewRow(scrollPanel) }
        buttonPanel.add(addButton)
        panel.add(buttonPanel, BorderLayout.SOUTH)

        mainPanel = panel
        return panel
    }

    private fun addRow(panel: JPanel, filePath: String, command: String, workDir: String = "") {
        val row = JPanel(GridBagLayout())
        row.border = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        )

        val gbc = GridBagConstraints()
        gbc.insets = Insets(2, 4, 2, 4)
        gbc.anchor = GridBagConstraints.WEST

        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 0.0
        gbc.fill = GridBagConstraints.NONE
        row.add(JLabel("File:"), gbc)

        val fileField = JTextField(filePath, 30)
        gbc.gridx = 1
        gbc.weightx = 1.0
        gbc.fill = GridBagConstraints.HORIZONTAL
        row.add(fileField, gbc)

        val browseFileButton = JButton("Browse...")
        browseFileButton.addActionListener {
            val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            val file = FileChooser.chooseFile(descriptor, null, null)
            if (file != null) {
                fileField.text = file.path
            }
        }
        gbc.gridx = 2
        gbc.weightx = 0.0
        gbc.fill = GridBagConstraints.NONE
        row.add(browseFileButton, gbc)

        gbc.gridx = 0
        gbc.gridy = 1
        gbc.weightx = 0.0
        gbc.fill = GridBagConstraints.NONE
        row.add(JLabel("Command:"), gbc)

        val commandField = JTextField(command, 30)
        gbc.gridx = 1
        gbc.weightx = 1.0
        gbc.fill = GridBagConstraints.HORIZONTAL
        row.add(commandField, gbc)

        gbc.gridx = 0
        gbc.gridy = 2
        gbc.weightx = 0.0
        gbc.fill = GridBagConstraints.NONE
        row.add(JLabel("WorkDir:"), gbc)

        val workDirField = JTextField(workDir, 30)
        gbc.gridx = 1
        gbc.weightx = 1.0
        gbc.fill = GridBagConstraints.HORIZONTAL
        row.add(workDirField, gbc)

        val browseWorkDirButton = JButton("Browse...")
        browseWorkDirButton.addActionListener {
            val descriptor = FileChooserDescriptor(false, true, false, false, false, false)
            val file = FileChooser.chooseFile(descriptor, null, null)
            if (file != null) {
                workDirField.text = file.path
            }
        }
        gbc.gridx = 2
        gbc.weightx = 0.0
        gbc.fill = GridBagConstraints.NONE
        row.add(browseWorkDirButton, gbc)

        val testButton = JButton("Test")
        testButton.addActionListener {
            val cmd = commandField.text
            val wd = workDirField.text
            if (cmd.isNotBlank()) {
                testCommand(cmd, wd)
            } else {
                Messages.showErrorDialog("Please enter a command", "Test Command")
            }
        }

        val deleteButton = JButton("Remove")
        deleteButton.addActionListener {
            val index = rows.indexOf(row)
            if (index >= 0) {
                rows.removeAt(index)
                fileFields.removeAt(index)
                commandFields.removeAt(index)
                workDirFields.removeAt(index)
            }
            panel.remove(row)
            panel.revalidate()
            panel.repaint()
        }

        val buttonGroup = JPanel(FlowLayout(FlowLayout.LEFT, 4, 0))
        buttonGroup.add(testButton)
        buttonGroup.add(deleteButton)

        gbc.gridx = 1
        gbc.gridy = 3
        gbc.gridwidth = 2
        gbc.gridheight = 1
        gbc.weightx = 1.0
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.anchor = GridBagConstraints.WEST
        row.add(buttonGroup, gbc)
        gbc.gridwidth = 1

        rows.add(row)
        fileFields.add(fileField)
        commandFields.add(commandField)
        workDirFields.add(workDirField)

        row.maximumSize = Dimension(Int.MAX_VALUE, row.preferredSize.height)
        row.alignmentX = Component.LEFT_ALIGNMENT

        panel.add(row)
        panel.revalidate()
        panel.repaint()
    }

    private fun addNewRow(panel: JPanel) {
        val filePath = chooseFile() ?: return
        addRow(panel, filePath, "", "")
    }

    private fun chooseFile(): String? {
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
        val file = FileChooser.chooseFile(descriptor, null, null)
        return file?.path
    }

    private fun testCommand(command: String, workDir: String) {
        Thread {
            try {
                val builder = ProcessBuilder("cmd", "/c", command)
                    .redirectErrorStream(true)

                if (workDir.isNotBlank()) {
                    val dir = File(workDir)
                    if (dir.exists() && dir.isDirectory) {
                        builder.directory(dir)
                    }
                }

                val process = builder.start()
                val output = process.inputStream.bufferedReader().readText()
                val exitCode = process.waitFor()

                SwingUtilities.invokeLater {
                    if (exitCode == 0) {
                        Messages.showInfoMessage("Command executed successfully!\n\nOutput:\n$output", "Test Result")
                    } else {
                        Messages.showErrorDialog("Command failed with exit code $exitCode\n\nOutput:\n$output", "Test Result")
                    }
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    Messages.showErrorDialog("Error: ${e.message}", "Test Failed")
                }
            }
        }.start()
    }

    override fun isModified(): Boolean {
        val settings = settingsProvider()
        if (settings.fileCommands.size != fileFields.size) return true

        for (i in fileFields.indices) {
            val filePath = fileFields[i].text
            val command = commandFields[i].text
            val workDir = workDirFields[i].text
            if (settings.getCommandForFile(filePath) != command) return true
            if ((settings.getWorkDirForFile(filePath) ?: "") != workDir) return true
        }

        return false
    }

    override fun apply() {
        val settings = settingsProvider()
        settings.fileCommands.clear()
        settings.workDirs.clear()

        for (i in fileFields.indices) {
            val filePath = fileFields[i].text
            val command = commandFields[i].text
            val workDir = workDirFields[i].text
            if (filePath.isNotBlank()) {
                settings.fileCommands[filePath] = command
                if (workDir.isNotBlank()) {
                    settings.workDirs[filePath] = workDir
                }
            }
        }
    }

    override fun reset() {
        val settings = settingsProvider()
        rows.clear()
        fileFields.clear()
        commandFields.clear()
        workDirFields.clear()

        mainPanel?.let { panel ->
            val scrollPanel = (panel.getComponent(0) as? JScrollPane)?.viewport?.view as? JPanel
            scrollPanel?.let {
                it.removeAll()
                for ((filePath, command) in settings.fileCommands) {
                    val workDir = settings.getWorkDirForFile(filePath) ?: ""
                    addRow(it, filePath, command, workDir)
                }
                it.revalidate()
                it.repaint()
            }
        }
    }

    override fun disposeUIResources() {
        rows.clear()
        fileFields.clear()
        commandFields.clear()
        workDirFields.clear()
        mainPanel = null
    }
}
