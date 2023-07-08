package editor.view

import editor.model.TextEditorCaret
import editor.view.item.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.internal.verification.VerificationModeFactory.noInteractions
import java.awt.Font
import java.awt.Graphics

class ViewItemsTest {

    private val measures = DrawMeasures(10, 2, 8)
    private val graphics = mock(Graphics::class.java)

    @Test
    fun drawTest() {
        val view = StringRow("abc")
        val line = 3
        view.draw(graphics, line, measures)
        verify(graphics, only()).drawString("abc", 0, measures.letterHeight + line * measures.letterHeight - measures.letterShift)
    }

    @Test
    fun drawCaret() {
        val view = Caret(TextEditorCaret(3, 2))
        val line = 3
        view.draw(graphics, line, measures)
        verify(graphics).drawLine(2 * 8, line * measures.letterHeight, measures.letterShift * measures.letterWidth, (line + 1) * measures.letterHeight)
    }

    @Test
    fun drawSelection() {
        val view = Selection(2, 4)
        val line = 3
        view.draw(graphics, line, measures)
        verify(graphics).fillRect(2 * measures.letterWidth, line * measures.letterHeight, (4 - 2) * measures.letterWidth, measures.letterHeight)
    }

    @Test
    fun drawErrorViewItem() {
        val message = "message"
        val view = ErrorViewItem(message, 2, 4)
        val line = 3
        view.draw(graphics, line, measures)
        verify(graphics).drawLine(2 * measures.letterWidth, (line + 1) * measures.letterHeight, 4 * measures.letterWidth, (line + 1) * measures.letterHeight)
    }

    @Test
    fun drawColoredString() {
        val view = ColoredString("abc", 1, Style.Comment)
        `when`(graphics.font).thenReturn(Font(null, Font.PLAIN, 10))
        val line = 3
        view.draw(graphics, line, measures)
        verify(graphics).drawString("abc", 1 * measures.letterWidth, measures.letterHeight + line * measures.letterHeight - measures.letterShift)
    }

    @Test
    fun drawTooltipErrorViewItem() {
        val message = "message"
        val view = ErrorViewItem(message, 2, 4)
        val line = 3
        val column = 3

        view.drawTooltip(graphics, line, column, measures)
        val x = (column + 2) * measures.letterWidth
        val y = (line + 1) * measures.letterHeight
        verify(graphics).fillRect(x, y, message.length * measures.letterWidth, measures.letterHeight)
        verify(graphics).drawString(message, x, y + measures.letterHeight - measures.letterShift)
    }

    @Test
    fun skipDrawTooltipErrorViewItem() {
        val message = "message"
        val view = ErrorViewItem(message, 20, 40)
        val line = 3
        val column = 3

        view.drawTooltip(graphics, line, column, measures)
        val x = (column + 2) * measures.letterWidth
        val y = (line + 1) * measures.letterHeight
        verify(graphics, noInteractions()).fillRect(x, y, message.length * measures.letterWidth, measures.letterHeight)
        verify(graphics, noInteractions()).drawString(message, x, y + measures.letterHeight - measures.letterShift)
    }
}