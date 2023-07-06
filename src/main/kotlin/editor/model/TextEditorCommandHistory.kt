package editor.model

import java.util.*

class TextEditorCommandHistory {
    private var undoStack = Stack<ITextEditorCommand>()
    private var redoStack = Stack<ITextEditorCommand>()

    fun run(command: ITextEditorCommand) {
        command.execute()
        undoStack.push(command)
        redoStack.clear()
    }

    fun undo() {
        if (!undoStack.isEmpty()) {
            val command = undoStack.pop()
            redoStack.push(command)
            command.undo()
        }
    }

    fun redo() {
        if (!redoStack.isEmpty()) {
            val command = redoStack.pop()
            undoStack.push(command)
            command.execute()
        }
    }
}