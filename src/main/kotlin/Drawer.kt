import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
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
//todo
class Fac {

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

            //canvas.text = File("""D:\Kotlin\bigInput2.txt""").readText()

//            // Run the GUI codes on the helpers.Event-Dispatching thread for thread safety
//            SwingUtilities.invokeLater {
//                CGTemplate() // Let the constructor do the job
//            }
        }
    }
}



interface IFormattingRuleProvider{
    fun getFormattingRule(lineIndex: Int): Array<FormattingRule>
}

class AggregateFormattingRuleProvider(private val providers: Array<IFormattingRuleProvider>) : IFormattingRuleProvider{

    override fun getFormattingRule(lineIndex: Int): Array<FormattingRule> {
        return providers.flatMap { p -> p.getFormattingRule(lineIndex).asIterable() }.toTypedArray()
    }
}

private class DrawCanvas : JPanel() {

    var initialized = false

    lateinit var model: EditorTextModel
    var lineHeight = 0
    var letterWidth = 0

    lateinit var formattingRuleProvider:IFormattingRuleProvider

    var text: String = ""
        get() {
            return field
        }
        set(value) {
            field = value
            model = EditorTextModel(value)
            val r1 = TokenizerFormattingRuleProvider(model)
            val r2 = SelectionFormattingRuleProvider(model)
            formattingRuleProvider = AggregateFormattingRuleProvider(arrayOf(r1,r2))
            repaint()
        }

    init {
        val mouseListener =  object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent?) {
                onMousePressed(e)
            }

            override fun mouseDragged(e: MouseEvent?) {
                onMouseDragged(e)
            }

            override fun mouseReleased(e: MouseEvent?) {
                onMouseReleased(e)
            }
        }

        val keyListener = object : KeyAdapter(){
            override fun keyPressed(e: KeyEvent?) {
                onKeyPressed(e)
            }
        }

        addMouseListener(mouseListener)
        addMouseMotionListener(mouseListener)
        addKeyListener(keyListener)
    }

    private fun onKeyPressed(e: KeyEvent?) {
        e?.consume()
        if (e ==null) return
        when(e.keyCode)
        {
            KeyEvent.VK_BACK_SPACE -> {
                model.backSpaceAction()
                repaint()
            }
            KeyEvent.VK_DELETE -> {
                model.deleteAction()
                repaint()
            }
            KeyEvent.VK_UP -> {
                if (hasShiftModifier(e)) {
                    model.moveEndCaretUp()
                }
                else{
                    model.moveBeginCaretUp()
                }
                repaint()
            }
            KeyEvent.VK_DOWN -> {
                if (hasShiftModifier(e)) {
                    model.moveEndCaretDown()
                }
                else{
                    model.moveBeginCaretDown()
                }
                repaint()
            }
            KeyEvent.VK_LEFT -> {
                if (hasShiftModifier(e)) {
                    model.moveEndCaretLeft()
                }
                else{
                    model.moveBeginCaretLeft()
                }
                repaint()
            }
            KeyEvent.VK_RIGHT -> {
                if (hasShiftModifier(e)) {
                    model.moveEndCaretRight()
                }
                else{
                    model.moveBeginCaretRight()
                }
                repaint()
            }
            KeyEvent.VK_ENTER -> {
                model.enterAction()
                repaint()
            }
            else -> {
                if (e.keyChar.isDefined() && e.keyCode != KeyEvent.VK_ESCAPE) {
                    model.addChar(e.keyChar)
                    repaint()
                }
            }
        }
    }

    fun onMousePressed(e: MouseEvent?) {
        if (e == null) return
        if (!hasShiftModifier(e)) {
            model.updateBeginCaret(e.y / lineHeight, (e.x.toFloat() / letterWidth).roundToInt())
        }
        model.updateEndCaret(e.y / lineHeight, (e.x.toFloat() / letterWidth).roundToInt())
        this.repaint()
    }

    private fun hasShiftModifier(e: MouseEvent) = (e.modifiersEx and KeyEvent.SHIFT_DOWN_MASK) != 0
    private fun hasShiftModifier(e: KeyEvent) = (e.modifiersEx and KeyEvent.SHIFT_DOWN_MASK) != 0

    fun onMouseReleased(e: MouseEvent?) {

    }

    fun onMouseDragged(e: MouseEvent?) {
        if (e == null) return
        model.updateEndCaret(e.y / lineHeight, (e.x.toFloat() / letterWidth).roundToInt())
        this.repaint()
    }

    public override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        if (!this::model.isInitialized) return

        setDefaultStyle(g)
        initValues(g)
        drawCaret(g)

        for ((lineIndex, line) in model.lines.withIndex()) {
            val lineY = lineHeight + lineIndex * lineHeight
            if (!isLineVisible(lineY, g)) continue

            val rules = formattingRuleProvider.getFormattingRule(lineIndex)

            for (rule in rules.filter { r -> r.style.background != null }) {
                usingColor(g, rule.style.background!!) {
                    g.fillRect(
                        rule.start * letterWidth,
                        lineIndex * lineHeight + 8,
                        (rule.end - rule.start) * letterWidth,
                        lineHeight - 3
                    )
                }
            }

            g.drawString(line.text, 0, lineY)

            for (rule in rules.filter { r -> r.style.color != null }) {
                val sub = line.text.substring(rule.start, rule.end)
                usingColor(g, rule.style.color!!) {
                    usingBold(g, rule.style.isBold)
                    {
                        g.drawString(sub, rule.start * letterWidth, lineY)
                    }
                }
            }

            for (rule in rules.filter { r -> r.style.underline != null }) {
                usingColor(g, rule.style.underline!!) {
                    usingStroke(g, 2) {
                        g.drawLine(
                            rule.start * letterWidth,
                            lineIndex * lineHeight + lineHeight + 3,
                            rule.end * letterWidth,
                            lineIndex * lineHeight + lineHeight + 3
                        )
                    }
                }
            }
        }

        updatePreferredSize()
    }


    private fun isLineVisible(lineY: Int, g: Graphics) =
        lineY >= g.clipBounds.y && lineY - lineHeight <= g.clipBounds.y + g.clipBounds.height

    private fun updatePreferredSize() {
        val longest = model.lines.maxBy { l -> l.text.length }.text.length
        val count = model.lines.size
        preferredSize = Dimension(longest * letterWidth, count * lineHeight)
    }

    private fun setDefaultStyle(g: Graphics) {
        background = Style.Background.background
        g.color = Style.Background.color
        g.font = Font("Monospaced", Font.PLAIN, 16)
    }

    private fun drawCaret(g: Graphics) {
        usingColor(g, Style.Caret.color!!) {
            usingStroke(g, 2) {
                g.drawLine(
                    model.beginCaret.column * letterWidth,
                    model.beginCaret.line * lineHeight + 9,
                    model.beginCaret.column * letterWidth,
                    (model.beginCaret.line + 1) * lineHeight + 4
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

    fun usingBold(g: Graphics, isBold:Boolean, statement: () -> Unit) {
        if(isBold) {
            val prevFont = g.font
            g.font = Font(prevFont.name, Font.BOLD, prevFont.size)
            statement()
            g.font = prevFont
        }
        else{
            statement()
        }
    }

    private fun usingStroke(g: Graphics, stroke: Int, statement: () -> Unit) {
        if (g is Graphics2D) {
            val prevStroke = g.stroke
            g.stroke = BasicStroke(stroke.toFloat())
            statement()
            g.stroke = prevStroke
        } else {
            statement()
        }
    }

    private fun initValues(g: Graphics) {
        if (!initialized) {
            lineHeight = g.fontMetrics.height
            letterWidth = g.fontMetrics.stringWidth("w")
            initialized = true
        }
    }
}