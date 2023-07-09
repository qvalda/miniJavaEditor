package editor.view

import base.TestUtils.assertIs
import editor.view.item.Caret
import editor.view.item.Selection
import editor.view.item.StringRow
import mocks.TextEditorModelMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TextViewItemsContainerTest {

    @Test
    fun testSize() {
        val ic = createTextViewItemsContainer("ab\r\ndef")
        assertEquals(2, ic.size.height)
        assertEquals(3, ic.size.width)
    }

    @Test
    fun testGetItemsReturnText() {
        val ic = createTextViewItemsContainer("ab\r\ndef\r\ng")
        val items = ic.getItems(1)
        assertEquals(1, items.size)
        assertIs<StringRow>(items[0])
    }

    @Test
    fun testGetItemsWithCaret() {
        val ic = createTextViewItemsContainer("ab\r\nd|ef\r\ng")
        val items = ic.getItems(1)
        assertEquals(2, items.size)
        assertIs<StringRow>(items[0])
        assertIs<Caret>(items[1])
    }

    @Test
    fun testGetItemsWithSelection() {
        val ic = createTextViewItemsContainer("ab\r\nd[e]f\r\ng")
        val items = ic.getItems(1)
        assertEquals(4, items.size)
        assertIs<Selection>(items[0])
        assertIs<StringRow>(items[1])
        assertIs<Caret>(items[2])
        assertIs<Caret>(items[3])
    }

    @Test
    fun testGetItemsWithMultilineSelection() {
        val ic = createTextViewItemsContainer("ab\r\nc[d\r\nef\r\ng]h")
        val itemsOnLine1 = ic.getItems(1)
        assertEquals(3, itemsOnLine1.size)
        assertIs<Selection>(itemsOnLine1[0])
        assertIs<StringRow>(itemsOnLine1[1])
        assertIs<Caret>(itemsOnLine1[2])
        val itemsOnLine2 = ic.getItems(2)
        assertEquals(2, itemsOnLine2.size)
        assertIs<Selection>(itemsOnLine2[0])
        assertIs<StringRow>(itemsOnLine2[1])
        val itemsOnLine3 = ic.getItems(3)
        assertEquals(3, itemsOnLine3.size)
        assertIs<Selection>(itemsOnLine3[0])
        assertIs<StringRow>(itemsOnLine3[1])
        assertIs<Caret>(itemsOnLine3[2])
    }

    @Test
    fun testOnItemsUpdated() {
        val model = TextEditorModelMock("ab\r\nd[ef\r\ng]h")
        val ic = TextViewItemsContainer(model)
        var called = false
        ic.onItemsUpdated += { called = true }
        model.invokeOnModified()
        assertTrue(called)
    }

    private fun createTextViewItemsContainer(text: String): TextViewItemsContainer {
        return TextViewItemsContainer(TextEditorModelMock(text))
    }
}