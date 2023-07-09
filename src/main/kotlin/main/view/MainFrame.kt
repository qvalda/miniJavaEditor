package main.view

import editor.view.TextEditorComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import main.model.FileBrowserCodeSource
import main.model.MainModel
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JToolBar

class MainFrame : JFrame("Mini java editor") {

    init {
        val text = """class Main{
    public static void main(String[] a){
        System.out.println(new Test().Method1(10));
    }
}

class Test {
    
    // comment line
    public int Method1(int num){
        int i;
        char c;
        double f;
        String s;		
        i = 1;
        if (num < 1)
            f = 1.2f;
        else {            
            c = 'q';
            s = "aab\r\n\"cdef";
        }
        return 0;
    }
}

"""

        val mainModel = MainModel(FileBrowserCodeSource(this), text)
        val editor = TextEditorComponent(mainModel.textModel, mainModel.visualItemsContainer)
        val scrollPane = JScrollPane(editor)
        val toolbar = createToolbar(mainModel, editor)

        contentPane.add(toolbar, BorderLayout.NORTH)
        contentPane.add(scrollPane, BorderLayout.CENTER)

        pack()
        setSize(800, 600)
        setLocationRelativeTo(null)
        isVisible = true
        defaultCloseOperation = EXIT_ON_CLOSE

        mainModel.onTextModelChanged += {
            editor.controller = mainModel.textModel
            editor.itemsContainer = mainModel.visualItemsContainer
        }
    }

    private fun createToolbar(mainModel: MainModel, editor: TextEditorComponent): JToolBar {
        val toolbar = JToolBar()
        toolbar.isFloatable = false

        val newButton = JButton("new")
        newButton.addActionListener { mainModel.newFile(); editor.requestFocus() }
        val openButton = JButton("open")
        openButton.addActionListener { mainModel.openFile(); editor.requestFocus() }
        val saveButton = JButton("save")
        saveButton.addActionListener { mainModel.saveFile(); editor.requestFocus() }

        val cutButton = JButton("cut")
        cutButton.toolTipText = "Ctrl + X"
        cutButton.addActionListener { mainModel.cutAction(); editor.requestFocus() }
        val copyButton = JButton("copy")
        copyButton.toolTipText = "Ctrl + C"
        copyButton.addActionListener { mainModel.copyAction(); editor.requestFocus() }
        val pasteButton = JButton("paste")
        pasteButton.toolTipText = "Ctrl + V"
        pasteButton.addActionListener { mainModel.pasteAction(); editor.requestFocus() }

        val undoButton = JButton("undo")
        undoButton.toolTipText = "Ctrl + Z"
        undoButton.addActionListener { mainModel.undoAction(); editor.requestFocus() }
        val redoButton = JButton("redo")
        redoButton.toolTipText = "Ctrl + Y"
        redoButton.addActionListener { mainModel.redoAction(); editor.requestFocus() }

        toolbar.add(newButton)
        toolbar.add(openButton)
        toolbar.add(saveButton)
        toolbar.addSeparator()
        toolbar.add(cutButton)
        toolbar.add(copyButton)
        toolbar.add(pasteButton)
        toolbar.addSeparator()
        toolbar.add(undoButton)
        toolbar.add(redoButton)
        return toolbar
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CoroutineScope(Dispatchers.Swing).launch {
                MainFrame()
            }
        }
    }
}