package editor.view

import base.BaseTest.Companion.getPrivateProperty
import base.BaseTest.Companion.setAndReturnPrivateProperty
import editor.model.ITextEditorController
import editor.view.item.IViewItem
import helpers.ThrottleCall
import kotlinx.coroutines.runBlocking
import mocks.ViewItemsContainerMock
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.internal.verification.VerificationModeFactory.noInteractions
import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent


class TextEditorComponentTest {

    @Test
    fun testKeyPressedActions() {
        val controller = createController()
        val container = createContainer()

        val editor = createFormattedTextEditor(controller, container)

        val actions = mapOf<KeyEvent, (ITextEditorController) -> Unit>(
            keyEvent(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK) to { it.selectAllAction() },
            keyEvent(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK) to { it.cutAction() },
            keyEvent(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK) to { it.copyAction() },
            keyEvent(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK) to { it.pasteAction() },
            keyEvent(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK) to { it.undo() },
            keyEvent(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK) to { it.redo() },

            keyEvent(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK) to { it.moveSelectionCaretUp() },
            keyEvent(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK) to { it.moveSelectionCaretDown() },
            keyEvent(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK) to { it.moveSelectionCaretLeft() },
            keyEvent(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK) to { it.moveSelectionCaretRight() },

            keyEvent(KeyEvent.VK_UP) to { it.moveEnterCaretUp() },
            keyEvent(KeyEvent.VK_DOWN) to { it.moveEnterCaretDown() },
            keyEvent(KeyEvent.VK_LEFT) to { it.moveEnterCaretLeft() },
            keyEvent(KeyEvent.VK_RIGHT) to { it.moveEnterCaretRight() },
            keyEvent(KeyEvent.VK_HOME) to { it.homeAction() },
            keyEvent(KeyEvent.VK_END) to { it.endAction() },
            keyEvent(KeyEvent.VK_PAGE_UP) to { it.pageUpAction(1) },
            keyEvent(KeyEvent.VK_PAGE_DOWN) to { it.pageDownAction(1) },

            keyEvent(KeyEvent.VK_BACK_SPACE) to { it.backSpaceAction() },
            keyEvent(KeyEvent.VK_DELETE) to { it.deleteAction() },
            keyEvent(KeyEvent.VK_TAB) to { it.tabAction() },
            keyEvent(KeyEvent.VK_ENTER) to { it.enterAction() },

            keyEvent('a'.code) to { it.addChar('a') },
        )

        for (action in actions) {
            editor.keyListeners[0].keyPressed(action.key)
            action.value(verify(controller, only()))
            reset(controller)
        }
    }

    @Test
    fun testMouseShiftPressedActions() {
        val controller = createController()
        val container = createContainer()

        val editor = createFormattedTextEditor(controller, container)
        editor.mouseListeners[0].mousePressed(MouseEvent(editor, 0, 0, KeyEvent.SHIFT_DOWN_MASK, 5, 6, 1, false))
        verify(controller, only()).setSelectionCaret(6, 5)
    }

    @Test
    fun testMousePressedHandler() {
        val controller = createController()
        val container = createContainer()

        val editor = createFormattedTextEditor(controller, container)
        editor.mouseListeners[0].mousePressed(MouseEvent(editor, 0, 0, 0, 5, 6, 1, false))
        verify(controller, only()).setCarets(6, 5)
    }

    @Test
    fun testMouseDraggedHandler() {
        val controller = createController()
        val container = createContainer()

        val editor = createFormattedTextEditor(controller, container)
        editor.mouseMotionListeners[0].mouseDragged(MouseEvent(editor, 0, 0, 0, 5, 6, 1, false))
        verify(controller, only()).setSelectionCaret(6, 5)
    }

    @Test
    fun testMouseMoveHandler() {
        val controller = createController()
        val container = createContainer()

        val editor = createFormattedTextEditor(controller, container)
        editor.mouseMotionListeners[0].mouseMoved(MouseEvent(editor, 0, 0, 0, 5, 6, 1, false))

        assertFalse(editor.getPrivateProperty("renderTooltip") as Boolean)
        runBlocking {
            (editor.getPrivateProperty("showTooltip") as ThrottleCall).wait()
        }
        assertTrue(editor.getPrivateProperty("renderTooltip") as Boolean)

        editor.mouseMotionListeners[0].mouseMoved(MouseEvent(editor, 0, 0, 0, 6, 6, 1, false))
        assertFalse(editor.getPrivateProperty("renderTooltip") as Boolean)
    }

    @Test
    fun testPaintComponent() {
        val controller = createController()

        val views = listOf(
            mock(IViewItem::class.java),
            mock(IViewItem::class.java),
            mock(IViewItem::class.java),
            mock(IViewItem::class.java),
            mock(IViewItem::class.java),
            mock(IViewItem::class.java),
        )

        val items = mapOf(
            0 to listOf(views[0], views[1]),
            1 to listOf(views[2], views[3]),
            2 to listOf(views[4]),
            3 to listOf(views[5]),
        )
        val container = ViewItemsContainerMock(items)
        container.size = Dimension(2, items.size)

        val fontMetrics = mock(FontMetrics::class.java)
        `when`(fontMetrics.height).thenReturn(1)
        `when`(fontMetrics.descent).thenReturn(1)
        `when`(fontMetrics.stringWidth("w")).thenReturn(1)

        val graphics = mock(Graphics::class.java)
        `when`(graphics.clipBounds).thenReturn(Rectangle(1, 1, 1, 1))
        `when`(graphics.fontMetrics).thenReturn(fontMetrics)

        val editor = createFormattedTextEditor(controller, container)
        editor.paintComponent(graphics)

        verify(views[0], noInteractions()).draw(graphics, 0, DrawMeasures(0, 0, 0))
        verify(views[1], noInteractions()).draw(graphics, 0, DrawMeasures(0, 0, 0))
        verify(views[2], only()).draw(graphics, 1, editor.getPrivateProperty("measures") as DrawMeasures)
        verify(views[3], only()).draw(graphics, 1, editor.getPrivateProperty("measures") as DrawMeasures)
        verify(views[4], only()).draw(graphics, 2, editor.getPrivateProperty("measures") as DrawMeasures)
        verify(views[5], noInteractions()).draw(graphics, 0, DrawMeasures(0, 0, 0))
    }

    @Test
    fun testTooltipPaintComponent() {
        val controller = createController()

        val views = listOf(
            mock(IViewItem::class.java),
            mock(IViewItem::class.java),
            mock(IViewItem::class.java),
            mock(IViewItem::class.java),
            mock(IViewItem::class.java),
            mock(IViewItem::class.java),
        )

        val items = mapOf(
            0 to listOf(views[0], views[1]),
            1 to listOf(views[2], views[3]),
            2 to listOf(views[4]),
            3 to listOf(views[5]),
        )
        val container = ViewItemsContainerMock(items)
        container.size = Dimension(2, items.size)

        val fontMetrics = mock(FontMetrics::class.java)
        `when`(fontMetrics.height).thenReturn(1)
        `when`(fontMetrics.descent).thenReturn(1)
        `when`(fontMetrics.stringWidth("w")).thenReturn(1)

        val graphics = mock(Graphics::class.java)
        `when`(graphics.clipBounds).thenReturn(Rectangle(1, 1, 1, 1))
        `when`(graphics.fontMetrics).thenReturn(fontMetrics)

        val editor = createFormattedTextEditor(controller, container)
        editor.setAndReturnPrivateProperty("renderTooltip", true)
        editor.setAndReturnPrivateProperty("tooltipPoint", Point(1, 2))
        editor.paintComponent(graphics)

        verify(views[0], times(0)).drawTooltip(graphics, 0, 0, DrawMeasures(0, 0, 0))
        verify(views[1], times(0)).drawTooltip(graphics, 0, 0, DrawMeasures(0, 0, 0))
        verify(views[2], times(0)).drawTooltip(graphics, 0, 0, DrawMeasures(0, 0, 0))
        verify(views[3], times(0)).drawTooltip(graphics, 0, 0, DrawMeasures(0, 0, 0))
        verify(views[4], times(1)).drawTooltip(graphics, 2, 1, editor.getPrivateProperty("measures") as DrawMeasures)
        verify(views[5], times(0)).drawTooltip(graphics, 0, 0, DrawMeasures(0, 0, 0))
    }

    private fun createFormattedTextEditor(controller: ITextEditorController, container: ViewItemsContainerMock): TextEditorComponent {
        val editor = TextEditorComponent(controller, container)
        editor.setAndReturnPrivateProperty("measures", DrawMeasures(1, 1, 1))
        return editor
    }

    private fun keyEvent(key: Int, modifiers: Int = 0) = KeyEvent(createComponent(), 0, 0, modifiers, key, key.toChar())

    private fun createController() = mock(ITextEditorController::class.java)
    private fun createComponent() = mock(Component::class.java)
    private fun createContainer() = ViewItemsContainerMock()
}