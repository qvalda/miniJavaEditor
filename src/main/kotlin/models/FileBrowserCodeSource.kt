package models

import java.awt.Component
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import javax.swing.JFileChooser
import javax.swing.JOptionPane


class FileBrowserCodeSource (private val parent: Component): ICodeSource {
    override fun openCode(): String? {
        val fileChooser = JFileChooser("f:")
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = File(fileChooser.selectedFile.absolutePath)
            try {
                return file.readText()
            } catch (evt: Exception) {
                JOptionPane.showMessageDialog(parent, evt.message)
            }
        }

        return null
    }

    override fun saveCode(text: String) {
//// Create an object of JFileChooser class
//        // Create an object of JFileChooser class
//        val j = JFileChooser("f:")
//
//        // Invoke the showsSaveDialog function to show the save dialog
//
//        // Invoke the showsSaveDialog function to show the save dialog
//        val r = j.showSaveDialog(null)
//
//        if (r == JFileChooser.APPROVE_OPTION) {
//
//            // Set the label to the path of the selected directory
//            val fi = File(j.selectedFile.absolutePath)
//            try {
//                // Create a file writer
//                val wr = FileWriter(fi, false)
//
//                // Create buffered writer to write
//                val w = BufferedWriter(wr)
//
//                // Write
//                w.write(t.getText())
//                w.flush()
//                w.close()
//            } catch (evt: java.lang.Exception) {
//                JOptionPane.showMessageDialog(f, evt.message)
//            }
//        } else JOptionPane.showMessageDialog(f, "the user cancelled the operation")
    }
}