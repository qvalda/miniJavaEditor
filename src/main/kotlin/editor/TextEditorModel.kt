package editor

import helpers.Event
import kotlin.math.max
import kotlin.math.min

class TextEditorModel (text:String = "", private val clipboard: IClipboard = SystemClipboard()) {
    val onLineDelete = Event<LineChangeArgs>()
    val onLineModified = Event<Int>()
    val onLineAdd = Event<Int>()
    val onCaretMove = Event<TextEditorCaret>()
    val onViewModified = Event<Unit>()

    var lines = mutableListOf<String>()
    var maxLength = 0
    var enterCaret = TextEditorCaret()
    var selectionCaret = TextEditorCaret()
    private val commands = TextEditorCommandHistory()

    init {
        val input = preprocessText(text)
        maxLength = 0
        for (s in input.split('\n')) {
            lines.add(s)
            maxLength = max(maxLength, s.length)
        }
        enterCaret = TextEditorCaret()
        selectionCaret = TextEditorCaret()
    }

    private var hasViewChanges = false
    private var isTrackingActive = false
    private fun trackChanges(statement: () -> Unit) {
        if (isTrackingActive) {
            statement()
            return
        }
        isTrackingActive = true
        val initialEnterCaret = enterCaret.copy()
        val initialSelectionCaret = selectionCaret.copy()
        hasViewChanges = false
        statement()
        if (initialEnterCaret != enterCaret) {
            onCaretMove(enterCaret)
        }
        if (initialEnterCaret != enterCaret || initialSelectionCaret != selectionCaret || hasViewChanges) {
            onViewModified(Unit)
        }
        isTrackingActive = false
    }

    private val hasSelection
        get() = enterCaret != selectionCaret

    private fun preprocessText(input: String): String {
        return input.replace("\r", "").replace("\t", "    ")
    }

    fun getText(): String {
        return lines.joinToString("\r\n")
    }

    fun getSelectedText(): String? {
        if (enterCaret == selectionCaret) return null

        val minCaret = minOf(enterCaret, selectionCaret)
        val maxCaret = maxOf(enterCaret, selectionCaret)

        if (minCaret.line == maxCaret.line) {
            return getSubstring(minCaret.line, minCaret.column, maxCaret.column)
        } else {
            val suffix = getSuffix(minCaret.line, minCaret.column)
            val prefix = getPrefix(maxCaret.line, maxCaret.column)
            val body = lines.subList(minCaret.line + 1, maxCaret.line)
            return (arrayOf(suffix) + body + arrayOf(prefix)).joinToString("\r\n")
        }
    }

    private fun getPrefix(lineIndex: Int, columnIndex: Int): String {
        return lines[lineIndex].substring(0, columnIndex)
    }

    private fun getSuffix(lineIndex: Int, columnIndex: Int): String {
        return lines[lineIndex].substring(columnIndex, lines[lineIndex].length)
    }

    private fun getSubstring(lineIndex: Int, columnBeginIndex: Int, columnEndIndex: Int): String {
        return lines[lineIndex].substring(columnBeginIndex, columnEndIndex)
    }

    private fun updateMaxLength() {
        maxLength = lines.maxByOrNull { l -> l.length }?.length ?: 0
    }

    //region text update
    private fun deleteLine(lineIndex: Int, count: Int = 1) {
        onLineDelete(LineChangeArgs(lineIndex, count))
        lines.subList(lineIndex, lineIndex + count).clear()
        updateMaxLength()
    }

    private fun addLine(lineIndex: Int, line: String) {
        lines.add(lineIndex, line)
        onLineAdd(lineIndex)
        updateMaxLength()
    }

    private fun removeRangeInLine(lineIndex: Int, startIndex: Int, endIndex: Int) {
        lines[lineIndex] = lines[lineIndex].removeRange(startIndex, endIndex)
        onLineModified(lineIndex)
        updateMaxLength()
    }

    private fun appendToLine(lineIndex: Int, appendix: String) {
        lines[lineIndex] += appendix
        onLineModified(lineIndex)
        updateMaxLength()
    }

    private fun insertInLine(lineIndex: Int, columnIndex: Int, value: String) {
        lines[lineIndex] = StringBuilder(lines[lineIndex]).insert(columnIndex, value).toString()
        onLineModified(lineIndex)
        updateMaxLength()
    }

    private fun insertInLine(lineIndex: Int, columnIndex: Int, values: List<String>) {

        val suffix = getSuffix(lineIndex, columnIndex)
        lines[lineIndex] = lines[lineIndex].removeRange(columnIndex, lines[lineIndex].length) + values[0]
        onLineModified(lineIndex)
        for (index in 1 until values.size - 1) {
            lines.add(lineIndex + index, values[index])
            onLineAdd(lineIndex + index)
        }
        val lastIndex = lineIndex + values.size - 1
        val lastValue = values.last() + suffix
        lines.add(lastIndex, lastValue)
        onLineAdd(lastIndex)

        updateMaxLength()
    }

    //endregion

    //region public actions
    fun backSpaceAction() {
        trackChanges {
            if (hasSelection) {
                commands.add(DeleteSelectionCommand(this))
            } else {
                commands.add(BackSpaceCommand(this))
            }
        }
    }

    fun deleteAction() {
        trackChanges {
            if (hasSelection) {
                commands.add(DeleteSelectionCommand(this))
            } else {
                commands.add(DeleteCommand(this))
            }
        }
    }

    fun addChar(keyChar: Char) {
        trackChanges {
            if (hasSelection) {
                commands.add(DeleteSelectionCommand(this))
            }
            commands.add(InsertSingleLineCommand(this, keyChar.toString()))
        }
    }

    fun tabAction() {
        trackChanges {
            if (hasSelection) {
                commands.add(DeleteSelectionCommand(this))
            }
            commands.add(InsertSingleLineCommand(this, "    "))
        }
    }

    fun enterAction() {
        trackChanges {
            if (hasSelection) {
                commands.add(DeleteSelectionCommand(this))
            }
            commands.add(InsertNewLineCommand(this))
        }
    }

    fun undo() {
        trackChanges {
            commands.undo()
        }
    }

    fun redo() {
        trackChanges {
            commands.redo()
        }
    }

    fun selectAllAction() {
        trackChanges {
            enterCaret = TextEditorCaret(0, 0)
            selectionCaret = TextEditorCaret(lines.lastIndex, lines.last().length)
        }
    }

    fun cutAction() {
        trackChanges {
            copyAction()
            if (hasSelection) {
                commands.add(DeleteSelectionCommand(this))
            }
        }
    }

    fun copyAction() {
        trackChanges {
            val text = getSelectedText()
            if (text != null) {
                clipboard.setData(text)
            }
        }
    }

    fun pasteAction() {
        trackChanges {
            val text = clipboard.getData()
            if (!text.isNullOrEmpty()) {
                if (hasSelection) {
                    commands.add(DeleteSelectionCommand(this))
                }
                val input = preprocessText(text).split('\n')
                if (input.size == 1) {
                    commands.add(InsertSingleLineCommand(this, input[0]))
                } else {
                    commands.add(InsertMultiLineCommand(this, input))
                }
            }
        }
    }

    fun homeAction() {
        trackChanges {
            setCaret(getAdjustedCaret(enterCaret.line, 0))
        }
    }

    fun endAction() {
        trackChanges {
            setCaret(getAdjustedCaret(enterCaret.line, lines[enterCaret.line].length))
        }
    }

    fun pageUpAction(offset: Int) {
        trackChanges {
            setCaret(getAdjustedCaret(enterCaret.line - offset, enterCaret.column))
        }
    }

    fun pageDownAction(offset: Int) {
        trackChanges {
            setCaret(getAdjustedCaret(enterCaret.line + offset, enterCaret.column))
        }
    }

    //endregion

    //region caret

    fun moveEnterCaretLeft() {
        trackChanges {
            enterCaret = getCaretLeft(enterCaret)
            selectionCaret = enterCaret.copy()
        }
    }

    fun moveSelectionCaretLeft() {
        trackChanges {
            selectionCaret = getCaretLeft(selectionCaret)
        }
    }

    fun moveEnterCaretRight() {
        trackChanges {
            enterCaret = getCaretRight(enterCaret)
            selectionCaret = enterCaret.copy()
        }
    }

    fun moveSelectionCaretRight() {
        trackChanges {
            selectionCaret = getCaretRight(selectionCaret)
        }
    }

    fun moveEnterCaretDown() {
        trackChanges {
            enterCaret = getCaretDown(enterCaret)
            selectionCaret = enterCaret.copy()
        }
    }

    fun moveSelectionCaretDown() {
        trackChanges {
            selectionCaret = getCaretDown(selectionCaret)
        }
    }

    fun moveEnterCaretUp() {
        trackChanges {
            enterCaret = getCaretUp(enterCaret)
            selectionCaret = enterCaret.copy()
        }
    }

    fun moveSelectionCaretUp() {
        trackChanges {
            selectionCaret = getCaretUp(selectionCaret)
        }
    }

    fun updateSelectionCaret(lineIndex: Int, columnIndex: Int) {
        trackChanges {
            selectionCaret = getAdjustedCaret(lineIndex, columnIndex)
        }
    }

    fun updateEnterCaret(lineIndex: Int, columnIndex: Int) {
        trackChanges {
            enterCaret = getAdjustedCaret(lineIndex, columnIndex)
        }
    }

    fun updateCarets(lineIndex: Int, columnIndex: Int) {
        trackChanges {
            enterCaret = getAdjustedCaret(lineIndex, columnIndex)
            selectionCaret = enterCaret.copy()
        }
    }

    private fun getCaretLeft(caret: TextEditorCaret): TextEditorCaret {
        if (caret.line == 0 && caret.column == 0) return caret
        if (caret.column > 0) {
            return TextEditorCaret(caret.line, caret.column - 1)
        } else {
            val line = max(caret.line - 1, 0)
            return TextEditorCaret(line, lines[line].length)
        }
    }

    private fun getCaretUp(caret: TextEditorCaret): TextEditorCaret {
        val line = max(caret.line - 1, 0)
        val column = min(caret.column, lines[line].length)
        return TextEditorCaret(line, column)
    }

    private fun getCaretRight(caret: TextEditorCaret): TextEditorCaret {
        if (caret.line == lines.size - 1 && caret.column == lines[lines.size - 1].length) return caret
        if (caret.column < lines[caret.line].length) {
            return TextEditorCaret(caret.line, caret.column + 1)
        } else {
            return TextEditorCaret(min(caret.line + 1, lines.size - 1), 0)
        }
    }

    private fun getCaretDown(caret: TextEditorCaret): TextEditorCaret {
        val line = min(caret.line + 1, lines.size - 1)
        val column = min(caret.column, lines[line].length)
        return TextEditorCaret(line, column)
    }

    private fun getAdjustedCaret(lineIndex: Int, columnIndex: Int): TextEditorCaret {
        val line = lineIndex.coerceIn(0, lines.size - 1)
        val column = columnIndex.coerceIn(0, lines[line].length)
        return TextEditorCaret(line, column)
    }

    private fun setCaret(line: Int, column: Int) {
        trackChanges {
            setCaret(TextEditorCaret(line, column))
        }
    }

    private fun setCaret(caret: TextEditorCaret) {
        trackChanges {
            enterCaret = caret.copy()
            selectionCaret = caret.copy()
        }
    }

    private fun setCaret(begin: TextEditorCaret, end: TextEditorCaret) {
        trackChanges {
            enterCaret = begin.copy()
            selectionCaret = end.copy()
        }
    }

    //endregion

    class DeleteSelectionCommand(private val textEditorModel: TextEditorModel) : ITextEditorCommand {
        private var minCaret: TextEditorCaret
        private var maxCaret: TextEditorCaret
        private var lines: List<String>

        init {
            minCaret = minOf(textEditorModel.enterCaret, textEditorModel.selectionCaret)
            maxCaret = maxOf(textEditorModel.enterCaret, textEditorModel.selectionCaret)
            lines = textEditorModel.lines.subList(minCaret.line, maxCaret.line + 1).toList() //todo optimize
        }

        override fun execute() {
            textEditorModel.setCaret(minCaret, maxCaret)

            if (minCaret.line == maxCaret.line) {
                textEditorModel.removeRangeInLine(minCaret.line, minCaret.column, maxCaret.column)
            } else {
                textEditorModel.removeRangeInLine(minCaret.line, minCaret.column, textEditorModel.lines[minCaret.line].length)
                val suffix = textEditorModel.getSuffix(maxCaret.line, maxCaret.column)
                textEditorModel.appendToLine(minCaret.line, suffix)
                val range = maxCaret.line - minCaret.line
                textEditorModel.deleteLine(minCaret.line + 1, range)
            }
            textEditorModel.setCaret(minCaret)
        }

        override fun undo() {
            textEditorModel.setCaret(minCaret, maxCaret)

            textEditorModel.deleteLine(minCaret.line)
            for ((index, line) in lines.withIndex()) { //todo optimize
                textEditorModel.addLine(minCaret.line + index, line)
            }

            textEditorModel.hasViewChanges = true
        }
    }

    class BackSpaceCommand(private val textEditorModel: TextEditorModel) : ITextEditorCommand {
        private var caretState: TextEditorCaret
        private lateinit var char: String
        private lateinit var result: BackSpaceResult
        private var length: Int = 0

        init {
            caretState = textEditorModel.enterCaret.copy()
        }

        override fun execute() {
            textEditorModel.setCaret(caretState)

            result = when {
                textEditorModel.enterCaret.column == 0 && textEditorModel.enterCaret.line == 0 -> BackSpaceResult.None
                textEditorModel.enterCaret.column == 0 -> BackSpaceResult.LineRemoved
                else -> BackSpaceResult.CharRemoved
            }

            when (result) {
                BackSpaceResult.None -> return

                BackSpaceResult.LineRemoved -> {
                    length = textEditorModel.lines[textEditorModel.enterCaret.line - 1].length
                    val suffix = textEditorModel.getSuffix(textEditorModel.enterCaret.line, 0)
                    textEditorModel.deleteLine(textEditorModel.enterCaret.line)
                    textEditorModel.moveEnterCaretLeft()
                    textEditorModel.appendToLine(textEditorModel.enterCaret.line, suffix)
                }

                BackSpaceResult.CharRemoved -> {
                    char = textEditorModel.getSubstring(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column - 1, textEditorModel.enterCaret.column)

                    textEditorModel.removeRangeInLine(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column - 1, textEditorModel.enterCaret.column)
                    textEditorModel.moveEnterCaretLeft()
                }
            }
        }

        override fun undo() {
            textEditorModel.setCaret(caretState)

            when (result) {
                BackSpaceResult.None -> return

                BackSpaceResult.LineRemoved -> {
                    val suffix = textEditorModel.getSuffix(textEditorModel.enterCaret.line - 1, length)
                    textEditorModel.removeRangeInLine(textEditorModel.enterCaret.line - 1, length, textEditorModel.lines[textEditorModel.enterCaret.line - 1].length)
                    textEditorModel.addLine(textEditorModel.enterCaret.line, suffix)
                }

                BackSpaceResult.CharRemoved -> {
                    textEditorModel.insertInLine(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column - 1, char)
                }
            }
        }

        enum class BackSpaceResult {
            None,
            LineRemoved,
            CharRemoved,
        }
    }

    class DeleteCommand(private val textEditorModel: TextEditorModel) : ITextEditorCommand {
        private var caretState: TextEditorCaret
        private lateinit var result: DeleteResult
        private lateinit var innerCommand: ITextEditorCommand

        init {
            caretState = textEditorModel.enterCaret.copy()
        }

        override fun execute() {
            textEditorModel.setCaret(caretState)

            result = when {
                textEditorModel.enterCaret.column == textEditorModel.lines.last().length
                        && textEditorModel.enterCaret.line == textEditorModel.lines.size - 1 -> DeleteResult.None

                else -> DeleteResult.Deleted
            }

            when (result) {
                DeleteResult.None -> return
                DeleteResult.Deleted -> {
                    textEditorModel.moveEnterCaretRight()
                    innerCommand = BackSpaceCommand(textEditorModel)
                    innerCommand.execute()
                    textEditorModel.hasViewChanges = true //todo fix hack
                }
            }
        }

        override fun undo() {
            when (result) {
                DeleteResult.None -> return
                DeleteResult.Deleted -> {
                    innerCommand.undo()
                    textEditorModel.setCaret(caretState)
                    textEditorModel.hasViewChanges = true
                }
            }
        }

        enum class DeleteResult {
            None,
            Deleted
        }
    }

    class InsertSingleLineCommand(private val textEditorModel: TextEditorModel, private val text: String) : ITextEditorCommand {
        private var caretState: TextEditorCaret

        init {
            caretState = textEditorModel.enterCaret.copy()
        }

        override fun execute() {
            textEditorModel.setCaret(caretState)

            textEditorModel.insertInLine(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column, text)
            textEditorModel.setCaret(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column + text.length)
        }

        override fun undo() {
            textEditorModel.setCaret(caretState)

            textEditorModel.removeRangeInLine(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column, textEditorModel.enterCaret.column + text.length)
        }
    }

    class InsertNewLineCommand(private val textEditorModel: TextEditorModel) : ITextEditorCommand {
        private var caretState: TextEditorCaret

        init {
            caretState = textEditorModel.enterCaret.copy()
        }

        override fun execute() {
            textEditorModel.setCaret(caretState)

            val suffix = textEditorModel.getSuffix(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column)
            textEditorModel.removeRangeInLine(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column, textEditorModel.lines[textEditorModel.enterCaret.line].length)
            textEditorModel.addLine(textEditorModel.enterCaret.line + 1, suffix)
            textEditorModel.moveEnterCaretRight()
        }

        override fun undo() {
            textEditorModel.setCaret(caretState)

            val suffix = textEditorModel.lines[textEditorModel.enterCaret.line + 1]
            textEditorModel.appendToLine(textEditorModel.enterCaret.line, suffix)
            textEditorModel.deleteLine(textEditorModel.enterCaret.line + 1)
        }
    }

    class InsertMultiLineCommand(private val textEditorModel: TextEditorModel, private val lines: List<String>) : ITextEditorCommand {
        private var caretState: TextEditorCaret

        init {
            caretState = textEditorModel.enterCaret.copy()
        }

        override fun execute() {
            textEditorModel.setCaret(caretState)

            textEditorModel.insertInLine(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column, lines)

            val line = textEditorModel.enterCaret.line + lines.size - 1
            val column = lines.last().length
            textEditorModel.setCaret(line, column)
            textEditorModel.hasViewChanges = true
        }

        override fun undo() {
            textEditorModel.setCaret(caretState)

            val suffix = textEditorModel.getSuffix(textEditorModel.enterCaret.line + lines.size - 1, lines.last().length)

            textEditorModel.removeRangeInLine(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column, textEditorModel.enterCaret.column + lines.first().length)
            textEditorModel.appendToLine(textEditorModel.enterCaret.line, suffix)
            textEditorModel.deleteLine(textEditorModel.enterCaret.line + 1, lines.size - 1)
            textEditorModel.hasViewChanges = true
        }
    }
}