package editor

import helpers.Event
import kotlin.math.max
import kotlin.math.min

class TextEditorModel (text:String = "", private val clipboard: IClipboard = SystemClipboard()) {
    val onLineDelete = Event<LineChangeArgs>()
    val onLineModified = Event<Int>()
    val onLineAdd = Event<LineChangeArgs>()
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
        hasViewChanges = true
        maxLength = lines.maxByOrNull { l -> l.length }?.length ?: 0
    }

    //region text update

    private fun removeRangeInLine(lineIndex: Int, startIndex: Int, endIndex: Int) {
        lines[lineIndex] = lines[lineIndex].removeRange(startIndex, endIndex)
        onLineModified(lineIndex)
        updateMaxLength()
    }

    private fun removeRangeInLines(lineIndex: Int, startIndex: Int, endLineIndex: Int, endIndex: Int) {
        lines[lineIndex] = getPrefix(lineIndex, startIndex) + getSuffix(endLineIndex, endIndex)
        onLineModified(lineIndex)
        onLineDelete(LineChangeArgs(lineIndex + 1, endLineIndex - lineIndex))
        lines.subList(lineIndex + 1, endLineIndex + 1).clear()
        updateMaxLength()
    }

    private fun insertInLine(lineIndex: Int, columnIndex: Int, value: String) {
        lines[lineIndex] = StringBuilder(lines[lineIndex]).insert(columnIndex, value).toString()
        onLineModified(lineIndex)
        updateMaxLength()
    }

    private fun insertInLine(lineIndex: Int, columnIndex: Int, values: List<String>) {
        val suffix = getSuffix(lineIndex, columnIndex)
        lines[lineIndex] = getPrefix(lineIndex, columnIndex) + values[0]
        onLineModified(lineIndex)
        if (values.size > 2) {
            lines.addAll(lineIndex + 1, values.drop(1).dropLast(1))
            onLineAdd(LineChangeArgs(lineIndex + 1, values.size - 2))
        }
        val lastIndex = lineIndex + values.size - 1
        val lastValue = values.last() + suffix
        lines.add(lastIndex, lastValue)
        onLineAdd(LineChangeArgs(lastIndex))
        updateMaxLength()
    }

    private fun insertLineBreak(lineIndex: Int, columnIndex: Int) {
        insertInLine(lineIndex, columnIndex, listOf("", ""))
    }

    private fun removeLineBreak(lineIndex: Int, columnIndex: Int) {
        lines[lineIndex] += lines[lineIndex + 1]
        onLineModified(lineIndex)
        onLineDelete(LineChangeArgs(lineIndex + 1))
        lines.removeAt(lineIndex + 1)
        updateMaxLength()
    }

    //endregion

    //region public actions
    fun backSpaceAction() {
        trackChanges {
            if (!deleteSelection() && !(enterCaret.column == 0 && enterCaret.line == 0)) {
                commands.add(BackSpaceCommand(this))
            }
        }
    }

    fun deleteAction() {
        trackChanges {
            if (!deleteSelection() && !(enterCaret.column == lines.last().length && enterCaret.line == lines.size - 1)) {
                commands.add(DeleteCommand(this))
            }
        }
    }

    fun addChar(keyChar: Char) {
        trackChanges {
            deleteSelection()
            commands.add(InsertSingleLineCommand(this, keyChar.toString()))
        }
    }

    fun tabAction() {
        trackChanges {
            deleteSelection()
            commands.add(InsertSingleLineCommand(this, "    "))
        }
    }

    fun enterAction() {
        trackChanges {
            deleteSelection()
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
            deleteSelection()
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
                deleteSelection()
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

    private fun deleteSelection(): Boolean {
        if (!hasSelection) return false
        if (enterCaret.line == selectionCaret.line) {
            commands.add(DeleteSingleSelectionCommand(this))
        } else {
            commands.add(DeleteMultiSelectionCommand(this))
        }
        return true
    }

    class DeleteSingleSelectionCommand(private val textEditorModel: TextEditorModel) : ITextEditorCommand {
        private var minCaret: TextEditorCaret
        private var maxCaret: TextEditorCaret
        private var text: String

        init {
            minCaret = minOf(textEditorModel.enterCaret, textEditorModel.selectionCaret)
            maxCaret = maxOf(textEditorModel.enterCaret, textEditorModel.selectionCaret)
            text = textEditorModel.getSelectedText()!!
        }

        override fun execute() {
            textEditorModel.setCaret(minCaret)
            textEditorModel.removeRangeInLine(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column, textEditorModel.enterCaret.column + text.length)
        }

        override fun undo() {
            textEditorModel.setCaret(minCaret, maxCaret)
            textEditorModel.insertInLine(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column, text)
        }
    }

    class DeleteMultiSelectionCommand(private val textEditorModel: TextEditorModel) : ITextEditorCommand {
        private var minCaret: TextEditorCaret
        private var maxCaret: TextEditorCaret
        private var lines: List<String>

        init {
            minCaret = minOf(textEditorModel.enterCaret, textEditorModel.selectionCaret)
            maxCaret = maxOf(textEditorModel.enterCaret, textEditorModel.selectionCaret)
            val suffix = textEditorModel.getSuffix(minCaret.line, minCaret.column)
            val prefix = textEditorModel.getPrefix(maxCaret.line, maxCaret.column)
            val body = textEditorModel.lines.subList(minCaret.line + 1, maxCaret.line)

            lines = (arrayOf(suffix) + body + arrayOf(prefix)).toList()
        }

        override fun execute() {
            textEditorModel.setCaret(minCaret)
            textEditorModel.removeRangeInLines(minCaret.line, minCaret.column, maxCaret.line, maxCaret.column)
        }

        override fun undo() {
            textEditorModel.setCaret(minCaret, maxCaret)
            textEditorModel.insertInLine(minCaret.line, minCaret.column, lines)
        }
    }

    class InsertNewLineCommand(private val textEditorModel: TextEditorModel) : ITextEditorCommand {
        private var caretState: TextEditorCaret

        init {
            caretState = textEditorModel.enterCaret.copy()
        }

        override fun execute() {
            textEditorModel.setCaret(caretState)
            textEditorModel.insertLineBreak(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column)
            textEditorModel.moveEnterCaretRight()
        }

        override fun undo() {
            textEditorModel.setCaret(caretState)
            textEditorModel.removeLineBreak(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column)
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

    class InsertMultiLineCommand(private val textEditorModel: TextEditorModel, private val lines: List<String>) : ITextEditorCommand {
        private var prevCaret: TextEditorCaret
        private lateinit var newCaret: TextEditorCaret

        init {
            prevCaret = textEditorModel.enterCaret.copy()
        }

        override fun execute() {
            textEditorModel.setCaret(prevCaret)

            textEditorModel.insertInLine(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column, lines)

            val line = textEditorModel.enterCaret.line + lines.size - 1
            val column = lines.last().length
            textEditorModel.setCaret(line, column)

            newCaret = textEditorModel.enterCaret.copy()
        }

        override fun undo() {
            textEditorModel.setCaret(prevCaret)
            textEditorModel.removeRangeInLines(prevCaret.line, prevCaret.column, newCaret.line, newCaret.column)
        }
    }

    class BackSpaceCommand(private val textEditorModel: TextEditorModel) : ITextEditorCommand {
        private var prevCaret: TextEditorCaret
        private lateinit var newCaret: TextEditorCaret
        private lateinit var char: String
        private lateinit var result: BackSpaceResult

        init {
            prevCaret = textEditorModel.enterCaret.copy()
        }

        override fun execute() {
            textEditorModel.setCaret(prevCaret)

            result = if (textEditorModel.enterCaret.column == 0) BackSpaceResult.LineRemoved else BackSpaceResult.CharRemoved

            when (result) {
                BackSpaceResult.LineRemoved -> {
                    textEditorModel.moveEnterCaretLeft()
                    textEditorModel.removeLineBreak(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column)
                }

                BackSpaceResult.CharRemoved -> {
                    char = textEditorModel.getSubstring(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column - 1, textEditorModel.enterCaret.column)
                    textEditorModel.removeRangeInLine(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column - 1, textEditorModel.enterCaret.column)
                    textEditorModel.moveEnterCaretLeft()
                }
            }
            newCaret = textEditorModel.enterCaret.copy()
        }

        override fun undo() {
            textEditorModel.setCaret(newCaret)
            when (result) {
                BackSpaceResult.LineRemoved -> {
                    textEditorModel.insertLineBreak(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column)
                    textEditorModel.moveEnterCaretRight()
                }

                BackSpaceResult.CharRemoved -> {
                    textEditorModel.insertInLine(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column, char)
                    textEditorModel.moveEnterCaretRight()
                }
            }
        }

        enum class BackSpaceResult {
            LineRemoved,
            CharRemoved,
        }
    }

    class DeleteCommand(private val textEditorModel: TextEditorModel) : ITextEditorCommand {
        private var caretState: TextEditorCaret
        private lateinit var result: DeleteResult
        private lateinit var char: String

        init {
            caretState = textEditorModel.enterCaret.copy()
        }

        override fun execute() {
            textEditorModel.setCaret(caretState)

            result = if (textEditorModel.enterCaret.column == textEditorModel.lines[textEditorModel.enterCaret.line].length) DeleteResult.LineRemoved else DeleteResult.CharRemoved

            when (result) {
                DeleteResult.LineRemoved -> {
                    textEditorModel.removeLineBreak(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column)
                }

                DeleteResult.CharRemoved -> {
                    char = textEditorModel.getSubstring(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column, textEditorModel.enterCaret.column + 1)
                    textEditorModel.removeRangeInLine(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column, textEditorModel.enterCaret.column + 1)
                }
            }
        }

        override fun undo() {
            textEditorModel.setCaret(caretState)
            when (result) {
                DeleteResult.LineRemoved -> {
                    textEditorModel.insertLineBreak(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column)
                }

                DeleteResult.CharRemoved -> {
                    textEditorModel.insertInLine(textEditorModel.enterCaret.line, textEditorModel.enterCaret.column, char)
                }
            }
        }

        enum class DeleteResult {
            LineRemoved,
            CharRemoved,
        }
    }
}