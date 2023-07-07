package editor.view

import editor.model.TextEditorCaret
import editor.view.item.Caret
import editor.view.item.ErrorViewItem
import editor.view.item.Selection
import editor.view.item.StringRow
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.only
import org.mockito.Mockito.verify
import java.awt.Graphics

class ViewItemsTest {

    private val measures = DrawMeasures(10, 2, 8)

    @Test
    fun drawTest() {
        val view = StringRow("abc")
        val g = createGraphics()
        view.draw(g, 2, measures)
        verify(g, only()).drawString("abc", 0, 10 + 2 * 10 - 2)
    }

    @Test
    fun drawCaret() {
        val view = Caret(TextEditorCaret(3, 2))
        val g = createGraphics()
        view.draw(g, 3, measures)
        verify(g).drawLine(2 * 8, 3 * 10, 2 * 8, (3 + 1) * 10)
    }

    @Test
    fun drawSelection() {
        val view = Selection(2, 4)
        val g = createGraphics()
        view.draw(g, 3, measures)
        verify(g).fillRect(2 * 8, 3 * 10, (4 - 2) * 8, 10)
    }

    @Test
    fun drawErrorViewItem() {
        val view = ErrorViewItem(2, 4, Style.Error)
        val g = createGraphics()
        view.draw(g, 3, measures)
        verify(g).drawLine(2 * 8, (3 + 1) * 10, 4 * 8, (3 + 1) * 10)
    }

    private fun createGraphics() = Mockito.mock(Graphics::class.java)
}