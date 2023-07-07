package main.model

import java.awt.Component
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane

class FileBrowserCodeSource (private val parent: Component): ICodeSource {
    override fun openCode(): String? {
        val fileChooser = JFileChooser()
        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            val file = File(fileChooser.selectedFile.absolutePath)
            try {
                return file.readText()
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(parent, e.message)
            }
        }

        return null
    }

    override fun saveCode(text: String) {
        val fileChooser = JFileChooser()
        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            val file = File(fileChooser.selectedFile.absolutePath)
            try {
                file.writeText(text)
            } catch (e: Exception) {
                JOptionPane.showMessageDialog(parent, e.message)
            }
        }
    }
}