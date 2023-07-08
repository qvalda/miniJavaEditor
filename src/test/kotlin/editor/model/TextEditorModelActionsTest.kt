package editor.model

import org.junit.jupiter.api.Test

class TextEditorModelActionsTest: TextEditorModelBaseTest() {

    @Test
    fun testBackSpaceAction() {
        val model = creteTextEditorModelWithCaret("ab|c")
        model.backSpaceAction()
        assertTextAndCaret(model, "a|c")
        model.undo()
        assertTextAndCaret(model, "ab|c")
    }

    @Test
    fun testBackSpaceWithLineAction() {
        val model = creteTextEditorModelWithCaret("abc\r\nd|ef")
        model.backSpaceAction()
        model.backSpaceAction()
        assertTextAndCaret(model, "abc|ef")
        model.undo()
        model.undo()
        assertTextAndCaret(model, "abc\r\nd|ef")
    }

    @Test
    fun testBackSpaceAtTheBeginning() {
        val model = creteTextEditorModelWithCaret("|abc\r\ndef")
        model.backSpaceAction()
        assertTextAndCaret(model, "|abc\r\ndef")
        model.undo()
        assertTextAndCaret(model, "|abc\r\ndef")
    }

    @Test
    fun testBackSpaceWithSelection() {
        val model = creteTextEditorModelWithCaret("ab[cd]ef")
        model.backSpaceAction()
        assertTextAndCaret(model, "ab|ef")
        model.undo()
        assertTextAndCaret(model, "ab[cd]ef")
    }

    @Test
    fun testBackSpaceWithMultilineSelection() {
        val model = creteTextEditorModelWithCaret("ab[c\r\nd]ef")
        model.backSpaceAction()
        assertTextAndCaret(model, "ab|ef")
        model.undo()
        assertTextAndCaret(model, "ab[c\r\nd]ef")
    }

    @Test
    fun testDeleteAction() {
        val model = creteTextEditorModelWithCaret("a|bc")
        model.deleteAction()
        assertTextAndCaret(model, "a|c")
        model.undo()
        assertTextAndCaret(model, "a|bc")
    }

    @Test
    fun testDeleteWithLineAction() {
        val model = creteTextEditorModelWithCaret("ab|c\r\ndef")
        model.deleteAction()
        model.deleteAction()
        assertTextAndCaret(model, "ab|def")
        model.undo()
        model.undo()
        assertTextAndCaret(model, "ab|c\r\ndef")
    }

    @Test
    fun testDeleteAtTheEnd() {
        val model = creteTextEditorModelWithCaret("abc|")
        model.deleteAction()
        assertTextAndCaret(model, "abc|")
        model.undo()
        assertTextAndCaret(model, "abc|")
    }

    @Test
    fun testDeleteWithSelection() {
        val model = creteTextEditorModelWithCaret("ab[cde]f")
        model.deleteAction()
        assertTextAndCaret(model, "ab|f")
        model.undo()
        assertTextAndCaret(model, "ab[cde]f")
    }

    @Test
    fun testAddChar() {
        val model = creteTextEditorModelWithCaret("ab|c")
        model.addChar('d')
        assertTextAndCaret(model, "abd|c")
        model.undo()
        assertTextAndCaret(model, "ab|c")
    }

    @Test
    fun testAddCharWithSelection() {
        val model = creteTextEditorModelWithCaret("ab[cd]ef")
        model.addChar('g')
        assertTextAndCaret(model, "abg|ef")
        model.undo()
        model.undo()
        assertTextAndCaret(model, "ab[cd]ef")
    }

    @Test
    fun testTabAction() {
        val model = creteTextEditorModelWithCaret("ab|c")
        model.tabAction()
        assertTextAndCaret(model, "ab    |c")
        model.undo()
        assertTextAndCaret(model, "ab|c")
    }

    @Test
    fun testEnterAction() {
        val model = creteTextEditorModelWithCaret("ab|c")
        model.enterAction()
        assertTextAndCaret(model, "ab\r\n|c")
        model.undo()
        assertTextAndCaret(model, "ab|c")
    }

    @Test
    fun testEnterActionAtTheEnd() {
        val model = creteTextEditorModelWithCaret("abc|")
        model.enterAction()
        assertTextAndCaret(model, "abc\r\n|")
        model.undo()
        assertTextAndCaret(model, "abc|")
    }

    @Test
    fun testEnterActionWithOffset() {
        val model = creteTextEditorModelWithCaret("  abc|")
        model.enterAction()
        assertTextAndCaret(model, "  abc\r\n  |")
        model.undo()
        assertTextAndCaret(model, "  abc\r\n|")
        model.undo()
        assertTextAndCaret(model, "  abc|")
    }

    @Test
    fun testSelectAll() {
        val model = creteTextEditorModelWithCaret("abc\r\n|def")
        model.selectAllAction()
        assertTextAndCaret(model, "[abc\r\ndef]")
    }

    @Test
    fun testRedo() {
        val model = creteTextEditorModelWithCaret("abc|def")
        model.backSpaceAction()
        assertTextAndCaret(model, "ab|def")
        model.undo()
        assertTextAndCaret(model, "abc|def")
        model.redo()
        assertTextAndCaret(model, "ab|def")
    }
}