import helpers.Event
import kotlin.math.max
import kotlin.math.min

class EditorLine (var text:String){

}

class EditorTextModel (text:String) {
    val onLineDelete = Event<Int>()
    val onLineModified = Event<Int>()
    val onLineAdd = Event<Int>()

    var lines = ArrayList<EditorLine>()
    var beginCaret = EditorTextCaret()
    var endCaret = EditorTextCaret()

    init {
        val input = text.replace("\r", "").replace("\t", "    ");
        for (s in input.split('\n')) {
            val line = EditorLine(s)
            lines.add(line)
        }
    }

    private fun deleteLine(lineIndex: Int) {
        onLineDelete(lineIndex)
        lines.removeAt(lineIndex)
    }

    private fun addLine(lineIndex: Int, editorLine: EditorLine) {
        lines.add(lineIndex, editorLine)
        onLineAdd(lineIndex)
    }

    fun removeRangeInLine(lineIndex: Int, startIndex: Int, endIndex: Int) {
        lines[lineIndex].text = lines[lineIndex].text.removeRange(startIndex, endIndex)
        onLineModified(lineIndex)
    }

    fun appendToLine(lineIndex: Int, appendix: String) {
        lines[lineIndex].text += appendix
        onLineModified(lineIndex)
    }

    fun insertCharInLine(lineIndex: Int, columnIndex: Int, char: Char) {
        lines[lineIndex].text = StringBuilder(lines[lineIndex].text).insert(columnIndex, char).toString()
        onLineModified(lineIndex)
    }

    fun getPrefix(lineIndex: Int, columnIndex: Int): String {
        return lines[lineIndex].text.substring(0, columnIndex)
    }

    fun getSuffix(lineIndex: Int, columnIndex: Int): String {
        return lines[lineIndex].text.substring(columnIndex, lines[lineIndex].text.length)
    }

    fun backSpaceAction() {
        if (deleteSelection()) return
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
        moveBeginCaretRight()
        backSpaceAction()
    }

    fun addChar(keyChar: Char) {
        deleteSelection()
        insertCharInLine(beginCaret.line, beginCaret.column, keyChar)
        moveBeginCaretRight()
    }

    private fun deleteSelection(): Boolean {
        if (beginCaret == endCaret) return false

        val minCaret = minOf(beginCaret, endCaret)
        val maxCaret = maxOf(beginCaret, endCaret)

        if (minCaret.line == maxCaret.line) {
            removeRangeInLine(minCaret.line, minCaret.column, maxCaret.column)
        } else {
            removeRangeInLine(minCaret.line, minCaret.column, lines[minCaret.line].text.length)
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
        removeRangeInLine(beginCaret.line, beginCaret.column, lines[beginCaret.line].text.length)
        addLine(beginCaret.line + 1, EditorLine(suffix))
        moveBeginCaretRight()
    }

    fun moveBeginCaretLeft() {
        moveCaretLeft(beginCaret)
        endCaret = beginCaret.copy()
    }

    fun moveEndCaretLeft() {
        moveCaretLeft(endCaret)
    }

    fun moveCaretLeft(caret: EditorTextCaret) {
        if (caret.line == 0 && caret.column == 0) return
        if (caret.column > 0) {
            caret.column--
        } else {
            caret.line = max(caret.line - 1, 0)
            caret.column = lines[caret.line].text.length
        }
    }

    fun moveBeginCaretRight() {
        moveCaretRight(beginCaret)
        endCaret = beginCaret.copy()
    }

    fun moveEndCaretRight() {
        moveCaretRight(endCaret)
    }

    fun moveCaretRight(caret: EditorTextCaret) {
        if (caret.line == lines.size - 1 && caret.column == lines[lines.size - 1].text.length) return
        if (caret.column < lines[caret.line].text.length) {
            caret.column++
        } else {
            caret.line = min(caret.line + 1, lines.size - 1)
            caret.column = 0
        }
    }

    fun moveBeginCaretDown() {
        moveCaretDown(beginCaret)
        endCaret = beginCaret.copy()
    }

    fun moveEndCaretDown() {
        moveCaretDown(endCaret)
    }

    private fun moveCaretDown(caret: EditorTextCaret) {
        caret.line = min(caret.line + 1, lines.size - 1)
        caret.column = min(caret.column, lines[caret.line].text.length)
    }

    fun moveBeginCaretUp() {
        moveCaretUp(beginCaret)
        endCaret = beginCaret.copy()
    }

    fun moveEndCaretUp() {
        moveCaretUp(endCaret)
    }

    private fun moveCaretUp(caret: EditorTextCaret) {
        caret.line = max(caret.line - 1, 0)
        caret.column = min(caret.column, lines[caret.line].text.length)
    }

    fun updateEndCaret(lineIndex: Int, columnIndex: Int) {
        endCaret = getAdjustedCaret(lineIndex, columnIndex)
    }

    fun updateBeginCaret(lineIndex: Int, columnIndex: Int) {
        beginCaret = getAdjustedCaret(lineIndex, columnIndex)
    }

    fun getAdjustedCaret(lineIndex: Int, columnIndex: Int): EditorTextCaret {
        val line = lineIndex.coerceIn(0, lines.size - 1)
        val column = columnIndex.coerceIn(0, lines[line].text.length)
        return EditorTextCaret(line, column)
    }
}