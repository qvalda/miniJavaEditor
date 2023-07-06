package editor.model

import editor.view.SystemClipboard
import helpers.Event
import kotlin.math.max
import kotlin.math.min

class TextEditorModel (text:String = "", private val clipboard: IClipboard = SystemClipboard()) : ITextEditorModel, ITextEditorController  {

    private val lines = mutableListOf<String>()
    private val commands = TextEditorCommandHistory()

    override val onLineDelete = Event<LineChangeArgs>()
    override val onLineModified = Event<LineChangeArgs>()
    override val onLineAdd = Event<LineChangeArgs>()
    override val onCaretMove = Event<TextEditorCaret>()
    override val onModified = Event<Unit>() //todo test

    override var enterCaret = TextEditorCaret()
    override var selectionCaret = TextEditorCaret()
    override var maxLength = 0

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

    override fun getLine(lineIndex: Int) : String{
        return lines[lineIndex]
    }

    override fun getLines(): List<String> {
        return lines
    }

    override val linesCount: Int
        get() = lines.size

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
            onModified(Unit)
        }
        isTrackingActive = false
    }

    private fun preprocessText(input: String): String {
        return input.replace("\r", "").replace("\t", "    ")
    }

    fun getText(): String {
        return lines.joinToString("\r\n")
    }

    fun getSelectedText(): String {
        return getSelectedLines().joinToString("\r\n")
    }

    private fun getSelectedLines(): List<String> {
        if (enterCaret == selectionCaret) return emptyList()

        val minCaret = minOf(enterCaret, selectionCaret)
        val maxCaret = maxOf(enterCaret, selectionCaret)

        if (minCaret.line == maxCaret.line) {
            return listOf(getSubstring(minCaret.line, minCaret.column, maxCaret.column))
        } else {
            val suffix = getSuffix(minCaret.line, minCaret.column)
            val prefix = getPrefix(maxCaret.line, maxCaret.column)
            val body = lines.subList(minCaret.line + 1, maxCaret.line)
            return (arrayOf(suffix) + body + arrayOf(prefix)).toList()
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
        onLineModified(LineChangeArgs(lineIndex))
        updateMaxLength()
    }

    private fun removeRangeInLines(lineIndex: Int, startIndex: Int, endLineIndex: Int, endIndex: Int) {
        lines[lineIndex] = getPrefix(lineIndex, startIndex) + getSuffix(endLineIndex, endIndex)
        onLineModified(LineChangeArgs(lineIndex))
        onLineDelete(LineChangeArgs(lineIndex + 1, endLineIndex - lineIndex))
        lines.subList(lineIndex + 1, endLineIndex + 1).clear()
        updateMaxLength()
    }

    private fun insertInLine(lineIndex: Int, columnIndex: Int, value: String) {
        lines[lineIndex] = StringBuilder(lines[lineIndex]).insert(columnIndex, value).toString()
        onLineModified(LineChangeArgs(lineIndex))
        updateMaxLength()
    }

    private fun insertInLine(lineIndex: Int, columnIndex: Int, values: List<String>) {
        val suffix = getSuffix(lineIndex, columnIndex)
        lines[lineIndex] = getPrefix(lineIndex, columnIndex) + values[0]
        onLineModified(LineChangeArgs(lineIndex))
        if (values.size > 2) {
            lines.addAll(lineIndex + 1, values.drop(1).dropLast(1))
        }
        val lastIndex = lineIndex + values.size - 1
        val lastValue = values.last() + suffix
        lines.add(lastIndex, lastValue)
        onLineAdd(LineChangeArgs(lineIndex + 1, values.size - 1))
        updateMaxLength()
    }

    private fun insertLineBreak(lineIndex: Int, columnIndex: Int) {
        insertInLine(lineIndex, columnIndex, listOf("", ""))
    }

    private fun removeLineBreak(lineIndex: Int, columnIndex: Int) {
        lines[lineIndex] += lines[lineIndex + 1]
        onLineModified(LineChangeArgs(lineIndex))
        onLineDelete(LineChangeArgs(lineIndex + 1))
        lines.removeAt(lineIndex + 1)
        updateMaxLength()
    }

    //endregion

    //region public actions
    override fun backSpaceAction() {
        trackChanges {
            if (!deleteSelection() && !(enterCaret.column == 0 && enterCaret.line == 0)) {
                commands.add(BackSpaceCommand(this))
            }
        }
    }

    override fun deleteAction() {
        trackChanges {
            if (!deleteSelection() && !(enterCaret.column == lines.last().length && enterCaret.line == lines.size - 1)) {
                commands.add(DeleteCommand(this))
            }
        }
    }

    override fun addChar(keyChar: Char) {
        trackChanges {
            deleteSelection()
            commands.add(InsertSingleLineCommand(this, keyChar.toString()))
        }
    }

    override fun tabAction() {
        trackChanges {
            deleteSelection()
            commands.add(InsertSingleLineCommand(this, "    "))
        }
    }

    override fun enterAction() {
        trackChanges {
            deleteSelection()
            commands.add(InsertNewLineCommand(this))
        }
    }

    override fun undo() {
        trackChanges {
            commands.undo()
        }
    }

    override fun redo() {
        trackChanges {
            commands.redo()
        }
    }

    override fun selectAllAction() {
        trackChanges {
            enterCaret = TextEditorCaret(0, 0)
            selectionCaret = TextEditorCaret(lines.lastIndex, lines.last().length)
        }
    }

    override fun cutAction() {
        trackChanges {
            copyAction()
            deleteSelection()
        }
    }

    override fun copyAction() {
        trackChanges {
            val text = getSelectedText()
            clipboard.setData(text)
        }
    }

    override fun pasteAction() {
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

    override fun homeAction() {
        trackChanges {
            setCaret(getAdjustedCaret(enterCaret.line, 0))
        }
    }

    override fun endAction() {
        trackChanges {
            setCaret(getAdjustedCaret(enterCaret.line, lines[enterCaret.line].length))
        }
    }

    override fun pageUpAction(offset: Int) {
        trackChanges {
            setCaret(getAdjustedCaret(enterCaret.line - offset, enterCaret.column))
        }
    }

    override fun pageDownAction(offset: Int) {
        trackChanges {
            setCaret(getAdjustedCaret(enterCaret.line + offset, enterCaret.column))
        }
    }

    //endregion

    //region caret

    override fun moveEnterCaretLeft() {
        trackChanges {
            enterCaret = getCaretLeft(enterCaret)
            selectionCaret = enterCaret.copy()
        }
    }

    override fun moveSelectionCaretLeft() {
        trackChanges {
            selectionCaret = getCaretLeft(selectionCaret)
        }
    }

    override fun moveEnterCaretRight() {
        trackChanges {
            enterCaret = getCaretRight(enterCaret)
            selectionCaret = enterCaret.copy()
        }
    }

    override fun moveSelectionCaretRight() {
        trackChanges {
            selectionCaret = getCaretRight(selectionCaret)
        }
    }

    override fun moveEnterCaretDown() {
        trackChanges {
            enterCaret = getCaretDown(enterCaret)
            selectionCaret = enterCaret.copy()
        }
    }

    override fun moveSelectionCaretDown() {
        trackChanges {
            selectionCaret = getCaretDown(selectionCaret)
        }
    }

    override fun moveEnterCaretUp() {
        trackChanges {
            enterCaret = getCaretUp(enterCaret)
            selectionCaret = enterCaret.copy()
        }
    }

    override fun moveSelectionCaretUp() {
        trackChanges {
            selectionCaret = getCaretUp(selectionCaret)
        }
    }

    override fun setSelectionCaret(lineIndex: Int, columnIndex: Int) {
        trackChanges {
            selectionCaret = getAdjustedCaret(lineIndex, columnIndex)
        }
    }

    override fun setCarets(lineIndex: Int, columnIndex: Int) {
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
        if (enterCaret == selectionCaret) return false
        if (enterCaret.line == selectionCaret.line) {
            commands.add(DeleteSingleSelectionCommand(this))
        } else {
            commands.add(DeleteMultiSelectionCommand(this))
        }
        return true
    }

    private class DeleteSingleSelectionCommand(private val textEditorModel: TextEditorModel) : ITextEditorCommand {
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

    private class DeleteMultiSelectionCommand(private val textEditorModel: TextEditorModel) : ITextEditorCommand {
        private var minCaret: TextEditorCaret
        private var maxCaret: TextEditorCaret
        private var lines: List<String>

        init {
            minCaret = minOf(textEditorModel.enterCaret, textEditorModel.selectionCaret)
            maxCaret = maxOf(textEditorModel.enterCaret, textEditorModel.selectionCaret)
            lines = textEditorModel.getSelectedLines()
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

    private class InsertNewLineCommand(private val textEditorModel: TextEditorModel) : ITextEditorCommand {
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

    private class InsertSingleLineCommand(private val textEditorModel: TextEditorModel, private val text: String) : ITextEditorCommand {
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

    private class InsertMultiLineCommand(private val textEditorModel: TextEditorModel, private val lines: List<String>) : ITextEditorCommand {
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

    private class BackSpaceCommand(private val textEditorModel: TextEditorModel) : ITextEditorCommand {
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

    private class DeleteCommand(private val textEditorModel: TextEditorModel) : ITextEditorCommand {
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