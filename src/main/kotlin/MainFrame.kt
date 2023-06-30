import editor.FormattedTextEditor
import models.FileBrowserCodeSource
import models.MainModel
import ruleProviders.EmptyFormattingRuleProvider
import java.awt.BorderLayout
import javax.swing.*

class MainFrame : JFrame("Mini java editor") {

    init {
        //editor.canvas.text = File("""D:\Kotlin\bigInput2.txt""").readText()
        val text = """class Factorial{
    public static void main(String[] a){
	System.out.println(new Fac().ComputeFac(10));
    }
}
//todo
class Fac {
    { few
    }
    public int ComputeFac(int num){
	int num_aux ; // abc
    l = 'AB'
    q = "dwq"
	if (num < 1)
	    num_aux = 1 ;
	else
	    num_aux = num * (this.ComputeFac(num-1)) ;
	return num_aux ;
    }

}
"""

        val mainModel = MainModel(FileBrowserCodeSource(this), text)

        val toolbar = JToolBar()
        toolbar.isFloatable = false

        val newButton = JButton("new")
        newButton.addActionListener { mainModel.newFile() }
        val openButton = JButton("open")
        openButton.addActionListener { mainModel.openFile() }
        val saveButton = JButton("save")
        saveButton.addActionListener { mainModel.saveFile() }

        val cutButton = JButton("cut")
        val copyButton = JButton("copy")
        val pasteButton = JButton("paste")

        toolbar.add(newButton)
        toolbar.add(openButton)
        toolbar.add(saveButton)
        toolbar.addSeparator()
        toolbar.add(cutButton)
        toolbar.add(copyButton)
        toolbar.add(pasteButton)

        val editor = FormattedTextEditor(mainModel.textModel, mainModel.formattingRuleProvider)
        val scrollPane = JScrollPane(editor)

        contentPane.add(toolbar, BorderLayout.NORTH)
        contentPane.add(scrollPane, BorderLayout.CENTER)

        pack()
        setSize(800, 600)
        setLocationRelativeTo(null)
        isVisible = true
        defaultCloseOperation = EXIT_ON_CLOSE

        mainModel.onTextModelChanged +=  {
            editor.formattingRuleProvider =  EmptyFormattingRuleProvider() //todo remove hack
            editor.model = mainModel.textModel
            editor.formattingRuleProvider =   mainModel.formattingRuleProvider
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SwingUtilities.invokeLater {
                MainFrame()
            }
        }
    }
}