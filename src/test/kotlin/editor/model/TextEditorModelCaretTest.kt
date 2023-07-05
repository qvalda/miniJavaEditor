package editor.model

import org.junit.jupiter.api.Test

class TextEditorModelCaretTest : TextEditorModelBaseTest() {

    @Test
    fun testCarteMoveLeft() {
        val model = creteTextEditorModelWithCaret("ab|c")
        model.moveEnterCaretLeft()
        assertEnterCaret(model, 0, 1)
    }

    @Test
    fun testCarteMoveLeftNewLine() {
        val model = creteTextEditorModelWithCaret("abc\r\n|d")
        model.moveEnterCaretLeft()
        assertEnterCaret(model, 0, 3)
    }

    @Test
    fun testCarteMoveLeftAtTheBeginning() {
        val model = creteTextEditorModelWithCaret("|abc")
        model.moveEnterCaretLeft()
        assertEnterCaret(model, 0, 0)
    }

    @Test
    fun testCarteMoveRight() {
        val model = creteTextEditorModelWithCaret("a|bc")
        model.moveEnterCaretRight()
        assertEnterCaret(model, 0, 2)
    }

    @Test
    fun testCarteMoveRightNewLine() {
        val model = creteTextEditorModelWithCaret("abc|\r\nd")
        model.moveEnterCaretRight()
        assertEnterCaret(model, 1, 0)
    }

    @Test
    fun testCarteMoveRightAtTheEnd() {
        val model = creteTextEditorModelWithCaret("abc|")
        model.moveEnterCaretRight()
        assertEnterCaret(model, 0, 3)
    }

    @Test
    fun testCarteMoveUp() {
        val model = creteTextEditorModelWithCaret("abc\r\nde|f")
        model.moveEnterCaretUp()
        assertEnterCaret(model, 0, 2)
    }

    @Test
    fun testCarteMoveDown() {
        val model = creteTextEditorModelWithCaret("a|bc\r\ndef")
        model.moveEnterCaretDown()
        assertEnterCaret(model, 1, 1)
    }

    @Test
    fun testHomeAction() {
        val model = creteTextEditorModelWithCaret("ab|c\r\ndef")
        model.homeAction()
        assertEnterCaret(model, 0, 0)
    }

    @Test
    fun testEndAction() {
        val model = creteTextEditorModelWithCaret("ab|c\r\ndef")
        model.endAction()
        assertEnterCaret(model, 0, 3)
    }

    @Test
    fun testPageUpAction() {
        val model = creteTextEditorModelWithCaret("abc\r\ndef\r\nabc\r\nd|ef")
        model.pageUpAction(2)
        assertEnterCaret(model, 1, 1)
    }

    @Test
    fun testPageDownAction() {
        val model = creteTextEditorModelWithCaret("ab|c\r\ndef\r\nabc\r\ndef")
        model.pageDownAction(3)
        assertEnterCaret(model, 3, 2)
    }

    @Test
    fun testCaretAdjusted() {
        val model = creteTextEditorModelWithCaret("ab|c\r\ndef")
        model.setCarets(100, 100)
        assertEnterCaret(model, 1, 3)
    }

    @Test
    fun testSelectionCaretLeftAction() {
        val model = creteTextEditorModelWithCaret("ab[cd]ef")
        model.moveSelectionCaretLeft()
        assertTextAndCaret(model, "ab[c]def")
    }

    @Test
    fun testSelectionCaretRightAction() {
        val model = creteTextEditorModelWithCaret("ab[cd]ef")
        model.moveSelectionCaretRight()
        assertTextAndCaret(model, "ab[cde]f")
    }

    @Test
    fun testSelectionCaretUpAction() {
        val model = creteTextEditorModelWithCaret("ab[c\r\nzxc\r\nd]ef")
        model.moveSelectionCaretUp()
        assertTextAndCaret(model, "ab[c\r\nz]xc\r\ndef")
    }

    @Test
    fun testSelectionCaretDownAction() {
        val model = creteTextEditorModelWithCaret("ab[c]\r\ndefg")
        model.moveSelectionCaretDown()
        assertTextAndCaret(model, "ab[c\r\ndef]g")
    }
}