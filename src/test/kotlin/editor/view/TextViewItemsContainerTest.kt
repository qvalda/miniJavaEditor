package editor.view

import base.BaseTest.Companion.assertIs
import editor.model.ITextEditorModel
import editor.model.LineChangeArgs
import editor.model.TextEditorCaret
import editor.model.TextEditorModelBaseTest.Companion.cleanTestText
import editor.model.TextEditorModelBaseTest.Companion.getCaretPos
import editor.model.TextEditorModelBaseTest.Companion.getEnterCaretPos
import editor.model.TextEditorModelBaseTest.Companion.getSelectionCaretPos
import editor.view.item.Caret
import editor.view.item.Selection
import editor.view.item.StringRow
import helpers.Event
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TextViewItemsContainerTest {

    @Test
    fun sizeTest(){
        val ic = createTextViewItemsContainer("ab\r\ndef")
        assertEquals(2, ic.size.height)
        assertEquals(3, ic.size.width)
    }

    @Test
    fun getItemsReturnText() {
        val ic = createTextViewItemsContainer("ab\r\ndef\r\ng")
        val items = ic.getItems(1)
        assertEquals(1, items.size)
        assertIs<StringRow>(items[0])
    }

    @Test
    fun getItemsWithCaret() {
        val ic = createTextViewItemsContainer("ab\r\nd|ef\r\ng")
        val items = ic.getItems(1)
        assertEquals(2, items.size)
        assertIs<StringRow>(items[0])
        assertIs<Caret>(items[1])
    }

    @Test
    fun getItemsWithSelection() {
        val ic = createTextViewItemsContainer("ab\r\nd[e]f\r\ng")
        val items = ic.getItems(1)
        assertEquals(4, items.size)
        assertIs<Selection>(items[0])
        assertIs<StringRow>(items[1])
        assertIs<Caret>(items[2])
        assertIs<Caret>(items[3])
    }

    @Test
    fun getItemsWithMultilineSelection() {
        val ic = createTextViewItemsContainer("ab\r\nd[ef\r\ng]")
        val items1 = ic.getItems(1)
        assertEquals(3, items1.size)
        assertIs<Selection>(items1[0])
        assertIs<StringRow>(items1[1])
        assertIs<Caret>(items1[2])
        val items2 = ic.getItems(2)
        assertEquals(3, items2.size)
        assertIs<Selection>(items2[0])
        assertIs<StringRow>(items2[1])
        assertIs<Caret>(items2[2])
    }

    private fun createTextViewItemsContainer(text: String): TextViewItemsContainer {
        return TextViewItemsContainer(TextEditorModelMock(text))
    }

    class TextEditorModelMock (text: String) : ITextEditorModel{
        override val onModified = Event<LineChangeArgs>()
        override val onLineDelete = Event<LineChangeArgs>()
        override val onLineModified = Event<LineChangeArgs>()
        override val onLineAdd = Event<LineChangeArgs>()
        override val onCaretMove = Event<TextEditorCaret>()

        override var enterCaret = TextEditorCaret()
        override var selectionCaret = TextEditorCaret()
        override var maxLength: Int = 0
        override var linesCount: Int = 0

        private val lines: List<String>

        init {
            lines = cleanTestText(text).split("\r\n")

            val caretPos = getCaretPos(text)
            val enterCaretPos = getEnterCaretPos(text)
            val selectionCaretPos = getSelectionCaretPos(text)

            if (enterCaretPos != null && selectionCaretPos != null) {
                enterCaret = TextEditorCaret(enterCaretPos.first, enterCaretPos.second)
                selectionCaret = TextEditorCaret(selectionCaretPos.first, selectionCaretPos.second)
            } else if (caretPos != null) {
                enterCaret = TextEditorCaret(caretPos.first, caretPos.second)
                selectionCaret = enterCaret.copy()
            }
            linesCount = lines.size
            maxLength = lines.maxByOrNull { l -> l.length }?.length ?: 0
        }

        override fun getLine(lineIndex: Int): String {
            return lines[lineIndex]
        }

        override fun getLines(): List<String> {
            return lines
        }
    }
}