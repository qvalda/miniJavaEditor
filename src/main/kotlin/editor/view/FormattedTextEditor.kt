package editor.view

import editor.model.ITextEditorController
import helpers.DrawStateSaver.Companion.usingColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.Scrollable
import kotlin.math.roundToInt

class FormattedTextEditor(controller: ITextEditorController, itemsContainer: IViewItemsContainer) : JComponent(), Scrollable {

    private var initialized = false
    private var prevPreferredSize = Dimension(0, 0)
    private var measures = DrawMeasures(0,0,0)

    var controller = controller
        set(value) {
            field = value
            repaint()
        }

    var itemsContainer = itemsContainer
        set(value) {
            field = value
            value.onItemsUpdated += ::onRepaintRequest
            repaint()
        }

    init {
        isFocusable = true
        focusTraversalKeysEnabled = false

        itemsContainer.onItemsUpdated += ::onRepaintRequest

        val mouseListener = object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                onMousePressed(e)
            }

            override fun mouseDragged(e: MouseEvent) {
                onMouseDragged(e)
            }

            override fun mouseMoved(e: MouseEvent) {
                onMouseMoved(e)
            }
        }

        val keyListener = object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                onKeyPressed(e)
            }
        }

        addMouseListener(mouseListener)
        addMouseMotionListener(mouseListener)
        addKeyListener(keyListener)
    }

    public override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        drawBackground(g)
        setDefaultStyle(g)
        initValues(g)

        val visibleLineFrom = (g.clipBounds.y / measures.letterHeight).coerceIn(0, itemsContainer.size.height - 1)
        val visibleLineTo = ((g.clipBounds.y + g.clipBounds.height) / measures.letterHeight).coerceIn(0, itemsContainer.size.height - 1)

        for (lineIndex in visibleLineFrom..visibleLineTo) {
            val items = itemsContainer.getItems(lineIndex)
            for (i  in items) {
                i.draw(g, lineIndex, measures)
            }
        }
        updatePreferredSize()
    }

    private fun onKeyPressed(e: KeyEvent) {
        e.consume()
        if (hasCtrlModifier(e)) {
            when (e.keyCode) {
                KeyEvent.VK_A -> {
                    controller.selectAllAction()
                    return
                }

                KeyEvent.VK_X -> {
                    controller.cutAction()
                    return
                }

                KeyEvent.VK_C -> {
                    controller.copyAction()
                    return
                }

                KeyEvent.VK_V -> {
                    controller.pasteAction()
                    return
                }

                KeyEvent.VK_Z -> {
                    controller.undo()
                    return
                }

                KeyEvent.VK_Y -> {
                    controller.redo()
                    return
                }

                else -> return
            }
        }
        if (hasShiftModifier(e)) {
            when (e.keyCode) {
                KeyEvent.VK_UP -> {
                    controller.moveSelectionCaretUp()
                    return
                }

                KeyEvent.VK_DOWN -> {
                    controller.moveSelectionCaretDown()
                    return
                }

                KeyEvent.VK_LEFT -> {
                    controller.moveSelectionCaretLeft()
                    return
                }

                KeyEvent.VK_RIGHT -> {
                    controller.moveSelectionCaretRight()
                    return
                }
            }
        }
        when (e.keyCode) {
            KeyEvent.VK_UP -> controller.moveEnterCaretUp()
            KeyEvent.VK_DOWN -> controller.moveEnterCaretDown()
            KeyEvent.VK_LEFT -> controller.moveEnterCaretLeft()
            KeyEvent.VK_RIGHT -> controller.moveEnterCaretRight()

            KeyEvent.VK_HOME -> controller.homeAction()
            KeyEvent.VK_END -> controller.endAction()
            KeyEvent.VK_PAGE_UP -> controller.pageUpAction(parent.size.height / measures.letterHeight)
            KeyEvent.VK_PAGE_DOWN -> controller.pageDownAction(parent.size.height / measures.letterHeight)

            KeyEvent.VK_BACK_SPACE -> controller.backSpaceAction()
            KeyEvent.VK_DELETE -> controller.deleteAction()
            KeyEvent.VK_TAB -> controller.tabAction()
            KeyEvent.VK_ENTER -> controller.enterAction()
            else -> {
                if (e.keyChar.isDefined() && e.keyCode != KeyEvent.VK_ESCAPE && !hasCtrlModifier(e)) {
                    controller.addChar(e.keyChar)
                }
            }
        }
    }

    private fun onRepaintRequest(args: Unit) {
        repaint()
        updatePreferredSize()
        CoroutineScope(Dispatchers.Swing).launch { // bug visibleRect is not updated after preferredSize changed
            val desiredRect = Rectangle(controller.selectionCaret.column * measures.letterWidth, (controller.selectionCaret.line - 1) * measures.letterHeight,
                measures.letterWidth, measures.letterHeight * 3)
            if (!visibleRect.contains(desiredRect)) {
                scrollRectToVisible(desiredRect)
            }
        }
    }

    private fun onMousePressed(e: MouseEvent) {
        requestFocus()
        if (!hasShiftModifier(e)) {
            controller.setCarets(e.y / measures.letterHeight, (e.x.toFloat() / measures.letterWidth).roundToInt())
        }
        else{
            controller.setSelectionCaret(e.y / measures.letterHeight, (e.x.toFloat() / measures.letterWidth).roundToInt())
        }
    }

    private fun hasShiftModifier(e: MouseEvent) = (e.modifiersEx and KeyEvent.SHIFT_DOWN_MASK) != 0
    private fun hasShiftModifier(e: KeyEvent) = (e.modifiersEx and KeyEvent.SHIFT_DOWN_MASK) != 0
    private fun hasCtrlModifier(e: KeyEvent) = (e.modifiersEx and KeyEvent.CTRL_DOWN_MASK) != 0

    private var mousePoint = Point()

    private fun onMouseMoved(e: MouseEvent) {
        mousePoint = Point(e.y / measures.letterHeight, (e.x.toFloat() / measures.letterWidth).roundToInt())
    }

    private fun onMouseDragged(e: MouseEvent) {
        controller.setSelectionCaret(e.y / measures.letterHeight, (e.x.toFloat() / measures.letterWidth).roundToInt())
    }

    private fun drawBackground(g: Graphics) {
        usingColor(g, Style.Background.background!!) {
            val b = g.clipBounds
            g.fillRect(b.x, b.y, b.width, b.height)
        }
    }

    private fun updatePreferredSize() {
        val newPreferredSize = Dimension((itemsContainer.size.width + 10) * measures.letterWidth, itemsContainer.size.height * measures.letterHeight)
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

    private fun initValues(g: Graphics) {
        if (!initialized) {
            val letterHeight = g.fontMetrics.height
            val letterShift = g.fontMetrics.descent
            val letterWidth = g.fontMetrics.stringWidth("w")
            measures = DrawMeasures(letterHeight, letterShift, letterWidth)
            initialized = true
        }
    }

    //region Scrollable
    override fun getPreferredScrollableViewportSize(): Dimension {
        return preferredSize
    }

    override fun getScrollableUnitIncrement(visibleRect: Rectangle?, orientation: Int, direction: Int): Int {
        return measures.letterHeight
    }

    override fun getScrollableBlockIncrement(visibleRect: Rectangle?, orientation: Int, direction: Int): Int {
        return measures.letterHeight * 3
    }

    override fun getScrollableTracksViewportWidth(): Boolean {
        return parent.size.width > preferredSize.width
    }

    override fun getScrollableTracksViewportHeight(): Boolean {
        return parent.size.height > preferredSize.height
    }
    //endregion
}

