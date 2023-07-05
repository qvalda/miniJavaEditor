package editor.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TextEditorCommandHistoryTest {

    @Test
    fun executeOnAdd() {
        val history = TextEditorCommandHistory()
        val c = TestCommand()
        history.add(c)
        assertTrue(c.isExecuted)
    }

    @Test
    fun canUndo() {
        val history = TextEditorCommandHistory()
        val c = TestCommand()
        history.add(c)
        c.undo()
        assertTrue(c.isUndo)
    }

    @Test
    fun undoRedoDoNotThrow() {
        val history = TextEditorCommandHistory()
        val c = TestCommand()
        history.add(c)
        history.undo()
        history.undo()
        history.undo()
        history.redo()
        history.redo()
    }

    @Test
    fun trackSeveralCommands() {
        val history = TextEditorCommandHistory()
        val c1 = TestCommand()
        val c2 = TestCommand()
        history.add(c1)
        history.add(c2)
        history.undo()
        assertTrue(c2.isUndo)
        assertFalse(c1.isUndo)
        history.undo()
        assertTrue(c2.isUndo)
        assertTrue(c1.isUndo)
        history.redo()
        assertFalse(c2.isExecuted)
        assertTrue(c1.isExecuted)
        history.redo()
        assertTrue(c2.isExecuted)
        assertTrue(c1.isExecuted)
    }

    class TestCommand : ITextEditorCommand {
        var isExecuted = false
        var isUndo = false

        override fun execute() {
            isExecuted = true
            isUndo = false
        }

        override fun undo() {
            isExecuted = false
            isUndo = true
        }
    }
}