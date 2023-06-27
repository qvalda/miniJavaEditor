import tokenizer.Tokenizer
import kotlin.math.max
import kotlin.math.min

class EditorTextModel (text:String) {

    var lines = ArrayList<EditorLine>()
    val tokenizer = Tokenizer()
    var beginCaret = EditorTextCaret()
    var endCaret = EditorTextCaret()

    init {
        val input = text.replace("\r", "").replace("\t", "    ");
        //val tokens = Tokenizer().getTokens(text)
        for (s in input.split('\n')) {
            val line = EditorLine(s)
            lines.add(line)
        }
    }

    fun backSpaceAction() {
        if(deleteSelection()) return
        if (beginCaret.column == 0 && beginCaret.line != 0) {
            val appendix = lines[beginCaret.line].text
            lines.removeAt(beginCaret.line)
            val row = lines[beginCaret.line - 1].text + appendix
            lines[beginCaret.line - 1].text = row
        } else {
            val row = lines[beginCaret.line].text.removeRange(beginCaret.column - 1, beginCaret.column)
            lines[beginCaret.line].text = row
        }
        moveBeginCaretLeft()
    }

    fun deleteAction() {
        if(deleteSelection()) return
        //todo
    }

    fun addChar(keyChar: Char) {
        deleteSelection()
        val row = lines[beginCaret.line].text
        lines[beginCaret.line].text = StringBuilder(row).insert(beginCaret.column, keyChar).toString()
        moveBeginCaretRight()
    }

    private fun deleteSelection() : Boolean {
        if (beginCaret == endCaret) return false

        val minCaret = minOf(beginCaret, endCaret)
        val maxCaret = maxOf(beginCaret, endCaret)

        if(minCaret.line == maxCaret.line){
            lines[minCaret.line].removeRange(minCaret.column, maxCaret.column)
        }
        else {
            lines[minCaret.line].removeRange(minCaret.column, lines[minCaret.line].text.length)
            val appendix = lines[maxCaret.line].text.substring(maxCaret.column, lines[maxCaret.line].text.length)
            lines[minCaret.line].text += appendix

            val range = maxCaret.line - minCaret.line
            repeat(range) {
                lines.removeAt(minCaret.line + 1)
            }
        }

        beginCaret = minCaret
        endCaret = minCaret
        return true
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