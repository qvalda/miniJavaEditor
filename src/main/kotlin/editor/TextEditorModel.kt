package editor

import helpers.Event
import kotlin.math.max
import kotlin.math.min

class TextEditorModel (text:String = "") {
    val onLineDelete = Event<Int>()
    val onLineModified = Event<Int>()
    val onLineAdd = Event<Int>()
    val onCaretMove = Event<TextEditorCaret>()

    var lines = ArrayList<String>()
    var maxLength = 0;
    var beginCaret: TextEditorCaret = TextEditorCaret()
        set(value) {
            if (field != value) {
                field = value
                onCaretMove(field)
            }
        }
    var endCaret: TextEditorCaret

    init {
        val input = text.replace("\r", "").replace("\t", "    ");
        maxLength = 0;
        for (s in input.split('\n')) {
            lines.add(s)
            maxLength = max(maxLength, s.length)
        }
        beginCaret = TextEditorCaret()
        endCaret = TextEditorCaret()
    }

    private fun updateMaxLength() {
        maxLength = lines.maxBy { l -> l.length }.length
    }

    private fun deleteLine(lineIndex: Int) {
        onLineDelete(lineIndex)
        lines.removeAt(lineIndex)
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

    private fun getPrefix(lineIndex: Int, columnIndex: Int): String {
        return lines[lineIndex].substring(0, columnIndex)
    }

    private fun getSuffix(lineIndex: Int, columnIndex: Int): String {
        return lines[lineIndex].substring(columnIndex, lines[lineIndex].length)
    }

    fun backSpaceAction() {
        if (deleteSelection()) return
        if (beginCaret.column == 0 && beginCaret.line == 0) return

        if (beginCaret.column == 0 && beginCaret.line != 0) {
            val suffix = getSuffix(beginCaret.line, 0)
            deleteLine(beginCaret.line)
            moveBeginCaretLeft()
            appendToLine(beginCaret.line, suffix)
        } else {
            removeRangeInLine(beginCaret.line, beginCaret.column - 1, beginCaret.column)
            moveBeginCaretLeft()
        }
    }

    fun deleteAction() {
        if (deleteSelection()) return
        if (beginCaret.column == lines.last().length && beginCaret.line == lines.size - 1) return
        moveBeginCaretRight()
        backSpaceAction()
    }

    fun addChar(keyChar: Char) {
        deleteSelection()
        insertInLine(beginCaret.line, beginCaret.column, keyChar.toString())
        moveBeginCaretRight()
    }

    fun tabAction() {
        deleteSelection()
        insertInLine(beginCaret.line, beginCaret.column, "    ")
        repeat(4) { //todo opt
            moveBeginCaretRight()
        }
    }

    private fun deleteSelection(): Boolean {
        if (beginCaret == endCaret) return false

        val minCaret = minOf(beginCaret, endCaret)
        val maxCaret = maxOf(beginCaret, endCaret)

        if (minCaret.line == maxCaret.line) {
            removeRangeInLine(minCaret.line, minCaret.column, maxCaret.column)
        } else {
            removeRangeInLine(minCaret.line, minCaret.column, lines[minCaret.line].length)
            val suffix = getSuffix(maxCaret.line, maxCaret.column)
            appendToLine(minCaret.line, suffix)
            val range = maxCaret.line - minCaret.line
            repeat(range) {
                deleteLine(minCaret.line + 1)
            }
        }

        beginCaret = minCaret
        endCaret = minCaret
        return true
    }

    fun enterAction() {
        deleteSelection()
        val suffix = getSuffix(beginCaret.line, beginCaret.column)
        removeRangeInLine(beginCaret.line, beginCaret.column, lines[beginCaret.line].length)
        addLine(beginCaret.line + 1, suffix)
        moveBeginCaretRight()
    }

    fun moveBeginCaretLeft() {
        beginCaret = moveCaretLeft(beginCaret)
        endCaret = beginCaret.copy()
    }

    fun moveEndCaretLeft() {
        endCaret = moveCaretLeft(endCaret)
    }

    private fun moveCaretLeft(caret: TextEditorCaret): TextEditorCaret {
        if (caret.line == 0 && caret.column == 0) return caret
        if (caret.column > 0) {
            return TextEditorCaret(caret.line, caret.column - 1)
        } else {
            val line = max(caret.line - 1, 0)
            return TextEditorCaret(line, lines[line].length)
        }
    }

    fun moveBeginCaretRight() {
        beginCaret = moveCaretRight(beginCaret)
        endCaret = beginCaret.copy()
    }

    fun moveEndCaretRight() {
        endCaret = moveCaretRight(endCaret)
    }

    private fun moveCaretRight(caret: TextEditorCaret): TextEditorCaret {
        if (caret.line == lines.size - 1 && caret.column == lines[lines.size - 1].length) return caret
        if (caret.column < lines[caret.line].length) {
            return TextEditorCaret(caret.line, caret.column + 1)
        } else {
            return TextEditorCaret(min(caret.line + 1, lines.size - 1), 0)
        }
    }

    fun moveBeginCaretDown() {
        beginCaret = moveCaretDown(beginCaret)
        endCaret = beginCaret.copy()
    }

    fun moveEndCaretDown() {
        endCaret = moveCaretDown(endCaret)
    }

    private fun moveCaretDown(caret: TextEditorCaret): TextEditorCaret {
        val line = min(caret.line + 1, lines.size - 1)
        val column = min(caret.column, lines[line].length)
        return TextEditorCaret(line, column)
    }

    fun moveBeginCaretUp() {
        beginCaret = moveCaretUp(beginCaret)
        endCaret = beginCaret.copy()
    }

    fun moveEndCaretUp() {
        endCaret = moveCaretUp(endCaret)
    }

    private fun moveCaretUp(caret: TextEditorCaret): TextEditorCaret {
        val line = max(caret.line - 1, 0)
        val column = min(caret.column, lines[line].length)
        return TextEditorCaret(line, column)
    }

    fun updateEndCaret(lineIndex: Int, columnIndex: Int) {
        endCaret = getAdjustedCaret(lineIndex, columnIndex)
    }

    fun updateBeginCaret(lineIndex: Int, columnIndex: Int) {
        beginCaret = getAdjustedCaret(lineIndex, columnIndex)
    }

    private fun getAdjustedCaret(lineIndex: Int, columnIndex: Int): TextEditorCaret {
        val line = lineIndex.coerceIn(0, lines.size - 1)
        val column = columnIndex.coerceIn(0, lines[line].length)
        return TextEditorCaret(line, column)
    }
}