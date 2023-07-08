package editor.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TextEditorModelClipboardTest: TextEditorModelBaseTest() {

    @Test
    fun testCopyAction() {
        val model = creteTextEditorModelWithCaret("ab[cd]ef")
        model.copyAction()
        Assertions.assertEquals("cd", clipboard.getData())
    }

    @Test
    fun testCutAction() {
        val model = creteTextEditorModelWithCaret("ab[cd]ef")
        model.cutAction()
        Assertions.assertEquals("cd", clipboard.getData())
        assertTextAndCaret(model, "ab|ef")
        model.undo()
        assertTextAndCaret(model, "ab[cd]ef")
    }

    @Test
    fun testPasteAction() {
        val model = creteTextEditorModelWithCaret("ab|c")
        clipboard.setData("def")
        model.pasteAction()
        assertTextAndCaret(model, "abdef|c")
        model.undo()
        assertTextAndCaret(model, "ab|c")
    }

    @Test
    fun testPasteActionWithSelection() {
        val model = creteTextEditorModelWithCaret("a[b]c")
        clipboard.setData("def")
        model.pasteAction()
        assertTextAndCaret(model, "adef|c")
        model.undo()
        model.undo()
        assertTextAndCaret(model, "a[b]c")
    }

    @Test
    fun testPasteActionMultiline() {
        val model = creteTextEditorModelWithCaret("ab|c")
        clipboard.setData("d\r\nef")
        model.pasteAction()
        assertTextAndCaret(model, "abd\r\nef|c")
        model.undo()
        assertTextAndCaret(model, "ab|c")
    }
}