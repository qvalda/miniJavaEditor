package editor

import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import kotlin.math.roundToInt

class FormattedTextEditor(model: TextEditorModel, formattingRuleProvider: IFormattingRuleProvider) : JComponent(), Scrollable {

    private var initialized = false
    private var letterHeight = 0
    private var letterShift = 0
    private var letterWidth = 0
    private var prevPreferredSize = Dimension(0, 0)

    var model = model
        set(value) {
            field = value
            repaint()
        }

    var formattingRuleProvider = formattingRuleProvider
        set(value) {
            field = value
            repaint()
        }

    init {
        isFocusable = true
        focusTraversalKeysEnabled = false

        val mouseListener = object : MouseAdapter() {
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

        val keyListener = object : KeyAdapter() {
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
        if (e == null) return
        when (e.keyCode) {
            KeyEvent.VK_BACK_SPACE -> {
                model.backSpaceAction()
                afterInput()
            }

            KeyEvent.VK_DELETE -> {
                model.deleteAction()
                afterInput()
            }

            KeyEvent.VK_TAB -> {
                model.tabAction()
                afterInput()
            }

            KeyEvent.VK_UP -> {
                if (hasShiftModifier(e)) {
                    model.moveEndCaretUp()
                } else {
                    model.moveBeginCaretUp()
                }
                afterInput()
            }

            KeyEvent.VK_DOWN -> {
                if (hasShiftModifier(e)) {
                    model.moveEndCaretDown()
                } else {
                    model.moveBeginCaretDown()
                }
                afterInput()
            }

            KeyEvent.VK_LEFT -> {
                if (hasShiftModifier(e)) {
                    model.moveEndCaretLeft()
                } else {
                    model.moveBeginCaretLeft()
                }
                afterInput()
            }

            KeyEvent.VK_RIGHT -> {
                if (hasShiftModifier(e)) {
                    model.moveEndCaretRight()
                } else {
                    model.moveBeginCaretRight()
                }
                afterInput()
            }

            KeyEvent.VK_ENTER -> {
                model.enterAction()
                afterInput()
            }

            else -> {
                if (e.keyChar.isDefined() && e.keyCode != KeyEvent.VK_ESCAPE) {
                    model.addChar(e.keyChar)
                    afterInput()
                }
            }
        }
    }

    private fun afterInput() {
        repaint()
        updatePreferredSize()
        scrollRectToVisible(Rectangle(model.endCaret.column * letterWidth, (model.endCaret.line - 1) * letterHeight, letterWidth, letterHeight * 3))
    }

    fun onMousePressed(e: MouseEvent?) {
        if (e == null) return
        requestFocus()
        if (!hasShiftModifier(e)) {
            model.updateBeginCaret(e.y / letterHeight, (e.x.toFloat() / letterWidth).roundToInt())
        }
        model.updateEndCaret(e.y / letterHeight, (e.x.toFloat() / letterWidth).roundToInt())
        repaint()
    }

    private fun hasShiftModifier(e: MouseEvent) = (e.modifiersEx and KeyEvent.SHIFT_DOWN_MASK) != 0
    private fun hasShiftModifier(e: KeyEvent) = (e.modifiersEx and KeyEvent.SHIFT_DOWN_MASK) != 0

    fun onMouseReleased(e: MouseEvent?) {

    }

    fun onMouseDragged(e: MouseEvent?) {
        if (e == null) return
        model.updateEndCaret(e.y / letterHeight, (e.x.toFloat() / letterWidth).roundToInt())
        //repaint()
        afterInput()
    }

    public override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        drawBackground(g)
        setDefaultStyle(g)
        initValues(g)
        drawCaret(g)

        val visibleLineFrom = (g.clipBounds.y / letterHeight).coerceIn(0, model.lines.size - 1)
        val visibleLineTo = ((g.clipBounds.y + g.clipBounds.height) / letterHeight).coerceIn(0, model.lines.size - 1)

        for (lineIndex in visibleLineFrom..visibleLineTo) {
            val line = model.lines[lineIndex]
            val lineY = letterHeight + lineIndex * letterHeight - letterShift

            val rules = formattingRuleProvider.getFormattingRule(lineIndex)

            for (rule in rules.filter { r -> r.style.background != null }) {
                usingColor(g, rule.style.background!!) {
                    g.fillRect(
                        rule.start * letterWidth,
                        lineIndex * letterHeight,
                        (rule.end - rule.start) * letterWidth,
                        letterHeight
                    )
                }
            }

            g.drawString(line, 0, lineY)

            for (rule in rules.filter { r -> r.style.color != null }) {
                val sub = line.substring(rule.start, rule.end)
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
                            lineIndex * letterHeight + letterHeight,
                            rule.end * letterWidth,
                            lineIndex * letterHeight + letterHeight
                        )
                    }
                }
            }
        }

        updatePreferredSize()
    }

    private fun drawBackground(g: Graphics) {
        usingColor(g, Style.Background.background!!) {
            val b = g.clipBounds
            g.fillRect(b.x, b.y, b.width, b.height)
        }
    }

    private fun updatePreferredSize() {
        val newPreferredSize = Dimension((model.maxLength + 10) * letterWidth, (model.lines.size) * letterHeight)
        if (prevPreferredSize != newPreferredSize) {
            preferredSize = newPreferredSize
            prevPreferredSize = newPreferredSize
            revalidate()
        }
    }

    private fun setDefaultStyle(g: Graphics) {
        g.color = Style.Background.color
        g.font = Style.Font
    }

    private fun drawCaret(g: Graphics) {
        if (!hasFocus()) return //todo fix
        drawCaret(g, model.beginCaret)
        if (model.endCaret != model.beginCaret) {
            drawCaret(g, model.endCaret)
        }
    }

    private fun drawCaret(g: Graphics, caret: TextEditorCaret) {
        usingColor(g, Style.Caret.color!!) {
            usingStroke(g, 2) {
                g.drawLine(
                    caret.column * letterWidth,
                    caret.line * letterHeight,
                    caret.column * letterWidth,
                    (caret.line + 1) * letterHeight
                )
            }
        }
    }

    private fun usingColor(g: Graphics, color: Color, statement: () -> Unit) {
        val prevColor = g.color
        g.color = color
        statement()
        g.color = prevColor
    }

    private fun usingBold(g: Graphics, isBold: Boolean, statement: () -> Unit) {
        if (isBold) {
            val prevFont = g.font
            g.font = Font(prevFont.name, Font.BOLD, prevFont.size)
            statement()
            g.font = prevFont
        } else {
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
            letterHeight = g.fontMetrics.height
            letterShift = g.fontMetrics.descent
            letterWidth = g.fontMetrics.stringWidth("w")
            initialized = true
        }
    }

    //region Scrollable
    override fun getPreferredScrollableViewportSize(): Dimension {
        return preferredSize
    }

    override fun getScrollableUnitIncrement(visibleRect: Rectangle?, orientation: Int, direction: Int): Int {
        return letterHeight
    }

    override fun getScrollableBlockIncrement(visibleRect: Rectangle?, orientation: Int, direction: Int): Int {
        return letterHeight * 3
    }

    override fun getScrollableTracksViewportWidth(): Boolean {
        return parent.size.width > preferredSize.width
    }

    override fun getScrollableTracksViewportHeight(): Boolean {
        return parent.size.height > preferredSize.height
    }
    //endregion
}