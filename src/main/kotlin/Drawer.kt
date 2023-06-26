import tokenizer.Token
import tokenizer.TokenType
import tokenizer.Tokenizer
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.event.*
import java.io.File
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import kotlin.math.roundToInt


class CGTemplate : JFrame() {
    private val canvas: DrawCanvas

    init {
        canvas = DrawCanvas() // Construct the drawing canvas
        canvas.preferredSize = Dimension(CANVAS_WIDTH, CANVAS_HEIGHT)

        // Set the Drawing JPanel as the JFrame's content-pane
        val cp = contentPane
        cp.add(canvas)
        // or "setContentPane(canvas);"
        defaultCloseOperation = EXIT_ON_CLOSE // Handle the CLOSE button
        pack() // Either pack() the components; or setSize()
        title = "......" // "super" JFrame sets the title
        isVisible = true // "super" JFrame show
    }


    companion object {
        // Define constants
        const val CANVAS_WIDTH = 500
        const val CANVAS_HEIGHT = 500

        // The entry main method
        @JvmStatic
        fun main(args: Array<String>) {

            var canvas = DrawCanvas()
            canvas.isFocusable = true
            //canvas.preferredSize = Dimension(CANVAS_WIDTH, CANVAS_HEIGHT)

            val jsp = JScrollPane(canvas)

            val frame = JFrame("Test")
            frame.contentPane.add(jsp)
            frame.pack()
            frame.setSize(500, 500)
            frame.setLocationRelativeTo(null)
            frame.isVisible = true
            frame.defaultCloseOperation = EXIT_ON_CLOSE
            jsp.verticalScrollBar.unitIncrement = 22;
            canvas.text = """class Factorial{
    public static void main(String[] a){
	System.out.println(new Fac().ComputeFac(10));
    }
}

class Fac {

    public int ComputeFac(int num){
	int num_aux ;
	if (num < 1)
	    num_aux = 1 ;
	else 
	    num_aux = num * (this.ComputeFac(num-1)) ;
	return num_aux ;
    }

}
""".replace("\r", "").replace("\t","    ");

            //canvas.text = File("""D:\Kotlin\bigInput.txt""").readText().replace("\r", "")

//            // Run the GUI codes on the Event-Dispatching thread for thread safety
//            SwingUtilities.invokeLater {
//                CGTemplate() // Let the constructor do the job
//            }
        }
    }
}

class EditorTextAttribute{

}

open class DefaultFormattingRule(val start: Int, val end:Int, val color:Color){}
//class KeyWordFormattingRule(line: Int, length: Int) : DefaultFormattingRule(line, length) {}

class FormattedSubstring(val value:String, val color:Color){}

class EditorLine (var text:String, var tokens:Array<Token>){
    //val length = 0;
    public var rules = ArrayList<DefaultFormattingRule>()

    fun getFormattingRules(): ArrayList<FormattedSubstring> {

        //todo yield

        val res =ArrayList<FormattedSubstring>()

        for (rule in rules.sortedBy { r->r.start }){
            res.add(FormattedSubstring(text.substring(rule.start, rule.end), rule.color))
        }
        return res
    }

}

class EditorTextCaret{


    var line = 0;
    var column = 0;
}

class EditorTextModel (text:String){

    var lines = ArrayList<EditorLine>()
    val tokenizer = Tokenizer()
    var caret = EditorTextCaret()

    init {
        val input = text.replace("\r", "").replace("\t", "    ");
        //val tokens = Tokenizer().getTokens(text)
        for (s in input.split('\n')) {
            val line = EditorLine(s, tokenizer.getTokens(s))
            lines.add(line)
        }
//        var lineIndex = 0;
//        for (t in tokens) {
//            if (t.type == TokenType.NewLine) {
//                lineIndex++;
//            } else {
//                var rule = if (t.type >= TokenType.KeyWordAbstract && t.type <= TokenType.KeyWordPrintln) {
//                    DefaultFormattingRule(t.beginIndex, t.endIndex, Color.ORANGE)
//                } else {
//                    null
//                }
//                if (rule != null) {
//                    lines[lineIndex].rules.add(rule)
//                }
//            }
//        }
    }

    fun updateCaret(lineIndex:Int, columnIndex:Int) {
        var line = if (lineIndex < 0) 0
                else if (lineIndex >= lines.size) lines.size
                else lineIndex
        var column = if(columnIndex<0) 0
        else if(columnIndex>lines[line].text.length) lines[line].text.length
        else columnIndex
        caret = EditorTextCaret()
        caret.line = line
        caret.column = column
    }

    fun backSpaceAction() {
        if(caret.column == 0 &&caret.line!=0){
            val appendix = lines[caret.line].text
            lines.removeAt(caret.line)
            val row = lines[caret.line-1].text + appendix
            lines[caret.line-1].text = row
            lines[caret.line-1].tokens= tokenizer.getTokens(row)
        }else{
            val row = lines[caret.line].text.removeRange(caret.column-1,caret.column)
            lines[caret.line].text = row
            lines[caret.line].tokens= tokenizer.getTokens(row)
        }
        moveCaretLeft()
    }

    fun moveCaretLeft() {
        if (caret.column > 0) {
            caret.column--
        } else {
            if (caret.line > 0) {
                caret.line--
                caret.column = lines[caret.line].text.length
            }
            else{
                caret.line = 0
                caret.column = 0
            }
        }
    }

    fun deleteAction() {

    }

    fun addChar(keyChar: Char) {
        val row = lines[caret.line].text
        lines[caret.line].text = StringBuilder(row).insert(caret.column, keyChar).toString()
        lines[caret.line].tokens = tokenizer.getTokens(lines[caret.line].text)
        moveCaretRight()
    }

    fun moveCaretRight() {
        if (caret.column < lines[caret.line].text.length) {
            caret.column++
        }
        else{
            if(caret.line < lines.size-1){
                caret.column = 0
                caret.line++
            }
            else{

            }
        }
    }

    fun moveCaretDown() {

    }

    fun moveCaretUp() {

    }


}

private class DrawCanvas : JPanel() {

    var initialized = false

    var model: EditorTextModel? = null

    init {
        val listener =  object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                onMouseClick(e)
            }
        }

        val kbListener = object : KeyAdapter(){
            override fun keyPressed(e: KeyEvent?) {
                onKeyPressed(e)
            }
        }

        addMouseListener(listener)
        addKeyListener(kbListener)
    }

    private fun onKeyPressed(e: KeyEvent?) {
        e?.consume()
        when(e?.keyCode)
        {
            KeyEvent.VK_BACK_SPACE -> {
                model!!.backSpaceAction()
                repaint()
            }
            KeyEvent.VK_DELETE -> {
                model!!.deleteAction()
                repaint()
            }
            KeyEvent.VK_UP -> {
                model!!.moveCaretUp()
                repaint()
            }
            KeyEvent.VK_DOWN -> {
                model!!.moveCaretDown()
                repaint()
            }
            KeyEvent.VK_LEFT -> {
                model!!.moveCaretLeft()
                repaint()
            }
            KeyEvent.VK_RIGHT -> {
                model!!.moveCaretRight()
                repaint()
            }
            else ->{
                model!!.addChar(e!!.keyChar)
                repaint()
            }
        }
    }

    var text: String = ""
        get() {
            return field
        }
        set(value) {
            field = value
            model = EditorTextModel(value)
            repaint()
        }

    var lineHeight = 0
    var letterWidth = 0

    //var caretPositionX = 0
    //var caretPositionY = 0

   // var caretLine = 0
    //var caretColumn = 0

    fun onMouseClick(e: MouseEvent) {
        //caretLine = e.y / lineHeight
        //caretColumn = e.x / letterWidth

        model?.updateCaret( e.y / lineHeight, (e.x.toFloat() / letterWidth).roundToInt())

        //caretPositionX = caretColumn
        //caretPositionY = caretLine
        this.repaint()

        //println(clickedLine)
    }

    fun getColor(tokenType: TokenType) : Color{
        return when(tokenType){
            TokenType.KeyWordAbstract,
            TokenType.KeyWordContinue,
            TokenType.KeyWordFor,
            TokenType.KeyWordNew,
            TokenType.KeyWordSwitch,
            TokenType.KeyWordAssert,
            TokenType.KeyWordDefault,
            TokenType.KeyWordGoto,
            TokenType.KeyWordPackage,
            TokenType.KeyWordSynchronized,
            TokenType.KeyWordBoolean,
            TokenType.KeyWordDo,
            TokenType.KeyWordIf,
            TokenType.KeyWordPrivate,
            TokenType.KeyWordThis,
            TokenType.KeyWordBreak,
            TokenType.KeyWordDouble,
            TokenType.KeyWordImplements,
            TokenType.KeyWordProtected,
            TokenType.KeyWordThrow,
            TokenType.KeyWordByte,
            TokenType.KeyWordElse,
            TokenType.KeyWordImport,
            TokenType.KeyWordPublic,
            TokenType.KeyWordThrows,
            TokenType.KeyWordCase,
            TokenType.KeyWordEnum,
            TokenType.KeyWordInstanceof,
            TokenType.KeyWordReturn,
            TokenType.KeyWordTransient,
            TokenType.KeyWordCatch,
            TokenType.KeyWordExtends,
            TokenType.KeyWordInt,
            TokenType.KeyWordShort,
            TokenType.KeyWordTry,
            TokenType.KeyWordChar,
            TokenType.KeyWordFinal,
            TokenType.KeyWordInterface,
            TokenType.KeyWordStatic,
            TokenType.KeyWordVoid,
            TokenType.KeyWordClass,
            TokenType.KeyWordFinally,
            TokenType.KeyWordLong,
            TokenType.KeyWordStrictfp,
            TokenType.KeyWordVolatile,
            TokenType.KeyWordConst,
            TokenType.KeyWordFloat,
            TokenType.KeyWordNative,
            TokenType.KeyWordSuper,
            TokenType.KeyWordWhile,
            TokenType.KeyWordString,
            TokenType.KeyWordSystem,
            TokenType.KeyWordOut,
            TokenType.KeyWordPrintln -> Color.ORANGE
            else -> Color.WHITE
        }
    }

    public override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        if(model==null) return

        setDefaultStyle(g)
        initValues(g)
        drawCaret(g)

//        for ( (index, line) in model!!.lines.withIndex()){
//            val lineY = lineHeight + index * lineHeight
//            for (w in line.getFormattingRules()){
//
//            }
//        }


        //val tokens = Tokenizer().getTokens(text)

        var lineIndex = 0;
        var columnIndex = 0;
        for (line in model!!.lines) {
            for (token in line.tokens){
                val lineY = lineHeight + lineIndex * lineHeight
                if (lineY >= g.clipBounds.y && lineY - lineHeight <= g.clipBounds.y + g.clipBounds.height) {
                    val dp = token.getDisplayValue()
                    val color = getColor(token.type)
                    usingColor(g, color){
                        g.drawString(dp, columnIndex, lineY)
                        columnIndex += dp.length * letterWidth
                    }
                    //println("draw line '${line}'")
                } else {
                    //println("hidden line")
                }
            }
            lineIndex++;
            columnIndex = 0;
        }

        val delim = '\n'

        val list = text.split(delim)
//
//        for ((index, line) in list.withIndex()) {
//            val lineY = lineHeight + index * lineHeight
//            if (lineY >= g.clipBounds.y && lineY - lineHeight <= g.clipBounds.y + g.clipBounds.height) {
//                line.value?.let {
//                    g.drawString(it, 0, lineY)
//                }
//                //println("draw line '${line}'")
//            } else {
//                //println("hidden line")
//            }
//        }

        var longest = list.maxBy { l -> l.length }.length
        var count = list.size
        preferredSize = Dimension(longest * letterWidth, count * lineHeight)
    }

    private fun setDefaultStyle(g: Graphics) {
        background = Color.DARK_GRAY
        g.color = Color.WHITE
        g.font = Font("Monospaced", Font.PLAIN, 16)
    }

    private fun drawCaret(g: Graphics) {
        if (model!=null) {
            usingColor(g, Color.GREEN) {
                g.drawLine(
                    model!!.caret.column * letterWidth,
                    model!!.caret.line * lineHeight + 10,
                    model!!.caret.column * letterWidth,
                    (model!!.caret.line + 1) * lineHeight + 2
                )
            }
        }
    }

    fun usingColor(g: Graphics, color: Color, statement: () -> Unit) {
        val prevColor = g.color
        g.color = color
        statement()
        g.color = prevColor
    }

    private fun initValues(g: Graphics) {
        if (!initialized) {
            lineHeight = g.fontMetrics.height
            letterWidth = g.fontMetrics.stringWidth("w")
            initialized = true
        }
    }
}