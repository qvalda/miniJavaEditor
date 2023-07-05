package editor.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TextEditorModelEventsTest : TextEditorModelBaseTest() {

    @Test
    fun callsOnLineAdd() {
        val model = creteTextEditorModelWithCaret("ab|c")
        var called = false
        model.onLineAdd += {
            called = true
            assertLineArgs(it, 1)
        }
        model.enterAction()
        assert(called)
    }

    @Test
    fun callsOnLineAddMultiple() {
        val model = creteTextEditorModelWithCaret("ab|c")
        var called = false
        model.onLineAdd += {
            called = true
            assertLineArgs(it, 1, 2)
        }
        clipboard.setData("d\r\ne\r\nf")
        model.pasteAction()
        assert(called)
    }

    @Test
    fun callsOnLineModified() {
        val model = creteTextEditorModelWithCaret("ab|c")
        var called = false
        model.onLineModified += {
            called = true
            assertLineArgs(it, 0)
        }
        model.addChar('d')
        assert(called)
    }

    @Test
    fun callsOnLineModifiedMultiple() {
        val model = creteTextEditorModelWithCaret("ab|c")
        var called = false
        model.onLineModified += {
            called = true
            assertLineArgs(it, 0)
        }
        clipboard.setData("d\r\ne\r\nf")
        model.pasteAction()
        assert(called)
    }

    @Test
    fun callsOnLineDelete() {
        val model = creteTextEditorModelWithCaret("ab\r\n|")
        var called = false
        model.onLineDelete += {
            called = true
            assertLineArgs(it, 1)
        }
        model.backSpaceAction()
        assert(called)
    }

    @Test
    fun callsOnLineDeleteMultiple() {
        val model = creteTextEditorModelWithCaret("ab[c\r\ndef\r\ng]")
        var called = false
        model.onLineDelete += {
            called = true
            assertLineArgs(it, 1, 2)
        }
        model.deleteAction()
        assert(called)
    }

    @Test
    fun callsOnCaretMove() {
        val model = creteTextEditorModelWithCaret("ab|def")
        var called = false
        model.onCaretMove += {
            called = true
            Assertions.assertEquals(0, it.line)
            Assertions.assertEquals(3, it.column)
        }
        model.moveEnterCaretRight()
        assert(called)
    }

    @Test
    fun updatesMaxLength() {
        val model = creteTextEditorModelWithCaret("ab|\r\ndef")
        Assertions.assertEquals(3, model.maxLength)
        model.addChar('q')
        model.addChar('2')
        Assertions.assertEquals(4, model.maxLength)
    }
}