package editor

import helpers.Event
import kotlin.math.max
import kotlin.math.min

class LineChangeArgs(val startIndex: Int, val count: Int)

//class TextEditorViewChangeTracker(textEditorModel: TextEditorModel) {
//
//    private var hasViewChanges = false
//    private var isTrackingActive = false
//
//    private fun trackChanges(statement: () -> Unit) {
//        if (isTrackingActive) {
//            statement()
//            return
//        }
//        isTrackingActive = true
//        hasViewChanges = false
//
//        val initialBeginCaret = beginCaret.copy()
//        val initialEndCaret = endCaret.copy()
//
//        statement()
//        if (initialBeginCaret != beginCaret) {
//            onCaretMove(beginCaret)
//        }
//        if (initialBeginCaret != beginCaret || initialEndCaret != endCaret || hasViewChanges) {
//            onViewModified(Unit)
//        }
//        isTrackingActive = false
//    }
//}

class TextEditorModel (text:String = "", private val clipboard: IClipboard = SystemClipboard()) {
    val onLineDelete = Event<LineChangeArgs>()
    val onLineModified = Event<Int>()
    val onLineAdd = Event<Int>()
    val onCaretMove = Event<TextEditorCaret>()
    val onViewModified = Event<Unit>()

    var lines = mutableListOf<String>()
    var maxLength = 0
    var beginCaret: TextEditorCaret = TextEditorCaret()
        set(value) {
            if (field != value) {
                field = value
                onCaretMove(field)
            }
        }
    var endCaret: TextEditorCaret

    init {
        val input = preprocessText(text)
        maxLength = 0
        for (s in input.split('\n')) {
            lines.add(s)
            maxLength = max(maxLength, s.length)
        }
        beginCaret = TextEditorCaret()
        endCaret = TextEditorCaret()
    }

    private var hasViewChanges = false
    private var isTrackingActive = false
    private fun trackChanges(statement: () -> Unit) {
        if (isTrackingActive) {
            statement()
            return
        }
        isTrackingActive = true
        val initialBeginCaret = beginCaret.copy()
        val initialEndCaret = endCaret.copy()
        hasViewChanges = false
        statement()
        if (initialBeginCaret != beginCaret) {
            onCaretMove(beginCaret)
        }
        if (initialBeginCaret != beginCaret || initialEndCaret != endCaret || hasViewChanges) {
            onViewModified(Unit)
        }
        isTrackingActive = false
    }

    private fun preprocessText(input:String):String{
        return input.replace("\r", "").replace("\t", "    ")
    }

    fun getText(): String {
        return lines.joinToString("\r\n")
    }

    fun getSelectedText(): String? {
        if (beginCaret == endCaret) return null

        val minCaret = minOf(beginCaret, endCaret)
        val maxCaret = maxOf(beginCaret, endCaret)

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
        maxLength = lines.maxBy { l -> l.length }.length
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
        if (values.size == 1) {
            insertInLine(lineIndex, columnIndex, values[0])
            moveBeginCaret(beginCaret.line, beginCaret.column + values[0].length) //todo move to action
        } else {
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
            moveBeginCaret(beginCaret.line + values.size - 1, lastValue.length) //todo move to action
        }
        updateMaxLength()
    }

    //endregion

    //region public actions
    fun backSpaceAction() {
        trackChanges {
            if (deleteSelection()) return@trackChanges
            if (beginCaret.column == 0 && beginCaret.line == 0) return@trackChanges

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
    }

    fun deleteAction() {
        trackChanges {
            if (deleteSelection()) return@trackChanges
            if (beginCaret.column == lines.last().length && beginCaret.line == lines.size - 1) return@trackChanges
            moveBeginCaretRight()
            backSpaceAction()
            hasViewChanges = true
        }
    }

    fun addChar(keyChar: Char) {
        trackChanges {
            deleteSelection()
            insertInLine(beginCaret.line, beginCaret.column, keyChar.toString())
            moveBeginCaretRight()
        }
    }

    fun tabAction() {
        trackChanges {
            deleteSelection()
            insertInLine(beginCaret.line, beginCaret.column, "    ")
            repeat(4) {
                moveBeginCaretRight()
            }
        }
    }

    fun enterAction() {
        trackChanges {
            deleteSelection()
            val suffix = getSuffix(beginCaret.line, beginCaret.column)
            removeRangeInLine(beginCaret.line, beginCaret.column, lines[beginCaret.line].length)
            addLine(beginCaret.line + 1, suffix)
            moveBeginCaretRight()
        }
    }

    fun selectAllAction() {
        trackChanges {
            beginCaret = TextEditorCaret(0, 0)
            endCaret = TextEditorCaret(lines.lastIndex, lines.last().length)
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
            if(!text.isNullOrEmpty()){
                deleteSelection()
                val input = preprocessText(text).split('\n')
                insertInLine(beginCaret.line, beginCaret.column, input)
            }
        }
    }

    fun homeAction() {
        trackChanges {
            moveBeginCaret(getAdjustedCaret(beginCaret.line, 0))
        }
    }

    fun endAction() {
        trackChanges {
            moveBeginCaret(getAdjustedCaret(beginCaret.line , lines[beginCaret.line].length))
        }
    }

    fun pageUpAction(offset: Int) {
        trackChanges {
            moveBeginCaret(getAdjustedCaret(beginCaret.line - offset, beginCaret.column))
        }
    }

    fun pageDownAction(offset: Int) {
        trackChanges {
            moveBeginCaret(getAdjustedCaret(beginCaret.line + offset, beginCaret.column))
        }
    }

    //endregion

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
            deleteLine(minCaret.line + 1, range)
        }
        beginCaret = minCaret
        endCaret = minCaret
        return true
    }

    //region caret

    fun moveBeginCaretLeft() {
        trackChanges {
            beginCaret = moveCaretLeft(beginCaret)
            endCaret = beginCaret.copy()
        }
    }

    fun moveEndCaretLeft() {
        trackChanges {
            endCaret = moveCaretLeft(endCaret)
        }
    }

    fun moveBeginCaretRight() {
        trackChanges {
            beginCaret = moveCaretRight(beginCaret)
            endCaret = beginCaret.copy()
        }
    }

    fun moveEndCaretRight() {
        trackChanges {
            endCaret = moveCaretRight(endCaret)
        }
    }

    fun moveBeginCaretDown() {
        trackChanges {
            beginCaret = moveCaretDown(beginCaret)
            endCaret = beginCaret.copy()
        }
    }

    fun moveEndCaretDown() {
        trackChanges {
            endCaret = moveCaretDown(endCaret)
        }
    }

    fun moveBeginCaretUp() {
        trackChanges {
            beginCaret = moveCaretUp(beginCaret)
            endCaret = beginCaret.copy()
        }
    }

    fun moveEndCaretUp() {
        trackChanges {
            endCaret = moveCaretUp(endCaret)
        }
    }

    fun updateEndCaret(lineIndex: Int, columnIndex: Int) {
        trackChanges {
            endCaret = getAdjustedCaret(lineIndex, columnIndex)
        }
    }

    fun updateBeginCaret(lineIndex: Int, columnIndex: Int) {
        trackChanges {
            beginCaret = getAdjustedCaret(lineIndex, columnIndex)
        }
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

    private fun moveCaretUp(caret: TextEditorCaret): TextEditorCaret {
        val line = max(caret.line - 1, 0)
        val column = min(caret.column, lines[line].length)
        return TextEditorCaret(line, column)
    }

    private fun moveCaretRight(caret: TextEditorCaret): TextEditorCaret {
        if (caret.line == lines.size - 1 && caret.column == lines[lines.size - 1].length) return caret
        if (caret.column < lines[caret.line].length) {
            return TextEditorCaret(caret.line, caret.column + 1)
        } else {
            return TextEditorCaret(min(caret.line + 1, lines.size - 1), 0)
        }
    }

    private fun moveCaretDown(caret: TextEditorCaret): TextEditorCaret {
        val line = min(caret.line + 1, lines.size - 1)
        val column = min(caret.column, lines[line].length)
        return TextEditorCaret(line, column)
    }

    private fun getAdjustedCaret(lineIndex: Int, columnIndex: Int): TextEditorCaret {
        val line = lineIndex.coerceIn(0, lines.size - 1)
        val column = columnIndex.coerceIn(0, lines[line].length)
        return TextEditorCaret(line, column)
    }

    private fun moveBeginCaret(line : Int,  column: Int) {
        trackChanges {
            beginCaret = TextEditorCaret(line, column)
            endCaret = beginCaret.copy()
        }
    }

    private fun moveBeginCaret(caret: TextEditorCaret) {
        trackChanges {
            beginCaret = caret.copy()
            endCaret = caret.copy()
        }
    }

    //endregion
}