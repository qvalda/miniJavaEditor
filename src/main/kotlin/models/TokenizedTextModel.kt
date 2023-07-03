package models

import editor.LineChangeArgs
import editor.TextEditorModel
import helpers.Event
import parser.ITokenSource
import tokenizer.Token
import tokenizer.Tokenizer

class TokenizedTextModel(private val textModel: TextEditorModel) : ITokenSource {
    val modified = Event<Unit>()
    var lines: MutableList<List<Token>>
    private val tokenizer = Tokenizer()

    init {
        lines = textModel.lines.map { l -> tokenizer.getTokens(l) }.toMutableList()

        textModel.onLineDelete += ::onLineDelete
        textModel.onLineModified += ::onLineModified
        textModel.onLineAdd += ::onLineAdd
    }

    private fun onLineDelete(lineChangeArgs: LineChangeArgs) {
        lines.subList(lineChangeArgs.startIndex, lineChangeArgs.startIndex + lineChangeArgs.count).clear()
        modified(Unit)
    }

    private fun onLineModified(lineIndex: Int) {
        lines[lineIndex] = tokenizer.getTokens(textModel.lines[lineIndex])
        modified(Unit)
    }

    private fun onLineAdd(lineIndex: Int) {
        lines.add(lineIndex, tokenizer.getTokens(textModel.lines[lineIndex]))
        modified(Unit)
    }

    fun iterateTokens(startIndex: Int = 0, startTokenIndex: Int = 0): Sequence<Pair<Token, Int>> {
        val sequence = sequence {
            for (index in startTokenIndex + 1 until lines[startIndex].size) {
                yield(Pair(lines[startIndex][index], startIndex))
            }
            for (index in startIndex + 1 until lines.size) {
                for (t in lines[index]) {
                    yield(Pair(t, index))
                }
            }
        }
        return sequence
    }

    fun iterateTokensBackward(startIndex: Int = 0, startTokenIndex: Int = 0): Sequence<Pair<Token, Int>> {
        val sequence = sequence {
            for (index in startTokenIndex - 1 downTo 0) {
                yield(Pair(lines[startIndex][index], startIndex))
            }
            for (index in startIndex - 1 downTo 0) {
                for (t in lines[index].size -1 downTo 0) {
                    yield(Pair(lines[index][t], index))
                }
            }
        }
        return sequence
    }


    private val source = TokenizedTextModelTokenSource(this)
    override val currentToken: Token
        get() = source.currentToken
    override val nextToken: Token
        get() = source.nextToken
    override val lineIndex: Int
        get() = source.lineIndex
    override fun accept() {        source.accept()    }

    override fun isEOF(): Boolean {     return   source.isEOF()    }

    override fun reset() {        source.reset()    }

//    private var currentLineIndex = 0
//    private var currentIndex = 0
//    override val currentToken: Token
//        get() {
//            return lines[currentLineIndex][currentIndex]
//        }
//    override val nextToken: Token
//        get() {
//            if(currentIndex<lines[currentLineIndex].size-1){
//                return lines[currentLineIndex][currentIndex+1]
//            }
//            else{
//                return lines[currentLineIndex+1][0]
//            }
//        }
//    override val lineIndex: Int
//        get() {return currentLineIndex }
//
//    override fun accept() {
//        if(currentIndex<lines[currentLineIndex].size-1){
//            currentIndex++
//        }
//        else{
//            do {
//                currentLineIndex++
//            }while (currentLineIndex<lines.size && lines[currentLineIndex].isEmpty())
//            currentIndex = 0
//        }
//    }
//
//    override fun isEOF(): Boolean {
//        return currentLineIndex >= lines.lastIndex && currentIndex >= lines.last().size
//    }
//
//    override fun reset() {
//        currentLineIndex = 0
//        currentIndex = 0
//    }
}

class TokenizedTextModelTokenSource(private val tokenizedTextModel: TokenizedTextModel) : ITokenSource {
    private class Cursor(val line: Int, val column: Int)

    private var cursor = Cursor(0, 0)

    override val currentToken: Token
        get() {
            if (isEOF()) return Token.EOF
            return tokenizedTextModel.lines[cursor.line][cursor.column]
        }
    override val nextToken: Token
        get() {
            val next = getNextCursor()
            return tokenizedTextModel.lines[next.line][next.column]
        }
    override val lineIndex: Int
        get() = cursor.line

    override fun accept() {
        cursor = getNextCursor()
    }

    private fun getNextCursor(): Cursor {
        return if (cursor.column < tokenizedTextModel.lines[cursor.line].size - 1) {
            Cursor(cursor.line, cursor.column + 1)
        } else {
            var newLine = cursor.line
            do {
                newLine++
            } while (newLine < tokenizedTextModel.lines.size && tokenizedTextModel.lines[newLine].isEmpty())
            Cursor(newLine, 0)
        }
    }

    override fun isEOF() = cursor.line >= tokenizedTextModel.lines.lastIndex && cursor.column >= tokenizedTextModel.lines.last().size

    override fun reset() {
        cursor = Cursor(0, 0)
    }
}